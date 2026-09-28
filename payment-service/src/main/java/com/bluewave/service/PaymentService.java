package com.bluewave.service;

import com.bluewave.client.AuthClient;
import com.bluewave.client.BookingClient;
import com.bluewave.client.CatalogClient;
import com.bluewave.config.FeePolicyConfig;
import com.bluewave.constants.BookingStatus;
import com.bluewave.constants.PaymentStatus;
import com.bluewave.dto.*;
import com.bluewave.exception.ResourceNotFoundException;
import com.bluewave.kafka.KafkaPaymentEventPublisher;
import com.bluewave.kafka_common_event.PaymentSuccessfulEvent;
import com.bluewave.model.Payment;
import com.bluewave.repo.PaymentRepo;
import com.google.gson.JsonSyntaxException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepo paymentRepo;
    private final BookingClient bookingClient;
    private final AuthClient authClient;
    private final CatalogClient catalogClient;
    private final KafkaPaymentEventPublisher eventPublisher;
    private final FeePolicyConfig feePolicyConfig;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO dto) {
        CommonApiResponse<BookingResponseDTO> bookingResponse = bookingClient.getSingleBookingDetails(dto.getBookingId());

        if (bookingResponse == null || !bookingResponse.isSuccess() || bookingResponse.getData() == null) {
            throw new IllegalArgumentException("Unable to retrieve booking information for id: " + dto.getBookingId());
        }

        BookingResponseDTO booking = bookingResponse.getData();

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Booking is not in PENDING_PAYMENT status. Current status: " + booking.getStatus());
        }

        BigDecimal totalAmount = booking.getTotalAmount();
        long amountInCents = totalAmount.multiply(BigDecimal.valueOf(100)).longValue();

        // 1. Resolve Provider ID via Space ID
        String providerId = null;
        try {
            if (booking.getSpaceId() != null) {
                CommonApiResponse<ResponseSpacesDTO> spaceResp = catalogClient.getSpaceById(booking.getSpaceId());
                if (spaceResp != null && spaceResp.getData() != null) {
                    providerId = spaceResp.getData().getProviderId();
                }
            }
        } catch (Exception ex) {
            log.warn("Could not query space details for spaceId {}: {}", booking.getSpaceId(), ex.getMessage());
        }

        // 2. Query Provider's Stripe Connected Account (if linked)
        String destinationStripeAccountId = null;
        if (providerId != null && !providerId.isBlank()) {
            try {
                CommonApiResponse<String> authResp = authClient.getProviderStripeAccountId(providerId);
                if (authResp != null && authResp.getData() != null && !authResp.getData().isBlank()) {
                    destinationStripeAccountId = authResp.getData();
                }
            } catch (Exception ex) {
                log.warn("Could not query Stripe account for provider {}: {}", providerId, ex.getMessage());
            }
        }

        // 3. Calculate Platform Commission and Provider Net Payout using Central Fee Policy
        BigDecimal commissionPercent = feePolicyConfig.getPlatformCommissionPercent();
        BigDecimal platformFeeAmount = totalAmount.multiply(commissionPercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal providerPayoutAmount = totalAmount.subtract(platformFeeAmount);

        try {
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .putMetadata("bookingId", booking.getId())
                    .putMetadata("customerId", booking.getCustomerId())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                    );

            if (providerId != null) {
                paramsBuilder.putMetadata("providerId", providerId);
            }

            // 4. Attach Destination Charge Transfer if Provider has Connected Stripe Account
            if (destinationStripeAccountId != null && !destinationStripeAccountId.isBlank()) {
                long appFeeInCents = platformFeeAmount.multiply(BigDecimal.valueOf(100)).longValue();
                paramsBuilder.setApplicationFeeAmount(appFeeInCents)
                        .setTransferData(
                                PaymentIntentCreateParams.TransferData.builder()
                                        .setDestination(destinationStripeAccountId)
                                        .build()
                        );
                log.info("Applying Stripe Connect Destination Charge: Total=${}, PlatformFee=${} ({}%), Destination={}",
                        totalAmount, platformFeeAmount, commissionPercent, destinationStripeAccountId);
            } else {
                log.info("Provider has no connected Stripe account. Processing standard direct charge of ${} to platform balance.", totalAmount);
            }

            PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build());

            Payment payment = paymentRepo.findByBookingId(booking.getId()).orElse(new Payment());
            payment.setBookingId(booking.getId());
            payment.setCustomerId(booking.getCustomerId());
            payment.setProviderId(providerId);
            payment.setAmount(totalAmount);
            payment.setPlatformFeeAmount(platformFeeAmount);
            payment.setProviderPayoutAmount(providerPayoutAmount);
            payment.setDestinationStripeAccountId(destinationStripeAccountId);
            payment.setStripePaymentIntentId(paymentIntent.getId());
            payment.setPaymentStatus(PaymentStatus.PENDING);

            Payment savedPayment = paymentRepo.save(payment);

            return PaymentResponseDTO.builder()
                    .id(savedPayment.getId())
                    .stripePaymentIntentId(paymentIntent.getId())
                    .clientSecret(paymentIntent.getClientSecret())
                    .amount(savedPayment.getAmount())
                    .paymentStatus(savedPayment.getPaymentStatus())
                    .bookingId(savedPayment.getBookingId())
                    .customerId(savedPayment.getCustomerId())
                    .createdAt(savedPayment.getCreatedAt())
                    .build();

        } catch (Exception e) {
            log.error("Stripe error while creating PaymentIntent: {}", e.getMessage(), e);
            throw new RuntimeException("Stripe gateway failure: " + e.getMessage());
        }
    }

    @Transactional
    public String handleWebhook(String payload, String sigHeader) {
        Event event;
        try {
            if (webhookSecret != null && !webhookSecret.isBlank() && sigHeader != null) {
                event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            } else {
                event = Event.GSON.fromJson(payload, Event.class);
            }
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe webhook signature: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid signature");
        } catch (JsonSyntaxException e) {
            log.error("Invalid Stripe webhook payload JSON: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid payload");
        }

        log.info("Received Stripe Webhook Event: {}", event.getType());

        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (paymentIntent != null) {
                confirmSuccessfulPayment(paymentIntent);
            }
        } else if ("payment_intent.payment_failed".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (paymentIntent != null) {
                markPaymentFailed(paymentIntent);
            }
        }

        return "Webhook processed successfully";
    }

    @Transactional
    public String verifyAndConfirmOrder(String bookingId) {
        Payment payment = paymentRepo.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for bookingId: " + bookingId));

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return "Payment already confirmed for booking: " + bookingId;
        }

        if (payment.getStripePaymentIntentId() == null || payment.getStripePaymentIntentId().isBlank()) {
            throw new IllegalStateException("No Stripe PaymentIntent ID associated with booking: " + bookingId);
        }

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());

            if ("succeeded".equalsIgnoreCase(paymentIntent.getStatus())) {
                confirmSuccessfulPayment(paymentIntent);
                return "Payment verified and booking confirmed successfully for: " + bookingId;
            } else if ("requires_payment_method".equalsIgnoreCase(paymentIntent.getStatus()) ||
                    "canceled".equalsIgnoreCase(paymentIntent.getStatus())) {
                markPaymentFailed(paymentIntent);
                return "Payment failed or canceled for booking: " + bookingId;
            } else {
                return "Payment is currently in status: " + paymentIntent.getStatus();
            }

        } catch (StripeException e) {
            log.error("Failed to verify Stripe PaymentIntent for booking {}: {}", bookingId, e.getMessage());
            throw new RuntimeException("Stripe verification failed: " + e.getMessage());
        }
    }

    private void confirmSuccessfulPayment(PaymentIntent paymentIntent) {
        Optional<Payment> optionalPayment = paymentRepo.findByStripePaymentIntentId(paymentIntent.getId());
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();

            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) return;

            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            paymentRepo.save(payment);

            // Direct synchronous status update to Booking Service
            try {
                UpdateBookingStatusRequestDTO updateDTO = new UpdateBookingStatusRequestDTO();
                updateDTO.setBookingStatus(BookingStatus.CONFIRMED);
                bookingClient.updateBookingStatus(payment.getBookingId(), updateDTO);
                log.info("Synchronously updated booking {} status to CONFIRMED", payment.getBookingId());
            } catch (Exception ex) {
                log.warn("Direct update to booking-service failed: {}", ex.getMessage());
            }

            // Publish Kafka success event
            try {
                PaymentSuccessfulEvent event = new PaymentSuccessfulEvent(
                        payment.getId(),
                        payment.getBookingId(),
                        payment.getCustomerId(),
                        payment.getStripePaymentIntentId(),
                        payment.getAmount(),
                        LocalDateTime.now()
                );
                eventPublisher.publishPaymentSuccessfulEvent(event);
                log.info("Payment confirmed and Kafka event dispatched for bookingId: {}", payment.getBookingId());
            } catch (Exception kEx) {
                log.warn("Kafka event dispatch failed: {}", kEx.getMessage());
            }

        } else {
            log.warn("Received successful payment confirmation for unknown PaymentIntent: {}", paymentIntent.getId());
        }
    }

    private void markPaymentFailed(PaymentIntent paymentIntent) {
        paymentRepo.findByStripePaymentIntentId(paymentIntent.getId()).ifPresent(payment -> {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason(paymentIntent.getLastPaymentError() != null ?
                    paymentIntent.getLastPaymentError().getMessage() : "Payment was not completed successfully");
            paymentRepo.save(payment);

            log.warn("Payment marked FAILED for bookingId: {} due to: {}", payment.getBookingId(), payment.getFailureReason());
        });
    }

    /**
     * Executes partial refund according to central business policy:
     * - Customer receives 80% (configurable via feePolicyConfig.customerRefundPercent)
     * - Provider retains 15% (compensation for reserved slot)
     * - Platform retains 5% (payment gateway & operational processing fee)
     */
    @Transactional
    public void processAutomatedRefund(String bookingId) {
        Payment payment = paymentRepo.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment record found for bookingId: " + bookingId));

        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            log.info("Payment for bookingId {} is already refunded.", bookingId);
            return;
        }

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            if (payment.getStripePaymentIntentId() != null && !payment.getStripePaymentIntentId().isBlank()) {
                try {
                    PaymentIntent intent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
                    if ("succeeded".equalsIgnoreCase(intent.getStatus())) {
                        payment.setPaymentStatus(PaymentStatus.SUCCESS);
                        paymentRepo.save(payment);
                    } else {
                        log.warn("Cannot refund PaymentIntent in status {} for bookingId: {}", intent.getStatus(), bookingId);
                        return;
                    }
                } catch (StripeException se) {
                    log.error("Could not retrieve PaymentIntent from Stripe: {}", se.getMessage());
                    return;
                }
            } else {
                log.warn("Cannot refund payment in status {} without PaymentIntent for bookingId: {}", payment.getPaymentStatus(), bookingId);
                return;
            }
        }

        BigDecimal totalAmount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
        BigDecimal customerRefundPercent = feePolicyConfig.getCustomerRefundPercent();
        BigDecimal refundAmount = totalAmount.multiply(customerRefundPercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        long refundInCents = refundAmount.multiply(BigDecimal.valueOf(100)).longValue();

        try {
            RefundCreateParams.Builder refundParamsBuilder = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getStripePaymentIntentId())
                    .setAmount(refundInCents);

            // If a Connect destination charge was used, reverse the corresponding transfer portion
            if (payment.getDestinationStripeAccountId() != null && !payment.getDestinationStripeAccountId().isBlank()) {
                refundParamsBuilder.setReverseTransfer(true);
            }

            Refund refund = Refund.create(refundParamsBuilder.build());

            payment.setPaymentStatus(PaymentStatus.REFUNDED);
            payment.setRefundAmount(refundAmount);
            payment.setRefundId(refund.getId());
            paymentRepo.save(payment);

            try {
                eventPublisher.publishPaymentRefundEvent(bookingId, payment.getStripePaymentIntentId());
            } catch (Exception kEx) {
                log.warn("Kafka refund event dispatch failed: {}", kEx.getMessage());
            }
            log.info("Stripe partial refund of ${} ({}%) processed for bookingId: {} (Refund ID: {})",
                    refundAmount, customerRefundPercent, bookingId, refund.getId());

        } catch (StripeException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("already been refunded")) {
                log.info("Payment was already refunded in Stripe for bookingId {}", bookingId);
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                paymentRepo.save(payment);
                return;
            }
            log.error("Failed to process Stripe refund for bookingId {}: {}", bookingId, e.getMessage());
            throw new RuntimeException("Stripe refund failed: " + e.getMessage());
        }
    }

    @Transactional
    public String refundPayment(PaymentRequestDTO dto) {
        processAutomatedRefund(dto.getBookingId());
        return "Payment refund completed successfully for booking: " + dto.getBookingId();
    }
}

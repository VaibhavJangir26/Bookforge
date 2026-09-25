package com.bluewave.service;

import com.bluewave.client.BookingClient;
import com.bluewave.dto.*;
import com.bluewave.exception.ResourceNotFoundException;
import com.bluewave.kafka.KafkaPaymentEventPublisher;
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
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepo paymentRepo;
    private final BookingClient bookingClient;
    private final KafkaPaymentEventPublisher eventPublisher;

    @Value("${stripe.webhook.secret}")
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

        try {
            long amountInCents = booking.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .putMetadata("bookingId", booking.getId())
                    .putMetadata("customerId", booking.getCustomerId())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            Payment payment = paymentRepo.findByBookingId(booking.getId()).orElse(new Payment());
            payment.setBookingId(booking.getId());
            payment.setCustomerId(booking.getCustomerId());
            payment.setAmount(booking.getTotalAmount());
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
            log.error("Stripe error while creating PaymentIntent: {}", e.getMessage());
            throw new RuntimeException("Stripe gateway failure: " + e.getMessage());
        }
    }

    @Transactional
    public String handleWebhook(String payload, String sigHeader) {
        Event event;
        try {
            // Cryptographically verify the webhook payload using the Stripe signature
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (JsonSyntaxException | SignatureVerificationException e) {
            log.error("Invalid webhook signature or payload: {}", e.getMessage());
            throw new IllegalArgumentException("Webhook verification failed");
        }

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

    private void confirmSuccessfulPayment(PaymentIntent paymentIntent) {
        Optional<Payment> optionalPayment = paymentRepo.findByStripePaymentIntentId(paymentIntent.getId());
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();

            // Idempotency check: If already processed, skip
            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) return;

            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            paymentRepo.save(payment);

            // Publish success event so BookingService can release the 5-min lock and update to CONFIRMED
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
        } else {
            log.warn("Received successful payment webhook for unknown PaymentIntent: {}", paymentIntent.getId());
        }
    }

    private void markPaymentFailed(PaymentIntent paymentIntent) {
        paymentRepo.findByStripePaymentIntentId(paymentIntent.getId()).ifPresent(payment -> {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason(paymentIntent.getLastPaymentError() != null ?
                    paymentIntent.getLastPaymentError().getMessage() : "Unknown gateway decline");
            paymentRepo.save(payment);

            log.warn("Payment failed for bookingId: {} due to: {}", payment.getBookingId(), payment.getFailureReason());
        });
    }

    @Transactional
    public void processAutomatedRefund(String bookingId) {
        Payment payment = paymentRepo.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment record found for bookingId: " + bookingId));

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            log.warn("Cannot refund payment in status {} for bookingId: {}", payment.getPaymentStatus(), bookingId);
            return; // Only SUCCESS payments can be refunded
        }

        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getStripePaymentIntentId())
                    .build();

            // Synchronously call Stripe to process the refund
            Refund.create(params);

            payment.setPaymentStatus(PaymentStatus.REFUNDED);
            paymentRepo.save(payment);

            eventPublisher.publishPaymentRefundEvent(bookingId, payment.getStripePaymentIntentId());
            log.info("Stripe refund processed successfully for bookingId: {}", bookingId);

        } catch (StripeException e) {
            log.error("Failed to process Stripe refund for bookingId {}: {}", bookingId, e.getMessage());
            throw new RuntimeException("Stripe refund failed: " + e.getMessage());
        }
    }

    @Transactional
    public String refundPayment(PaymentRequestDTO dto) {
        // Manual override or API-triggered refund
        processAutomatedRefund(dto.getBookingId());
        return "Payment refund completed successfully for booking: " + dto.getBookingId();
    }

    @Transactional(readOnly = true)
    public String verifyAndConfirmOrder(String bookingId) {
        Payment payment = paymentRepo.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for bookingId: " + bookingId));
        return "Current payment status for booking " + bookingId + " is: " + payment.getPaymentStatus();
    }
}
package com.bluewave.controller;

import com.bluewave.dto.CommonApiResponse;
import com.bluewave.dto.PaymentRequestDTO;
import com.bluewave.dto.PaymentResponseDTO;
import com.bluewave.dto.StripeConnectOnboardingResponseDTO;
import com.bluewave.service.PaymentService;
import com.bluewave.service.StripeConnectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final StripeConnectService stripeConnectService;

    @PostMapping("/checkout")
    public ResponseEntity<PaymentResponseDTO> createPayment(@RequestBody PaymentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(dto));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> stripeWebhook(@RequestBody String payload, @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        return ResponseEntity.ok(paymentService.handleWebhook(payload, sigHeader));
    }

    @PostMapping("/refund")
    public ResponseEntity<String> refundPayment(@RequestBody PaymentRequestDTO dto) {
        return ResponseEntity.ok(paymentService.refundPayment(dto));
    }

    @PostMapping("/verify/{bookingId}")
    public ResponseEntity<String> verifyPaymentIntent(@PathVariable String bookingId) {
        return ResponseEntity.ok(paymentService.verifyAndConfirmOrder(bookingId));
    }

    @PostMapping("/connect/onboard")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<CommonApiResponse<StripeConnectOnboardingResponseDTO>> createConnectOnboardingLink(
            @RequestParam String email,
            @RequestParam String providerId
    ) {
        return ResponseEntity.ok(stripeConnectService.createProviderOnboardingLink(email, providerId));
    }
}

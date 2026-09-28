package com.bluewave.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload sent by booking-service/frontend to create a destination charge.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InitiatePaymentRequestDTO {

    @NotBlank(message = "booking ID is required")
    private String bookingId;

    @NotBlank(message = "customer ID is required")
    private String customerId;

    @NotBlank(message = "provider ID is required")
    private String providerId; // Used to fetch destination Stripe Account

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "1.00", message = "amount should be positive")
    private BigDecimal amount;

    // Optional override. If null, PaymentService uses default platform % (e.g. 10%)
    private BigDecimal platformFeePercent;
}

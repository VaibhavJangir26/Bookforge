package com.bluewave.model;

import com.bluewave.constants.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String bookingId;

    @Column(nullable = false)
    private String customerId;

    private String providerId;

    @Column(unique = true)
    private String stripePaymentIntentId;

    private String destinationStripeAccountId; // Provider's Express account

    private BigDecimal amount;                 // Total customer paid

    private BigDecimal platformFeeAmount;      // Platform commission

    private BigDecimal providerPayoutAmount;   // Provider payout

    private BigDecimal refundAmount;           // Refunded portion

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private String failureReason;

    private String refundId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

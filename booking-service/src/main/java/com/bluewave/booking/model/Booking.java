package com.bluewave.booking.model;

import com.bluewave.booking.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "booking", uniqueConstraints = {
        @UniqueConstraint(name = "uk_idempotency", columnNames = {"idempotencyKey"}),
        // Prevents two confirmed/pending bookings for the same space at the same time
        @UniqueConstraint(name = "uk_space_time_status", columnNames = {"spaceId", "slotStartTime", "slotEndTime", "status"})
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String venueId;

    @Column(nullable = false)
    private String spaceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status=BookingStatus.PENDING_PAYMENT;

    @Column(nullable = false)
    private LocalDateTime slotStartTime;

    @Column(nullable = false)
    private LocalDateTime slotEndTime;

    @Version
    private Long version;

    @Column(nullable = false)
    private BigDecimal basePriceAmount; // Base space rate at time of booking

    @Column(nullable = false)
    private BigDecimal resourceTotalAmount; // Total add-on cost

    @Column(nullable = false)
    private BigDecimal taxAmount;

    @Column(nullable = false)
    private BigDecimal discountAmount;

    @Column(nullable = false)
    private BigDecimal totalAmount; // Final charge to customer

    private String cancellationReason;

    private LocalDateTime holdExpiresAt; // 10-minute hold window timestamp

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "booking",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<BookingResource> bookingResourceList=new ArrayList<>();


}

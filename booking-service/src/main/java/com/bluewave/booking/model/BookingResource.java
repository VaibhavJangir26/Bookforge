package com.bluewave.booking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class BookingResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String resourceId; // Original resource ID in Catalog Service

    @Column(nullable = false)
    private String name; // e.g., "4K Camera Unit" (Saved as text in case resource is renamed later)

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal pricePerUnit; // Price snapshot at the time of booking

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id",nullable = false)
    private Booking booking;

}

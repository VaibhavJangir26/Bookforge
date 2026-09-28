package com.bluewave.entity;


import com.bluewave.utils.ProviderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString(exclude = {"users"})
@EqualsAndHashCode(exclude = {"users"})
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Embedded
    private Address address;

    @Column(unique = true)
    private String mobileNo;

    private String fullName;

    private String businessName;

    @Column(unique = true)
    private String taxOrGstNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderStatus providerStatus = ProviderStatus.NONE;


    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "profile")
    private Users users;

    /**
     * The Provider's connected Stripe Express Account ID (e.g. "acct_1Ox...")
     */
    @Column(name = "stripe_account_id")
    private String stripeAccountId;
    /**
     * True once the provider verifies their bank details and KYC on Stripe
     */
    @Column(name = "stripe_payouts_enabled", nullable = false)
    private boolean stripePayoutsEnabled = false;
}

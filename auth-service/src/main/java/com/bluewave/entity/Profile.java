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
}

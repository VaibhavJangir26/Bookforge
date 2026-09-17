package com.bluewave.venue;

import com.bluewave.category.Category;
import com.bluewave.space.Space;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString(exclude = "category")
@EqualsAndHashCode(exclude = "category")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String providerId;

    private String name;

    private String slug;

    private String description;

    private String contactEmail;

    private String contactPhone;

    @Enumerated(EnumType.STRING)
    private VenueStatus venueStatus=VenueStatus.VERIFICATION_PENDING;

    @Embedded
    private Address address;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id",nullable = false)
    private Category category;


    @OneToMany(mappedBy = "venue",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Space> spaceList=new ArrayList<>();

}

package com.bluewave.space;

import com.bluewave.availablity.model.AvailableRule;
import com.bluewave.availablity.model.BlackoutSlot;
import com.bluewave.resources.Resources;
import com.bluewave.venue.Venue;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString(exclude = "venue")
@EqualsAndHashCode(exclude = "venue")
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String description;

    private int capacity;

    private BigDecimal basePrice;

    private Boolean active = true;

    @ElementCollection
    private List<String> imgUrls = new ArrayList<>();

    @ElementCollection
    private List<String> imgPublicIds = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id",nullable = false)
    private Venue venue;

    @OneToMany(mappedBy = "space",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Resources> resourcesList=new ArrayList<>();

    @OneToMany(mappedBy = "space",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<AvailableRule> availableRuleList=new ArrayList<>();

    @OneToMany(mappedBy = "space",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<BlackoutSlot> blackoutSlotList=new ArrayList<>();

}

package com.bluewave.resources;

import com.bluewave.space.Space;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString(exclude = "space")
@EqualsAndHashCode(exclude = "space")
public class Resources {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal resourcePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourcePriceType resourcePriceType;

    @Column(nullable = false)
    private int resourceCountQuantity;

    @Column(nullable = false)
    private boolean mandatory = false;

    @ElementCollection
    @CollectionTable(name = "resource_img_urls", joinColumns = @JoinColumn(name = "resource_id"))
    @Column(name = "img_url")
    private List<String> imgUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "resource_img_public_ids", joinColumns = @JoinColumn(name = "resource_id"))
    @Column(name = "public_id")
    private List<String> imgPublicIds = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;
}
package com.bluewave.availablity.model;


import com.bluewave.space.Space;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString(exclude = "space")
@EqualsAndHashCode(exclude = "space")
public class BlackoutSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false,name = "space_id")
    private Space space;
}

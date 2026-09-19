package com.bluewave.availablity.model;

import com.bluewave.availablity.DayOfWeek;
import com.bluewave.space.Space;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString(exclude = "space")
@EqualsAndHashCode(exclude = "space")
public class AvailableRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private LocalTime openingTime;

    private LocalTime closingTime;

    private boolean open=true;

    private int slotDurationInMinutes=60;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false,name = "space_id")
    private Space space;


}

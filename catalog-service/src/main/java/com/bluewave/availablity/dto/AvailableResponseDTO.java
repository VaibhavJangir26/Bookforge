package com.bluewave.availablity.dto;

import com.bluewave.availablity.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvailableResponseDTO {

    private String id;
    private String spaceId;
    private DayOfWeek dayOfWeek;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private boolean open;
    private int slotDurationInMinutes;

}

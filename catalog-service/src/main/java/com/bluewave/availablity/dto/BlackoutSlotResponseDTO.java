package com.bluewave.availablity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlackoutSlotResponseDTO {

    private String id;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String reason;
    private String spaceId;

}

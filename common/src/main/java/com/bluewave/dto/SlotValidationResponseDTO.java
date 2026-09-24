package com.bluewave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SlotValidationResponseDTO {
    private String spaceId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Boolean isAvailable;
    private String reason;
}
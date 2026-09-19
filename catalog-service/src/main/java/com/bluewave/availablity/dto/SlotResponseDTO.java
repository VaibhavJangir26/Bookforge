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
public class SlotResponseDTO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean available;
    private String reason;
}
package com.bluewave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalculatePriceRequestDTO {

    @NotBlank(message = "space id is required")
    private String spaceId;

    @NotNull(message = "slot start time is required")
    private LocalDateTime slotStartTime;

    @NotNull(message = "slot end time is required")
    private LocalDateTime slotEndTime;

    private List<String> resourceIds;
}

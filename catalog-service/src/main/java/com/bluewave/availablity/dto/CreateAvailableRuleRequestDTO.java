package com.bluewave.availablity.dto;

import com.bluewave.availablity.DayOfWeek;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class CreateAvailableRuleRequestDTO {

    @NotBlank(message = "space id is required")
    private String spaceId;

    @NotNull(message = "day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "opening time is required")
    private LocalTime openingTime;

    @NotNull(message = "closing time is required")
    private LocalTime closingTime;

    @NotNull(message = "open status flag is required")
    private Boolean open;

    @Min(value = 15, message = "slot duration must be at least 15 minutes")
    @Max(value = 720, message = "slot duration cannot exceed 12 hours")
    private int slotDurationInMinutes = 60;


}

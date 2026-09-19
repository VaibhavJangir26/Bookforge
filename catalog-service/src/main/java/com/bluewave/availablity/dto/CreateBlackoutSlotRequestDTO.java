package com.bluewave.availablity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateBlackoutSlotRequestDTO {

    @NotBlank(message = "space id is required")
    private String spaceId;

    @NotNull(message = "start date time is required")
    private LocalDateTime startDateTime;

    @NotNull(message = "end date time is required")
    private LocalDateTime endDateTime;

    @NotBlank(message = "reason for blackout is  required")
    private String reason;

}

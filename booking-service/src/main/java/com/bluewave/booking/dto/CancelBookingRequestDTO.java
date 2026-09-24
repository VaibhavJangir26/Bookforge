package com.bluewave.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelBookingRequestDTO {

    @NotBlank(message = "booking id is required")
    private String bookingId;

    @NotBlank(message = "cancel reason is required")
    private String cancellationReason;
}

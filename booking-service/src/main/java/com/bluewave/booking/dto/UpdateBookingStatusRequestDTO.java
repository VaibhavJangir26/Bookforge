package com.bluewave.booking.dto;

import com.bluewave.dto.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateBookingStatusRequestDTO {

    @NotNull(message = "booking status is required")
    private BookingStatus bookingStatus;

}

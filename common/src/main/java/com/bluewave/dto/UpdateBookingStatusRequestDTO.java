package com.bluewave.dto;

import com.bluewave.constants.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingStatusRequestDTO {
    private BookingStatus bookingStatus;
}

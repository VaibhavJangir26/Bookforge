package com.bluewave.booking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CreateBookingRequestDTO {

    @NotBlank(message = "a unique idempotency key is required")
    private String idempotencyKey;

    @NotBlank(message = "customer id is required")
    private String customerId;

    @NotBlank(message = "venue id is required")
    private String venueId;

    @NotBlank(message = "space id is required")
    private String spaceId;

    @DecimalMin(value = "0.0",message = "base price can't be negative")
    @NotNull(message = "base price is required")
    private BigDecimal basePriceAmount;

    @NotNull(message = "Slot start time is required")
    private LocalDateTime slotStartTime;

    @NotNull(message = "Slot end time is required")
    private LocalDateTime slotEndTime;

    private List<BookingResourceRequestDTO> bookingResourceItem=new ArrayList<>();



}

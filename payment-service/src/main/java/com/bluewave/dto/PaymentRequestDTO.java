package com.bluewave.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentRequestDTO {

    @NotBlank(message = "booking id is required")
    private String bookingId;

}

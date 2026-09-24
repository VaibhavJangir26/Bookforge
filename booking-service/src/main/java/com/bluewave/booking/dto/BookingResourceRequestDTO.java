package com.bluewave.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookingResourceRequestDTO {

    @NotBlank(message = "resource id is required")
    private String resourceId;

    @NotBlank(message = "resource name is required")
    private String name;

    @NotNull(message = "customer id is required")
    @Min(message = "quantity can't be less then 1",value = 1)
    private Integer quantity;

    @NotNull(message = "price per unit of resource is required")
    private BigDecimal pricePerUnit;

}

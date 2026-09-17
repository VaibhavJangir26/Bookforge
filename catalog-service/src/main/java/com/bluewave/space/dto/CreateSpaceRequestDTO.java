package com.bluewave.space.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSpaceRequestDTO {

    @NotBlank(message = "space name is required")
    private String name;

    @NotBlank(message = "brief description about space is required")
    private String description;

    @NotNull(message = "max capacity of space is required")
    @Min(value = 1, message = "capacity should be at least 1")
    private Integer capacity;

    @NotNull(message = "space base price is required")
    @DecimalMin(value = "0.0", message = "price should be positive or zero")
    private BigDecimal basePrice;

    @NotBlank(message = "venue id is required")
    private String venueId;

}

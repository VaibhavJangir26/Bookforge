package com.bluewave.space.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateSpaceRequestDTO {


    private String name;

    private String description;

    @Min(value = 1, message = "capacity should be at least 1")
    private Integer capacity;

    @DecimalMin(value = "0.0", message = "price should be positive or zero")
    private BigDecimal basePrice;

    private Boolean active;

}

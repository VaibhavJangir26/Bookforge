package com.bluewave.resources.dto;

import com.bluewave.resources.ResourcePriceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateResourceRequestDTO {

    @NotBlank(message = "resource name is required")
    private String name;

    private String description;

    @NotNull(message = "resource price is required")
    @DecimalMin(value = "0.0", message = "price should not be negative")
    private BigDecimal resourcePrice;

    @NotNull(message = "resource price type is required")
    private ResourcePriceType resourcePriceType;

    @NotNull(message = "resource count quantity is required")
    @Min(value = 1, message = "there should be at least one quantity")
    private Integer resourceCountQuantity;

    @NotNull(message = "mandatory flag is required")
    private Boolean mandatory;

    @NotBlank(message = "space id is required")
    private String spaceId;
}
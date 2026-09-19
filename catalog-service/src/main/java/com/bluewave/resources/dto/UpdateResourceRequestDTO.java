package com.bluewave.resources.dto;

import com.bluewave.resources.ResourcePriceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateResourceRequestDTO {

    private String name;

    private String description;

    @DecimalMin(value = "0.0",  message = "price should not be negative")
    private BigDecimal resourcePrice;

    private ResourcePriceType resourcePriceType;

    @Min(value = 1, message = "there should be at least one quantity")
    private Integer resourceCountQuantity;

    private Boolean mandatory;

    private List<String> publicIdsToDelete;
}
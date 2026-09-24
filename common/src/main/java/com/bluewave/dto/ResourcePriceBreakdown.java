package com.bluewave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public  class ResourcePriceBreakdown {
    private String resourceId;
    private String resourceName;
    private BigDecimal price;
}
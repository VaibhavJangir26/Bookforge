package com.bluewave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalculatePriceResponseDTO {
    private String spaceId;
    private BigDecimal basePrice;
    private BigDecimal calculatedSpacePrice;
    private BigDecimal totalResourcePrice;
    private BigDecimal finalTotalPrice;
    private String appliedRuleName;
    private String appliedRuleType;
    private List<ResourcePriceBreakdown> resourceBreakdowns;
}

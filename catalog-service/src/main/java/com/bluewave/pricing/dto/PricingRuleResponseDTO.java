package com.bluewave.pricing.dto;

import com.bluewave.availablity.DayOfWeek;
import com.bluewave.pricing.AdjustmentType;
import com.bluewave.pricing.RuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PricingRuleResponseDTO {

    private String id;
    private String spaceId;
    private String name;
    private RuleType ruleType;
    private AdjustmentType adjustmentType;
    private BigDecimal priceAdjustment;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Integer priority;
    private Boolean active;
    private LocalDateTime createdAt;
}
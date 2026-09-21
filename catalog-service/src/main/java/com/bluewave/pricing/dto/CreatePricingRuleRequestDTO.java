package com.bluewave.pricing.dto;

import com.bluewave.availablity.DayOfWeek;
import com.bluewave.pricing.AdjustmentType;
import com.bluewave.pricing.RuleType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePricingRuleRequestDTO {

    @NotBlank(message = "space id is required")
    private String spaceId;

    @NotBlank(message = "rule name is required")
    private String name;

    @NotNull(message = "rule type is required")
    private RuleType ruleType;

    @NotNull(message = "adjustment type is required")
    private AdjustmentType adjustmentType;

    @NotNull(message = "price adjustment value is required")
    @DecimalMin(value = "0.0", message = "price adjustment cannot be negative")
    private BigDecimal priceAdjustment;

    private DayOfWeek dayOfWeek;

    private LocalTime startTime;
    private LocalTime endTime;

    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    @Min(value = 1, message = "priority must be at least 1")
    private int priority = 1;

    private Boolean active = true;
}
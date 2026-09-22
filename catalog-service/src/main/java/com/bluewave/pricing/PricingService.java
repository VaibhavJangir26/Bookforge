package com.bluewave.pricing;

import com.bluewave.availablity.DayOfWeek;
import com.bluewave.dto.CommonApiResponse;
import com.bluewave.exception.ResourceNotFoundException;
import com.bluewave.pricing.dto.CalculatePriceRequestDTO;
import com.bluewave.pricing.dto.CalculatePriceResponseDTO;
import com.bluewave.pricing.dto.CreatePricingRuleRequestDTO;
import com.bluewave.pricing.dto.PricingRuleResponseDTO;
import com.bluewave.resources.ResourcePriceType;
import com.bluewave.resources.ResourceRepo;
import com.bluewave.resources.Resources;
import com.bluewave.space.Space;
import com.bluewave.space.SpaceRepo;
import com.bluewave.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingRepo pricingRuleRepo;
    private final SpaceRepo spaceRepo;
    private final ResourceRepo resourceRepo;

    private void validateSpaceOwnership(Space space) {
        String currentUserId = UserContext.getUserId();
        if (!UserContext.isAdmin() && !space.getVenue().getProviderId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to configure pricing rules for this space");
        }
    }

    @Transactional
    public CommonApiResponse<PricingRuleResponseDTO> createPricingRule(CreatePricingRuleRequestDTO requestDTO) {
        Space space = spaceRepo.findById(requestDTO.getSpaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Space not found with id: " + requestDTO.getSpaceId()));

        validateSpaceOwnership(space);
        validateRuleParameters(requestDTO);

        PricingRule rule = PricingRule.builder()
                .space(space)
                .name(requestDTO.getName().trim())
                .ruleType(requestDTO.getRuleType())
                .adjustmentType(requestDTO.getAdjustmentType())
                .priceAdjustment(requestDTO.getPriceAdjustment())
                .dayOfWeek(requestDTO.getDayOfWeek())
                .startTime(requestDTO.getStartTime())
                .endTime(requestDTO.getEndTime())
                .validFrom(requestDTO.getValidFrom())
                .validTo(requestDTO.getValidTo())
                .priority(requestDTO.getPriority() > 0 ? requestDTO.getPriority() : 1)
                .active(requestDTO.getActive() != null ? requestDTO.getActive() : true)
                .build();

        PricingRule saved = pricingRuleRepo.save(rule);

        return CommonApiResponse.<PricingRuleResponseDTO>builder()
                .status(HttpStatus.CREATED.value())
                .timestamp(LocalDateTime.now())
                .message("Pricing rule created successfully")
                .data(mapToDTO(saved))
                .success(true)
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<List<PricingRuleResponseDTO>> getPricingRulesBySpaceId(String spaceId) {
        if (!spaceRepo.existsById(spaceId)) {
            throw new ResourceNotFoundException("Space not found with id: " + spaceId);
        }

        List<PricingRuleResponseDTO> dtos = pricingRuleRepo.findBySpaceId(spaceId).stream()
                .map(this::mapToDTO)
                .toList();

        return CommonApiResponse.<List<PricingRuleResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .message("Pricing rules fetched successfully")
                .data(dtos)
                .success(true)
                .build();
    }

    @Transactional
    public String deletePricingRule(String ruleId) {
        PricingRule rule = pricingRuleRepo.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing rule not found with id: " + ruleId));

        validateSpaceOwnership(rule.getSpace());
        pricingRuleRepo.delete(rule);

        return "Pricing rule deleted successfully";
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<CalculatePriceResponseDTO> calculatePrice(CalculatePriceRequestDTO requestDTO) {
        Space space = spaceRepo.findById(requestDTO.getSpaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Space not found with id: " + requestDTO.getSpaceId()));

        if (!requestDTO.getSlotStartTime().isBefore(requestDTO.getSlotEndTime())) {
            throw new IllegalArgumentException("Slot start time must be before slot end time");
        }

        BigDecimal basePrice = space.getBasePrice();
        List<PricingRule> activeRules = pricingRuleRepo.findBySpaceIdAndActiveTrueOrderByPriorityDesc(space.getId());

        // Find highest priority matching dynamic rule
        Optional<PricingRule> matchingRule = activeRules.stream()
                .filter(rule -> isRuleApplicable(rule, requestDTO.getSlotStartTime(), requestDTO.getSlotEndTime()))
                .findFirst();

        BigDecimal calculatedSpacePrice = basePrice;
        String appliedRuleName = "BASE_RATE";
        String appliedRuleType = "STANDARD";

        if (matchingRule.isPresent()) {
            PricingRule rule = matchingRule.get();
            appliedRuleName = rule.getName();
            appliedRuleType = rule.getRuleType().name();

            if (rule.getAdjustmentType() == AdjustmentType.PERCENTAGE) {
                BigDecimal multiplier = rule.getPriceAdjustment().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                BigDecimal modifier = basePrice.multiply(multiplier);
                calculatedSpacePrice = basePrice.add(modifier).setScale(2, RoundingMode.HALF_UP);
            } else if (rule.getAdjustmentType() == AdjustmentType.FIXED_PRICE) {
                calculatedSpacePrice = rule.getPriceAdjustment().setScale(2, RoundingMode.HALF_UP);
            }
        }

        // Calculate selected resource add-ons
        BigDecimal totalResourcePrice = BigDecimal.ZERO;
        List<ResourcePriceBreakdown> breakdowns = new ArrayList<>();
        long durationHours = Math.max(1, Duration.between(requestDTO.getSlotStartTime(), requestDTO.getSlotEndTime()).toHours());

        if (requestDTO.getResourceIds() != null && !requestDTO.getResourceIds().isEmpty()) {
            List<Resources> resources = resourceRepo.findAllById(requestDTO.getResourceIds());

            for (Resources resource : resources) {
                BigDecimal cost = resource.getResourcePrice();
                if (resource.getResourcePriceType() == ResourcePriceType.PER_HOUR) {
                    cost = cost.multiply(BigDecimal.valueOf(durationHours));
                }

                totalResourcePrice = totalResourcePrice.add(cost);
                breakdowns.add(ResourcePriceBreakdown.builder()
                        .resourceId(resource.getId())
                        .resourceName(resource.getName())
                        .price(cost)
                        .build());
            }
        }

        BigDecimal finalTotal = calculatedSpacePrice.add(totalResourcePrice).setScale(2, RoundingMode.HALF_UP);

        CalculatePriceResponseDTO response = CalculatePriceResponseDTO.builder()
                .spaceId(space.getId())
                .basePrice(basePrice)
                .calculatedSpacePrice(calculatedSpacePrice)
                .totalResourcePrice(totalResourcePrice)
                .finalTotalPrice(finalTotal)
                .appliedRuleName(appliedRuleName)
                .appliedRuleType(appliedRuleType)
                .resourceBreakdowns(breakdowns)
                .build();

        return CommonApiResponse.<CalculatePriceResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .message("Dynamic price calculated successfully")
                .data(response)
                .success(true)
                .build();
    }

    private boolean isRuleApplicable(PricingRule rule, LocalDateTime slotStart, LocalDateTime slotEnd) {
        DayOfWeek slotDay = DayOfWeek.valueOf(slotStart.getDayOfWeek().name());
        LocalTime slotStartTime = slotStart.toLocalTime();
        LocalTime slotEndTime = slotEnd.toLocalTime();

        return switch (rule.getRuleType()) {
            case PEAK_HOUR -> {
                if (rule.getDayOfWeek() != null && rule.getDayOfWeek() != slotDay) {
                    yield false;
                }
                yield rule.getStartTime() != null && rule.getEndTime() != null
                        && !slotStartTime.isBefore(rule.getStartTime())
                        && !slotEndTime.isAfter(rule.getEndTime());
            }
            case WEEKEND -> {
                if (rule.getDayOfWeek() != null) {
                    yield rule.getDayOfWeek() == slotDay;
                }
                yield slotDay == DayOfWeek.SATURDAY || slotDay == DayOfWeek.SUNDAY;
            }
            case SPECIFIC_DATE, SEASONAL -> rule.getValidFrom() != null && rule.getValidTo() != null
                    && !slotStart.isBefore(rule.getValidFrom())
                    && !slotEnd.isAfter(rule.getValidTo());
        };
    }

    private void validateRuleParameters(CreatePricingRuleRequestDTO dto) {
        switch (dto.getRuleType()) {
            case PEAK_HOUR:
                if (dto.getStartTime() == null || dto.getEndTime() == null) {
                    throw new IllegalArgumentException("PEAK_HOUR rules require startTime and endTime");
                }
                if (!dto.getStartTime().isBefore(dto.getEndTime())) {
                    throw new IllegalArgumentException("startTime must be before endTime for PEAK_HOUR");
                }
                break;

            case SPECIFIC_DATE:
            case SEASONAL:
                if (dto.getValidFrom() == null || dto.getValidTo() == null) {
                    throw new IllegalArgumentException(dto.getRuleType() + " rules require validFrom and validTo");
                }
                if (!dto.getValidFrom().isBefore(dto.getValidTo())) {
                    throw new IllegalArgumentException("validFrom must be before validTo");
                }
                break;

            case WEEKEND:
                break;
        }
    }

    private PricingRuleResponseDTO mapToDTO(PricingRule rule) {
        return PricingRuleResponseDTO.builder()
                .id(rule.getId())
                .spaceId(rule.getSpace().getId())
                .name(rule.getName())
                .ruleType(rule.getRuleType())
                .adjustmentType(rule.getAdjustmentType())
                .priceAdjustment(rule.getPriceAdjustment())
                .dayOfWeek(rule.getDayOfWeek())
                .startTime(rule.getStartTime())
                .endTime(rule.getEndTime())
                .validFrom(rule.getValidFrom())
                .validTo(rule.getValidTo())
                .priority(rule.getPriority())
                .active(rule.getActive())
                .createdAt(rule.getCreatedAt())
                .build();
    }
}
package com.bluewave.pricing;

import com.bluewave.dto.CommonApiResponse;
import com.bluewave.pricing.dto.CalculatePriceRequestDTO;
import com.bluewave.pricing.dto.CalculatePriceResponseDTO;
import com.bluewave.pricing.dto.CreatePricingRuleRequestDTO;
import com.bluewave.pricing.dto.PricingRuleResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pricing")
public class PricingController {

    private final PricingService pricingService;

    @PostMapping("/rules")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<CommonApiResponse<PricingRuleResponseDTO>> createPricingRule(@Valid @RequestBody CreatePricingRuleRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pricingService.createPricingRule(requestDTO));
    }

    @GetMapping("/rules/space/{spaceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'CUSTOMER')")
    public ResponseEntity<CommonApiResponse<List<PricingRuleResponseDTO>>> getPricingRulesBySpaceId(@PathVariable String spaceId) {
        return ResponseEntity.ok(pricingService.getPricingRulesBySpaceId(spaceId));
    }

    @DeleteMapping("/rules/{ruleId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, String>> deletePricingRule(@PathVariable String ruleId) {
        String message = pricingService.deletePricingRule(ruleId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'CUSTOMER')")
    public ResponseEntity<CommonApiResponse<CalculatePriceResponseDTO>> calculatePrice(@Valid @RequestBody CalculatePriceRequestDTO requestDTO) {
        return ResponseEntity.ok(pricingService.calculatePrice(requestDTO));
    }

}
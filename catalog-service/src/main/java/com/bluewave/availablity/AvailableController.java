package com.bluewave.availablity;

import com.bluewave.availablity.dto.*;
import com.bluewave.dto.CommonApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
public class AvailableController {

    private final AvailableService availableService;

    @PostMapping("/rules")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<CommonApiResponse<AvailableResponseDTO>> createAvailableRule(@Valid @RequestBody CreateAvailableRuleRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(availableService.createAvailableRule(requestDTO));
    }

    @PostMapping("/blackouts")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<CommonApiResponse<BlackoutSlotResponseDTO>> createBlackoutSlots(@Valid @RequestBody CreateBlackoutSlotRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(availableService.createBlackoutSlots(requestDTO));
    }

    @GetMapping("/rules/space/{spaceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'CUSTOMER')")
    public ResponseEntity<CommonApiResponse<List<AvailableResponseDTO>>> getAllAvailability(@PathVariable String spaceId) {
        return ResponseEntity.ok(availableService.getAllAvailability(spaceId));
    }

    @GetMapping("/blackouts/space/{spaceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'CUSTOMER')")
    public ResponseEntity<CommonApiResponse<List<BlackoutSlotResponseDTO>>> getAllBlackoutSlots(@PathVariable String spaceId) {
        return ResponseEntity.ok(availableService.getAllBlackoutSlots(spaceId));
    }

    @GetMapping("/slots/space/{spaceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'CUSTOMER')")
    public ResponseEntity<CommonApiResponse<List<SlotResponseDTO>>> getAvailableSlots(
            @PathVariable String spaceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(availableService.getAvailableSlots(spaceId, startDate, endDate));
    }

    @DeleteMapping("/rules/{availabilityId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, String>> deleteAvailability(@PathVariable String availabilityId) {
        String message = availableService.deleteAvailability(availabilityId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @DeleteMapping("/blackouts/{blackoutId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, String>> deleteBlackouts(@PathVariable String blackoutId) {
        String message = availableService.deleteBlackouts(blackoutId);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
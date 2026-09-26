package com.bluewave.booking.client;

import com.bluewave.dto.CalculatePriceRequestDTO;
import com.bluewave.dto.CalculatePriceResponseDTO;
import com.bluewave.dto.CommonApiResponse;
import com.bluewave.dto.ResponseSpacesDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@FeignClient(name = "CATALOG-SERVICE", fallbackFactory = CatalogClientFallbackFactory.class)
public interface CatalogClient {

    @PostMapping("/api/v1/pricing/calculate")
    CommonApiResponse<CalculatePriceResponseDTO> calculatePrice(@Valid @RequestBody CalculatePriceRequestDTO requestDTO);

    @PostMapping("/api/v1/availability/validate")
    CommonApiResponse<Void> validateSlot(
            @RequestParam("spaceId") String spaceId,
            @RequestParam("startDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam("endDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime
    );

    @GetMapping("/api/v1/spaces/{spaceId}")
    CommonApiResponse<ResponseSpacesDTO> getSpaceById(@PathVariable("spaceId") String spaceId);

    @PostMapping("/api/v1/resources/{resourceId}/deduct-stock")
    CommonApiResponse<String> deductResourceStock(
            @PathVariable("resourceId") String resourceId,
            @RequestParam("quantity") int quantity);

    @PostMapping("/api/v1/resources/{resourceId}/restore-stock")
    CommonApiResponse<String> restoreResourceStock(
            @PathVariable("resourceId") String resourceId,
            @RequestParam("quantity") int quantity);
}
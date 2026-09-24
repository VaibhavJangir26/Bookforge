package com.bluewave.booking.client;

import com.bluewave.dto.CalculatePriceRequestDTO;
import com.bluewave.dto.CalculatePriceResponseDTO;
import com.bluewave.dto.CommonApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class CatalogClientFallbackFactory implements FallbackFactory<CatalogClient> {

    @Override
    public CatalogClient create(Throwable cause) {
        return new CatalogClient() {

            @Override
            public CommonApiResponse<CalculatePriceResponseDTO> calculatePrice(CalculatePriceRequestDTO requestDTO) {
                log.error("CatalogClient fallback triggered for calculatePrice. Error: {}", cause.getMessage());
                return CommonApiResponse.<CalculatePriceResponseDTO>builder()
                        .success(false)
                        .status(Integer.valueOf(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value())))
                        .message("Catalog service is currently unavailable for price calculation: " + cause.getMessage())
                        .timestamp(LocalDateTime.now())
                        .data(null)
                        .build();
            }

            @Override
            public CommonApiResponse<Void> validateSlot(String spaceId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
                log.error("CatalogClient fallback triggered for validateSlot on space {}. Error: {}", spaceId, cause.getMessage());
                return CommonApiResponse.<Void>builder()
                        .success(false)
                        .status(Integer.valueOf(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value())))
                        .message("Catalog service is currently unavailable for slot validation: " + cause.getMessage())
                        .timestamp(LocalDateTime.now())
                        .data(null)
                        .build();
            }
        };
    }
}
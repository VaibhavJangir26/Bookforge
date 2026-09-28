package com.bluewave.client;

import com.bluewave.dto.CommonApiResponse;
import com.bluewave.dto.ResponseSpacesDTO;
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
            public CommonApiResponse<ResponseSpacesDTO> getSpaceById(String spaceId) {
                log.error("CatalogClient fallback triggered for getSpaceById {}. Reason: {}", spaceId, cause.getMessage());
                return CommonApiResponse.<ResponseSpacesDTO>builder()
                        .success(false)
                        .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .message("Catalog service currently unavailable: " + cause.getMessage())
                        .timestamp(LocalDateTime.now())
                        .data(null)
                        .build();
            }
        };
    }
}

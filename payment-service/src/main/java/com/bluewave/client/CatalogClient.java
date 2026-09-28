package com.bluewave.client;

import com.bluewave.dto.CommonApiResponse;
import com.bluewave.dto.ResponseSpacesDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CATALOG-SERVICE", fallbackFactory = CatalogClientFallbackFactory.class)
public interface CatalogClient {

    @GetMapping("/api/v1/spaces/{spaceId}")
    CommonApiResponse<ResponseSpacesDTO> getSpaceById(@PathVariable("spaceId") String spaceId);
}

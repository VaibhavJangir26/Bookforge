package com.bluewave.resources.dto;

import com.bluewave.resources.ResourcePriceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourceResponseDTO {

    private String id;
    private String name;
    private String description;
    private BigDecimal resourcePrice;
    private ResourcePriceType resourcePriceType;
    private int resourceCountQuantity;
    private boolean mandatory;
    private List<String> imgUrls;
    private String spaceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
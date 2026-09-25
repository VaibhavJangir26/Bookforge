package com.bluewave.dto;

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
public class ResponseSpacesDTO {
    private String id;
    private String providerId;
    private String name;
    private String description;
    private int capacity;
    private BigDecimal basePrice;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String venueId;
    private List<String> imgUrls;
}

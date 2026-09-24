package com.bluewave.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResourceResponseDTO {

    private String id;
    private String resourceId;
    private String name;
    private Integer quantity;
    private BigDecimal pricePerUnit;

}

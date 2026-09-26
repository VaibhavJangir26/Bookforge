package com.bluewave.dto;

import com.bluewave.constants.BookingStatus;
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
public class BookingResponseDTO {

    private String id;
    private String customerId;
    private String venueId;
    private String spaceId;
    private BookingStatus status;
    private BigDecimal basePriceAmount;
    private BigDecimal resourceTotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private String cancellationReason;
    private List<BookingResourceResponseDTO> resourceItems;

}

package com.bluewave.venue.dto;

import com.bluewave.category.dto.CategoryResponseDTO;
import com.bluewave.venue.Address;
import com.bluewave.venue.VenueStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VenueResponseDTO {

    private String id;
    private String providerId;
    private String slug;
    private String description;
    private String contactEmail;
    private String contactPhone;
    private VenueStatus venueStatus;
    private Address address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CategoryResponseDTO categoryResponseDTO;


}

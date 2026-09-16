package com.bluewave.venue.dto;

import com.bluewave.venue.VenueStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateVenueStatusDTO {

    @NotNull(message = "venue status is required")
    private VenueStatus venueStatus;

}
package com.bluewave.venue;

import com.bluewave.dto.CommonApiResponse;
import com.bluewave.venue.dto.CreateVenueRequestDTO;
import com.bluewave.venue.dto.UpdateVenueDetailsRequestDTO;
import com.bluewave.venue.dto.UpdateVenueStatusDTO;
import com.bluewave.venue.dto.VenueResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepo venueRepo;


    public CommonApiResponse<VenueResponseDTO> createVenue (CreateVenueRequestDTO requestDTO) {
        return null;
    }

    public CommonApiResponse<VenueResponseDTO> updateVenueDetails(String venueId, UpdateVenueDetailsRequestDTO requestDTO) {
        return null;
    }

    public CommonApiResponse<VenueResponseDTO> updateVenueStatus(String venueId, UpdateVenueStatusDTO requestDTO) {
        return null;
    }

    public CommonApiResponse<List<VenueResponseDTO>> getAllVenues() {
        return null;
    }

    public CommonApiResponse<VenueResponseDTO> getVenueDetails(String venueId) {
        return null;
    }

    public String deleteVenue(String venueId) {
        return null;
    }
}

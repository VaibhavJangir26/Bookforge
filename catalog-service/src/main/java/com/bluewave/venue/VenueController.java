package com.bluewave.venue;

import com.bluewave.dto.CommonApiResponse;
import com.bluewave.venue.dto.CreateVenueRequestDTO;
import com.bluewave.venue.dto.UpdateVenueDetailsRequestDTO;
import com.bluewave.venue.dto.UpdateVenueStatusDTO;
import com.bluewave.venue.dto.VenueResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<CommonApiResponse<VenueResponseDTO>> createVenue(@Valid @RequestBody CreateVenueRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.createVenue(requestDTO));
    }

    @PatchMapping("/{venueId}/details")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<CommonApiResponse<VenueResponseDTO>> updateVenueDetails(
            @PathVariable String venueId,
            @Valid @RequestBody UpdateVenueDetailsRequestDTO requestDTO) {
        return ResponseEntity.ok(venueService.updateVenueDetails(venueId, requestDTO));
    }

    @PatchMapping("/{venueId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonApiResponse<VenueResponseDTO>> updateVenueStatus(
            @PathVariable String venueId,
            @Valid @RequestBody UpdateVenueStatusDTO requestDTO) {
        return ResponseEntity.ok(venueService.updateVenueStatus(venueId, requestDTO));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'CUSTOMER')")
    public ResponseEntity<CommonApiResponse<List<VenueResponseDTO>>> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    }

    @GetMapping("/{venueId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'CUSTOMER')")
    public ResponseEntity<CommonApiResponse<VenueResponseDTO>> getVenueDetails(@PathVariable String venueId) {
        return ResponseEntity.ok(venueService.getVenueDetails(venueId));
    }

    @DeleteMapping("/{venueId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    public ResponseEntity<Map<String, String>> deleteVenue(@PathVariable String venueId) {
        String message = venueService.deleteVenue(venueId);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
package com.bluewave.venue;

import com.bluewave.category.Category;
import com.bluewave.category.CategoryRepo;
import com.bluewave.dto.CommonApiResponse;
import com.bluewave.exception.ResourceConflictException;
import com.bluewave.exception.ResourceNotFoundException;
import com.bluewave.utils.UserContext;
import com.bluewave.venue.dto.CreateVenueRequestDTO;
import com.bluewave.venue.dto.UpdateVenueDetailsRequestDTO;
import com.bluewave.venue.dto.UpdateVenueStatusDTO;
import com.bluewave.venue.dto.VenueResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepo venueRepo;
    private final CategoryRepo categoryRepo;

    @Transactional
    public CommonApiResponse<VenueResponseDTO> createVenue(CreateVenueRequestDTO requestDTO) {
        if (venueRepo.existsBySlug(requestDTO.getSlug())) {
            throw new ResourceConflictException("This venue slug already exists");
        }

        Category category = categoryRepo.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with this id"));

        Venue venue = buildVenueEntity(requestDTO, category);
        Venue savedVenue = venueRepo.save(venue);

        return CommonApiResponse.<VenueResponseDTO>builder()
                .success(true)
                .data(mapToResponseDTO(savedVenue))
                .message("Venue created successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.toString())
                .build();
    }

    @Transactional
    public CommonApiResponse<VenueResponseDTO> updateVenueDetails(String venueId, UpdateVenueDetailsRequestDTO requestDTO) {
        Venue existsVenue = venueRepo.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("venue with this id not found for update"));

        // Ownership Validation: Only owner provider or admin can update
        String currentUserId = UserContext.getUserId();
        if (!UserContext.isAdmin() && !existsVenue.getProviderId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to update this venue");
        }

        if (requestDTO.getAddress() != null) {
            existsVenue.setAddress(requestDTO.getAddress());
        }
        if (requestDTO.getSlug() != null && !requestDTO.getSlug().isBlank()) {
            if (venueRepo.existsBySlugAndIdNot(requestDTO.getSlug(), venueId)) {
                throw new ResourceConflictException("slug name already used try new one for update");
            }
            existsVenue.setSlug(requestDTO.getSlug().trim());
        }
        if (requestDTO.getContactEmail() != null && !requestDTO.getContactEmail().isBlank()) {
            existsVenue.setContactEmail(requestDTO.getContactEmail().trim());
        }
        if (requestDTO.getContactPhone() != null && !requestDTO.getContactPhone().isBlank()) {
            existsVenue.setContactPhone(requestDTO.getContactPhone().trim());
        }
        if (requestDTO.getDescription() != null && !requestDTO.getDescription().isBlank()) {
            existsVenue.setDescription(requestDTO.getDescription().trim());
        }
        if (requestDTO.getName() != null && !requestDTO.getName().isBlank()) {
            existsVenue.setName(requestDTO.getName().trim());
        }

        Venue updatedVenue = venueRepo.save(existsVenue);

        return CommonApiResponse.<VenueResponseDTO>builder()
                .success(true)
                .data(mapToResponseDTO(updatedVenue))
                .message("venue details updated successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.toString())
                .build();
    }

    @Transactional
    public CommonApiResponse<VenueResponseDTO> updateVenueStatus(String venueId, UpdateVenueStatusDTO requestDTO) {
        Venue existsVenue = venueRepo.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("venue with this id not found for update"));

        existsVenue.setVenueStatus(requestDTO.getVenueStatus());
        Venue updatedVenue = venueRepo.save(existsVenue);

        return CommonApiResponse.<VenueResponseDTO>builder()
                .success(true)
                .data(mapToResponseDTO(updatedVenue))
                .message("venue status updated successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.toString())
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<List<VenueResponseDTO>> getVenuesByCurrentProvider() {
        String currentUserId = UserContext.getUserId();

        List<VenueResponseDTO> dtoList = venueRepo.findByProviderId(currentUserId).stream()
                .map(this::mapToResponseDTO)
                .toList();

        return CommonApiResponse.<List<VenueResponseDTO>>builder()
                .success(true)
                .data(dtoList)
                .message("Provider venues fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.toString())
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<List<VenueResponseDTO>> getAllVenues() {
        List<VenueResponseDTO> dtoList = venueRepo.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();

        return CommonApiResponse.<List<VenueResponseDTO>>builder()
                .success(true)
                .data(dtoList)
                .message("venue details fetch successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.toString())
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<VenueResponseDTO> getVenueDetails(String venueId) {
        Venue venue = venueRepo.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("venue with this id not found"));

        return CommonApiResponse.<VenueResponseDTO>builder()
                .success(true)
                .data(mapToResponseDTO(venue))
                .message("venue details fetch successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.toString())
                .build();
    }

    @Transactional
    public String deleteVenue(String venueId) {
        Venue venue = venueRepo.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("venue with this id not found"));

        String currentUserId = UserContext.getUserId();
        if (!UserContext.isAdmin() && !venue.getProviderId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to delete this venue");
        }

        venueRepo.delete(venue);
        return "venue deleted successfully";
    }

    private VenueResponseDTO mapToResponseDTO(Venue venue) {
        return VenueResponseDTO.builder()
                .id(venue.getId())
                .providerId(venue.getProviderId())
                .name(venue.getName())
                .slug(venue.getSlug())
                .description(venue.getDescription())
                .contactEmail(venue.getContactEmail())
                .contactPhone(venue.getContactPhone())
                .venueStatus(venue.getVenueStatus())
                .address(venue.getAddress())
                .createdAt(venue.getCreatedAt())
                .updatedAt(venue.getUpdatedAt())
                .categoryId(venue.getCategory() != null ? venue.getCategory().getId() : null)
                .build();
    }

    private Venue buildVenueEntity(CreateVenueRequestDTO requestDTO, Category category) {
        Venue venue = new Venue();
        venue.setName(requestDTO.getName());
        venue.setAddress(requestDTO.getAddress());
        venue.setCategory(category);
        venue.setContactEmail(requestDTO.getContactEmail());
        venue.setContactPhone(requestDTO.getContactPhone());
        venue.setDescription(requestDTO.getDescription());
        venue.setSlug(requestDTO.getSlug());
        venue.setProviderId(UserContext.getUserId());
        venue.setVenueStatus(VenueStatus.VERIFICATION_PENDING);
        return venue;
    }

}
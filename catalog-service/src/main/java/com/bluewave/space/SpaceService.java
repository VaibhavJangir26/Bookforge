package com.bluewave.space;

import com.bluewave.dto.CommonApiResponse;
import com.bluewave.exception.ResourceNotFoundException;
import com.bluewave.space.dto.CreateSpaceRequestDTO;
import com.bluewave.space.dto.ResponseSpacesDTO;
import com.bluewave.space.dto.UpdateSpaceRequestDTO;
import com.bluewave.utils.UserContext;
import com.bluewave.venue.Venue;
import com.bluewave.venue.VenueRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final SpaceRepo spaceRepo;
    private final VenueRepo venueRepo;

    private void validateVenueOwnership(Venue venue) {
        String currentUserId = UserContext.getUserId();
        if (!UserContext.isAdmin() && !venue.getProviderId().equals(currentUserId)) {
            throw new AccessDeniedException("you do not have permission to manage spaces in this venue");
        }
    }

    @Transactional
    public CommonApiResponse<ResponseSpacesDTO> createNewSpace(CreateSpaceRequestDTO requestDTO) {
        Venue venue = venueRepo.findById(requestDTO.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("venue id not found"));

        validateVenueOwnership(venue);

        Space space = new Space();
        space.setBasePrice(requestDTO.getBasePrice());
        space.setCapacity(requestDTO.getCapacity());
        space.setName(requestDTO.getName().trim());
        space.setDescription(requestDTO.getDescription().trim());
        space.setVenue(venue);
        space.setActive(true);

        Space savedSpace = spaceRepo.save(space);

        return CommonApiResponse.<ResponseSpacesDTO>builder()
                .status(HttpStatus.CREATED.toString())
                .timestamp(LocalDateTime.now())
                .message("space created successfully")
                .data(mapToDTO(savedSpace))
                .success(true)
                .build();
    }

    @Transactional
    public CommonApiResponse<ResponseSpacesDTO> updateSpaceDetails(UpdateSpaceRequestDTO requestDTO, String spaceId) {
        Space exists = spaceRepo.findById(spaceId)
                .orElseThrow(() -> new ResourceNotFoundException("space id not found"));

        validateVenueOwnership(exists.getVenue());

        if (requestDTO.getName() != null && !requestDTO.getName().isBlank()) {
            exists.setName(requestDTO.getName().trim());
        }
        if (requestDTO.getDescription() != null && !requestDTO.getDescription().isBlank()) {
            exists.setDescription(requestDTO.getDescription().trim());
        }
        if (requestDTO.getCapacity() != null && requestDTO.getCapacity() >= 1) {
            exists.setCapacity(requestDTO.getCapacity());
        }
        if (requestDTO.getBasePrice() != null) {
            exists.setBasePrice(requestDTO.getBasePrice());
        }
        if (requestDTO.getActive() != null) {
            exists.setActive(requestDTO.getActive());
        }

        Space updatedSpace = spaceRepo.save(exists);

        return CommonApiResponse.<ResponseSpacesDTO>builder()
                .status(HttpStatus.OK.toString())
                .timestamp(LocalDateTime.now())
                .message("space updated successfully")
                .data(mapToDTO(updatedSpace))
                .success(true)
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<List<ResponseSpacesDTO>> getAllSpaces() {
        List<ResponseSpacesDTO> dtoList = spaceRepo.findAll().stream()
                .map(this::mapToDTO)
                .toList();

        return CommonApiResponse.<List<ResponseSpacesDTO>>builder()
                .data(dtoList)
                .message("all spaces fetched successfully")
                .status(HttpStatus.OK.toString())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<ResponseSpacesDTO> getSpaceDetails(String spaceId) {
        Space space = spaceRepo.findById(spaceId)
                .orElseThrow(() -> new ResourceNotFoundException("no space with this id"));

        return CommonApiResponse.<ResponseSpacesDTO>builder()
                .status(HttpStatus.OK.toString())
                .timestamp(LocalDateTime.now())
                .message("space details fetched successfully")
                .data(mapToDTO(space))
                .success(true)
                .build();
    }

    @Transactional
    public String deleteSpace(String spaceId) {
        Space space = spaceRepo.findById(spaceId)
                .orElseThrow(() -> new ResourceNotFoundException("space with id not found"));

        validateVenueOwnership(space.getVenue());

        spaceRepo.delete(space);
        return "space deleted successfully";
    }

    private ResponseSpacesDTO mapToDTO(Space space) {
        return ResponseSpacesDTO.builder()
                .id(space.getId())
                .active(space.getActive())
                .basePrice(space.getBasePrice())
                .capacity(space.getCapacity())
                .description(space.getDescription())
                .createdAt(space.getCreatedAt())
                .updatedAt(space.getUpdatedAt())
                .name(space.getName())
                .venueId(space.getVenue().getId())
                .build();
    }
}
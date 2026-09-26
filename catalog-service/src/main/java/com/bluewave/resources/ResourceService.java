package com.bluewave.resources;

import com.bluewave.cloudinary.CloudinaryService;
import com.bluewave.dto.CloudinaryResponseDTO;
import com.bluewave.dto.CommonApiResponse;
import com.bluewave.exception.ResourceNotFoundException;
import com.bluewave.resources.dto.CreateResourceRequestDTO;
import com.bluewave.resources.dto.ResourceResponseDTO;
import com.bluewave.resources.dto.UpdateResourceRequestDTO;
import com.bluewave.space.Space;
import com.bluewave.space.SpaceRepo;
import com.bluewave.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private static final int MAX_IMAGES = 4;
    private static final String CLOUDINARY_FOLDER = "resources";

    private final ResourceRepo resourceRepo;
    private final SpaceRepo spaceRepo;
    private final CloudinaryService cloudinaryService;

    private void validateSpaceOwnership(Space space) {
        String currentUserId = UserContext.getUserId();
        if (!UserContext.isAdmin() && !space.getVenue().getProviderId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to manage resources in this space");
        }
    }

    private List<MultipartFile> filterValidFiles(List<MultipartFile> files) {
        if (files == null) {
            return List.of();
        }
        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
    }

    @Transactional
    public CommonApiResponse<ResourceResponseDTO> createResource(CreateResourceRequestDTO requestDTO, List<MultipartFile> resourceImages) {
        Space space = spaceRepo.findById(requestDTO.getSpaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Space not found with id: " + requestDTO.getSpaceId()));

        validateSpaceOwnership(space);

        List<MultipartFile> validImg = filterValidFiles(resourceImages);
        if (validImg.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("You can upload a maximum of " + MAX_IMAGES + " images per resource");
        }

        Resources resource = new Resources();
        resource.setName(requestDTO.getName().trim());
        resource.setDescription(requestDTO.getDescription() != null ? requestDTO.getDescription().trim() : null);
        resource.setResourcePrice(requestDTO.getResourcePrice());
        resource.setResourceCountQuantity(requestDTO.getResourceCountQuantity());
        resource.setResourcePriceType(requestDTO.getResourcePriceType());
        resource.setMandatory(Boolean.TRUE.equals(requestDTO.getMandatory()));
        resource.setSpace(space);

        if (!validImg.isEmpty()) {
            for (MultipartFile file : validImg) {
                CloudinaryResponseDTO uploadResult = cloudinaryService.uploadImage(file, CLOUDINARY_FOLDER);
                resource.getImgPublicIds().add(uploadResult.getPublicId());
                resource.getImgUrls().add(uploadResult.getSecureUrl());
            }
        }

        Resources savedResource = resourceRepo.save(resource);

        return CommonApiResponse.<ResourceResponseDTO>builder()
                .data(mapToDto(savedResource))
                .message("Resource created successfully")
                .status(HttpStatus.CREATED.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<List<ResourceResponseDTO>> getAllResource() {
        List<ResourceResponseDTO> dtos = resourceRepo.findAll().stream()
                .map(this::mapToDto)
                .toList();

        return CommonApiResponse.<List<ResourceResponseDTO>>builder()
                .data(dtos)
                .message("All resources fetched successfully")
                .status(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<List<ResourceResponseDTO>> getResourcesBySpaceId(String spaceId) {
        if (!spaceRepo.existsById(spaceId)) {
            throw new ResourceNotFoundException("Space not found with id: " + spaceId);
        }

        List<ResourceResponseDTO> dtos = resourceRepo.findBySpaceId(spaceId).stream()
                .map(this::mapToDto)
                .toList();

        return CommonApiResponse.<List<ResourceResponseDTO>>builder()
                .data(dtos)
                .message("Resources for space fetched successfully")
                .status(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<ResourceResponseDTO> getResourceById(String resourceId) {
        Resources resource = resourceRepo.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + resourceId));

        return CommonApiResponse.<ResourceResponseDTO>builder()
                .data(mapToDto(resource))
                .message("Resource details fetched successfully")
                .status(HttpStatus.OK.value())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Transactional
    public CommonApiResponse<ResourceResponseDTO> updateResource(String resourceId, UpdateResourceRequestDTO requestDTO, List<MultipartFile> updateResourceImg) {
        Resources exists = resourceRepo.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + resourceId));

        validateSpaceOwnership(exists.getSpace());

        if (requestDTO.getName() != null && !requestDTO.getName().isBlank()) {
            exists.setName(requestDTO.getName().trim());
        }
        if (requestDTO.getDescription() != null && !requestDTO.getDescription().isBlank()) {
            exists.setDescription(requestDTO.getDescription().trim());
        }
        if (requestDTO.getResourcePrice() != null) {
            exists.setResourcePrice(requestDTO.getResourcePrice());
        }
        if (requestDTO.getResourcePriceType() != null) {
            exists.setResourcePriceType(requestDTO.getResourcePriceType());
        }
        if (requestDTO.getResourceCountQuantity() != null && requestDTO.getResourceCountQuantity() >= 1) {
            exists.setResourceCountQuantity(requestDTO.getResourceCountQuantity());
        }
        if (requestDTO.getMandatory() != null) {
            exists.setMandatory(requestDTO.getMandatory());
        }

        if (requestDTO.getPublicIdsToDelete() != null && !requestDTO.getPublicIdsToDelete().isEmpty()) {
            for (String publicId : requestDTO.getPublicIdsToDelete()) {
                int index = exists.getImgPublicIds().indexOf(publicId);
                if (index != -1) {
                    cloudinaryService.deleteImage(publicId);
                    exists.getImgPublicIds().remove(index);
                    exists.getImgUrls().remove(index);
                }
            }
        }

        List<MultipartFile> validNewImages = filterValidFiles(updateResourceImg);
        if (exists.getImgUrls().size() + validNewImages.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Total images cannot exceed " + MAX_IMAGES + ". Remove existing images first.");
        }

        for (MultipartFile file : validNewImages) {
            CloudinaryResponseDTO uploaded = cloudinaryService.uploadImage(file, CLOUDINARY_FOLDER);
            exists.getImgUrls().add(uploaded.getSecureUrl());
            exists.getImgPublicIds().add(uploaded.getPublicId());
        }

        Resources savedResource = resourceRepo.save(exists);

        return CommonApiResponse.<ResourceResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .message("Resource updated successfully")
                .data(mapToDto(savedResource))
                .success(true)
                .build();
    }

    @Transactional
    public void deductResourceQuantity(String resourceId, int quantityToDeduct) {
        Resources resource = resourceRepo.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + resourceId));

        int currentStock = resource.getResourceCountQuantity();
        if (currentStock < quantityToDeduct) {
            throw new IllegalStateException("Insufficient inventory for resource: " + resource.getName());
        }

        resource.setResourceCountQuantity(currentStock - quantityToDeduct);
        resourceRepo.save(resource);
    }

    @Transactional
    public void restoreResourceQuantity(String resourceId, int quantityToRestore) {
        Resources resource = resourceRepo.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + resourceId));

        resource.setResourceCountQuantity(resource.getResourceCountQuantity() + quantityToRestore);
        resourceRepo.save(resource);
    }



    @Transactional
    public String deleteResource(String resourceId) {
        Resources resources = resourceRepo.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + resourceId));

        validateSpaceOwnership(resources.getSpace());

        if (resources.getImgPublicIds() != null && !resources.getImgPublicIds().isEmpty()) {
            for (String publicId : resources.getImgPublicIds()) {
                cloudinaryService.deleteImage(publicId);
            }
        }

        resourceRepo.delete(resources);
        return "Resource deleted successfully";
    }

    private ResourceResponseDTO mapToDto(Resources resources) {
        return ResourceResponseDTO.builder()
                .id(resources.getId())
                .name(resources.getName())
                .description(resources.getDescription())
                .resourcePrice(resources.getResourcePrice())
                .resourcePriceType(resources.getResourcePriceType())
                .resourceCountQuantity(resources.getResourceCountQuantity())
                .createdAt(resources.getCreatedAt())
                .updatedAt(resources.getUpdatedAt())
                .spaceId(resources.getSpace().getId())
                .mandatory(resources.isMandatory())
                .imgUrls(resources.getImgUrls() != null ? new ArrayList<>(resources.getImgUrls()) : List.of())
                .build();
    }
}
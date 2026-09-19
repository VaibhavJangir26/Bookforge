package com.bluewave.resources;

import com.bluewave.dto.CommonApiResponse;
import com.bluewave.resources.dto.CreateResourceRequestDTO;
import com.bluewave.resources.dto.ResourceResponseDTO;
import com.bluewave.resources.dto.UpdateResourceRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<CommonApiResponse<ResourceResponseDTO>> createResource(
            @Valid @RequestPart("request") CreateResourceRequestDTO requestDTO,
            @RequestPart(required = false, value = "images") List<MultipartFile> resourceImages
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.createResource(requestDTO, resourceImages));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'CUSTOMER')")
    public ResponseEntity<CommonApiResponse<List<ResourceResponseDTO>>> getAllResource() {
        return ResponseEntity.ok(resourceService.getAllResource());
    }

    @GetMapping("/space/{spaceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'CUSTOMER')")
    public ResponseEntity<CommonApiResponse<List<ResourceResponseDTO>>> getResourcesBySpaceId(@PathVariable String spaceId) {
        return ResponseEntity.ok(resourceService.getResourcesBySpaceId(spaceId));
    }

    @GetMapping("/{resourceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'CUSTOMER')")
    public ResponseEntity<CommonApiResponse<ResourceResponseDTO>> getResourceById(@PathVariable String resourceId) {
        return ResponseEntity.ok(resourceService.getResourceById(resourceId));
    }

    @PatchMapping(value = "/{resourceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<CommonApiResponse<ResourceResponseDTO>> updateResource(
            @PathVariable String resourceId,
            @Valid @RequestPart("request") UpdateResourceRequestDTO requestDTO,
            @RequestPart(required = false, value = "images") List<MultipartFile> updateResourceImg
    ) {
        return ResponseEntity.ok(resourceService.updateResource(resourceId, requestDTO, updateResourceImg));
    }

    @DeleteMapping("/{resourceId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, String>> deleteResource(@PathVariable String resourceId) {
        String message = resourceService.deleteResource(resourceId);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
package com.bluewave.space;

import com.bluewave.dto.CommonApiResponse;
import com.bluewave.space.dto.CreateSpaceRequestDTO;
import com.bluewave.space.dto.ResponseSpacesDTO;
import com.bluewave.space.dto.UpdateSpaceRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/spaces")
public class SpaceController {

    private final SpaceService spaceService;

    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<CommonApiResponse<ResponseSpacesDTO>> createNewSpace(@Valid @RequestBody CreateSpaceRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(spaceService.createNewSpace(requestDTO));
    }

    @PatchMapping("/{spaceId}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<CommonApiResponse<ResponseSpacesDTO>> updateSpaceDetails(
            @Valid @RequestBody UpdateSpaceRequestDTO requestDTO,
            @PathVariable String spaceId) {
        return ResponseEntity.ok(spaceService.updateSpaceDetails(requestDTO, spaceId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'CUSTOMER')")
    public ResponseEntity<CommonApiResponse<List<ResponseSpacesDTO>>> getAllSpaces() {
        return ResponseEntity.ok(spaceService.getAllSpaces());
    }

    @GetMapping("/{spaceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'CUSTOMER')")
    public ResponseEntity<CommonApiResponse<ResponseSpacesDTO>> getSpaceDetails(@PathVariable String spaceId) {
        return ResponseEntity.ok(spaceService.getSpaceDetails(spaceId));
    }

    @DeleteMapping("/{spaceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    public ResponseEntity<Map<String, String>> deleteSpace(@PathVariable String spaceId) {
        String message = spaceService.deleteSpace(spaceId);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
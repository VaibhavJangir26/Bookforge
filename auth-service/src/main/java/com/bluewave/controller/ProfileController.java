package com.bluewave.controller;

import com.bluewave.dto.*;
import com.bluewave.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','PROVIDER')")
    public ResponseEntity<CommonApiResponse<ProfileResponseDTO>> getCurrentUserProfile() {
        return ResponseEntity.ok(profileService.currentUserProfile());
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','PROVIDER')")
    public ResponseEntity<CommonApiResponse<ProfileResponseDTO>> updateUserProfile(
            @Valid @RequestBody ProfileUpdateRequestDTO dto
    ) {
        return ResponseEntity.ok(profileService.updateUserProfile(dto));
    }

    @PostMapping("/apply-provider")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CommonApiResponse<ProfileResponseDTO>> applyToBecomeProvider(
            @Valid @RequestBody ApplyProviderRequestDTO dto
    ) {
        return ResponseEntity.ok(profileService.applyToBecomeProvider(dto));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<CommonApiResponse<UserPersonalDetailResponseDTO>> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(profileService.getUserById(userId));
    }
}

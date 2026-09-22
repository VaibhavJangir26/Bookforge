package com.bluewave.controller;

import com.bluewave.dto.ApplyProviderRequestDTO;
import com.bluewave.dto.CommonApiResponse;
import com.bluewave.dto.ProfileResponseDTO;
import com.bluewave.dto.ProfileUpdateRequestDTO;
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


    @GetMapping("/me")
    public ResponseEntity<CommonApiResponse<ProfileResponseDTO>> currentUserProfile(){
        return ResponseEntity.ok(profileService.currentUserProfile());
    }

    @PatchMapping("/me")
    public ResponseEntity<CommonApiResponse<ProfileResponseDTO>> updateUserProfile(@Valid @RequestBody ProfileUpdateRequestDTO dto){
        return ResponseEntity.ok(profileService.updateUserProfile(dto));
    }

    @PostMapping("/apply-provider")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CommonApiResponse<ProfileResponseDTO>> applyToBecomeProvider(
            @Valid @RequestBody ApplyProviderRequestDTO dto) {
        return ResponseEntity.ok(profileService.applyToBecomeProvider(dto));
    }

}

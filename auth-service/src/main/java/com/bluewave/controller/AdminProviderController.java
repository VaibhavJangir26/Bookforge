package com.bluewave.controller;

import com.bluewave.dto.ApproveProviderRequestDTO;
import com.bluewave.dto.CommonApiResponse;
import com.bluewave.dto.ProfileResponseDTO;
import com.bluewave.service.AdminProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/providers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProviderController {

    private final AdminProviderService adminProviderService;

    @GetMapping("/pending")
    public ResponseEntity<CommonApiResponse<List<ProfileResponseDTO>>> getPendingApplications() {
        return ResponseEntity.ok(adminProviderService.getPendingApplications());
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<CommonApiResponse<ProfileResponseDTO>> reviewProviderApplication(
            @PathVariable String userId,
            @Valid @RequestBody ApproveProviderRequestDTO requestDTO) {
        return ResponseEntity.ok(adminProviderService.reviewProviderApplication(userId, requestDTO));
    }
}
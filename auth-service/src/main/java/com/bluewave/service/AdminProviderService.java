package com.bluewave.service;

import com.bluewave.constants.AppRole;
import com.bluewave.dto.ApproveProviderRequestDTO;
import com.bluewave.dto.CommonApiResponse;
import com.bluewave.dto.ProfileResponseDTO;
import com.bluewave.entity.Profile;
import com.bluewave.entity.Roles;
import com.bluewave.entity.Users;
import com.bluewave.exception.BadRequestException;
import com.bluewave.exception.ResourceNotFoundException;
import com.bluewave.repo.RoleRepo;
import com.bluewave.repo.UsersRepo;
import com.bluewave.utils.ProviderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProviderService {

    private final UsersRepo usersRepo;
    private final RoleRepo roleRepo;
    private final StringRedisTemplate redisTemplate;
    private final ObjectProvider<CacheManager> cacheManagerProvider;

    @Transactional(readOnly = true)
    public CommonApiResponse<List<ProfileResponseDTO>> getPendingApplications() {
        List<ProfileResponseDTO> pending = usersRepo.findAll().stream()
                .filter(u -> u.getProfile() != null && u.getProfile().getProviderStatus() == ProviderStatus.PENDING)
                .map(this::mapToDTO)
                .toList();

        return CommonApiResponse.<List<ProfileResponseDTO>>builder()
                .message("Pending provider applications fetched successfully")
                .data(pending)
                .success(true)
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Transactional
    public CommonApiResponse<ProfileResponseDTO> reviewProviderApplication(String userId, ApproveProviderRequestDTO requestDTO) {
        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Profile profile = user.getProfile();
        if (profile == null || profile.getProviderStatus() != ProviderStatus.PENDING) {
            throw new BadRequestException("User does not have a pending provider application");
        }

        if (requestDTO.getStatus() == ProviderStatus.APPROVED) {
            profile.setProviderStatus(ProviderStatus.APPROVED);

            Roles providerRole = roleRepo.findByAppRole(AppRole.ROLE_PROVIDER)
                    .orElseGet(() -> roleRepo.save(new Roles(null, AppRole.ROLE_PROVIDER, new HashSet<>())));

            user.getRoles().add(providerRole);
            log.info("Admin approved provider application for user: {}", user.getUsername());
        } else if (requestDTO.getStatus() == ProviderStatus.REJECTED) {
            profile.setProviderStatus(ProviderStatus.REJECTED);
            log.info("Admin rejected provider application for user: {}", user.getUsername());
        } else {
            throw new BadRequestException("Invalid status update. Must be APPROVED or REJECTED.");
        }

        Users updatedUser = usersRepo.save(user);

        // 1. Invalidate profile cache safely
        CacheManager cacheManager = cacheManagerProvider.getIfAvailable();
        if (cacheManager != null && cacheManager.getCache("profile") != null) {
            cacheManager.getCache("profile").evict(user.getUsername());
        }

        // 2. Clear user active Redis refresh sessions
        Set<String> sessionKeys = redisTemplate.keys("user:" + user.getUsername() + ":*");
        if (sessionKeys != null && !sessionKeys.isEmpty()) {
            redisTemplate.delete(sessionKeys);
        }

        return CommonApiResponse.<ProfileResponseDTO>builder()
                .message("Provider status updated to " + requestDTO.getStatus())
                .data(mapToDTO(updatedUser))
                .success(true)
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private ProfileResponseDTO mapToDTO(Users user) {
        Profile profile = user.getProfile();
        return ProfileResponseDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profileId(profile != null ? profile.getId() : null)
                .fullName(profile != null ? profile.getFullName() : null)
                .mobileNo(profile != null ? profile.getMobileNo() : null)
                .businessName(profile != null ? profile.getBusinessName() : null)
                .taxOrGstNumber(profile != null ? profile.getTaxOrGstNumber() : null)
                .providerStatus(profile != null ? profile.getProviderStatus() : ProviderStatus.NONE)
                .address(profile != null ? profile.getAddress() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(profile != null ? profile.getUpdatedAt() : null)
                .build();
    }
}
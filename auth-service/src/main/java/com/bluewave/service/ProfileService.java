package com.bluewave.service;

import com.bluewave.dto.CommonApiResponse;
import com.bluewave.dto.ProfileResponseDTO;
import com.bluewave.dto.ProfileUpdateRequestDTO;
import com.bluewave.entity.Profile;
import com.bluewave.entity.Users;
import com.bluewave.repo.UsersRepo;
import com.bluewave.utils.SecurityPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final SecurityPrincipal securityPrincipal;
    private final UsersRepo usersRepo;


    @Cacheable(value = "profile", key = "@securityPrincipal.getCurrentLoginUsername()")
    @Transactional(readOnly = true)
    public CommonApiResponse<ProfileResponseDTO> currentUserProfile() {
        Users users=securityPrincipal.getCurrentLoginUserEntity();
        ProfileResponseDTO dto=mapToResponseDTO(users);
        return CommonApiResponse.<ProfileResponseDTO>builder()
                .message("Profile fetched successfully")
                .data(dto)
                .success(true)
                .status(String.valueOf(HttpStatus.OK.value()))
                .timestamp(LocalDateTime.now())
                .build();

    }

    @Transactional
    @CacheEvict(value = "profile", key = "@securityPrincipal.getCurrentLoginUsername()")
    public CommonApiResponse<ProfileResponseDTO> updateUserProfile(ProfileUpdateRequestDTO dto) {
        Users users=securityPrincipal.getCurrentLoginUserEntity();
        Profile profile=users.getProfile();
        if (profile == null) {
            profile = new Profile();
        }
        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            profile.setFullName(dto.getFullName().trim());
        }

        if (dto.getMobileNo() != null && !dto.getMobileNo().isBlank()) {
            profile.setMobileNo(dto.getMobileNo().trim());
        }

        if (dto.getAddress() != null) {
            profile.setAddress(dto.getAddress());
        }
        users.setProfile(profile);
        profile.setUsers(users);
        Users updatedUser = usersRepo.save(users);
        log.info("users profile updated successfully for user {}", updatedUser.getUsername());
        return CommonApiResponse.<ProfileResponseDTO>builder()
                .message("Profile updated successfully")
                .data(mapToResponseDTO(updatedUser))
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();

    }

    private ProfileResponseDTO mapToResponseDTO(Users user) {
        Profile profile = user.getProfile();

        return ProfileResponseDTO.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(securityPrincipal.getUserRole(user))
                .userId(user.getId())
                .profileId(profile != null ? profile.getId() : null)
                .fullName(profile != null ? profile.getFullName() : null)
                .mobileNo(profile != null ? profile.getMobileNo() : null)
                .address(profile != null ? profile.getAddress() : null)
                .createdAt(profile != null ? profile.getUpdatedAt() : null)
                .updatedAt(profile != null ? profile.getUpdatedAt() : null)
                .build();
    }
}

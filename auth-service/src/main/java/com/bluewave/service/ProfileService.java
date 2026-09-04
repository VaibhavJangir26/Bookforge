package com.bluewave.service;

import com.bluewave.dto.CommonApiResponse;
import com.bluewave.dto.ProfileResponseDTO;
import com.bluewave.dto.ProfileUpdateRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {



    public CommonApiResponse<ProfileResponseDTO> currentUserProfile() {
        return  null;
    }

    public ProfileUpdateRequestDTO updateUserProfile(ProfileUpdateRequestDTO dto) {
        return null;
    }
}

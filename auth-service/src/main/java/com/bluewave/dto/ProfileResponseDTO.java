package com.bluewave.dto;

import com.bluewave.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileResponseDTO {

    private String profileId;
    private String userId;
    private String fullName;
    private String username;
    private String mobileNo;
    private String email;
    private Address address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

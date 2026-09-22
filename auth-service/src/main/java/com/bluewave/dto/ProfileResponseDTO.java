package com.bluewave.dto;

import com.bluewave.entity.Address;
import com.bluewave.utils.ProviderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileResponseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String profileId;
    private String userId;
    private String fullName;
    private String username;
    private String mobileNo;
    private String email;
    private Address address;
    private Set<String> roles;
    private String businessName;
    private String taxOrGstNumber;
    private ProviderStatus providerStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

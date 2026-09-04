package com.bluewave.dto;

import com.bluewave.entity.Address;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileUpdateRequestDTO {
    private String fullName;

    private String mobileNo;

    private Address address;

}

package com.bluewave.dto;

import com.bluewave.entity.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplyProviderRequestDTO {

    @NotBlank(message = "Business or studio name is required")
    private String businessName;

    @NotBlank(message = "Tax or GST identification number is required")
    private String taxOrGstNumber;

    @NotBlank(message = "Contact mobile number is required")
    private String mobileNo;

    @NotNull(message = "Business operating address is required")
    @Valid
    private Address businessAddress;
}
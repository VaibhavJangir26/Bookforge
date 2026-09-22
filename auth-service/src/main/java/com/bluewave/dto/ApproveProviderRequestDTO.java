package com.bluewave.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApproveProviderRequestDTO {

    @NotNull(message = "Approval status is required (APPROVED or REJECTED)")
    private com.bluewave.utils.ProviderStatus status;

    private String rejectionReason;
}
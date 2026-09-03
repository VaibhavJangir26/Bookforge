package com.bluewave.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewTokenGenerationRequestDTO {

    @NotBlank(message = "refresh token is required")
    private String refreshToken;
}

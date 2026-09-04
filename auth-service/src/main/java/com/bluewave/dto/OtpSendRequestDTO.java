package com.bluewave.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpSendRequestDTO {
    @Email(message = "invalid email format")
    @NotBlank(message = "email is required")
    private String email;

}

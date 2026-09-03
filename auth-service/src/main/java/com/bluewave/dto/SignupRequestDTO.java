package com.bluewave.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignupRequestDTO {

    @NotBlank(message = "username is required")
    private String username;

    @Email(message = "valid email format is required")
    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "password is required")
    @Min(message = "minimum 6 digit password is required", value = 6)
    private String password;

}

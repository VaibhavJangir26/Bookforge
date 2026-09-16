package com.bluewave.venue.dto;

import com.bluewave.venue.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateVenueRequestDTO {

    @NotBlank(message = "provider id is required to create venue")
    private String providerId;

    @NotBlank(message = "unique slug name is required")
    private String slug;

    @NotBlank(message = "venue description is required")
    private String description;

    @NotBlank(message = "provider email id is required")
    @Email(message = "valid format email is required")
    private String contactEmail;

    @NotBlank(message = "provider phone no is required")
    @Size(min = 10,max = 10,message = "10 digit phone no is required")
    private String contactPhone;

    @NotNull(message = "venue address is required")
    @Valid
    private Address address;

    @NotBlank(message = "category id is required to create venue")
    private String categoryId;



}

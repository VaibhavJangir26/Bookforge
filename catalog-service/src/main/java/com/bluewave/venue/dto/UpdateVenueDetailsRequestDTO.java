package com.bluewave.venue.dto;

import com.bluewave.venue.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateVenueDetailsRequestDTO {

    private String name;

    private String slug;

    @Valid
    private Address address;

    private String description;

    @Email(message = "valid format email is required")
    private String contactEmail;

    @Size(min = 10,max = 10,message = "10 digit phone no is required")
    private String contactPhone;


}

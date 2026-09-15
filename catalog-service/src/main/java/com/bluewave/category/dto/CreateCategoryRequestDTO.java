package com.bluewave.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCategoryRequestDTO {

    @NotBlank(message = "category name is required")
    private String name;

    @NotBlank(message = "unique category slug is required")
    private String slug;

    @NotBlank(message = "category description is required")
    private String description;

}

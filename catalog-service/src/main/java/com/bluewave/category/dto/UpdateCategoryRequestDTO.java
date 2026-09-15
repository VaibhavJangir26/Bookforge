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
public class UpdateCategoryRequestDTO {

    @NotBlank(message = "category id is required")
    private String id;

    private String name;

    private String slug;

    private String description;

}

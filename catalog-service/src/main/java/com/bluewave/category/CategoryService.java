package com.bluewave.category;

import com.bluewave.category.dto.CategoryResponseDTO;
import com.bluewave.category.dto.CreateCategoryRequestDTO;
import com.bluewave.category.dto.UpdateCategoryRequestDTO;
import com.bluewave.dto.CommonApiResponse;
import com.bluewave.exception.ResourceConflictException;
import com.bluewave.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepo categoryRepo;

    @Transactional
    public CommonApiResponse<CreateCategoryRequestDTO> createCategory(CreateCategoryRequestDTO requestDTO) {
        if (categoryRepo.existsBySlug(requestDTO.getSlug())) {
            throw new ResourceConflictException("category slug already exists");
        }

        Category category = new Category();
        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());
        category.setSlug(requestDTO.getSlug());

        categoryRepo.save(category);

        return CommonApiResponse.<CreateCategoryRequestDTO>builder()
                .data(requestDTO)
                .message("category created successfully")
                .status(HttpStatus.CREATED.value())
                .timestamp(LocalDateTime.now())
                .success(true)
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<List<CategoryResponseDTO>> getAllCategoryList() {
        List<CategoryResponseDTO> list = categoryRepo.findAll().stream().map(category -> CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .slug(category.getSlug())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build()
        ).toList();

        return CommonApiResponse.<List<CategoryResponseDTO>>builder()
                .data(list)
                .message("all categories fetched successfully")
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .success(true)
                .build();
    }

    @Transactional
    public CommonApiResponse<UpdateCategoryRequestDTO> updateCategory(UpdateCategoryRequestDTO requestDTO) {
        Category existingCategory = categoryRepo.findById(requestDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("category not found with id " + requestDTO.getId()));

        if (requestDTO.getSlug() != null && !requestDTO.getSlug().isBlank()) {
            if (categoryRepo.existsBySlugAndIdNot(requestDTO.getSlug(), requestDTO.getId())) {
                throw new ResourceConflictException("slug already taken by another category");
            }
            existingCategory.setSlug(requestDTO.getSlug().trim());
        }

        if (requestDTO.getName() != null && !requestDTO.getName().isBlank()) {
            existingCategory.setName(requestDTO.getName().trim());
        }
        if (requestDTO.getDescription() != null) {
            existingCategory.setDescription(requestDTO.getDescription().trim());
        }
        categoryRepo.save(existingCategory);

        UpdateCategoryRequestDTO responseDto = UpdateCategoryRequestDTO.builder()
                .id(existingCategory.getId())
                .name(existingCategory.getName())
                .description(existingCategory.getDescription())
                .slug(existingCategory.getSlug())
                .build();

        return CommonApiResponse.<UpdateCategoryRequestDTO>builder()
                .data(responseDto)
                .message("Category updated successfully")
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .success(true)
                .build();
    }

    @Transactional(readOnly = true)
    public CommonApiResponse<CategoryResponseDTO> getCategoryById(String categoryId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category not found with id " + categoryId));

        CategoryResponseDTO dto = CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .slug(category.getSlug())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();

        return CommonApiResponse.<CategoryResponseDTO>builder()
                .data(dto)
                .message("Category details fetched successfully")
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .success(true)
                .build();
    }

    @Transactional
    public String deleteCategory(String categoryId) {
        Category category= categoryRepo.findById(categoryId).orElseThrow(()->new ResourceNotFoundException("category not found with id " + categoryId));
        categoryRepo.delete(category);
        return "category deleted successfully";
    }
}
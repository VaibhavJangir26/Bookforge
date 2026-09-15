package com.bluewave.category;

import com.bluewave.category.dto.CategoryResponseDTO;
import com.bluewave.category.dto.CreateCategoryRequestDTO;
import com.bluewave.category.dto.UpdateCategoryRequestDTO;
import com.bluewave.dto.CommonApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepo categoryRepo;


    public CommonApiResponse<CreateCategoryRequestDTO> createCategory(CreateCategoryRequestDTO requestDTO) {
        return null;
    }

    public CommonApiResponse<List<CategoryResponseDTO>> getAllCategoryList() {
        return null;
    }

    public CommonApiResponse<UpdateCategoryRequestDTO> updateCategory(UpdateCategoryRequestDTO requestDTO) {
        return null;
    }

    public String deleteCategory(String categoryId) {
        return null;
    }

}

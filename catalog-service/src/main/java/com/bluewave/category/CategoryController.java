package com.bluewave.category;

import com.bluewave.category.dto.CategoryResponseDTO;
import com.bluewave.category.dto.CreateCategoryRequestDTO;
import com.bluewave.category.dto.UpdateCategoryRequestDTO;
import com.bluewave.dto.CommonApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonApiResponse<CreateCategoryRequestDTO>> createCategory(@Valid @RequestBody CreateCategoryRequestDTO requestDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(requestDTO));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<CommonApiResponse<List<CategoryResponseDTO>>> getAllCategoryList(){
        return ResponseEntity.ok(categoryService.getAllCategoryList());
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER')")
    public ResponseEntity<CommonApiResponse<UpdateCategoryRequestDTO>> updateCategory(@Valid @RequestBody UpdateCategoryRequestDTO requestDTO){
        return ResponseEntity.ok(categoryService.updateCategory(requestDTO));
    }
    
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Map<String,String>> deleteCategory(@PathVariable String categoryId){
        String message=categoryService.deleteCategory(categoryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of("message",message));
    }

}

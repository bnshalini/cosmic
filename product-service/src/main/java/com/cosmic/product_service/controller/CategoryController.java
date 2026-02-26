package com.cosmic.product_service.controller;

import com.cosmic.product_service.Service.CategoryService;
import com.cosmic.product_service.dto.CategoryRequestDto;
import com.cosmic.product_service.dto.CategoryResponseDto;
import com.cosmic.product_service.payload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;


    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(
            @Valid @RequestBody CategoryRequestDto requestDto) {


        CategoryResponseDto responseDto = categoryService.createCategory(requestDto);


        return ResponseEntity.ok(
                ApiResponse.<CategoryResponseDto>builder()
                        .success(true)
                        .message("Category created successfully")
                        .data(responseDto)
                        .build()
        );
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getAllCategories() {


        List<CategoryResponseDto> categories = categoryService.getAllCategories();


        return ResponseEntity.ok(
                ApiResponse.<List<CategoryResponseDto>>builder()
                        .success(true)
                        .message("Categories fetched successfully")
                        .data(categories)
                        .build()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(@PathVariable Long id) {


        CategoryResponseDto category = categoryService.getCategoryById(id);


        return ResponseEntity.ok(
                ApiResponse.<CategoryResponseDto>builder()
                        .success(true)
                        .message("Category fetched successfully")
                        .data(category)
                        .build()
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Long id) {


        categoryService.deleteCategory(id);


        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Category deleted successfully")
                        .data("Deleted category id: " + id)
                        .build()
        );
    }
}

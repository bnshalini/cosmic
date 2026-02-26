package com.cosmic.product_service.controller;

import com.cosmic.product_service.Service.SubCategoryService;
import com.cosmic.product_service.dto.SubCategoryRequestDto;
import com.cosmic.product_service.dto.SubCategoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subcategories")
@RequiredArgsConstructor
public class SubCategoryController {
    private final SubCategoryService subCategoryService;

    @PostMapping
    public ResponseEntity<SubCategoryResponseDto> createSubCategory(@RequestBody SubCategoryRequestDto dto) {
        return ResponseEntity.ok(subCategoryService.createSubCategory(dto));
    }

    @GetMapping
    public ResponseEntity<List<SubCategoryResponseDto>> getAllSubCategories() {
        return ResponseEntity.ok(subCategoryService.getAllSubCategories());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SubCategoryResponseDto>> getSubCategoriesByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(subCategoryService.getSubCategoriesByCategory(categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubCategoryResponseDto> getSubCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(subCategoryService.getSubCategoryById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubCategory(@PathVariable Long id) {
        subCategoryService.deleteSubCategory(id);
        return ResponseEntity.ok("SubCategory deleted successfully");
    }
}

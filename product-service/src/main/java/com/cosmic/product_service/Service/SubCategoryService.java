package com.cosmic.product_service.Service;

import com.cosmic.product_service.dto.SubCategoryRequestDto;
import com.cosmic.product_service.dto.SubCategoryResponseDto;

import java.util.List;

public interface SubCategoryService {
    SubCategoryResponseDto createSubCategory(SubCategoryRequestDto dto);

    List<SubCategoryResponseDto> getAllSubCategories();

    List<SubCategoryResponseDto> getSubCategoriesByCategory(Long categoryId);

    SubCategoryResponseDto getSubCategoryById(Long id);

    void deleteSubCategory(Long id);
}

package com.cosmic.product_service.Service;

import com.cosmic.product_service.Repository.CategoryRepository;
import com.cosmic.product_service.Repository.SubCategoryRepository;
import com.cosmic.product_service.dto.SubCategoryRequestDto;
import com.cosmic.product_service.dto.SubCategoryResponseDto;
import com.cosmic.product_service.entity.Category;
import com.cosmic.product_service.entity.SubCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubCategoryServiceImpl implements SubCategoryService{
    private final SubCategoryRepository subCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public SubCategoryResponseDto createSubCategory(SubCategoryRequestDto dto) {

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));

        if (subCategoryRepository.existsByNameIgnoreCaseAndCategoryId(dto.getName(), dto.getCategoryId())) {
            throw new RuntimeException("SubCategory already exists in this category");
        }

        SubCategory subCategory = SubCategory.builder()
                .name(dto.getName())
                .category(category)
                .build();

        SubCategory saved = subCategoryRepository.save(subCategory);

        return SubCategoryResponseDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .build();
    }

    @Override
    public List<SubCategoryResponseDto> getAllSubCategories() {

        return subCategoryRepository.findAll()
                .stream()
                .map(sc -> SubCategoryResponseDto.builder()
                        .id(sc.getId())
                        .name(sc.getName())
                        .categoryId(sc.getCategory().getId())
                        .categoryName(sc.getCategory().getName())
                        .build())
                .toList();
    }

    @Override
    public List<SubCategoryResponseDto> getSubCategoriesByCategory(Long categoryId) {

        return subCategoryRepository.findByCategoryId(categoryId)
                .stream()
                .map(sc -> SubCategoryResponseDto.builder()
                        .id(sc.getId())
                        .name(sc.getName())
                        .categoryId(sc.getCategory().getId())
                        .categoryName(sc.getCategory().getName())
                        .build())
                .toList();
    }

    @Override
    public SubCategoryResponseDto getSubCategoryById(Long id) {

        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubCategory not found with id: " + id));

        return SubCategoryResponseDto.builder()
                .id(subCategory.getId())
                .name(subCategory.getName())
                .categoryId(subCategory.getCategory().getId())
                .categoryName(subCategory.getCategory().getName())
                .build();
    }

    @Override
    public void deleteSubCategory(Long id) {

        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubCategory not found with id: " + id));

        subCategoryRepository.delete(subCategory);
    }
}

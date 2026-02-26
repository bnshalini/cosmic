package com.cosmic.product_service.Service;

import com.cosmic.product_service.dto.PagedResponseDto;
import com.cosmic.product_service.dto.ProductImageResponseDto;
import com.cosmic.product_service.dto.ProductRequestDto;
import com.cosmic.product_service.dto.ProductResponseDto;
import com.cosmic.product_service.entity.Product;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    ProductResponseDto addProduct(ProductRequestDto product);
    List<ProductResponseDto> getAllProducts();
    ProductResponseDto getProductById(Long id);
    ProductResponseDto updateProduct(Long id, ProductRequestDto product);
    void deleteProduct(Long id);
    PagedResponseDto<ProductResponseDto> getAllProducts(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String keyword,
            Long categoryId
    );
    ProductResponseDto uploadProductImage(Long productId, MultipartFile file);

    ProductResponseDto deleteProductImage(Long productId);

    ProductResponseDto updateProductImage(Long productId, MultipartFile file);

    List<ProductImageResponseDto> uploadProductImages(Long productId, List<MultipartFile> files);

    List<ProductImageResponseDto> getProductImages(Long productId);

    void deleteProductImageById(Long imageId);

    ProductResponseDto setPrimaryProductImage(Long imageId);

    PagedResponseDto<ProductImageResponseDto> getProductImagesWithPagination(Long productId, int page, int size, String sortDir);

    void reserveStock(Long productId, Integer quantity);
    void confirmStock(Long productId, Integer quantity);
    void releaseStock(Long productId, Integer quantity);
}

package com.cosmic.product_service.controller;

import com.cosmic.product_service.dto.PagedResponseDto;
import com.cosmic.product_service.dto.ProductImageResponseDto;
import com.cosmic.product_service.dto.ProductRequestDto;
import com.cosmic.product_service.dto.ProductResponseDto;
import com.cosmic.product_service.payload.ApiResponse;
import com.cosmic.product_service.Service.ProductService;
import com.cosmic.product_service.Service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final S3Service s3Service;

    @Operation(summary = "Create Product", description = "Creates a new product and saves it into database")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Product created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDto>> addProduct(
            @Valid @RequestBody ProductRequestDto productRequestDto
    ) {

        ProductResponseDto savedProduct = productService.addProduct(productRequestDto);

        ApiResponse<ProductResponseDto> response = ApiResponse.<ProductResponseDto>builder()
                .success(true)
                .message("Product created successfully")
                .data(savedProduct)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get All Products", description = "Fetch all products from database")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Products fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDto<ProductResponseDto>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {


        PagedResponseDto<ProductResponseDto> responseDto =
                productService.getAllProducts(page, size, sortBy, sortDir, keyword, categoryId);


        return ResponseEntity.ok(
                ApiResponse.<PagedResponseDto<ProductResponseDto>>builder()
                        .success(true)
                        .message("Products fetched successfully")
                        .data(responseDto)
                        .build()
        );
    }

    @Operation(summary = "Get Product By Id", description = "Fetch a product using product id")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProductById(@PathVariable Long id) {

        ProductResponseDto product = productService.getProductById(id);

        ApiResponse<ProductResponseDto> response = ApiResponse.<ProductResponseDto>builder()
                .success(true)
                .message("Product fetched successfully")
                .data(product)
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Product", description = "Update an existing product using id")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDto productRequestDto
    ) {

        ProductResponseDto updatedProduct = productService.updateProduct(id, productRequestDto);

        ApiResponse<ProductResponseDto> response = ApiResponse.<ProductResponseDto>builder()
                .success(true)
                .message("Product updated successfully")
                .data(updatedProduct)
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Product", description = "Delete product using id")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {

        productService.deleteProduct(id);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Product deleted successfully")
                .data("Product deleted with id : " + id)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}/upload-image")
    public ResponseEntity<ApiResponse<ProductResponseDto>> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file
    ) {
        ProductResponseDto response = productService.uploadProductImage(productId, file);
        return ResponseEntity.ok(new ApiResponse<>(true, "Image uploaded successfully", response));
    }

    @DeleteMapping("/{productId}/delete-image")
    public ResponseEntity<ApiResponse<ProductResponseDto>> deleteProductImage(
            @PathVariable Long productId
    ) {
        ProductResponseDto response = productService.deleteProductImage(productId);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Product image deleted successfully", response)
        );
    }

    @PutMapping("/{productId}/update-image")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file
    ) {
        ProductResponseDto response = productService.updateProductImage(productId, file);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Product image updated successfully", response)
        );
    }

    @PostMapping("/{productId}/upload-images")
    public ResponseEntity<ApiResponse<List<ProductImageResponseDto>>> uploadProductImages(
            @PathVariable Long productId,
            @RequestParam("files") List<MultipartFile> files
    ) {
        List<ProductImageResponseDto> response = productService.uploadProductImages(productId, files);

        return ResponseEntity.ok(new ApiResponse<>(true, "Images uploaded successfully", response));
    }

    @GetMapping("/{productId}/images")
    public ResponseEntity<ApiResponse<List<ProductImageResponseDto>>> getProductImages(
            @PathVariable Long productId
    ) {
        List<ProductImageResponseDto> response = productService.getProductImages(productId);

        return ResponseEntity.ok(new ApiResponse<>(true, "Product images fetched successfully", response));
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<ApiResponse<Object>> deleteProductImageById(
            @PathVariable Long imageId
    ) {
        productService.deleteProductImageById(imageId);

        return ResponseEntity.ok(new ApiResponse<>(true, "Image deleted successfully", null));
    }

    @PutMapping("/images/{imageId}/set-primary")
    public ResponseEntity<ApiResponse<ProductResponseDto>> setPrimaryImage(
            @PathVariable Long imageId
    ) {
        ProductResponseDto response = productService.setPrimaryProductImage(imageId);

        return ResponseEntity.ok(new ApiResponse<>(true, "Primary image updated successfully", response));
    }

    @GetMapping("/{productId}/images/paged")
    public ResponseEntity<ApiResponse<PagedResponseDto<ProductImageResponseDto>>> getProductImagesPaged(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {

        PagedResponseDto<ProductImageResponseDto> response =
                productService.getProductImagesWithPagination(productId, page, size, sortDir);

        return ResponseEntity.ok(new ApiResponse<>(true, "Product images fetched successfully", response));
    }

//    //http://localhost:8081/api/products/images/presigned-url?fileUrl=<S3_FILE_URL>
//    //Image Url : https://advann-product-images-2026.s3.ap-south-1.amazonaws.com/products/full/b4e639f8-7639-4632-ba09-7e6e150aa5d9_mobile-cover.jpg
//    //http://localhost:8081/api/products/images/presigned-url?fileUrl=https://advann-product-images-2026.s3.ap-south-1.amazonaws.com/products/full/b4e639f8-7639-4632-ba09-7e6e150aa5d9_mobile-cover.jpg
//    @GetMapping("/images/presigned-url")
//    public ResponseEntity<ApiResponse<String>> generatePresignedUrl(
//            @RequestParam String fileUrl
//    ) {
//
//        String presignedUrl = s3Service.generatePresignedUrl(fileUrl);
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(true, "Presigned URL generated successfully", presignedUrl)
//        );
//    }

}
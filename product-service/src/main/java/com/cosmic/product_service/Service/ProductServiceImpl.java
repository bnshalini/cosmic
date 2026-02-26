package com.cosmic.product_service.Service;

import com.cosmic.product_service.Repository.CategoryRepository;
import com.cosmic.product_service.Repository.ProductImageRepository;
import com.cosmic.product_service.Repository.ProductRepository;
import com.cosmic.product_service.Repository.SubCategoryRepository;
import com.cosmic.product_service.dto.PagedResponseDto;
import com.cosmic.product_service.dto.ProductImageResponseDto;
import com.cosmic.product_service.dto.ProductRequestDto;
import com.cosmic.product_service.dto.ProductResponseDto;
import com.cosmic.product_service.entity.Category;
import com.cosmic.product_service.entity.Product;
import com.cosmic.product_service.entity.ProductImage;
import com.cosmic.product_service.entity.SubCategory;
import com.cosmic.product_service.exceptions.InvalidFileException;
import com.cosmic.product_service.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import jdk.jfr.EventType;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ProductImageRepository productImageRepository;
    private final S3Service s3Service;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final int MAX_GALLERY_IMAGES = 5;

    @Override
    public ProductResponseDto addProduct(ProductRequestDto productRequestDto) {

        log.info("Creating new product with name: {}", productRequestDto.getName());

        Product product = modelMapper.map(productRequestDto, Product.class);
        product.setId(null);

        Category category = categoryRepository.findById(productRequestDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + productRequestDto.getCategoryId()
                ));

        SubCategory subCategory = subCategoryRepository.findById(productRequestDto.getSubCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SubCategory not found with id: " + productRequestDto.getSubCategoryId()
                ));

        if (!subCategory.getCategory().getId().equals(category.getId())) {
            throw new ResourceNotFoundException(
                    "SubCategory id " + productRequestDto.getSubCategoryId() +
                            " does not belong to Category id " + productRequestDto.getCategoryId()
            );
        }

        product.setCategory(category);
        product.setSubCategory(subCategory);

        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with id: {}", savedProduct.getId());

        ProductResponseDto responseDto = modelMapper.map(savedProduct, ProductResponseDto.class);

        responseDto.setCategoryId(category.getId());
        responseDto.setCategoryName(category.getName());

        responseDto.setSubCategoryId(subCategory.getId());
        responseDto.setSubCategoryName(subCategory.getName());

        responseDto.setImageUrl(buildImageUrl(savedProduct.getImagePath()));

        return responseDto;
    }

    @Override
    public List<ProductResponseDto> getAllProducts() {

        log.info("Fetching all products");

        List<Product> products = productRepository.findAll();

        log.info("Total products found: {}", products.size());

        return products.stream()
                .map(product -> {

                    ProductResponseDto dto = modelMapper.map(product, ProductResponseDto.class);

                    dto.setImageUrl(buildImageUrl(product.getImagePath()));

                    if (product.getCategory() != null) {
                        dto.setCategoryId(product.getCategory().getId());
                        dto.setCategoryName(product.getCategory().getName());
                    }

                    if (product.getSubCategory() != null) {
                        dto.setSubCategoryId(product.getSubCategory().getId());
                        dto.setSubCategoryName(product.getSubCategory().getName());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDto getProductById(Long id) {

        log.info("Fetching product by id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with id: {}", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });

        ProductResponseDto dto = modelMapper.map(product, ProductResponseDto.class);

        dto.setImageUrl(buildImageUrl(product.getImagePath()));

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getSubCategory() != null) {
            dto.setSubCategoryId(product.getSubCategory().getId());
            dto.setSubCategoryName(product.getSubCategory().getName());
        }

        return dto;
    }

    @Override
    public ProductResponseDto updateProduct(Long id, ProductRequestDto productRequestDto) {

        log.info("Updating product with id: {}", id);

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot update. Product not found with id: {}", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });

        existing.setName(productRequestDto.getName());
        existing.setPrice(productRequestDto.getPrice());
        existing.setStock(productRequestDto.getStock());

        Category category = categoryRepository.findById(productRequestDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + productRequestDto.getCategoryId()
                ));

        SubCategory subCategory = subCategoryRepository.findById(productRequestDto.getSubCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SubCategory not found with id: " + productRequestDto.getSubCategoryId()
                ));

        if (!subCategory.getCategory().getId().equals(category.getId())) {
            throw new ResourceNotFoundException(
                    "SubCategory id " + productRequestDto.getSubCategoryId() +
                            " does not belong to Category id " + productRequestDto.getCategoryId()
            );
        }

        existing.setCategory(category);
        existing.setSubCategory(subCategory);

        Product updated = productRepository.save(existing);

        ProductResponseDto responseDto = modelMapper.map(updated, ProductResponseDto.class);

        responseDto.setCategoryId(category.getId());
        responseDto.setCategoryName(category.getName());

        responseDto.setSubCategoryId(subCategory.getId());
        responseDto.setSubCategoryName(subCategory.getName());

        responseDto.setImageUrl(buildImageUrl(updated.getImagePath()));

        return responseDto;
    }

    @Override
    public void deleteProduct(Long id) {

        log.info("Deleting product with id: {}", id);

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot delete. Product not found with id: {}", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });

        // ✅ delete primary image from S3
        deleteOldImageIfExists(existing);

        // ✅ delete gallery images from S3
        List<ProductImage> images = productImageRepository.findByProductId(existing.getId());

        for (ProductImage img : images) {
            if (img.getImagePath() != null) {
                s3Service.deleteFileByUrl(img.getImagePath());
            }
            if (img.getThumbnailPath() != null) {
                s3Service.deleteFileByUrl(img.getThumbnailPath());
            }
        }

        productRepository.delete(existing);

        log.info("Product deleted successfully with id: {}", id);
    }

    @Override
    public PagedResponseDto<ProductResponseDto> getAllProducts(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String keyword,
            Long categoryId
    ) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage;

        if (categoryId != null && keyword != null && !keyword.isBlank()) {
            productPage = productRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, keyword, pageable);
        } else if (categoryId != null) {
            productPage = productRepository.findByCategoryId(categoryId, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            productPage = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        List<ProductResponseDto> products = productPage.getContent()
                .stream()
                .map(product -> ProductResponseDto.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .stock(product.getStock())

                        .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                        .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)

                        .subCategoryId(product.getSubCategory() != null ? product.getSubCategory().getId() : null)
                        .subCategoryName(product.getSubCategory() != null ? product.getSubCategory().getName() : null)

                        .imagePath(product.getImagePath())
                        .imageUrl(buildImageUrl(product.getImagePath()))
                        .build()
                )
                .toList();

        return PagedResponseDto.<ProductResponseDto>builder()
                .content(products)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
    }

    @Override
    public ProductResponseDto uploadProductImage(Long productId, MultipartFile file) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        validateImageFile(file);

        String imageUrl = s3Service.uploadFile(file, "products/full");

        product.setImagePath(imageUrl);

        Product savedProduct = productRepository.save(product);

        ProductResponseDto dto = modelMapper.map(savedProduct, ProductResponseDto.class);
        dto.setImageUrl(savedProduct.getImagePath());

        return dto;
    }

    private void validateImageFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required. Please upload a file.");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !(contentType.equals("image/jpeg") ||
                        contentType.equals("image/png"))) {
            throw new InvalidFileException("Only JPG and PNG images are allowed");
        }

        long maxSize = 2 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new InvalidFileException("File size must be less than 2MB");
        }
    }

    private void deleteOldImageIfExists(Product product) {

        if (product.getImagePath() == null || product.getImagePath().isBlank()) {
            log.info("No primary image found for product id: {}", product.getId());
            return;
        }

        try {
            s3Service.deleteFileByUrl(product.getImagePath());
            log.info("Old primary image deleted from S3 for product id: {}", product.getId());
        } catch (Exception e) {
            log.warn("Failed to delete old image from S3. Skipping delete. {}", e.getMessage());
        }
    }

    private String buildImageUrl(String imagePath) {

        if (imagePath == null || imagePath.isBlank()) {
            return baseUrl + "/images/default.jpg";
        }

        // ✅ S3 imagePath already contains full URL
        return imagePath;
    }

    @Override
    public ProductResponseDto deleteProductImage(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getImagePath() == null || product.getImagePath().isBlank()) {
            throw new InvalidFileException("No image found for this product.");
        }

        // ✅ delete from S3
        s3Service.deleteFileByUrl(product.getImagePath());

        product.setImagePath(null);

        Product savedProduct = productRepository.save(product);

        ProductResponseDto dto = modelMapper.map(savedProduct, ProductResponseDto.class);
        dto.setImageUrl(null);

        return dto;
    }

    @Override
    public ProductResponseDto updateProductImage(Long productId, MultipartFile file) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        validateImageFile(file);

        // ✅ delete old primary image from S3
        deleteOldImageIfExists(product);

        // ✅ upload new image to S3
        String imageUrl = s3Service.uploadFile(file, "products/full");

        product.setImagePath(imageUrl);

        Product savedProduct = productRepository.save(product);

        ProductResponseDto dto = modelMapper.map(savedProduct, ProductResponseDto.class);
        dto.setImageUrl(savedProduct.getImagePath());

        return dto;
    }

    @Override
    public List<ProductImageResponseDto> uploadProductImages(Long productId, List<MultipartFile> files) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (files == null || files.isEmpty()) {
            throw new InvalidFileException("Please upload at least one image.");
        }

        long existingCount = productImageRepository.countByProductId(productId);

        if (existingCount + files.size() > MAX_GALLERY_IMAGES) {
            throw new InvalidFileException(
                    "Maximum " + MAX_GALLERY_IMAGES + " images allowed per product. Already uploaded: "
                            + existingCount + ", trying to upload: " + files.size()
            );
        }

        List<ProductImageResponseDto> responseList = new ArrayList<>();

        for (MultipartFile file : files) {

            validateImageFile(file);

            try {
                ByteArrayOutputStream fullOutputStream = new ByteArrayOutputStream();

                Thumbnails.of(file.getInputStream())
                        .size(800, 800)
                        .outputQuality(0.8)
                        .toOutputStream(fullOutputStream);

                byte[] fullBytes = fullOutputStream.toByteArray();

                ByteArrayOutputStream thumbOutputStream = new ByteArrayOutputStream();

                Thumbnails.of(file.getInputStream())
                        .size(300, 300)
                        .outputQuality(0.7)
                        .toOutputStream(thumbOutputStream);

                byte[] thumbBytes = thumbOutputStream.toByteArray();

                String fullImageUrl = s3Service.uploadBytes(fullBytes, file.getContentType(), "products/gallery/full");
                String thumbImageUrl = s3Service.uploadBytes(thumbBytes, file.getContentType(), "products/gallery/thumb");

                ProductImage productImage = ProductImage.builder()
                        .imagePath(fullImageUrl)
                        .thumbnailPath(thumbImageUrl)
                        .product(product)
                        .build();

                ProductImage savedImage = productImageRepository.save(productImage);

                responseList.add(ProductImageResponseDto.builder()
                        .id(savedImage.getId())
                        .imagePath(savedImage.getImagePath())
                        .imageUrl(savedImage.getImagePath())
                        .thumbnailPath(savedImage.getThumbnailPath())
                        .thumbnailUrl(savedImage.getThumbnailPath())
                        .build());

            } catch (Exception e) {
                throw new InvalidFileException("Failed to upload image to S3.");
            }
        }

        if (!responseList.isEmpty()) {
            ProductImageResponseDto latestUploadedImage = responseList.get(responseList.size() - 1);

            product.setImagePath(latestUploadedImage.getImagePath());
            productRepository.save(product);

            log.info("Primary image updated to latest uploaded image for product id: {}", product.getId());
        }

        return responseList;
    }

    @Override
    public List<ProductImageResponseDto> getProductImages(Long productId) {

        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        List<ProductImage> images = productImageRepository.findByProductId(productId);

        return images.stream()
                .map(img -> ProductImageResponseDto.builder()
                        .id(img.getId())
                        .imagePath(img.getImagePath())
                        .imageUrl(img.getImagePath())
                        .thumbnailPath(img.getThumbnailPath())
                        .thumbnailUrl(img.getThumbnailPath())
                        .build())
                .toList();
    }

    @Override
    public void deleteProductImageById(Long imageId) {

        ProductImage productImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));

        Product product = productImage.getProduct();

        boolean isPrimary = product.getImagePath() != null &&
                product.getImagePath().equals(productImage.getImagePath());

        // ✅ delete full image from S3
        if (productImage.getImagePath() != null) {
            s3Service.deleteFileByUrl(productImage.getImagePath());
        }

        // ✅ delete thumbnail from S3
        if (productImage.getThumbnailPath() != null) {
            s3Service.deleteFileByUrl(productImage.getThumbnailPath());
        }

        productImageRepository.delete(productImage);
        log.info("Gallery image deleted from DB with id: {}", imageId);

        if (isPrimary) {

            List<ProductImage> remainingImages =
                    productImageRepository.findByProductIdOrderByCreatedAtDesc(product.getId());

            if (!remainingImages.isEmpty()) {
                product.setImagePath(remainingImages.get(0).getImagePath());
                log.info("Primary image updated to another gallery image for product id: {}", product.getId());
            } else {
                product.setImagePath(null);
                log.info("No gallery images left. Primary image reset to null for product id: {}", product.getId());
            }

            productRepository.save(product);
        }
    }

    @Override
    public ProductResponseDto setPrimaryProductImage(Long imageId) {

        ProductImage productImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));

        Product product = productImage.getProduct();

        product.setImagePath(productImage.getImagePath());

        Product savedProduct = productRepository.save(product);

        ProductResponseDto dto = modelMapper.map(savedProduct, ProductResponseDto.class);
        dto.setImageUrl(savedProduct.getImagePath());

        return dto;
    }

    @Override
    public PagedResponseDto<ProductImageResponseDto> getProductImagesWithPagination(Long productId, int page, int size, String sortDir) {

        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by("createdAt").descending()
                : Sort.by("createdAt").ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductImage> imagePage = productImageRepository.findByProductId(productId, pageable);

        List<ProductImageResponseDto> images = imagePage.getContent()
                .stream()
                .map(img -> ProductImageResponseDto.builder()
                        .id(img.getId())
                        .imagePath(img.getImagePath())
                        .imageUrl(img.getImagePath())
                        .thumbnailPath(img.getThumbnailPath())
                        .thumbnailUrl(img.getThumbnailPath())
                        .build())
                .toList();

        return PagedResponseDto.<ProductImageResponseDto>builder()
                .content(images)
                .pageNumber(imagePage.getNumber())
                .pageSize(imagePage.getSize())
                .totalElements(imagePage.getTotalElements())
                .totalPages(imagePage.getTotalPages())
                .last(imagePage.isLast())
                .build();
    }

    @Override
    @Transactional
    public void reserveStock(Long productId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getStock() < quantity) {
            throw new IllegalStateException("Insufficient stock for product id: " + productId);
        }

        product.setStock(product.getStock() - quantity);
        product.setReservedStock(product.getReservedStock() + quantity);

        productRepository.save(product);
    }

    @Override
    @Transactional
    public void confirmStock(Long productId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getReservedStock() < quantity) {
            throw new IllegalStateException("Reserved stock is less than quantity for product id: " + productId);
        }

        product.setReservedStock(product.getReservedStock() - quantity);

        productRepository.save(product);
    }

    @Override
    @Transactional
    public void releaseStock(Long productId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getReservedStock() < quantity) {
            throw new IllegalStateException("Reserved stock is less than quantity for product id: " + productId);
        }

        product.setReservedStock(product.getReservedStock() - quantity);
        product.setStock(product.getStock() + quantity);

        productRepository.save(product);
    }
}


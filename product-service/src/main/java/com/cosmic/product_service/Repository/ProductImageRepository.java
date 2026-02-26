package com.cosmic.product_service.Repository;
import com.cosmic.product_service.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long>{
    List<ProductImage> findByProductId(Long productId);

    List<ProductImage> findByProductIdOrderByCreatedAtDesc(Long productId);

    long countByProductId(Long productId);

    Page<ProductImage> findByProductId(Long productId, Pageable pageable);
}

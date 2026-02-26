package com.cosmic.product_service.Repository;

import com.cosmic.product_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Long> {
    // Search by product name
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);


    // Filter by category
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);


    // Search + category filter combined
    Page<Product> findByCategoryIdAndNameContainingIgnoreCase(Long categoryId, String keyword, Pageable pageable);
}

package com.ruafashion.store.repository;


import com.ruafashion.store.model.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductSizeRepository extends JpaRepository<ProductSize, Long> {

    List<ProductSize> findByProductIdOrderByIdAsc(Long productId);

    boolean existsByProductIdAndSizeIdAndColor(
            Long productId,
            Long sizeId,
            String color
    );

    void deleteByProductId(Long productId);
    List<ProductSize> findByProductIdAndQuantityGreaterThanOrderByIdAsc(
            Long productId,
            Integer quantity
    );
}
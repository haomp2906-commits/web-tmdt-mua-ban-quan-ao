package com.ruafashion.store.repository;


import com.ruafashion.store.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCaseOrderByIdDesc(String keyword);

    long countByCategoryId(Long categoryId);

    @Query("""
        SELECT p
        FROM Product p
        WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:status IS NULL OR p.status = :status)
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        ORDER BY p.id DESC
    """)
    List<Product> searchAndFilter(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") String status,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );
    List<Product> findTop6ByStatusOrderByCreatedAtDesc(String status);
    
    @Query("""
    		SELECT p
    		FROM Product p
    		WHERE p.status = 'ACTIVE'
    		AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
    		AND (:categoryId IS NULL OR p.category.id = :categoryId)
    		AND (:minPrice IS NULL OR p.price >= :minPrice)
    		AND (:maxPrice IS NULL OR p.price <= :maxPrice)
    		""")
    		Page<Product> findActiveProducts(
    		        @Param("keyword") String keyword,
    		        @Param("categoryId") Long categoryId,
    		        @Param("minPrice") Double minPrice,
    		        @Param("maxPrice") Double maxPrice,
    		        Pageable pageable
    		);
    Optional<Product> findByIdAndStatus(Long id, String status);
    long countByStatus(String status);
}
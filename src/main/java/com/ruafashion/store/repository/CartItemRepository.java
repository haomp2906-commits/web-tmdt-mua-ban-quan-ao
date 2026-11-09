package com.ruafashion.store.repository;


import com.ruafashion.store.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndProductSizeId(Long cartId, Long productSizeId);
    
    void deleteByProductId(Long productId);
}
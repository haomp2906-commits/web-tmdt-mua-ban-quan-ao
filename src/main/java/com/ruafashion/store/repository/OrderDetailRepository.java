package com.ruafashion.store.repository;


import com.ruafashion.store.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    long countByProductId(Long productId);
    List<OrderDetail> findByOrderId(Long orderId);
}

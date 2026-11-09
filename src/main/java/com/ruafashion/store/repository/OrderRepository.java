package com.ruafashion.store.repository;

import java.util.Optional;
import com.ruafashion.store.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByStatusOrderByCreatedAtDesc(String status);
    Optional<Order> findByIdAndUserId(Long id, Long userId);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    Double sumTotalAmountByStatus(@Param("status") String status);
}
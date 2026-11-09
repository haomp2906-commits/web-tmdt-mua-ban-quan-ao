package com.ruafashion.store.repository;


import com.ruafashion.store.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByNameContainingIgnoreCaseOrderByIdDesc(String keyword);
}
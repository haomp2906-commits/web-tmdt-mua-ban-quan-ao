package com.ruafashion.store.service;


import com.ruafashion.store.model.Category;
import com.ruafashion.store.repository.CategoryRepository;
import com.ruafashion.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> searchCategories(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return categoryRepository.findAll();
        }

        return categoryRepository
                .findByNameContainingIgnoreCaseOrderByIdDesc(keyword.trim());
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
    }

    public void saveCategory(Category category) {
        if (category.getSlug() == null || category.getSlug().trim().isEmpty()) {
            category.setSlug(generateSlug(category.getName()));
        }

        if (category.getStatus() == null) {
            category.setStatus(true);
        }

        categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        long productCount = productRepository.countByCategoryId(id);

        if (productCount > 0) {
            throw new RuntimeException(
                    "Không thể xóa danh mục vì vẫn còn sản phẩm thuộc danh mục này"
            );
        }

        categoryRepository.deleteById(id);
    }

    private String generateSlug(String input) {
        String slug = input.toLowerCase(Locale.ROOT);

        slug = Normalizer.normalize(slug, Normalizer.Form.NFD);
        slug = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                .matcher(slug)
                .replaceAll("");

        slug = slug.replace("đ", "d");
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.replaceAll("\\s+", "-");
        slug = slug.replaceAll("-+", "-");

        return slug.trim();
    }
}
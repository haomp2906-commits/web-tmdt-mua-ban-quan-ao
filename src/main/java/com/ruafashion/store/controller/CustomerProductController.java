package com.ruafashion.store.controller;


import com.ruafashion.store.model.Product;
import com.ruafashion.store.model.ProductImage;
import com.ruafashion.store.model.ProductSize;
import com.ruafashion.store.repository.CategoryRepository;
import com.ruafashion.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CustomerProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/products")
    public String listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<Product> productPage =
                productService.getCustomerProducts(
                        keyword,
                        categoryId,
                        minPrice,
                        maxPrice,
                        page
                );

        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categoryRepository.findAll());

        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "user/products/list";
    }

    @GetMapping("/products/{id}")
    public String productDetail(
            @PathVariable Long id,
            Model model
    ) {
        Product product = productService.getActiveProductById(id);

        List<ProductImage> productImages =
                productService.getProductImages(id);

        List<ProductSize> productSizes =
                productService.getAvailableProductSizes(id);

        model.addAttribute("product", product);
        model.addAttribute("productImages", productImages);
        model.addAttribute("productSizes", productSizes);

        return "user/products/detail";
    }
}
package com.ruafashion.store.controller;


import com.ruafashion.store.model.Product;
import com.ruafashion.store.service.CategoryService;
import com.ruafashion.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            Model model
    ) {
        model.addAttribute(
                "products",
                productService.searchAndFilterProducts(
                        keyword,
                        categoryId,
                        status,
                        minPrice,
                        maxPrice
                )
        );

        model.addAttribute("categories", categoryService.getAllCategories());

        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "admin/products/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Product product = new Product();
        product.setStatus("ACTIVE");
        product.setViewCount(0);

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "admin/products/form";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product,
                              @RequestParam("imageFiles") MultipartFile[] imageFiles,
                              RedirectAttributes redirectAttributes) {
        productService.saveProduct(product, imageFiles);

        redirectAttributes.addFlashAttribute(
                "success",
                "Lưu sản phẩm thành công!"
        );

        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "admin/products/form";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Xóa sản phẩm thành công!"
            );

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/products";
    }
}
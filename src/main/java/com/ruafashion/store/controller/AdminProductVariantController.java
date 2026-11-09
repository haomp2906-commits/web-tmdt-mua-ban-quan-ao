package com.ruafashion.store.controller;


import com.ruafashion.store.dto.ProductVariantRequest;
import com.ruafashion.store.model.Product;
import com.ruafashion.store.service.ProductService;
import com.ruafashion.store.service.ProductVariantService;
import com.ruafashion.store.repository.SizeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminProductVariantController {

    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final SizeRepository sizeRepository;

    @GetMapping("/admin/products/{productId}/variants")
    public String listVariants(@PathVariable Long productId, Model model) {
        Product product = productService.getProductById(productId);

        model.addAttribute("product", product);
        model.addAttribute(
                "variants",
                productVariantService.getVariantsByProductId(productId)
        );

        return "admin/products/variants/list";
    }

    @GetMapping("/admin/products/{productId}/variants/create")
    public String showCreateForm(@PathVariable Long productId, Model model) {
        Product product = productService.getProductById(productId);

        model.addAttribute("product", product);
        model.addAttribute("variantRequest", new ProductVariantRequest());
        model.addAttribute("sizes", sizeRepository.findAll());

        return "admin/products/variants/form";
    }

    @PostMapping("/admin/products/{productId}/variants/save")
    public String saveVariant(
            @PathVariable Long productId,
            @Valid @ModelAttribute("variantRequest") ProductVariantRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            model.addAttribute("product", productService.getProductById(productId));
            model.addAttribute("sizes", sizeRepository.findAll());

            return "admin/products/variants/form";
        }

        try {
            productVariantService.saveVariant(productId, request);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Lưu biến thể thành công!"
            );

            return "redirect:/admin/products/" + productId + "/variants";

        } catch (RuntimeException e) {
            model.addAttribute("product", productService.getProductById(productId));
            model.addAttribute("sizes", sizeRepository.findAll());
            model.addAttribute("error", e.getMessage());

            return "admin/products/variants/form";
        }
    }

    @GetMapping("/admin/products/{productId}/variants/edit/{variantId}")
    public String showEditForm(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            Model model
    ) {
        Product product = productService.getProductById(productId);

        var variant = productVariantService.getVariantById(variantId);

        if (!variant.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Biến thể không thuộc sản phẩm này");
        }

        ProductVariantRequest request = new ProductVariantRequest();
        request.setId(variant.getId());
        request.setSizeId(variant.getSize().getId());
        request.setColor(variant.getColor());
        request.setQuantity(variant.getQuantity());

        model.addAttribute("product", product);
        model.addAttribute("variantRequest", request);
        model.addAttribute("sizes", sizeRepository.findAll());

        return "admin/products/variants/form";
    }

    @PostMapping("/admin/products/{productId}/variants/delete/{variantId}")
    public String deleteVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            var variant = productVariantService.getVariantById(variantId);

            if (!variant.getProduct().getId().equals(productId)) {
                throw new RuntimeException("Biến thể không thuộc sản phẩm này");
            }

            productVariantService.deleteVariant(variantId);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Xóa biến thể thành công!"
            );

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/products/" + productId + "/variants";
    }
}
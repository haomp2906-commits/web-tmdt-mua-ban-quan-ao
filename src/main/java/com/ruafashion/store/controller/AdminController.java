package com.ruafashion.store.controller;


import com.ruafashion.store.repository.CategoryRepository;
import com.ruafashion.store.repository.OrderRepository;
import com.ruafashion.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;

    @GetMapping("/admin/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        model.addAttribute("email", authentication.getName());

        model.addAttribute("productCount", productRepository.count());
        model.addAttribute(
                "activeProductCount",
                productRepository.countByStatus("ACTIVE")
        );
        model.addAttribute("categoryCount", categoryRepository.count());
        model.addAttribute("orderCount", orderRepository.count());
        model.addAttribute(
                "pendingOrderCount",
                orderRepository.countByStatus("PENDING")
        );
        model.addAttribute(
                "shippingOrderCount",
                orderRepository.countByStatus("SHIPPING")
        );
        model.addAttribute(
                "completedOrderCount",
                orderRepository.countByStatus("COMPLETED")
        );
        model.addAttribute(
                "cancelledOrderCount",
                orderRepository.countByStatus("CANCELLED")
        );

        Double revenue = orderRepository
                .sumTotalAmountByStatus("COMPLETED");

        model.addAttribute(
                "revenue",
                revenue != null ? revenue : 0D
        );

        return "admin/dashboard";
    }
}
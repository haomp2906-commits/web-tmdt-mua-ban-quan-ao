package com.ruafashion.store.controller;


import com.ruafashion.store.model.Order;
import com.ruafashion.store.model.OrderDetail;
import com.ruafashion.store.repository.OrderDetailRepository;
import com.ruafashion.store.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final OrderDetailRepository orderDetailRepository;

    @GetMapping
    public String listOrders(
            @RequestParam(required = false) String status,
            Model model
    ) {
        List<Order> orders;

        if (status != null && !status.trim().isEmpty()) {
            orders = orderService.getOrdersByStatus(status);
        } else {
            orders = orderService.getAllOrders();
        }

        model.addAttribute("orders", orders);
        model.addAttribute("currentStatus", status);

        return "admin/orders/list";
    }

    @GetMapping("/{id}")
    public String orderDetail(
            @PathVariable Long id,
            Model model
    ) {
        Order order = orderService.getOrderById(id);
        List<OrderDetail> orderDetails =
                orderDetailRepository.findByOrderId(id);

        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);

        return "admin/orders/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes
    ) {
        try {
            orderService.updateOrderStatus(id, status);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Đã cập nhật trạng thái đơn hàng #" + id
            );
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/orders/" + id;
    }
}
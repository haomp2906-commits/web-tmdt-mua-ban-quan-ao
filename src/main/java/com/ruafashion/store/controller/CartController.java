package com.ruafashion.store.controller;



import com.ruafashion.store.model.CartItem;
import com.ruafashion.store.model.Order;
import com.ruafashion.store.model.User;
import com.ruafashion.store.service.CartService;
import com.ruafashion.store.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;

    @GetMapping("/cart")
    public String viewCart(
            Principal principal,
            Model model
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = cartService.getUserByEmail(principal.getName());

        model.addAttribute(
                "cartItems",
                cartService.getCartItems(user)
        );

        model.addAttribute(
                "cartTotal",
                cartService.getCartTotal(user)
        );

        return "user/cart";
    }

    @GetMapping("/checkout")
    public String showCheckout(
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = cartService.getUserByEmail(principal.getName());

        List<CartItem> cartItems = cartService.getCartItems(user);

        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Giỏ hàng đang trống. Không thể đặt hàng."
            );

            return "redirect:/cart";
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute(
                "cartTotal",
                cartService.getCartTotal(user)
        );

        return "user/checkout";
    }
    @GetMapping("/order-success/{id}")
    public String orderSuccess(
            Principal principal,
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            User user = cartService.getUserByEmail(principal.getName());

            Order order = orderService.getOrderForUser(id, user);

            model.addAttribute("order", order);

            return "user/order-success";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Không tìm thấy đơn hàng của bạn."
            );

            return "redirect:/cart";
        }
    }
    @GetMapping("/orders")
    public String orderHistory(
            Principal principal,
            Model model
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = cartService.getUserByEmail(principal.getName());

        model.addAttribute(
                "orders",
                orderService.getOrderHistory(user)
        );

        return "user/orders";
    }

    @PostMapping("/cart/add")
    public String addToCart(
            Principal principal,
            @RequestParam Long productId,
            @RequestParam Long productSizeId,
            @RequestParam Integer quantity,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            User user = cartService.getUserByEmail(principal.getName());

            cartService.addToCart(
                    user,
                    productId,
                    productSizeId,
                    quantity
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Đã thêm sản phẩm vào giỏ hàng!"
            );
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/products/" + productId;
    }

    @PostMapping("/cart/update")
    public String updateCartItem(
            Principal principal,
            @RequestParam Long cartItemId,
            @RequestParam Integer quantity,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            User user = cartService.getUserByEmail(principal.getName());

            cartService.updateCartItem(
                    user,
                    cartItemId,
                    quantity
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Đã cập nhật số lượng sản phẩm!"
            );
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/cart";
    }

    @PostMapping("/cart/delete")
    public String removeCartItem(
            Principal principal,
            @RequestParam Long cartItemId,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            User user = cartService.getUserByEmail(principal.getName());

            cartService.removeCartItem(user, cartItemId);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Đã xóa sản phẩm khỏi giỏ hàng!"
            );
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(
            Principal principal,
            @RequestParam String recipientName,
            @RequestParam String recipientPhone,
            @RequestParam String shippingAddress,
            @RequestParam(required = false) String note,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            User user = cartService.getUserByEmail(principal.getName());

            Long orderId = orderService.checkout(
                    user,
                    recipientName,
                    recipientPhone,
                    shippingAddress,
                    note
            );

            return "redirect:/order-success/" + orderId;

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/cart";
        }
    }
}
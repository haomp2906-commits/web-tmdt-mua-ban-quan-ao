package com.ruafashion.store.service;


import com.ruafashion.store.model.Cart;
import com.ruafashion.store.model.CartItem;
import com.ruafashion.store.model.Product;
import com.ruafashion.store.model.ProductSize;
import com.ruafashion.store.model.User;
import com.ruafashion.store.repository.CartItemRepository;
import com.ruafashion.store.repository.CartRepository;
import com.ruafashion.store.repository.ProductSizeRepository;
import com.ruafashion.store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ProductService productService;
    private final UserRepository userRepository;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy tài khoản: " + email));
    }

    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setCreatedAt(LocalDateTime.now());
                    return cartRepository.save(newCart);
                });
    }

    public List<CartItem> getCartItems(User user) {
        Cart cart = getOrCreateCart(user);
        return cartItemRepository.findByCartId(cart.getId());
    }

    @Transactional
    public void addToCart(
            User user,
            Long productId,
            Long productSizeId,
            Integer quantity
    ) {
        if (quantity == null || quantity < 1) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        Product product = productService.getProductById(productId);

        ProductSize productSize = productSizeRepository.findById(productSizeId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy biến thể sản phẩm"));

        if (!productSize.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Biến thể không thuộc sản phẩm này");
        }

        if (productSize.getQuantity() < quantity) {
            throw new RuntimeException("Số lượng tồn kho không đủ");
        }

        Cart cart = getOrCreateCart(user);

        CartItem existingItem = cartItemRepository
                .findByCartIdAndProductSizeId(cart.getId(), productSizeId)
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;

            if (newQuantity > productSize.getQuantity()) {
                throw new RuntimeException("Tổng số lượng vượt quá tồn kho");
            }

            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
            return;
        }

        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setProduct(product);
        newItem.setProductSize(productSize);
        newItem.setQuantity(quantity);
        newItem.setCreatedAt(LocalDateTime.now());

        cartItemRepository.save(newItem);
    }
    private Cart getExistingCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Giỏ hàng chưa tồn tại"));
    }

    @Transactional
    public void updateCartItem(
            User user,
            Long cartItemId,
            Integer quantity
    ) {
        if (quantity == null || quantity < 1) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        Cart cart = getExistingCart(user);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException(
                    "Bạn không có quyền cập nhật sản phẩm này"
            );
        }

        if (quantity > cartItem.getProductSize().getQuantity()) {
            throw new RuntimeException("Số lượng vượt quá tồn kho");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeCartItem(User user, Long cartItemId) {
        Cart cart = getExistingCart(user);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException(
                    "Bạn không có quyền xóa sản phẩm này"
            );
        }

        cartItemRepository.delete(cartItem);
    }
    public Double getCartTotal(User user) {
        List<CartItem> cartItems = getCartItems(user);

        double total = 0;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            Double unitPrice = product.getDiscountPrice();

            if (unitPrice == null || unitPrice <= 0) {
                unitPrice = product.getPrice();
            }

            total += unitPrice * cartItem.getQuantity();
        }

        return total;
    }
}


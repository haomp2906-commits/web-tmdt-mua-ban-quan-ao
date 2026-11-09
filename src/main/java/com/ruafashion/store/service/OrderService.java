package com.ruafashion.store.service;


import com.ruafashion.store.model.CartItem;
import com.ruafashion.store.model.Order;
import com.ruafashion.store.model.OrderDetail;
import com.ruafashion.store.model.Product;
import com.ruafashion.store.model.ProductSize;
import com.ruafashion.store.model.User;
import com.ruafashion.store.repository.CartItemRepository;
import com.ruafashion.store.repository.OrderDetailRepository;
import com.ruafashion.store.repository.OrderRepository;
import com.ruafashion.store.repository.ProductSizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductSizeRepository productSizeRepository;

    @Transactional
    public Long checkout(
            User user,
            String recipientName,
            String recipientPhone,
            String shippingAddress,
            String note
    ) {
        validateCheckoutInfo(
                recipientName,
                recipientPhone,
                shippingAddress
        );

        List<CartItem> cartItems = cartService.getCartItems(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang trống");
        }

        // Kiểm tra tồn kho của toàn bộ sản phẩm trước khi tạo đơn
        Map<Long, ProductSize> productSizes = new HashMap<>();

        for (CartItem cartItem : cartItems) {
            Long productSizeId = cartItem.getProductSize().getId();

            ProductSize productSize = productSizeRepository
                    .findById(productSizeId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Không tìm thấy biến thể sản phẩm"
                            ));

            if (productSize.getQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException(
                        "Sản phẩm "
                                + cartItem.getProduct().getName()
                                + " không đủ số lượng tồn kho"
                );
            }

            productSizes.put(productSizeId, productSize);
        }

        double totalAmount = 0;

        for (CartItem cartItem : cartItems) {
            double unitPrice = getSellingPrice(cartItem.getProduct());

            totalAmount += unitPrice * cartItem.getQuantity();
        }

        Order order = new Order();
        order.setUser(user);
        order.setRecipientName(recipientName.trim());
        order.setRecipientPhone(recipientPhone.trim());
        order.setShippingAddress(shippingAddress.trim());
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod("COD");
        order.setStatus("PENDING");
        order.setNote(
                note == null || note.trim().isEmpty()
                        ? null
                        : note.trim()
        );
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setOrder(savedOrder);
            orderDetail.setProduct(cartItem.getProduct());
            orderDetail.setProductSize(cartItem.getProductSize());
            orderDetail.setPrice(
                    getSellingPrice(cartItem.getProduct())
            );
            orderDetail.setQuantity(cartItem.getQuantity());

            orderDetailRepository.save(orderDetail);

            // Trừ tồn kho theo đúng biến thể size + màu
            ProductSize productSize = productSizes.get(
                    cartItem.getProductSize().getId()
            );

            int remainingQuantity =
                    productSize.getQuantity() - cartItem.getQuantity();

            productSize.setQuantity(remainingQuantity);

            productSizeRepository.save(productSize);
        }

        // Chỉ xóa giỏ hàng sau khi đơn, chi tiết đơn và tồn kho đã xử lý
        cartItemRepository.deleteAll(cartItems);

        return savedOrder.getId();
    }

    private double getSellingPrice(Product product) {
        Double discountPrice = product.getDiscountPrice();

        if (discountPrice != null && discountPrice > 0) {
            return discountPrice;
        }

        return product.getPrice();
    }

    private void validateCheckoutInfo(
            String recipientName,
            String recipientPhone,
            String shippingAddress
    ) {
        if (recipientName == null || recipientName.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập tên người nhận");
        }

        if (recipientPhone == null || recipientPhone.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập số điện thoại");
        }

        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập địa chỉ giao hàng");
        }
    }
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus) {
        List<String> validStatuses = List.of(
                "PENDING",
                "SHIPPING",
                "COMPLETED",
                "CANCELLED"
        );

        if (!validStatuses.contains(newStatus)) {
            throw new RuntimeException("Trạng thái không hợp lệ");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy đơn hàng"));

        String oldStatus = order.getStatus();

        if (oldStatus.equals(newStatus)) {
            return;
        }

        if ("CANCELLED".equals(oldStatus)) {
            throw new RuntimeException(
                    "Đơn hàng đã bị hủy, không thể đổi trạng thái"
            );
        }

        if ("CANCELLED".equals(newStatus)) {
            List<OrderDetail> orderDetails =
                    orderDetailRepository.findByOrderId(orderId);

            for (OrderDetail orderDetail : orderDetails) {
                ProductSize productSize = orderDetail.getProductSize();

                productSize.setQuantity(
                        productSize.getQuantity() + orderDetail.getQuantity()
                );

                productSizeRepository.save(productSize);
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }
    public Order getOrderForUser(Long orderId, User user) {
        return orderRepository.findByIdAndUserId(
                orderId,
                user.getId()
        ).orElseThrow(() ->
                new RuntimeException("Không tìm thấy đơn hàng"));
    }
    public List<Order> getOrderHistory(User user) {
        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());
    }
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy đơn hàng"));
    }
}
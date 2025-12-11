package com.ecommerce.productorder.service.impl;

import com.ecommerce.productorder.exception.InsufficientStockException;
import com.ecommerce.productorder.exception.ResourceNotFoundException;
import com.ecommerce.productorder.model.dto.request.OrderItemRequest;
import com.ecommerce.productorder.model.dto.response.OrderItemResponse;
import com.ecommerce.productorder.model.dto.request.OrderRequest;
import com.ecommerce.productorder.model.dto.response.OrderResponse;
import com.ecommerce.productorder.model.entity.Order;
import com.ecommerce.productorder.model.entity.OrderItem;
import com.ecommerce.productorder.model.entity.Product;
import com.ecommerce.productorder.model.entity.User;
import com.ecommerce.productorder.repository.OrderRepository;
import com.ecommerce.productorder.service.OrderService;
import com.ecommerce.productorder.service.ProductService;
import com.ecommerce.productorder.service.UserService;
import com.ecommerce.productorder.service.discount.DiscountCalculator;
import com.ecommerce.productorder.util.LoggedInUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private DiscountCalculator discountCalculator;
    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        String username = LoggedInUser.get().getUsername();
        log.info("Placing order for user: {}", username);
        User user = userService.getUserByUsername(username);
        Order order = buildOrder(user);
        BigDecimal subtotalPrice = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productService.getProduct(itemRequest.getProductId());
            if (product.getQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(product.getName(), itemRequest.getQuantity(), product.getQuantity());
            }

            product.decreaseQuantity(itemRequest.getQuantity());
            productService.saveProduct(product);

            BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            OrderItem orderItem = buildOrderItem(itemRequest, product, totalPrice);
            order.addItem(orderItem);
            subtotalPrice = subtotalPrice.add(totalPrice);
        }

        BigDecimal totalDiscount = discountCalculator.calculateTotalDiscount(subtotalPrice, user.getRole());
        BigDecimal orderTotal = subtotalPrice.subtract(totalDiscount);

        order.setTotalDiscount(totalDiscount);
        order.setOrderTotal(orderTotal);
        Order savedOrder = orderRepository.save(order);
        log.info("Order placed successfully with ID: {} for user: {}", savedOrder.getId(), username);

        return mapToResponse(savedOrder);
    }

    private OrderItem buildOrderItem(OrderItemRequest itemRequest, Product product, BigDecimal itemTotal) {
        return OrderItem.builder()
                .product(product)
                .quantity(itemRequest.getQuantity())
                .unitPrice(product.getPrice())
                .discountApplied(BigDecimal.ZERO)
                .totalPrice(itemTotal)
                .build();
    }

    private static Order buildOrder(User user) {
        return Order.builder()
                .user(user)
                .items(new ArrayList<>())
                .build();
    }

    @Transactional
    public OrderResponse getOrderById(Long orderId) {
        String username = LoggedInUser.get().getUsername();
        log.debug("Fetching order with ID: {} for user: {}", orderId, username);
        Order order = getOrder(orderId);
        if (!LoggedInUser.isAdmin() && !order.getUser().getUsername().equals(username)) {
            throw new ResourceNotFoundException("Order", "id", orderId);
        }

        return mapToResponse(order);
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }

    @Transactional
    public Page<OrderResponse> getUserOrders(Pageable pageable) {
        String username = LoggedInUser.get().getUsername();
        log.debug("Fetching orders for user: {}", username);
        User user = userService.getUserByUsername(username);

        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders");
        return orderRepository.findAll(pageable).map(this::mapToResponse);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream().map(this::mapItemToResponse).toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .items(itemResponses)
                .orderTotal(order.getOrderTotal())
                .totalDiscount(order.getTotalDiscount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemResponse mapItemToResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountApplied(item.getDiscountApplied())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}

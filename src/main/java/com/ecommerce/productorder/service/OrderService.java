package com.ecommerce.productorder.service;

import com.ecommerce.productorder.model.dto.request.OrderRequest;
import com.ecommerce.productorder.model.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(Long orderId);

    Page<OrderResponse> getUserOrders(Pageable pageable);

    Page<OrderResponse> getAllOrders(Pageable pageable);
}

package com.ecommerce.productorder.service;

import com.ecommerce.productorder.exception.InsufficientStockException;
import com.ecommerce.productorder.exception.ResourceNotFoundException;
import com.ecommerce.productorder.model.dto.request.OrderItemRequest;
import com.ecommerce.productorder.model.dto.request.OrderRequest;
import com.ecommerce.productorder.model.dto.response.OrderResponse;
import com.ecommerce.productorder.model.entity.Order;
import com.ecommerce.productorder.model.entity.OrderItem;
import com.ecommerce.productorder.model.entity.Product;
import com.ecommerce.productorder.model.entity.User;
import com.ecommerce.productorder.model.enums.UserRole;
import com.ecommerce.productorder.repository.OrderRepository;
import com.ecommerce.productorder.service.discount.DiscountCalculator;
import com.ecommerce.productorder.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DiscountCalculator discountCalculator;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Product testProduct;
    private Order testOrder;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testProduct = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("100.00"))
                .quantity(10)
                .build();
        ReflectionTestUtils.setField(testProduct, "id", 1L);

        OrderItem orderItem = OrderItem.builder()
                .product(testProduct)
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("200.00"))
                .discountApplied(BigDecimal.ZERO)
                .build();
        ReflectionTestUtils.setField(orderItem, "id", 1L);

        testOrder = Order.builder()
                .user(testUser)
                .items(new ArrayList<>(List.of(orderItem)))
                .orderTotal(new BigDecimal("200.00"))
                .totalDiscount(BigDecimal.ZERO)
                .build();
        ReflectionTestUtils.setField(testOrder, "id", 1L);

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(1L)
                .quantity(2)
                .build();

        orderRequest = OrderRequest.builder()
                .items(List.of(itemRequest))
                .build();

        // Setup SecurityContext for LoggedInUser
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should create order successfully")
    void testCreateOrder() {
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(productService.getProduct(1L)).thenReturn(testProduct);
        when(discountCalculator.calculateTotalDiscount(any(BigDecimal.class), any(UserRole.class)))
                .thenReturn(BigDecimal.ZERO);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderResponse response = orderService.createOrder(orderRequest);

        assertNotNull(response);
        assertEquals(testOrder.getId(), response.getId());
        assertEquals(testOrder.getOrderTotal(), response.getOrderTotal());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(productService, times(1)).saveProduct(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found during order creation")
    void testCreateOrderProductNotFound() {
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(productService.getProduct(anyLong())).thenThrow(new ResourceNotFoundException("Product", "id", 999L));

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(orderRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when insufficient stock")
    void testCreateOrderInsufficientStock() {
        Product lowStockProduct = Product.builder()
                .name("Low Stock Product")
                .description("Test Description")
                .price(new BigDecimal("100.00"))
                .quantity(1)
                .build();
        ReflectionTestUtils.setField(lowStockProduct, "id", 1L);

        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(productService.getProduct(1L)).thenReturn(lowStockProduct);

        assertThrows(InsufficientStockException.class, () -> {
            orderService.createOrder(orderRequest);
        });
    }

    @Test
    @DisplayName("Should get order by ID successfully")
    void testGetOrderById() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        OrderResponse response = orderService.getOrderById(1L);

        assertNotNull(response);
        assertEquals(testOrder.getId(), response.getId());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void testGetOrderByIdNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderById(999L);
        });
    }

    @Test
    @DisplayName("Should get user orders with pagination")
    void testGetUserOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder));

        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable))
                .thenReturn(orderPage);

        Page<OrderResponse> result = orderService.getUserOrders(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findByUserIdOrderByCreatedAtDesc(1L, pageable);
    }

    @Test
    @DisplayName("Should get all orders with pagination")
    void testGetAllOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder));

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        Page<OrderResponse> result = orderService.getAllOrders(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should apply discount correctly")
    void testCreateOrderWithDiscount() {
        BigDecimal discount = new BigDecimal("20.00");
        BigDecimal subtotal = new BigDecimal("200.00");
        BigDecimal orderTotal = subtotal.subtract(discount);

        Order discountedOrder = Order.builder()
                .user(testUser)
                .items(new ArrayList<>())
                .orderTotal(orderTotal)
                .totalDiscount(discount)
                .build();
        ReflectionTestUtils.setField(discountedOrder, "id", 2L);

        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(productService.getProduct(1L)).thenReturn(testProduct);
        when(discountCalculator.calculateTotalDiscount(any(BigDecimal.class), any(UserRole.class)))
                .thenReturn(discount);
        when(orderRepository.save(any(Order.class))).thenReturn(discountedOrder);

        OrderResponse response = orderService.createOrder(orderRequest);

        assertNotNull(response);
        verify(discountCalculator, times(1)).calculateTotalDiscount(any(BigDecimal.class), any(UserRole.class));
    }
}

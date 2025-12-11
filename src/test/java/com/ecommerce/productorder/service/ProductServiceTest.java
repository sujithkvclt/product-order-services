package com.ecommerce.productorder.service;

import com.ecommerce.productorder.exception.ResourceNotFoundException;
import com.ecommerce.productorder.model.dto.request.ProductRequest;
import com.ecommerce.productorder.model.dto.response.ProductResponse;
import com.ecommerce.productorder.model.entity.Product;
import com.ecommerce.productorder.repository.ProductRepository;
import com.ecommerce.productorder.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();

        product.setDeleted(false);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        productRequest = ProductRequest.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(productRequest);

        assertNotNull(response);
        assertEquals(product.getId(), response.getId());
        assertEquals(product.getName(), response.getName());
        assertEquals(product.getPrice(), response.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void testGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(product.getId(), response.getId());
        assertEquals(product.getName(), response.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found")
    void testGetProductByIdNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(999L);
        });

        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct() {
        ProductRequest updateRequest = ProductRequest.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(new BigDecimal("149.99"))
                .quantity(20)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.updateProduct(1L, updateRequest);

        assertNotNull(response);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(any(Product.class));

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).delete(any(Product.class));
    }

    @Test
    @DisplayName("Product should be available when quantity is greater than 0")
    void testProductIsAvailable() {
        assertTrue(product.isAvailable());
    }

    @Test
    @DisplayName("Product should not be available when quantity is 0")
    void testProductIsNotAvailableWhenQuantityZero() {
        product.setQuantity(0);
        assertFalse(product.isAvailable());
    }

    @Test
    @DisplayName("Should decrease product quantity successfully")
    void testDecreaseProductQuantity() {
        int initialQuantity = product.getQuantity();
        int decreaseAmount = 3;

        product.decreaseQuantity(decreaseAmount);

        assertEquals(initialQuantity - decreaseAmount, product.getQuantity());
    }

    @Test
    @DisplayName("Should throw exception when decreasing quantity more than available")
    void testDecreaseQuantityInsufficientStock() {
        assertThrows(IllegalStateException.class, () -> {
            product.decreaseQuantity(100);
        });
    }
}

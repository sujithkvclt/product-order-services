package com.ecommerce.productorder.service;

import com.ecommerce.productorder.model.dto.request.ProductRequest;
import com.ecommerce.productorder.model.dto.response.ProductResponse;
import com.ecommerce.productorder.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Long id);

    Page<ProductResponse> getAllProducts(Pageable pageable);

    Page<ProductResponse> searchProducts(String name, BigDecimal minPrice, BigDecimal maxPrice, Boolean available, Pageable pageable);

    ProductResponse updateProduct(Long id, ProductRequest request);

    Product getProduct(Long id);

    Product saveProduct(Product product);

    void deleteProduct(Long id);
}

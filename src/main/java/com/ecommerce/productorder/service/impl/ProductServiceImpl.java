package com.ecommerce.productorder.service.impl;

import com.ecommerce.productorder.exception.ResourceNotFoundException;
import com.ecommerce.productorder.model.dto.request.ProductRequest;
import com.ecommerce.productorder.model.dto.response.ProductResponse;
import com.ecommerce.productorder.model.entity.Product;
import com.ecommerce.productorder.repository.ProductRepository;
import com.ecommerce.productorder.repository.specification.ProductSpecifications;
import com.ecommerce.productorder.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating new product: {}", request.getName());
        Product product = buildProduct(request);
        Product savedProduct = saveProduct(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        log.info("Fetching product with ID: {}", id);
        Product product = getProduct(id);

        return mapToResponse(product);
    }

    @Override
    @Transactional
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination");

        return productRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public Page<ProductResponse> searchProducts(String name, BigDecimal minPrice, BigDecimal maxPrice, Boolean available,
                                                Pageable pageable) {
        log.info("Searching products with filters - name: {}, minPrice: {}, maxPrice: {}, available: {}",
                name, minPrice, maxPrice, available);
        Specification<Product> spec = Specification
                .where(ProductSpecifications.notDeleted())
                .and(ProductSpecifications.nameContains(name))
                .and(ProductSpecifications.minPrice(minPrice))
                .and(ProductSpecifications.maxPrice(maxPrice))
                .and(ProductSpecifications.available(available));

        return productRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Updating product with ID: {}", id);
        Product product = getProduct(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        Product updatedProduct = saveProduct(product);
        log.info("Product updated successfully: {}", id);

        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional
    public Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        Product product = getProduct(id);
        productRepository.delete(product);
        log.info("Product soft-deleted successfully: {}", id);
    }

    private static Product buildProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();
        product.setDeleted(false);

        return product;
    }

    private ProductResponse mapToResponse(Product product) {

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .available(product.isAvailable())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}

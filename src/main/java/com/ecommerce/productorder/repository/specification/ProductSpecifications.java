package com.ecommerce.productorder.repository.specification;

import com.ecommerce.productorder.model.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecifications {

    public static Specification<Product> nameContains(String name) {
        return (root, query, cb) -> name == null ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Product> minPrice(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> maxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> available(Boolean available) {
        return (root, query, cb) -> {
            if (available == null || !available) return null;
            return cb.greaterThan(root.get("quantity"), 0);
        };
    }

    public static Specification<Product> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }
}


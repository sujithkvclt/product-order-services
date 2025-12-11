package com.ecommerce.productorder.service.discount;

import com.ecommerce.productorder.model.enums.UserRole;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * No discount strategy for regular users
 */
@Component
public class NoDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderTotal, UserRole userRole) {
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isApplicable(BigDecimal orderTotal, UserRole userRole) {
        return userRole == UserRole.USER;
    }
}

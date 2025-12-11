package com.ecommerce.productorder.service.discount;

import com.ecommerce.productorder.model.enums.UserRole;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 10% discount strategy for premium users
 */
@Component
public class PremiumUserDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.10"); // 10%

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderTotal, UserRole userRole) {
        return orderTotal.multiply(DISCOUNT_PERCENTAGE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean isApplicable(BigDecimal orderTotal, UserRole userRole) {
        return userRole == UserRole.PREMIUM_USER;
    }
}

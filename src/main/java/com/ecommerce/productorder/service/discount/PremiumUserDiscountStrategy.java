package com.ecommerce.productorder.service.discount;

import com.ecommerce.productorder.model.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Discount strategy for premium users
 * Applies a configurable percentage discount for all PREMIUM_USER role orders
 */
@Component
public class PremiumUserDiscountStrategy implements DiscountStrategy {

    private final BigDecimal discountPercentage;

    public PremiumUserDiscountStrategy(
            @Value("${discount.premium-user.percentage}") BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderTotal, UserRole userRole) {
        return orderTotal.multiply(discountPercentage)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean isApplicable(BigDecimal orderTotal, UserRole userRole) {
        return userRole == UserRole.PREMIUM_USER;
    }
}

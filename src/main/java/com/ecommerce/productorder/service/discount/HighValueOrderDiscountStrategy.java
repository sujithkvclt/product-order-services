package com.ecommerce.productorder.service.discount;

import com.ecommerce.productorder.model.enums.UserRole;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Additional 5% discount strategy for orders over $500
 */
@Component
public class HighValueOrderDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal THRESHOLD = new BigDecimal("500.00");
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.05"); // 5%

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderTotal, UserRole userRole) {
        if (orderTotal.compareTo(THRESHOLD) > 0) {
            return orderTotal.multiply(DISCOUNT_PERCENTAGE)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isApplicable(BigDecimal orderTotal, UserRole userRole) {
        return orderTotal.compareTo(THRESHOLD) > 0;
    }
}

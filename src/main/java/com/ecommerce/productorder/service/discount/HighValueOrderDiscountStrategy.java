package com.ecommerce.productorder.service.discount;

import com.ecommerce.productorder.model.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Additional discount strategy for high-value orders
 * Applies discount when order total is greater than or equal to the configured threshold
 */
@Component
public class HighValueOrderDiscountStrategy implements DiscountStrategy {

    private final BigDecimal threshold;
    private final BigDecimal discountPercentage;

    public HighValueOrderDiscountStrategy(
            @Value("${discount.high-value-order.threshold}") BigDecimal threshold,
            @Value("${discount.high-value-order.percentage}") BigDecimal discountPercentage) {
        this.threshold = threshold;
        this.discountPercentage = discountPercentage;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderTotal, UserRole userRole) {
        if (orderTotal.compareTo(threshold) >= 0) {
            return orderTotal.multiply(discountPercentage)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isApplicable(BigDecimal orderTotal, UserRole userRole) {
        return orderTotal.compareTo(threshold) >= 0;
    }
}

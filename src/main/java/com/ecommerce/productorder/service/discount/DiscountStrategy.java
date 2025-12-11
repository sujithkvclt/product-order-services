package com.ecommerce.productorder.service.discount;

import com.ecommerce.productorder.model.enums.UserRole;

import java.math.BigDecimal;

/**
 * Strategy pattern interface for discount calculation
 */
public interface DiscountStrategy {

    /**
     * Calculate discount amount based on strategy
     * @param orderTotal The total order amount before discount
     * @param userRole The role of the user placing the order
     * @return The discount amount to be applied
     */
    BigDecimal calculateDiscount(BigDecimal orderTotal, UserRole userRole);

    /**
     * Check if this strategy is applicable
     * @param orderTotal The total order amount
     * @param userRole The role of the user
     * @return true if this strategy should be applied
     */
    boolean isApplicable(BigDecimal orderTotal, UserRole userRole);
}

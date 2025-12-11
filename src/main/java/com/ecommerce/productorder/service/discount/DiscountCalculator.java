package com.ecommerce.productorder.service.discount;

import com.ecommerce.productorder.model.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service to calculate total discount by combining multiple discount strategies
 */
@Slf4j
@Component
public class DiscountCalculator {

    @Autowired
    private List<DiscountStrategy> discountStrategies;

    /**
     * Calculate total discount by applying all applicable strategies
     *
     * @param orderTotal The total order amount before discount
     * @param userRole   The role of the user placing the order
     * @return The total discount amount to be applied
     */
    public BigDecimal calculateTotalDiscount(BigDecimal orderTotal, UserRole userRole) {
        log.debug("Calculating discount for order total: {} and user role: {}", orderTotal, userRole);

        BigDecimal totalDiscount = discountStrategies.stream()
                .filter(strategy -> strategy.isApplicable(orderTotal, userRole))
                .map(strategy -> {
                    BigDecimal discount = strategy.calculateDiscount(orderTotal, userRole);
                    log.debug("Applied {}: {}", strategy.getClass().getSimpleName(), discount);
                    return discount;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        log.debug("Total discount calculated: {}", totalDiscount);
        return totalDiscount;
    }
}

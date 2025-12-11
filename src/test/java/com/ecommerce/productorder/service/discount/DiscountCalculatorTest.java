package com.ecommerce.productorder.service.discount;

import com.ecommerce.productorder.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Discount Calculator Tests")
class DiscountCalculatorTest {

    private DiscountCalculator discountCalculator;

    @BeforeEach
    void setUp() {
        discountCalculator = new DiscountCalculator();

        List<DiscountStrategy> strategies = List.of(
                new NoDiscountStrategy(),
                new PremiumUserDiscountStrategy(),
                new HighValueOrderDiscountStrategy()
        );

        ReflectionTestUtils.setField(discountCalculator, "discountStrategies", strategies);
    }

    @Test
    @DisplayName("Should return zero discount for regular user with order under $500")
    void testNoDiscountForRegularUser() {
        BigDecimal orderTotal = new BigDecimal("100.00");
        UserRole userRole = UserRole.USER;

        BigDecimal discount = discountCalculator.calculateTotalDiscount(orderTotal, userRole);

        assertEquals(new BigDecimal("0.00"), discount);
    }

    @Test
    @DisplayName("Should apply 10% discount for premium user with order under $500")
    void testPremiumUserDiscount() {
        BigDecimal orderTotal = new BigDecimal("100.00");
        UserRole userRole = UserRole.PREMIUM_USER;

        BigDecimal discount = discountCalculator.calculateTotalDiscount(orderTotal, userRole);

        assertEquals(new BigDecimal("10.00"), discount);
    }

    @Test
    @DisplayName("Should apply 5% discount for regular user with order over $500")
    void testHighValueOrderDiscount() {
        BigDecimal orderTotal = new BigDecimal("600.00");
        UserRole userRole = UserRole.USER;

        BigDecimal discount = discountCalculator.calculateTotalDiscount(orderTotal, userRole);

        assertEquals(new BigDecimal("30.00"), discount);
    }

    @Test
    @DisplayName("Should apply 15% total discount for premium user with order over $500 (10% + 5%)")
    void testCombinedDiscounts() {
        BigDecimal orderTotal = new BigDecimal("600.00");
        UserRole userRole = UserRole.PREMIUM_USER;

        BigDecimal discount = discountCalculator.calculateTotalDiscount(orderTotal, userRole);

        // 10% premium discount (60.00) + 5% high value discount (30.00) = 90.00
        assertEquals(new BigDecimal("90.00"), discount);
    }

    @Test
    @DisplayName("Should apply no discount for admin user with order under $500")
    void testAdminUserNoDiscount() {
        BigDecimal orderTotal = new BigDecimal("100.00");
        UserRole userRole = UserRole.ADMIN;

        BigDecimal discount = discountCalculator.calculateTotalDiscount(orderTotal, userRole);

        assertEquals(new BigDecimal("0.00"), discount);
    }

    @Test
    @DisplayName("Should apply only high value discount for admin user with order over $500")
    void testAdminUserHighValueDiscount() {
        BigDecimal orderTotal = new BigDecimal("600.00");
        UserRole userRole = UserRole.ADMIN;

        BigDecimal discount = discountCalculator.calculateTotalDiscount(orderTotal, userRole);

        assertEquals(new BigDecimal("30.00"), discount);
    }
}

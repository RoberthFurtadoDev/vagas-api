package com.estapar.vagas.service.pricing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingStrategyResolverTest {

    private PricingStrategyResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new PricingStrategyResolver();
    }

    @Test
    void shouldApplyDiscountWhenOccupancyBelow25Percent() {
        final PricingStrategy strategy = resolver.resolve(20, 100);
        assertEquals(new BigDecimal("0.90"), strategy.modifier());
    }

    @Test
    void shouldApplyStandardWhenOccupancyBetween25And50Percent() {
        final PricingStrategy strategy = resolver.resolve(40, 100);
        assertEquals(new BigDecimal("1.00"), strategy.modifier());
    }

    @Test
    void shouldApplyIncreasedWhenOccupancyBetween50And75Percent() {
        final PricingStrategy strategy = resolver.resolve(60, 100);
        assertEquals(new BigDecimal("1.10"), strategy.modifier());
    }

    @Test
    void shouldApplyPremiumWhenOccupancyBetween75And100Percent() {
        final PricingStrategy strategy = resolver.resolve(80, 100);
        assertEquals(new BigDecimal("1.25"), strategy.modifier());
    }

    @Test
    void shouldApplyStandardWhenCapacityIsZero() {
        final PricingStrategy strategy = resolver.resolve(0, 0);
        assertEquals(new BigDecimal("1.00"), strategy.modifier());
    }

    @Test
    void shouldApplyDiscountAtExactly24PercentOccupancy() {
        final PricingStrategy strategy = resolver.resolve(24, 100);
        assertEquals(new BigDecimal("0.90"), strategy.modifier());
    }

    @Test
    void shouldApplyStandardAtExactly25PercentOccupancy() {
        final PricingStrategy strategy = resolver.resolve(25, 100);
        assertEquals(new BigDecimal("1.00"), strategy.modifier());
    }
}

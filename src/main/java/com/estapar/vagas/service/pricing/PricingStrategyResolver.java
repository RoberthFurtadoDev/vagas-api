package com.estapar.vagas.service.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PricingStrategyResolver {

    private static final BigDecimal THRESHOLD_25 = new BigDecimal("0.25");
    private static final BigDecimal THRESHOLD_50 = new BigDecimal("0.50");
    private static final BigDecimal THRESHOLD_75 = new BigDecimal("0.75");
    private static final BigDecimal THRESHOLD_100 = new BigDecimal("1.00");

    public PricingStrategy resolve(int totalOccupied, int totalCapacity) {
        if (totalCapacity == 0) {
            return new StandardPricingStrategy();
        }

        final BigDecimal occupancyRate = BigDecimal.valueOf(totalOccupied)
                .divide(BigDecimal.valueOf(totalCapacity), 4, RoundingMode.HALF_UP);

        if (occupancyRate.compareTo(THRESHOLD_25) < 0) {
            return new DiscountPricingStrategy();
        }
        if (occupancyRate.compareTo(THRESHOLD_50) < 0) {
            return new StandardPricingStrategy();
        }
        if (occupancyRate.compareTo(THRESHOLD_75) < 0) {
            return new IncreasedPricingStrategy();
        }
        return new PremiumPricingStrategy();
    }
}

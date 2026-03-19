package com.estapar.vagas.service.pricing;

import java.math.BigDecimal;

public class PremiumPricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal modifier() {
        return new BigDecimal("1.25");
    }
}

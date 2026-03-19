package com.estapar.vagas.service.pricing;

import java.math.BigDecimal;

public class DiscountPricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal modifier() {
        return new BigDecimal("0.90");
    }
}

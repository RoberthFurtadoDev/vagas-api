package com.estapar.vagas.service.pricing;

import java.math.BigDecimal;

public class IncreasedPricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal modifier() {
        return new BigDecimal("1.10");
    }
}

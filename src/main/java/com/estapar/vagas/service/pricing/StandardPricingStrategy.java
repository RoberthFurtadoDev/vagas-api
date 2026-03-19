package com.estapar.vagas.service.pricing;

import java.math.BigDecimal;

public class StandardPricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal modifier() {
        return new BigDecimal("1.00");
    }
}

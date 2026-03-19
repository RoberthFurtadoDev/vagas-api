package com.estapar.vagas.service.pricing;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal modifier();
}

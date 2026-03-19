# Testes

Framework: **JUnit 5 + Mockito**. Nenhum teste sobe o contexto do Spring — execução rápida e isolada.

---

## Como executar

**Linux/Mac:**
```bash
mvn test
```

**Windows (IntelliJ):**
Clique com o botão direito na pasta `src/test/java` → `Run 'All Tests'`

**Resultado esperado:**
```
Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## Cobertura

### `PricingStrategyResolverTest`

Testa o `PricingStrategyResolver` em isolamento puro — sem mocks, apenas lógica.

| Teste | Ocupação | `modifier` esperado |
|---|---|---|
| `shouldApplyDiscountWhenOccupancyBelow25Percent` | 20/100 | `0.90` |
| `shouldApplyStandardWhenOccupancyBetween25And50Percent` | 40/100 | `1.00` |
| `shouldApplyIncreasedWhenOccupancyBetween50And75Percent` | 60/100 | `1.10` |
| `shouldApplyPremiumWhenOccupancyBetween75And100Percent` | 80/100 | `1.25` |
| `shouldApplyStandardWhenCapacityIsZero` | 0/0 | `1.00` |
| `shouldApplyDiscountAtExactly24PercentOccupancy` | 24/100 | `0.90` |
| `shouldApplyStandardAtExactly25PercentOccupancy` | 25/100 | `1.00` |

### `WebhookServiceTest`

Testa os três fluxos de evento e os caminhos de erro. Repositories são mockados com Mockito.

| Teste | Cenário |
|---|---|
| `handleEntry_shouldSaveSessionWithModifier` | `ENTRY` normal com 30% de ocupação |
| `handleEntry_shouldThrowWhenGarageIsFull` | `ENTRY` com 100% → `GarageFullException` |
| `handleParked_shouldSetSpotOccupiedAndPricePerHour` | `price_per_hour` travado em `11.00` (10.00 × 1.10) |
| `handleExit_shouldCalculateAmountForStay` | 91 min → 2 horas → `amount = 20.00` |
| `handleExit_shouldChargeZeroForFreePeriod` | 30 min exatos → `amount = 0.00` |
| `handleExit_shouldThrowWhenNoParkedSession` | Sem sessão `PARKED` → `VehicleNotFoundException` |

### `RevenueServiceTest`

| Teste | Cenário |
|---|---|
| `getRevenue_shouldReturnSumForSectorAndDate` | Repository retorna `150.00` |
| `getRevenue_shouldReturnZeroWhenNoSessions` | Repository retorna `0.00` |

---

## Decisão de design dos testes

Os testes são unitários puros — sem `@SpringBootTest`, sem banco em memória.
Isso os torna rápidos e focados: cada teste verifica uma única responsabilidade.
A lógica de negócio está nos services e no resolver, que são exatamente o que está sendo testado.

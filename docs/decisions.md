# Decisões de Design

---

## Ocupação global para o preço dinâmico

O documento do teste afirma que os setores são divisões lógicas e que a garagem possui um único grupo de cancelas na entrada.

No momento do `ENTRY`, o sistema ainda não sabe em qual setor o veículo vai estacionar. Por isso, o cálculo do `occupancy_modifier` usa a ocupação **global** (soma de todos os setores), não a ocupação por setor.

O modificador é travado em `parking_session.occupancy_modifier` e nunca recalculado.

---

## Strategy Pattern para precificação

`PricingStrategy` é uma interface com um único método `modifier()`. Cada faixa de preço é uma classe separada. `PricingStrategyResolver` seleciona a implementação correta com base na taxa de ocupação.

**Por que isso importa:** adicionar ou alterar uma faixa de preço requer criar uma nova classe e modificar apenas o resolver — o `WebhookService` não precisa saber nada sobre as regras de preço.

---

## Locking pessimista em `Sector` e `Spot`

`SELECT ... FOR UPDATE` é usado ao ler `Sector` (para atualizar `current_occupancy`) e `Spot` (para marcar como ocupado). Isso previne que duas transações concorrentes passem pela checagem de capacidade ao mesmo tempo e causem overbooking.

---

## `BigDecimal` para todos os valores monetários

`double` e `float` usam ponto flutuante binário e não conseguem representar todas as frações decimais com exatidão. `BigDecimal` com escala explícita e arredondamento `HALF_UP` é usado em todos os campos monetários para evitar erros de cobrança.

---

## `Instant` (UTC) para timestamps

Todos os timestamps são armazenados e processados como `Instant` em UTC. O simulador envia strings ISO-8601 UTC. Usar `LocalDateTime` sem timezone causaria cálculos incorretos de duração se a JVM estiver em um timezone diferente de UTC.

---

## Records para DTOs

Java records são usados para todos os DTOs. São imutáveis por padrão, não exigem boilerplate e deixam clara a intenção: objetos de transferência de dados, não objetos de domínio.

---

## `ProblemDetail` para respostas de erro

`ProblemDetail` do Spring 6 (RFC 7807) é usado no `GlobalExceptionHandler`. Provê um formato padronizado de erro sem exigir um DTO customizado.

---

## `current_occupancy` como contador no `Sector`

A ocupação poderia ser derivada contando sessões `PARKED` por setor a cada `ENTRY`. Porém, isso geraria uma query de agregação em toda entrada de veículo.

Manter `current_occupancy` como contador na entidade `Sector` — incrementado/decrementado dentro da mesma transação que altera `spot.occupied` — evita essa query e mantém consistência garantida pelo lock pessimista.

---

## `ddl-auto=update`

Escolha adequada para o contexto de avaliação técnica. Em produção, substituir por Flyway ou Liquibase para migrações controladas e rastreáveis.

---

## Idempotência no `GarageInitService`

O `GarageInitService` usa upsert (busca existente → atualiza ou cria) em vez de insert puro. Se o simulador for reiniciado com dados diferentes e a aplicação subir novamente, o init não falhará com violação de constraint.

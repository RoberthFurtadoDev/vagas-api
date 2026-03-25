# vagas-api

Backend para gerenciamento de estacionamento desenvolvido como desafio técnico para a Estapar.

Recebe eventos de veículos via Webhook, aplica regras de preço dinâmico por lotação e expõe Endpoint de faturamento por setor.

---

## Tecnologias

| Item | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 3.2.5 |
| MySQL | 8 |
| Docker | qualquer versão recente |

---

## Quick Start

### 1. Pré-requisitos

- Java 21 instalado
- Docker instalado e em execução
- Maven 3.9+ (ou use o Maven embutido do IntelliJ)

### 2. Subir o MySQL via Docker

> O comando com `\` (barra invertida para quebra de linha) funciona apenas no Linux/Mac.
> No Windows (PowerShell) use o comando em uma única linha, sem `\`.

**Linux/Mac:**
```bash
docker run -d \
  --name vagas-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=vagas_db \
  -p 3306:3306 \
  mysql:8
```

**Windows (PowerShell):**
```powershell
docker run -d --name vagas-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=vagas_db -p 3306:3306 mysql:8
```

Aguarde 15 segundos e valide:

**Linux/Mac:**
```bash
docker exec -it vagas-mysql mysql -uroot -proot -e "SHOW DATABASES;"
```

**Windows (PowerShell):**
```powershell
docker exec -it vagas-mysql mysql -uroot -proot -e "SHOW DATABASES;"
```

> O aviso `[Warning] Using a password on the command line interface can be insecure.` é esperado e inofensivo. O MySQL exibe isso sempre que a senha é passada via `-p` na linha de comando.

### 3. Subir o simulador da Estapar

> `--network="host"` é uma funcionalidade exclusiva do Linux. No Windows, use o comando com mapeamento de porta abaixo.

**Linux/Mac:**
```bash
docker run -d --network="host" cfontes0estapar/garage-sim:1.0.0
```

**Windows (PowerShell):**
```powershell
docker run -d -p 8080:3000 --add-host=host.docker.internal:host-gateway --name vagas-simulador cfontes0estapar/garage-sim:1.0.0
```

> O `--add-host=host.docker.internal:host-gateway` permite que o simulador (dentro do container) alcance a aplicacao rodando na sua maquina Windows na porta 3003 para enviar os eventos de webhook. Sem isso, o container nao consegue se comunicar com `localhost` da maquina host.

Confirme que está respondendo:

**Linux/Mac:**
```bash
curl http://localhost:8080/garage
```

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Method GET -Uri "http://localhost:8080/garage"
```

> Se a porta for diferente de 8080, ajuste `simulator.base-url` no `application.properties`.

### 4. Configurar a aplicação

Edite `src/main/resources/application.properties` se necessário:

```properties
simulator.base-url=http://localhost:8080
spring.datasource.url=jdbc:mysql://localhost:3306/vagas_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
```

### 5. Rodar a aplicação

**Linux/Mac:**
```bash
mvn spring-boot:run
```

**Windows — via IntelliJ (recomendado):**
Abra `VagasApiApplication.java` e clique no botão verde de play.

**Windows — via terminal do IntelliJ (ALT+F12):**
```powershell
mvn spring-boot:run
```

A aplicação inicia na porta **3003** e ao subir carrega automaticamente setores e vagas do simulador.

### 6. Verificar inicialização

**Linux/Mac:**
```bash
curl http://localhost:3003/actuator/health
```

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Method GET -Uri "http://localhost:3003/actuator/health"
```

Resposta esperada: `{"status":"UP"}`

Swagger UI: `http://localhost:3003/swagger-ui.html`

---

## Testes Unitários

### Rodando no IntelliJ (recomendado para Windows)

1. No painel esquerdo, clique com o botão direito na pasta `src/test/java`
2. Selecione `Run 'All Tests'`

**Resultado esperado:**
```
Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

**Linux/Mac via terminal:**
```bash
mvn test
```

**Windows via terminal do IntelliJ (ALT+F12):**
```powershell
mvn test
```

> Durante os testes unitários aparecem avisos em vermelho como:
> `WARNING: A Java agent has been loaded dynamically (byte-buddy-agent.jar)`
>
> Esses avisos são **inofensivos**. São emitidos pelo Java 21 sobre o `byte-buddy`, biblioteca interna do Mockito. Não afetam os testes nem a aplicação.

---

## Limpar dados para refazer os testes

Antes de iniciar uma nova rodada de testes, limpe as sessões do banco:

```powershell
docker exec -it vagas-mysql mysql -uroot -proot vagas_db -e "DELETE FROM parking_session;"
```

Para verificar se limpou:

```powershell
docker exec -it vagas-mysql mysql -uroot -proot vagas_db -e "SELECT * FROM parking_session;"
```

Resultado esperado: tabela vazia (zero linhas).

---

## Testes via Swagger UI

Acesse: `http://localhost:3003/swagger-ui.html`

### POST /webhook — ENTRY

1. Clique em `POST /webhook` -> `Try it out`
2. Cole no campo Request body:
```json
{
  "license_plate": "ZUL0001",
  "entry_time": "2025-01-01T12:00:00.000Z",
  "event_type": "ENTRY"
}
```
3. Clique `Execute`
4. Esperado: `Code 200`

### POST /webhook — PARKED

1. Clique em `POST /webhook` -> `Try it out`
2. Cole no campo Request body:
```json
{
  "license_plate": "ZUL0001",
  "lat": -23.561684,
  "lng": -46.655981,
  "event_type": "PARKED"
}
```
3. Clique `Execute`
4. Esperado: `Code 200`

### POST /webhook — EXIT

1. Clique em `POST /webhook` -> `Try it out`
2. Cole no campo Request body:
```json
{
  "license_plate": "ZUL0001",
  "exit_time": "2025-01-01T13:31:00.000Z",
  "event_type": "EXIT"
}
```
3. Clique `Execute`
4. Esperado: `Code 200`

### GET /revenue

1. Clique em `GET /revenue` -> `Try it out`
2. Preencha os campos:
   - `date`: `2025-01-01`
   - `sector`: `A`
3. Clique `Execute`
4. Esperado: `200` com `amount`, `currency` e `timestamp`

---

## Testes via Postman

### Header obrigatorio em todas as requisicoes POST

| Key | Value |
|---|---|
| `Content-Type` | `application/json` |

### POST /webhook — ENTRY

- Metodo: `POST`
- URL: `http://localhost:3003/webhook`
- Body -> raw -> JSON:
```json
{
  "license_plate": "ZUL0001",
  "entry_time": "2025-01-01T12:00:00.000Z",
  "event_type": "ENTRY"
}
```
- Esperado: `200 OK`

### POST /webhook — PARKED

- Metodo: `POST`
- URL: `http://localhost:3003/webhook`
- Body -> raw -> JSON:
```json
{
  "license_plate": "ZUL0001",
  "lat": -23.561684,
  "lng": -46.655981,
  "event_type": "PARKED"
}
```
- Esperado: `200 OK`

### POST /webhook — EXIT

- Metodo: `POST`
- URL: `http://localhost:3003/webhook`
- Body -> raw -> JSON:
```json
{
  "license_plate": "ZUL0001",
  "exit_time": "2025-01-01T13:31:00.000Z",
  "event_type": "EXIT"
}
```
- Esperado: `200 OK`

### GET /revenue

- Metodo: `GET`
- URL: `http://localhost:3003/revenue?date=2025-01-01&sector=A`
- Sem body
- Esperado:
```json
{
  "amount": 72.90,
  "currency": "BRL",
  "timestamp": "2026-03-18T..."
}
```

---

## Testes de API via Terminal

> **Atencao:** No Windows (PowerShell), use sempre `Invoke-RestMethod`.
> Nunca use `\` para quebrar linhas no PowerShell.

---

### TESTE 1 — Verificar saude da aplicacao

**Linux/Mac:**
```bash
curl http://localhost:3003/actuator/health
```

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Method GET -Uri "http://localhost:3003/actuator/health"
```

Esperado: `{"status":"UP"}`

---

### TESTE 2 — Verificar banco de dados populado

```powershell
docker exec -it vagas-mysql mysql -uroot -proot vagas_db -e "SELECT * FROM sector;"
```

```powershell
docker exec -it vagas-mysql mysql -uroot -proot vagas_db -e "SELECT COUNT(*) as total_spots FROM spot;"
```

Esperado: tabelas `sector` e `spot` com dados vindos do simulador.

---

### TESTE 3 — ENTRY (entrada de veículo)

**Linux/Mac:**
```bash
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:3003/webhook \
  -H 'Content-Type: application/json' \
  -d '{"license_plate":"ZUL0001","entry_time":"2025-01-01T12:00:00.000Z","event_type":"ENTRY"}'
```

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:3003/webhook" -ContentType "application/json" -Body '{"license_plate":"ZUL0001","entry_time":"2025-01-01T12:00:00.000Z","event_type":"ENTRY"}'
```

Esperado: `HTTP 200` sem body.

**Validar no banco:**
```powershell
docker exec -it vagas-mysql mysql -uroot -proot vagas_db -e "SELECT license_plate, status, occupancy_modifier FROM parking_session ORDER BY id DESC LIMIT 5;"
```

---

### TESTE 4 — PARKED (veículo estacionou)

**Linux/Mac:**
```bash
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:3003/webhook \
  -H 'Content-Type: application/json' \
  -d '{"license_plate":"ZUL0001","lat":-23.561684,"lng":-46.655981,"event_type":"PARKED"}'
```

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:3003/webhook" -ContentType "application/json" -Body '{"license_plate":"ZUL0001","lat":-23.561684,"lng":-46.655981,"event_type":"PARKED"}'
```

Esperado: `HTTP 200` sem body.

**Validar no banco:**
```powershell
docker exec -it vagas-mysql mysql -uroot -proot vagas_db -e "SELECT license_plate, status, sector_name, price_per_hour FROM parking_session ORDER BY id DESC LIMIT 5;"
```

---

### TESTE 5 — EXIT com cobrança (91 minutos = 2 horas)

**Linux/Mac:**
```bash
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:3003/webhook \
  -H 'Content-Type: application/json' \
  -d '{"license_plate":"ZUL0001","exit_time":"2025-01-01T13:31:00.000Z","event_type":"EXIT"}'
```

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:3003/webhook" -ContentType "application/json" -Body '{"license_plate":"ZUL0001","exit_time":"2025-01-01T13:31:00.000Z","event_type":"EXIT"}'
```

Esperado: `HTTP 200` sem body.

**Validar no banco:**
```powershell
docker exec -it vagas-mysql mysql -uroot -proot vagas_db -e "SELECT license_plate, status, amount FROM parking_session ORDER BY id DESC LIMIT 5;"
```

A coluna `amount` deve estar preenchida com `status = EXITED`.

> O valor exato depende do `base_price` real do simulador e do `occupancy_modifier` aplicado no ENTRY.
> Exemplo real com o simulador atual: `base_price = 40.50`, `modifier = 0.90` → `pricePerHour = 36.45` → 91 min = 2h → `amount = 72.90`.

---

### TESTE 6 — Período de cortesia (30 minutos = grátis)

**Linux/Mac:**
```bash
curl -s -X POST http://localhost:3003/webhook \
  -H 'Content-Type: application/json' \
  -d '{"license_plate":"ZUL0002","entry_time":"2025-01-01T12:00:00.000Z","event_type":"ENTRY"}'

curl -s -X POST http://localhost:3003/webhook \
  -H 'Content-Type: application/json' \
  -d '{"license_plate":"ZUL0002","lat":-23.561684,"lng":-46.655981,"event_type":"PARKED"}'

curl -s -X POST http://localhost:3003/webhook \
  -H 'Content-Type: application/json' \
  -d '{"license_plate":"ZUL0002","exit_time":"2025-01-01T12:30:00.000Z","event_type":"EXIT"}'
```

**Windows (PowerShell) — rode os três em sequência:**
```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:3003/webhook" -ContentType "application/json" -Body '{"license_plate":"ZUL0002","entry_time":"2025-01-01T12:00:00.000Z","event_type":"ENTRY"}'
```
```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:3003/webhook" -ContentType "application/json" -Body '{"license_plate":"ZUL0002","lat":-23.561684,"lng":-46.655981,"event_type":"PARKED"}'
```
```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:3003/webhook" -ContentType "application/json" -Body '{"license_plate":"ZUL0002","exit_time":"2025-01-01T12:30:00.000Z","event_type":"EXIT"}'
```

**Validar:**
```powershell
docker exec -it vagas-mysql mysql -uroot -proot vagas_db -e "SELECT license_plate, amount FROM parking_session WHERE license_plate='ZUL0002';"
```

`amount` deve ser `0.00`.

> Se o banco mostrar `amount = NULL`, significa que o evento `EXIT` ainda não foi executado para essa placa. `NULL` é o estado correto enquanto o veículo está estacionado — o valor só é preenchido no EXIT.

---

### TESTE 7 — Erro esperado: placa sem sessão ativa (HTTP 404)

> No PowerShell, respostas 4xx/5xx lançam exceção. Use `try/catch` para capturar e exibir o status de forma legível.

**Linux/Mac:**
```bash
curl -s -w "\nHTTP Status: %{http_code}\n" -X POST http://localhost:3003/webhook \
  -H 'Content-Type: application/json' \
  -d '{"license_plate":"XXX9999","exit_time":"2025-01-01T13:00:00.000Z","event_type":"EXIT"}'
```

**Windows (PowerShell):**
```powershell
try {
    Invoke-RestMethod -Method POST -Uri "http://localhost:3003/webhook" -ContentType "application/json" -Body '{"license_plate":"XXX9999","exit_time":"2025-01-01T13:00:00.000Z","event_type":"EXIT"}'
} catch {
    Write-Host "Status retornado (esperado 404):" $_.Exception.Response.StatusCode.value__
    Write-Host $_.Exception.Message
}
```

Esperado: `404` — `No active session found for plate: XXX9999`.

---

### TESTE 8 — GET /revenue

O endpoint usa **query parameters na URL**. Não usa body. Execute cada linha individualmente no PowerShell.

**Windows (PowerShell — setor A):**
```powershell
Invoke-RestMethod -Method GET -Uri "http://localhost:3003/revenue?date=2025-01-01&sector=A"
```

**Windows (PowerShell — setor B):**
```powershell
Invoke-RestMethod -Method GET -Uri "http://localhost:3003/revenue?date=2025-01-01&sector=B"
```

**Linux/Mac:**
```bash
curl -s "http://localhost:3003/revenue?date=2025-01-01&sector=A"
```

**Postman:**
- Método: `GET`
- URL: `http://localhost:3003/revenue?date=2025-01-01&sector=A`
- Sem body necessário

**Swagger UI:**
1. Acesse `http://localhost:3003/swagger-ui.html`
2. Clique em `GET /revenue` → `Try it out`
3. Preencha `date: 2025-01-01` e `sector: A` nos campos exibidos
4. Clique `Execute`

**Resposta esperada:**
```json
{
  "amount": 72.90,
  "currency": "BRL",
  "timestamp": "2026-03-18T16:06:50.820Z"
}
```

> O valor de `amount` reflete a receita real acumulada do simulador no dia e setor consultados. Não é fixo — qualquer número é correto desde que retorne `200 OK` com `currency: BRL`.

> O erro `Required request parameter 'date' is not present` significa que a requisição chegou sem os parâmetros na URL. Nunca use body ou `$body` com `curl.exe` para este endpoint — o controller usa `@RequestParam` e só lê parâmetros da URL.

---

## Documentação completa

| Arquivo | Conteúdo |
|---|---|
| [docs/architecture.md](docs/architecture.md) | Arquitetura em camadas e diagrama |
| [docs/flow.md](docs/flow.md) | Fluxo dos eventos ENTRY, PARKED, EXIT |
| [docs/business-rules.md](docs/business-rules.md) | Regras de negócio, preço dinâmico e cálculo de horas |
| [docs/api.md](docs/api.md) | Referência completa da API com exemplos |
| [docs/database.md](docs/database.md) | Modelo de dados, schema e índices |
| [docs/tests.md](docs/tests.md) | Cobertura de testes unitários |
| [docs/decisions.md](docs/decisions.md) | Decisões de design e trade-offs |
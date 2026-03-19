# Referência da API

Base URL: `http://localhost:3003`

Swagger UI interativo: `http://localhost:3003/swagger-ui.html`

---

## POST /webhook

Recebe eventos de veículos enviados pelo simulador.

### Configuração no Postman / Insomnia

1. Método: **POST**
2. URL: `http://localhost:3003/webhook`
3. Aba **Headers**: adicione `Content-Type: application/json`
4. Aba **Body**: selecione `raw` → `JSON`
5. Cole o payload desejado abaixo

---

### Evento ENTRY

**Payload:**
```json
{
  "license_plate": "ZUL0001",
  "entry_time": "2025-01-01T12:00:00.000Z",
  "event_type": "ENTRY"
}
```

**Linux/Mac:**
```bash
curl -s -X POST http://localhost:3003/webhook \
  -H 'Content-Type: application/json' \
  -d '{"license_plate":"ZUL0001","entry_time":"2025-01-01T12:00:00.000Z","event_type":"ENTRY"}'
```

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:3003/webhook" -ContentType "application/json" -Body '{"license_plate":"ZUL0001","entry_time":"2025-01-01T12:00:00.000Z","event_type":"ENTRY"}'
```

**Resposta esperada:** `HTTP 200` sem body

---

### Evento PARKED

**Payload:**
```json
{
  "license_plate": "ZUL0001",
  "lat": -23.561684,
  "lng": -46.655981,
  "event_type": "PARKED"
}
```

**Linux/Mac:**
```bash
curl -s -X POST http://localhost:3003/webhook \
  -H 'Content-Type: application/json' \
  -d '{"license_plate":"ZUL0001","lat":-23.561684,"lng":-46.655981,"event_type":"PARKED"}'
```

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:3003/webhook" -ContentType "application/json" -Body '{"license_plate":"ZUL0001","lat":-23.561684,"lng":-46.655981,"event_type":"PARKED"}'
```

**Resposta esperada:** `HTTP 200` sem body

---

### Evento EXIT

**Payload:**
```json
{
  "license_plate": "ZUL0001",
  "exit_time": "2025-01-01T13:31:00.000Z",
  "event_type": "EXIT"
}
```

**Linux/Mac:**
```bash
curl -s -X POST http://localhost:3003/webhook \
  -H 'Content-Type: application/json' \
  -d '{"license_plate":"ZUL0001","exit_time":"2025-01-01T13:31:00.000Z","event_type":"EXIT"}'
```

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:3003/webhook" -ContentType "application/json" -Body '{"license_plate":"ZUL0001","exit_time":"2025-01-01T13:31:00.000Z","event_type":"EXIT"}'
```

**Resposta esperada:** `HTTP 200` sem body

---

## GET /revenue

Retorna o faturamento total de um setor em uma data específica via **query parameters**.

**Linux/Mac:**
```bash
curl -s "http://localhost:3003/revenue?date=2025-01-01&sector=A"
```

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Method GET -Uri "http://localhost:3003/revenue?date=2025-01-01&sector=A"
```

**Postman:**
- Método: `GET`
- URL: `http://localhost:3003/revenue?date=2025-01-01&sector=A`
- Sem body necessário

**Swagger UI:**
1. Acesse `http://localhost:3003/swagger-ui.html`
2. Clique em `GET /revenue` → `Try it out`
3. Preencha `date: 2025-01-01` e `sector: A`
4. Clique `Execute`

**Parâmetros:**

| Parâmetro | Tipo | Formato | Exemplo |
|---|---|---|---|
| `date` | query | `YYYY-MM-DD` | `2025-01-01` |
| `sector` | query | string | `A` |

**Resposta esperada:**
```json
{
  "amount": 72.90,
  "currency": "BRL",
  "timestamp": "2026-03-18T14:00:49.988Z"
}
```

> O valor de `amount` reflete a receita real acumulada do simulador no dia e setor consultados. Não é um valor fixo.

---

## GET /actuator/health

**Linux/Mac:**
```bash
curl http://localhost:3003/actuator/health
```

**Windows (PowerShell):**
```powershell
Invoke-RestMethod -Method GET -Uri "http://localhost:3003/actuator/health"
```

**Resposta esperada:** `{"status":"UP"}`

---

## Tabela de códigos HTTP

| Status | Significado |
|---|---|
| 200 | Requisição processada com sucesso |
| 400 | Payload inválido ou parâmetro ausente |
| 404 | Sessão ativa não encontrada para a placa informada |
| 409 | Garagem cheia ou sessão duplicada |
| 500 | Erro inesperado no servidor |

> No PowerShell, respostas 4xx/5xx lançam exceção. Use `try/catch` para capturar:
> ```powershell
> try {
>     Invoke-RestMethod -Method POST -Uri "http://localhost:3003/webhook" -ContentType "application/json" -Body '{"license_plate":"XXX","exit_time":"2025-01-01T13:00:00.000Z","event_type":"EXIT"}'
> } catch {
>     Write-Host "HTTP Status:" $_.Exception.Response.StatusCode.value__
> }
> ```

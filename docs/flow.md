# Fluxo de Eventos

O simulador envia três tipos de evento em sequência para cada veículo: `ENTRY` → `PARKED` → `EXIT`.

---

## Diagrama de Sequência Completo

```mermaid
sequenceDiagram
    participant Sim as Simulador
    participant WC as WebhookController
    participant WS as WebhookService
    participant PSR as PricingStrategyResolver
    participant DB as MySQL

    Note over Sim,DB: Evento ENTRY

    Sim->>WC: POST /webhook event_type ENTRY
    WC->>WS: handleEntry(request)
    WS->>DB: existsByLicensePlateAndStatusNot
    DB-->>WS: false - sem sessao ativa
    WS->>DB: sumTotalOccupancy e sumTotalCapacity
    DB-->>WS: 30 de 100 = 30%
    WS->>PSR: resolve(30, 100)
    PSR-->>WS: StandardPricingStrategy modifier 1.00
    WS->>DB: INSERT parking_session status ENTERED
    WC-->>Sim: HTTP 200

    Note over Sim,DB: Evento PARKED

    Sim->>WC: POST /webhook event_type PARKED lat lng
    WC->>WS: handleParked(request)
    WS->>DB: findByLicensePlateAndStatus ENTERED
    DB-->>WS: ParkingSession encontrada
    WS->>DB: findByLatAndLng FOR UPDATE
    DB-->>WS: Spot encontrado
    WS->>DB: findSector FOR UPDATE
    DB-->>WS: Sector encontrado
    WS->>WS: pricePerHour = basePrice x modifier
    WS->>DB: UPDATE spot.occupied = true
    WS->>DB: INCREMENT sector.current_occupancy
    WS->>DB: UPDATE session status PARKED pricePerHour
    WC-->>Sim: HTTP 200

    Note over Sim,DB: Evento EXIT

    Sim->>WC: POST /webhook event_type EXIT
    WC->>WS: handleExit(request)
    WS->>DB: findByLicensePlateAndStatus PARKED
    DB-->>WS: ParkingSession encontrada
    WS->>DB: findSector FOR UPDATE
    WS->>WS: calculateAmount entryTime exitTime pricePerHour
    WS->>DB: UPDATE spot.occupied = false
    WS->>DB: DECREMENT sector.current_occupancy
    WS->>DB: UPDATE session status EXITED amount
    WC-->>Sim: HTTP 200
```

---

## Caminhos de Erro

| Situação | Exceção | HTTP |
|---|---|---|
| Garagem 100% cheia no `ENTRY` | `GarageFullException` | 409 |
| Placa já tem sessão ativa no `ENTRY` | `ActiveSessionAlreadyExistsException` | 409 |
| Placa não encontrada com status `ENTERED` no `PARKED` | `VehicleNotFoundException` | 404 |
| Coordenadas não correspondem a nenhuma vaga | `SpotNotFoundException` | 404 |
| Placa não encontrada com status `PARKED` no `EXIT` | `VehicleNotFoundException` | 404 |
| Payload inválido (campo obrigatório ausente) | `MethodArgumentNotValidException` | 400 |

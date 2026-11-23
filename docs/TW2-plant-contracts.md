# Ecoembes TW2 Plant API Contracts

## Shared Java interface

The Central Server uses `PlantGateway` for both plants:

- `PlantCapacity checkCapacity(LocalDate date)`
- `AssignmentReceipt registerAssignment(AssignmentNotice notice)`

| Java type | Fields in order |
|---|---|
| `PlantCapacity` | `LocalDate date`, `BigDecimal availableCapacityTons` |
| `AssignmentNotice` | `String assignmentId`, `LocalDate date`, `int totalDumpsters`, `int totalPackagingContainers`, `BigDecimal requiredTons` |
| `AssignmentReceipt` | `String assignmentId`, `boolean accepted` |

Dates use `YYYY-MM-DD`. Counts are integers. Weights and capacities are decimal values in tonnes. The Central Server uses `0.00005` tonnes per packaging item for the TW2 demo.

## PlasSB (PL-01) — HTTP

Default address: `http://localhost:8082`.

| Request | Success response | Error responses |
|---|---|---|
| `GET /capacity?date=YYYY-MM-DD` | `200` with `date` and `availableCapacityTons` | `400` for an invalid date. `404` when the plant has no data for that date. |
| `POST /assignments` with an `AssignmentNotice` JSON body | `201` for a new assignment. `200` if the same ID and data were already accepted. The body contains `assignmentId` and `accepted`. | `400` for invalid data. `404` for a date with no data. `409` for insufficient capacity or when the same ID is reused with different data. |

Example `POST /assignments` body:

```json
{"assignmentId":"AS-DEMO-01","date":"2026-11-23","totalDumpsters":2,"totalPackagingContainers":105,"requiredTons":0.00525}
```

Example accepted response:

```json
{"assignmentId":"AS-DEMO-01","accepted":true}
```

## ContSocket (PL-02) — TCP

Default address: `localhost:8081`. Each connection carries one request and one response. Each message is a single UTF-8 JSON line followed by a newline. The server flushes the response before closing the connection.

| Operation | Request fields | Success response fields |
|---|---|---|
| `CAP` | `op`, `date` | `ok: true`, `date`, `availableCapacityTons` |
| `ASSIGN` | `op` plus the five fields of `AssignmentNotice` | `ok: true`, `assignmentId`, `accepted: true` |

Example capacity request and response:

```json
{"op":"CAP","date":"2026-11-23"}
{"ok":true,"date":"2026-11-23","availableCapacityTons":30.00000}
```

Example assignment request and response:

```json
{"op":"ASSIGN","assignmentId":"AS-DEMO-02","date":"2026-11-23","totalDumpsters":2,"totalPackagingContainers":105,"requiredTons":0.00525}
{"ok":true,"assignmentId":"AS-DEMO-02","accepted":true}
```

On failure, return `ok: false` and a `code`: `INVALID_REQUEST`, `DATE_NOT_FOUND`, `INSUFFICIENT_CAPACITY` or `ASSIGNMENT_ID_CONFLICT`.

## Shared behaviour

- The plant checks and deducts capacity in the same operation.
- If the plant receives the same assignment ID and the same data again, it returns the previous result without deducting capacity again.
- If the ID is reused with different data, the plant rejects the request.
- The Central Server uses a configurable timeout of 3,000 ms. When the plant cannot be reached, the Central Server returns HTTP 503. It keeps the assignment ID if the request may already have reached the plant.
- The dates in the examples are illustrative. During the demo, use a date for which the plant has capacity data.
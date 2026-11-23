# Ecoembes
Software Design - Ecoembes

## TW2 setup

Use JDK 25. The Central Server is in `EcoembesServer`.

### Run the Central Server

From the repository root, open PowerShell and run:

```powershell
cd .\EcoembesServer
.\gradlew.bat test
.\gradlew.bat bootRun
```

The API runs at `http://localhost:8080`. Swagger UI is available at
`http://localhost:8080/swagger-ui/index.html`. To check that the server
is running, use the demo employee to send `POST /login` and check for
HTTP `200`.

### Recycling plant servers

The other two projects will be added during the plant implementation stages.

| Server | Project directory | Connection |
|---|---|---|
| PlasSB (PL-01) | `PlasSBServer` | HTTP on port `8082` |
| ContSocket (PL-02) | `ContSocketServer` | TCP on port `8081` |

See [TW2 Plant API Contracts](docs/TW2-plant-contracts.md) for the shared
Java interface, messages and error responses.

### Demo data

- Use the demo day's local date for the readings and assignment.
- Use postcode `48200`, with `75` packaging items in `D-201` and `30` in `D-202`.
- Seed `PL-01` with `25.5 t` and `PL-02` with `30.0 t` for the demo day and the following ten days.
- Assigning both dumpsters to `PL-01` reserves `0.00525 t`, leaving `25.49475 t`.
- If a previous demo is still stored after persistence is added, use fresh dumpster IDs and verify the plant's starting capacity before repeating the scenario.

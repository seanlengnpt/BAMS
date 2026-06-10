# BAMS

Banking Management System scaffold built with Java, Maven, and Spring Boot.

## Modules

- `bams-common`: shared constants, utilities, and cross-cutting contracts.
- `bams-domain`: domain model and domain services.
- `bams-infra`: infrastructure adapters such as persistence and external clients.
- `bams-app`: application orchestration layer.
- `bams-api`: API contracts and request/response types.
- `bams-adapter`: Spring Boot entrypoint and delivery adapters.
- `bams-test`: integration and end-to-end test module.

## Build

```bash
mvn clean test
```

The scaffold intentionally contains no business use-case implementation.

## Local Infra Deployment

Use [`scripts/local-stack`](/Users/NPTSG0492/Documents/BAMS/scripts/local-stack) to manage the local infra dependencies needed by BAMS:

- RocketMQ on Colima-backed Docker
- VictoriaLogs on `127.0.0.1:9428`
- Fluent Bit as a host process tailing `log/data.log`

This workflow is intentionally infra-only. It does not start BAMS or MySQL.

### Prerequisites

- `colima`
- `docker`
- `fluent-bit`
- `java`
- `mvn`
- A local MySQL instance for BAMS if you want to run the application

The script validates these tools with `bootstrap` and `check`. It does not install them for you.

### Commands

```bash
scripts/local-stack bootstrap
scripts/local-stack check
scripts/local-stack up
scripts/local-stack status
scripts/local-stack logs fluent-bit
scripts/local-stack logs victorialogs
scripts/local-stack logs app
scripts/local-stack logs app-error
scripts/local-stack down
```

`up` starts Colima if needed, creates a dedicated Docker network, starts VictoriaLogs, starts RocketMQ name server and broker, then starts Fluent Bit.
When the script starts Colima itself, it uses `4` GiB memory and `2` CPUs by default.

### Runtime Defaults

- RocketMQ name server: `localhost:9876`
- RocketMQ broker: `localhost:10911`
- VictoriaLogs:
  - `VICTORIALOGS_HOST=127.0.0.1`
  - `VICTORIALOGS_PORT=9428`
- Fluent Bit config: [`observability/fluent-bit/fluent-bit.conf`](/Users/NPTSG0492/Documents/BAMS/observability/fluent-bit/fluent-bit.conf)
- Local stack state and logs: `.local/local-stack/`
- Colima startup defaults used by the script:
  - `COLIMA_MEMORY_GB=4`
  - `COLIMA_CPU=2`

These defaults match the current BAMS app configuration in [`bams-adapter/src/main/resources/application.yml`](/Users/NPTSG0492/Documents/BAMS/bams-adapter/src/main/resources/application.yml), which already points RocketMQ to `localhost:9876`.
You can override the Colima sizing for a run, for example:

```bash
COLIMA_MEMORY_GB=6 COLIMA_CPU=4 scripts/local-stack up
```

### Running BAMS Against The Local Infra

BAMS still expects MySQL at `localhost:3306` with the credentials configured in [`bams-adapter/src/main/resources/application.yml`](/Users/NPTSG0492/Documents/BAMS/bams-adapter/src/main/resources/application.yml).

Start the infra first:

```bash
scripts/local-stack up
```

Then run the application separately, for example:

```bash
mvn -pl bams-adapter spring-boot:run
```

If `log/data.log` does not exist yet, Fluent Bit will still start and wait until BAMS begins writing logs.

While BAMS is starting, you can watch the main application log and error log in separate terminals:

```bash
scripts/local-stack logs app
scripts/local-stack logs app-error
```

If you also want infra-side visibility during startup, use:

```bash
scripts/local-stack logs namesrv
scripts/local-stack logs broker
scripts/local-stack logs fluent-bit
```

### Verifying Log Shipping

1. Start the infra with `scripts/local-stack up`.
2. Run BAMS and trigger a flow that produces an `AdminMessageConsumer` log line.
3. Watch Fluent Bit output with `scripts/local-stack logs fluent-bit`.
4. Check VictoriaLogs container output with `scripts/local-stack logs victorialogs`.

Fluent Bit reads from `log/data.log`, extracts `Consumed admin message...` events, and forwards them to VictoriaLogs at `/insert/jsonline`.

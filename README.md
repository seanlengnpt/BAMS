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

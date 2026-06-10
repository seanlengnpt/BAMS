# BAMS admin message log shipping

This Fluent Bit configuration tails BAMS application logs from `log/data.log`, extracts admin message consumption events, and forwards them to VictoriaLogs.

## Expected environment

- `VICTORIALOGS_HOST`: VictoriaLogs host name
- `VICTORIALOGS_PORT`: VictoriaLogs HTTP port

## Data flow

1. Tail `log/data.log`
2. Parse the Logback line format from `bams-adapter/src/main/resources/logback/logback-spring.xml`
3. Keep only `AdminMessageConsumer` records
4. Parse fields from:

```text
Consumed admin message. topic=..., msgId=..., adminId=..., eventId=...
```

5. Add stable metadata fields:
   `app=bams`, `source=fluent-bit`, `pipeline=admin-message`
6. Send JSON lines to VictoriaLogs at `/insert/jsonline`

## Run example

```bash
fluent-bit -c observability/fluent-bit/fluent-bit.conf
```

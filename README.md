# Legacy Remittance Processing Service

> **WARNING**: This is a legacy monolithic application. It is currently in maintenance mode.
> We are planning a migration to microservices (see JIRA epic REM-4896).

## Overview

This service handles bank remittance files, validates cash application lines, matches
open invoices, creates remittance advices and posts payment instructions for BigFake
Company. It intentionally remains a 2019-era monolith under REM-4896.

### File format and business rules

Inbound files are auto-detected: a first line containing `|` is delimited; all other
files use 120-character fixed-width H/D/T records.

Fixed-width detail fields use the legacy offsets below (the first character is the
record type):

| Positions (exclusive end) | Width | Field | Format |
| --- | ---: | --- | --- |
| `1..11` | 10 | Payer code | Trimmed text |
| `11..31` | 20 | Invoice reference | Trimmed text |
| `31..49` | 18 | Paid amount | Integer cents |
| `49..67` | 18 | Invoiced amount | Integer cents |
| `67..71` | 4 | Deduction code | Trimmed text |
| `71..79` | 8 | Remittance date | `yyyyMMdd` |
| `79..120` | 41 | Memo/reserved | Text |

Delimited detail records are:
`D|payer|invoice|paid|invoiced|deduction|yyyyMMdd|memo`.
Headers contain sender, date, control number, detail count and amount; trailers
repeat the count and amount. The header and trailer count and amount must exactly
equal the parsed detail lines or the whole file is rejected.

Accepted lines require an active payer, positive paid amount, non-negative invoiced
amount, a date no more than 90 days in the future, and an active deduction code for
short pays. Reject reasons are `CONTROL_TOTAL_MISMATCH`, `MALFORMED_STRUCTURE`,
`DUPLICATE_FILE`, `UNKNOWN_PAYER`, `INACTIVE_PAYER`, `INVALID_AMOUNT`,
`INVALID_DATE`, `MISSING_INVOICE_REFERENCE`, `MISSING_DEDUCTION_CODE`,
`INVALID_DEDUCTION_CODE`, and `MALFORMED_RECORD`.

Matching uses three tiers: normalized invoice reference; exact payer and outstanding
amount; then fuzzy amount matching. The fuzzy window is:
`max(minimum absolute tolerance, tolerance percentage * paid amount)`.
The defaults are configured by `remittance.matching.min-absolute-tolerance` and
`remittance.matching.tolerance-percent`. Exact matches mark the invoice paid.
Short pays mark the line `SHORT_PAID`, partially pay the invoice, and require a
deduction reason. Overpays mark the invoice paid and store the residual in
`unappliedAmount`. Unmatched lines enter the exception queue. Posting creates one
advice per payer/date and a `PENDING` payment instruction.

Files transition through `RECEIVED -> PARSING -> PARSED` or `REJECTED`.
Duplicate SHA-256 checksums create a rejected file linked to the original and return
HTTP 409. The inbound poller processes `.txt` and `.dat` files, moves successful
files to `processed-*`, moves failed files to `failed-*`, dispatches pending
instructions, and periodically sweeps stale exceptions.

## Tech Stack

- **Java 25** (LTS)
- **Spring Boot 3.5.x**
- **Hibernate / Spring Data JPA**
- **H2** (dev) / **PostgreSQL** (prod)
- **Flyway** for database migrations
- **Maven** build system

## Endpoints

- `POST /api/v1/remittance-files` multipart upload
- `GET /api/v1/remittance-files`, `/api/v1/remittance-files/{id}`, and `/{id}/rejects`
- `POST /api/v1/remittance-files/{id}/post`
- `GET /api/v1/advices`, `/api/v1/advices/{adviceNumber}`
- `GET /api/v1/exceptions`, `POST /api/v1/exceptions/{lineId}/match`
- `GET /api/v1/payers`, `/api/v1/payers/{payerCode}`, `POST /api/v1/payers`
- `POST /webhooks/bank-remittance` (open bank intake)

All `/api/**` endpoints require HTTP Basic. Local development defaults to
`admin` / `changeit`; override `remittance.security.username` and
`remittance.security.password`. The webhook, `/actuator/health`, Swagger endpoints,
API docs, and H2 console are open. Scheduling can be disabled in tests or local
experiments with `remittance.scheduling.enabled=false`.

## Known Tech Debt / upgrade blockers

- [x] `javax.*` imports migrated to `jakarta.*`.
- [x] `SecurityConfig` migrated to a `SecurityFilterChain` bean (REM-5580).
- [x] `springdoc-openapi-ui` 1.7.x replaced with the springdoc 2.x starter.
- [x] Hibernate 6: `@Type(type = "yes_no")` replaced with `YesNoConverter`.
- [ ] JUnit 4 Vintage compatibility is retained by `LegacyAmountUtilsTest`,
  `LegacyDateUtilsTest`, and `LegacyFixedWidthParserTest`.
- [x] `Dockerfile` moved to a Java 25 base image.
- [ ] `src/main/java/com/bigfake/remittance/util/DateUtils.java` and
  `src/main/java/com/bigfake/remittance/service/impl/RemittanceProcessingServiceImpl.java`
  use `SimpleDateFormat`, `java.util.Date`, and `Calendar`.
- [ ] `src/main/java/com/bigfake/remittance/client/CashApplicationClient.java` has no
  timeout, circuit breaker or proper retry (REM-5709).
- [ ] Hibernate naming assumptions remain undocumented in migrations (FIXME REM-5522)

## Building

```bash
mvn clean verify
```

## Running Locally

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=default
```

The service starts on port 8080.

## Deployment

Docker image built via:

```bash
docker build -t java-remittance .
docker run -p 8080:8080 java-remittance
```

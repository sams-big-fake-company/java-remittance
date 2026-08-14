# Legacy Remittance Processing Service

> **WARNING**: This is a legacy monolithic application. It is currently in maintenance mode.
> We are planning a migration to microservices (see JIRA epic REM-4896).

## Overview

This service handles bank remittance files, validates cash application lines, matches
open invoices, creates remittance advices and posts payment instructions for BigFake
Company. It intentionally remains a 2019-era monolith under REM-4896.

### File format and business rules

Inbound files are auto-detected: a first line containing `|` is delimited; all other
files use 120-character fixed-width H/D/T records. The header carries sender, date,
control number, detail count and amount in cents; the trailer repeats count and amount.
Both totals must exactly equal parsed detail lines or the entire file is rejected.
Delimited detail records are `D|payer|invoice|paid|invoiced|deduction|yyyyMMdd|memo`.
Accepted lines require an active payer, positive paid amount, non-negative invoiced
amount, valid date (no more than 90 days ahead), and an active deduction code for
short pays. Exact invoice reference, exact payer/amount, and tolerance-based amount
matching are attempted in that order. Unmatched lines enter the exception queue.
Short pays partially pay an invoice; overpays close the invoice and retain residual
unapplied cash. Posting creates one advice per payer/date and a pending instruction.

## Tech Stack

- **Java 11** (EOL - upgrade to 17 planned)
- **Spring Boot 2.7.x** (upgrade to 3.x blocked by Java 11 requirement)
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

`/api/**` uses HTTP Basic. Local development defaults to `admin` / `changeit`;
override `remittance.security.username` and `remittance.security.password`.
Health, Swagger, H2 console and webhook routes are open.

## Known Tech Debt / upgrade blockers

- [ ] `javax.*` imports throughout domain, DTO and servlet integrations must become `jakarta.*`
- [ ] `config/SecurityConfig.java` uses removed `WebSecurityConfigurerAdapter` and `antMatchers` (REM-5580)
- [ ] `springdoc-openapi-ui` 1.x must become springdoc 2.x starter
- [ ] Hibernate 5 dialect properties and `@Type(type = "yes_no")` need Hibernate 6 migration
- [ ] JUnit 4 tests rely on the Vintage engine
- [ ] Dockerfile remains on the Java 11 base image
- [ ] `util/DateUtils.java` and parser use mutable `Date`/`SimpleDateFormat`
- [ ] `client/CashApplicationClient.java` has no timeout, circuit breaker or proper retry (REM-5709)
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

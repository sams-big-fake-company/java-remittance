# Legacy Remittance Processing Service

> **WARNING**: This is a legacy monolithic application. It is currently in maintenance mode.
> We are planning a migration to microservices (see JIRA epic REM-4896).

## Overview

This service handles remittance processing for BigFake Company.

## Tech Stack

- **Java 11** (EOL - upgrade to 17 planned)
- **Spring Boot 2.7.x** (upgrade to 3.x blocked by Java 11 requirement)
- **Hibernate / Spring Data JPA**
- **H2** (dev) / **PostgreSQL** (prod)
- **Flyway** for database migrations
- **Maven** build system

## Known Tech Debt

- [ ] Application class is a placeholder until domain logic is migrated (REM-5701)
- [ ] Security config uses deprecated WebSecurityConfigurerAdapter (REM-5580)
- [ ] No circuit breaker for external service calls (REM-5709)
- [ ] Database migrations need audit columns (REM-5522)
- [ ] Missing integration tests (REM-5678)

## Building

```bash
mvn clean install
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

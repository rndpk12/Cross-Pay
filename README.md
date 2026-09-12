# Cross Pay

Cross Pay is a secure multi-currency wallet backend with ledger-derived balances, FX quotes, transfers, deposits, withdrawals, and transaction history.

[![CI](https://github.com/rndpk12/Cross-Pay/actions/workflows/ci.yml/badge.svg)](https://github.com/rndpk12/Cross-Pay/actions/workflows/ci.yml)

## CI

GitHub Actions runs on pull requests and pushes to `main`. It uses Java 21 and the Maven Wrapper, executes the complete unit and PostgreSQL 17 Testcontainers integration/concurrency suite, packages the application, and builds the production Docker image with the commit SHA as its tag. Images are validated only; they are not pushed to a registry.

Reproduce the important checks locally from `backend` with `./mvnw clean test`, `./mvnw package`, and `docker build -t crosspay-backend:ci-test .`.

## Docker development

Prerequisites: Docker and Docker Compose. Copy `.env.example` to `.env`, then replace `DB_PASSWORD` and `JWT_SECRET` with development values; `.env` is ignored and must not be committed.

Start the complete local stack from the repository root:

```sh
docker compose up --build
```

The backend is available at `http://localhost:8080`, PostgreSQL is available to the host at `localhost:5432`, Swagger is at `http://localhost:8080/swagger-ui.html`, and health is at `http://localhost:8080/actuator/health`. Set `BACKEND_PORT` only when port 8080 is already in use. Inside Compose, the backend reaches PostgreSQL at `postgres:5432`; Flyway applies the packaged migrations before the application serves requests.

Stop it with `docker compose down`. PostgreSQL development data persists in the `crosspay_postgres_data` named volume. Use `docker compose down -v` only when you intentionally want to delete that local database data.

## Render deployment foundation

This repository is ready for manual Docker deployment to Render; it has not been deployed by this project. Create a managed PostgreSQL database, then create a Render Web Service from this repository using the backend Dockerfile. Render terminates HTTPS at its proxy, forwards requests to the container, and the container connects to managed PostgreSQL:

`Client → HTTPS → Render Web Service → Cross Pay container → managed PostgreSQL`

Set `SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (32+ bytes), `JWT_EXPIRATION`, and an exact `CORS_ALLOWED_ORIGINS` frontend origin. Render normally provides `PORT` (default `10000`); `SERVER_PORT` remains supported as an explicit override. Configure `/actuator/health` as the health check. The production profile runs Flyway and keeps Hibernate at `validate`; health details and sensitive Actuator endpoints remain unavailable.

Render dashboard settings: create a **Web Service**, connect the `main` branch, choose the **Docker** runtime with root directory `backend`, leave Docker Command empty so the image `ENTRYPOINT` runs, select a region close to the managed database, enable auto-deploy only when desired, and set the health-check path to `/actuator/health`. Use the smallest instance only for demos; a single instance is not highly available. Do not configure build-time secrets: Render makes Docker-service variables available as build arguments, and this Dockerfile deliberately does not consume secret build arguments.

After deployment, check the service logs for Flyway startup, then verify `/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness`, `/swagger-ui.html`, `/v3/api-docs`, an unauthenticated protected endpoint (`401`), and an authenticated API flow. Use a dedicated, non-public managed database user with TLS when the provider supports it; never reuse local credentials.

Deployment checklist: create PostgreSQL; configure all variables; configure Docker and health check; deploy; verify Flyway, health probes, Swagger/OpenAPI, `401` protection, and authenticated access. Cloud logs retain safe request IDs and operation events, never credentials or request bodies.

## API documentation

When the backend is running locally, interactive documentation is available at [Swagger UI](http://localhost:8080/swagger-ui.html). The OpenAPI JSON document is available at [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs).

Register or log in through the public authentication endpoints, then use **Authorize** in Swagger UI with the returned JWT. Protected endpoints require `Authorization: Bearer <token>`.

Endpoint groups include Authentication, Users, Wallets, FX Rates/Quotes, Transfers, Deposits, Withdrawals, and Transactions. Deposits, withdrawals, and transfers also require an `Idempotency-Key` request header so safe retries cannot duplicate financial operations.

## Observability

- `GET /actuator/health` is public and intentionally returns only the overall status. It includes database health internally without exposing connection details. Liveness and readiness groups are available at `/actuator/health/liveness` and `/actuator/health/readiness` for future deployment health checks.
- `GET /actuator/metrics` is available only to authenticated callers. It includes standard HTTP, JVM, and Hikari connection-pool metrics plus safe Cross Pay operation counters such as `crosspay.transfer.success`. Sensitive management endpoints (including `env`, `configprops`, heap dumps, and thread dumps) are not exposed.
- Clients may send a safe, 1–64 character `X-Request-Id` (letters, numbers, `.`, `_`, and `-`). The service returns it on every response; absent or unsafe values are replaced with a generated UUID. Logs include this ID, HTTP metadata, and financial operation outcomes.
- Logs never contain passwords, password hashes, JWTs, Authorization headers, JWT/database secrets, idempotency keys, request/response bodies, or raw monetary amounts. Financial counters have no high-cardinality user, transaction, request, amount, or idempotency labels; the ledger remains the source of truth.

## Security

The API uses short-lived, HS256-signed JWT bearer tokens and Argon2 password hashes; it is stateless, so CSRF remains disabled because authentication is sent by an explicit bearer header rather than a browser session cookie. Authenticated operations derive identity from the JWT and use ownership-scoped resource lookups. Idempotency, database transactions, and pessimistic locks protect financial state transitions.

JWT and database secrets are environment-driven, the Docker runtime uses the non-root `crosspay` user, and only public health probes plus authenticated metrics are exposed through Actuator. Browser cross-origin access is denied by default; configure the comma-separated `CORS_ALLOWED_ORIGINS` environment variable for explicitly trusted frontend origins. Standard security headers include content-type protection, frame protection, cache controls, and a no-referrer policy.

Distributed login rate limiting, token revocation, and refresh-token support are intentionally deferred because they require a shared security-state design; no in-process limiter is presented as a multi-instance control.

# Ticket Booking Microservices

Technical README for a Spring Boot ticket-booking system split into three independently deployable services. The repository focuses on service boundaries, persistence ownership, gRPC contracts, and asynchronous ticket replication rather than marketing material.

## System overview

The application is organized around three Spring Boot services, each with its own Maven project and PostgreSQL database:

| Service | Purpose | HTTP port | gRPC port | Database port in local config | Main package |
| --- | --- | ---: | ---: | ---: | --- |
| `user-service` | Tenant/user domain and user lookup over gRPC. | `8080` | `9090` | `5432` | `com.booking.userservice` |
| `admin-service` | Venue layout, shows, events, ticket generation, and ticket publication. | `8081` | `9091` | `5433` | `com.booking.adminservice` |
| `booking-service` | Local ticket read model and booking creation. | `8082` | `9092` | `5434` | `com.booking.bookingservice` |

High-level data flow:

1. Admin creates venues, maps, sections, seats, shows, and events through REST endpoints.
2. Creating an event generates tickets for all seats in the selected map inside `admin-service`.
3. `admin-service` publishes generated ticket DTOs to RabbitMQ.
4. `booking-service` consumes those messages and stores a local copy of tickets in its own database.
5. Booking requests validate local ticket availability, create a booking, and mark selected tickets as booked.

An architecture image is available at [`arch.png`](arch.png).

## Repository layout

```text
.
├── admin-service/       # Spring Boot service for admin/catalog operations
├── booking-service/     # Spring Boot service for booking operations and ticket read model
├── user-service/        # Spring Boot service for tenants/users and gRPC user lookup
├── protos/              # Shared Protocol Buffer contracts compiled by each Maven module
├── docker-compose.yml   # PostgreSQL/RabbitMQ/application service composition
├── arch.png             # Architecture diagram
└── README.md
```

Each service is intentionally a separate Maven project with its own Maven wrapper (`mvnw`) and generated protobuf sources under `target/generated-sources` during build.

## Technology stack

| Area | Specification |
| --- | --- |
| Language/runtime | Java 21 |
| Framework | Spring Boot `3.3.3` |
| Build | Maven with per-service Maven wrappers |
| HTTP API | Spring MVC / `spring-boot-starter-web` |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL for runtime; H2 is present in test scope |
| Messaging | RabbitMQ via Spring AMQP |
| RPC | gRPC Java with `net.devh:grpc-spring-boot-starter:3.1.0.RELEASE` |
| Serialization contracts | Protocol Buffers `3.25.1`, gRPC Java `1.60.0` |
| Boilerplate reduction | Lombok `1.18.32` |
| Test framework dependencies | Spring Boot Test, H2, TestNG dependency present |

## Service details

### user-service

Responsibilities:

- Owns `Tenant` and `User` persistence.
- Exposes a tenant onboarding placeholder endpoint.
- Implements the `UserService` gRPC contract for user lookups.

REST endpoint:

| Method | Path | Request body | Current behavior |
| --- | --- | --- | --- |
| `POST` | `/api/v1/onboarding/tenant` | `{ "tenantName": "...", "adminUserName": "...", "adminEmail": "...", "adminPassword": "..." }` | Returns an acknowledgement string. Persistence is not implemented in this controller yet. |

gRPC endpoint:

| Service | Method | Request | Response |
| --- | --- | --- | --- |
| `user.UserService` | `GetUserDetails` | `GetUserDetailsRequest { common.UUID user_id }` | `UserDetailsResponse { user_id, tenant_id, user_name, email, role }` |

Primary entities:

- `Tenant`: `id`, `name`
- `User`: `id`, `tenant`, `name`, `email`, `passwordHash`, `role`

### admin-service

Responsibilities:

- Owns event administration data: venues, maps, sections, seats, shows, events, and canonical tickets.
- Generates tickets when an event is created.
- Publishes generated tickets to RabbitMQ for downstream replication.

REST endpoints:

| Method | Path | Request body | Result |
| --- | --- | --- | --- |
| `POST` | `/api/v1/admin/venues` | `{ "name": "Main Hall", "location": "City" }` | Creates a venue. |
| `POST` | `/api/v1/admin/venues/{venueId}/maps` | `{ "name": "Default Map" }` | Creates a map for a venue. |
| `POST` | `/api/v1/admin/venues/maps/{mapId}/sections` | `{ "name": "Orchestra" }` | Creates a section for a map. |
| `POST` | `/api/v1/admin/venues/sections/{sectionId}/seats` | `{ "row": "A", "number": "1" }` | Creates a seat for a section. |
| `POST` | `/api/v1/admin/shows` | `{ "title": "Show", "description": "...", "durationInMinutes": 120 }` | Creates a show. |
| `POST` | `/api/v1/admin/events` | `{ "showId": "...", "mapId": "...", "startTime": "2026-06-01T19:00:00Z", "endTime": "2026-06-01T21:00:00Z" }` | Creates an event, generates tickets, publishes ticket data. |

> Current implementation note: controllers use a hardcoded tenant UUID (`123e4567-e89b-12d3-a456-426614174000`) instead of authenticated tenant context.

Primary entities:

- `Venue`: `id`, `tenantId`, `name`, `location`
- `Map`: `id`, `venue`, `name`
- `Section`: `id`, `map`, `name`
- `Seat`: `id`, `section`, `rowIdentifier`, `seatNumber`
- `Show`: `id`, `tenantId`, `title`, `description`, `durationInMinutes`
- `Event`: `id`, `showId`, `mapId`, `tenantId`, `startTime`, `endTime`, `status`
- `Ticket`: `id`, `event`, `seat`, `price`, `status`

Ticket generation behavior:

- Triggered by `POST /api/v1/admin/events`.
- Loads all sections and seats for the selected map.
- Creates one `Ticket` per seat with default price `25.00` and status `AVAILABLE`.
- Publishes a list of ticket DTOs to RabbitMQ exchange `ticket-exchange` using routing key `tickets.new`.

### booking-service

Responsibilities:

- Maintains a local replicated ticket table for booking-time availability checks.
- Consumes ticket-generation messages from RabbitMQ.
- Creates bookings and marks selected tickets as booked.

REST endpoint:

| Method | Path | Request body | Result |
| --- | --- | --- | --- |
| `POST` | `/api/v1/bookings` | `{ "ticketIds": ["uuid-1", "uuid-2"] }` | Creates a confirmed booking for available tickets. |

> Current implementation note: the booking controller uses a hardcoded user UUID (`123e4567-e89b-12d3-a456-426614174000`) rather than authenticated user context.

Primary entities:

- `Booking`: `id`, `userId`, `bookingTime`, `totalAmount`, `status`
- `Ticket`: `id`, `eventId`, `seatInfo`, `price`, `status`, `booking`

Booking behavior:

- Fetches all requested tickets from the booking database.
- Rejects any ticket whose status is not `AVAILABLE`.
- Computes total amount from ticket prices.
- Creates a `Booking` with status `CONFIRMED`.
- Updates tickets to `BOOKED` and links them to the booking.

## Inter-service contracts

### Protocol Buffers

Shared `.proto` files live in `protos/` and are compiled by each Maven module via `protobuf-maven-plugin`.

| File | Package | Java package | Contents |
| --- | --- | --- | --- |
| `protos/common.proto` | `common` | `com.booking.common` | Shared `UUID` and `Empty` messages. |
| `protos/user.proto` | `user` | `com.booking.user` | `UserService.GetUserDetails`. |
| `protos/admin.proto` | `admin` | `com.booking.admin` | `AdminService.GetEventDetails` contract. |

### RabbitMQ topology

| Component | Value |
| --- | --- |
| Exchange | `ticket-exchange` |
| Queue | `ticket-queue` |
| Admin routing key | `tickets.new` |
| Booking binding pattern | `tickets.#` |
| Message converter | Jackson JSON message converter |
| Payload | List of ticket DTOs: `ticketId`, `eventId`, `seatInfo`, `price`, `status` |

## Configuration

Default local configuration is stored in each service's `src/main/resources/application.properties`.

| Service | Application name | HTTP port | gRPC port | JDBC URL |
| --- | --- | ---: | ---: | --- |
| `user-service` | `user-service` | `8080` | `9090` | `jdbc:postgresql://localhost:5432/postgres` |
| `admin-service` | `admin-service` | `8081` | `9091` | `jdbc:postgresql://localhost:5433/postgres` |
| `booking-service` | `booking-service` | `8082` | `9092` | `jdbc:postgresql://localhost:5434/postgres?sessionTimezone=UTC` |

Common defaults:

- PostgreSQL username: `postgres`
- PostgreSQL password: `password`
- Hibernate DDL mode: `update`
- SQL logging: enabled via `spring.jpa.show-sql=true`
- RabbitMQ host: `localhost` for local process execution; `rabbitmq` in Docker Compose environment overrides
- RabbitMQ credentials: `guest` / `guest`

## Local development

### Prerequisites

- JDK 21
- Docker and Docker Compose for PostgreSQL/RabbitMQ
- `curl` for REST calls
- Optional: `grpcurl` for gRPC calls

### Start infrastructure only

The checked-in `docker-compose.yml` also declares application services, but this repository currently does not include Dockerfiles for the three Spring Boot services. For local development, start only infrastructure containers and run the services from Maven:

```bash
docker compose up -d user-db admin-db booking-db rabbitmq
```

RabbitMQ management UI:

- URL: <http://localhost:15672>
- Username: `guest`
- Password: `guest`

### Build all services

From the repository root:

```bash
(cd user-service && bash ./mvnw clean test)
(cd admin-service && bash ./mvnw clean test)
(cd booking-service && bash ./mvnw clean test)
```

### Run services locally

Open one terminal per service:

```bash
cd user-service
bash ./mvnw spring-boot:run
```

```bash
cd admin-service
bash ./mvnw spring-boot:run
```

```bash
cd booking-service
bash ./mvnw spring-boot:run
```

Expected local ports:

- User HTTP: <http://localhost:8080>
- User gRPC: `localhost:9090`
- Admin HTTP: <http://localhost:8081>
- Admin gRPC: `localhost:9091`
- Booking HTTP: <http://localhost:8082>
- Booking gRPC port property: `9092`

## Example workflow

The IDs below are examples. Use IDs returned by your own API calls.

### 1. Create a venue

```bash
curl -X POST http://localhost:8081/api/v1/admin/venues \
  -H 'Content-Type: application/json' \
  -d '{"name":"Main Hall","location":"Downtown"}'
```

### 2. Create a venue map

```bash
curl -X POST http://localhost:8081/api/v1/admin/venues/{venueId}/maps \
  -H 'Content-Type: application/json' \
  -d '{"name":"Main Hall Default Map"}'
```

### 3. Create sections and seats

```bash
curl -X POST http://localhost:8081/api/v1/admin/venues/maps/{mapId}/sections \
  -H 'Content-Type: application/json' \
  -d '{"name":"Orchestra"}'
```

```bash
curl -X POST http://localhost:8081/api/v1/admin/venues/sections/{sectionId}/seats \
  -H 'Content-Type: application/json' \
  -d '{"row":"A","number":"1"}'
```

### 4. Create a show

```bash
curl -X POST http://localhost:8081/api/v1/admin/shows \
  -H 'Content-Type: application/json' \
  -d '{"title":"Hamlet","description":"Evening performance","durationInMinutes":150}'
```

### 5. Create an event and publish tickets

```bash
curl -X POST http://localhost:8081/api/v1/admin/events \
  -H 'Content-Type: application/json' \
  -d '{"showId":"{showId}","mapId":"{mapId}","startTime":"2026-06-01T19:00:00Z","endTime":"2026-06-01T21:30:00Z"}'
```

After this call, `admin-service` should persist generated tickets and publish them to RabbitMQ. `booking-service` should consume the message and replicate tickets into its own `tickets` table.

### 6. Create a booking

```bash
curl -X POST http://localhost:8082/api/v1/bookings \
  -H 'Content-Type: application/json' \
  -d '{"ticketIds":["{ticketId}"]}'
```

## Build and code generation notes

- Each service's `pom.xml` sets `protoSourceRoot` to `../protos`.
- Generated protobuf and gRPC Java sources are added to compilation from:
  - `target/generated-sources/protobuf/java`
  - `target/generated-sources/protobuf/grpc-java`
- Lombok annotation processing is configured through `maven-compiler-plugin`.
- There is no parent multi-module Maven build at the repository root; build each service separately.

## Current limitations and engineering notes

This repository is a functional service skeleton with some intentionally incomplete production concerns:

- No authentication/authorization layer is implemented.
- Tenant and user IDs are hardcoded in several controllers.
- `docker-compose.yml` references application builds, but service Dockerfiles are not currently present.
- Service-to-service gRPC clients are not wired into the booking/admin flows yet.
- The `AdminService` protobuf defines `GetEventDetails`, but no server implementation is present in `admin-service` yet.
- Error handling currently relies mostly on runtime exceptions and default Spring error responses.
- Database migrations are not managed with Flyway/Liquibase; Hibernate `ddl-auto=update` is used.
- Message delivery semantics are basic; there is no dead-letter queue, retry policy, outbox pattern, or idempotency guard.
- Booking validation does not currently verify that all requested ticket IDs were found before creating a booking.
- Docker Compose does not pin Postgres image versions; pin image tags before production use.

## Useful commands

```bash
# List REST controllers
rg -n "@RestController|@RequestMapping|@PostMapping" */src/main/java

# List RabbitMQ usage
rg -n "RabbitMQ|RabbitListener|convertAndSend|ticket-exchange|ticket-queue" */src/main/java

# List gRPC service implementations
rg -n "@GrpcService|extends .*ImplBase" */src/main/java

# Clean generated build output for every service
(cd user-service && bash ./mvnw clean)
(cd admin-service && bash ./mvnw clean)
(cd booking-service && bash ./mvnw clean)
```

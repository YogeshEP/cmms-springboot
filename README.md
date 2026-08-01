# CMMS - Computerized Maintenance Management System

A full-stack Spring Boot + PostgreSQL app for managing assets, work orders, maintenance schedules,
technicians, locations, and spare parts — with JWT login (Admin/Technician roles), a built-in
dashboard UI, and Docker/CI setup for DevOps practice.

## Tech Stack
- Java 21 (compiles fine on 17+ too)
- Spring Boot 3.3.2 (Web, Data JPA, Validation, Security)
- PostgreSQL
- JWT (jjwt)
- Lombok
- Maven
- Plain HTML/CSS/JS frontend (no build step, served by Spring Boot itself)
- Docker + Docker Compose, GitHub Actions CI

## Project Structure
```
src/main/java/com/cmms
 ├── entity          # JPA entities (Asset, WorkOrder, Technician, Location, MaintenanceSchedule, SparePart, User)
 ├── repository      # Spring Data JPA repositories
 ├── service         # Business logic
 ├── controller      # REST controllers (incl. AuthController)
 ├── security        # JWT util, filter, UserDetailsService
 ├── dto             # Auth request/response DTOs
 ├── enums           # Status/priority/type/frequency/role enums
 ├── exception       # Global exception handling
 └── config          # Security config, CORS, DataSeeder (random sample data + demo users)

src/main/resources/static  # Frontend - login page + dashboard, served automatically at "/"
database/schema.sql        # Reference SQL schema + sample seed data
Dockerfile                 # Multi-stage build (Maven -> slim JRE)
docker-compose.yml         # App + PostgreSQL, one command to run everything
.github/workflows/ci.yml   # Build/test pipeline against a real Postgres service container
requests.http              # IntelliJ HTTP Client file - click-to-run requests for every endpoint
```

## 1. Prerequisites
- JDK 17+
- Maven 3.8+
- PostgreSQL 13+ running locally (or Docker)

## 2. Create the database
```bash
psql -U postgres -c "CREATE DATABASE cmms_db;"
```
Tables are created automatically on startup (`spring.jpa.hibernate.ddl-auto=update`).
If you'd rather manage schema manually, run `database/schema.sql` yourself and set
`spring.jpa.hibernate.ddl-auto=validate` or `none`.

### Optional: run PostgreSQL with Docker
```bash
docker run --name cmms-postgres -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=cmms_db -p 5432:5432 -d postgres:16
```

## 3. Configure connection
Edit `src/main/resources/application.properties` if your DB credentials differ:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/cmms_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

## 4. Run the application
```bash
mvn spring-boot:run
```
Or build a jar and run it:
```bash
mvn clean package
java -jar target/cmms-system.jar
```
The API starts on `http://localhost:8080`.

## 5. Authentication & Roles

The API is secured with JWT. Two roles exist: `ADMIN` and `TECHNICIAN`.
- `ADMIN` can do everything, including `DELETE` operations.
- `TECHNICIAN` can view and create/update everything except delete.

On first startup, `DataSeeder` automatically creates sample data **and** two ready-to-use accounts:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `tech1` | `tech123` | TECHNICIAN |
| `tech2` | `tech123` | TECHNICIAN |

It also seeds random locations, technicians, assets, work orders, maintenance schedules, and spare parts so you can explore the full app immediately.

Seeding only runs once (it skips itself if any user already exists). To disable it entirely, set `cmms.seed.enabled=false` in `application.properties`.

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
Response:
```json
{ "token": "eyJhbGciOi...", "username": "admin", "role": "ADMIN" }
```

### Register a new user
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"newtech","password":"pass123","email":"newtech@cmms.com","role":"TECHNICIAN"}'
```

### Using the token
Every other `/api/**` endpoint requires the JWT in the `Authorization` header:
```bash
curl http://localhost:8080/api/assets \
  -H "Authorization: Bearer <token>"
```

### Get current user
```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <token>"
```

## 6. Frontend

A lightweight dashboard UI is included and served automatically by the Spring Boot app itself (static files under `src/main/resources/static`) — no separate build step, no Node.js required.

Once the app is running, open:
```
http://localhost:8080/
```
- Log in with `admin` / `admin123` (or `tech1` / `tech123`)
- Browse Assets, Work Orders, Maintenance Schedules, Technicians, Locations, Spare Parts
- Create, edit, and delete records (delete is ADMIN-only, matching the API)
- Low-stock parts and overdue-ish stats show on the Overview page

Since it's plain HTML/CSS/JS served as static resources, it ships inside the same jar/Docker image as the API — one deployable unit.

## 7. Running with Docker (recommended for DevOps practice)

This repo includes a `Dockerfile` (multi-stage Maven build → slim JRE image) and a `docker-compose.yml` that runs the app + PostgreSQL together.

```bash
# 1. Copy the example env file and adjust if you want
cp .env.example .env

# 2. Build and start everything
docker compose up --build

# App available at http://localhost:8080
# Postgres available at localhost:5432 (for inspection with a DB client)
```

To stop:
```bash
docker compose down
```

To also wipe the database volume (fresh reseed next start):
```bash
docker compose down -v
```

**What's in `docker-compose.yml`:**
- `postgres` — official Postgres 16 image, healthchecked, persisted via a named volume
- `app` — built from the local `Dockerfile`, waits for Postgres to be healthy before starting, configured entirely through environment variables (no editing `application.properties` needed)

**CI pipeline:** `.github/workflows/ci.yml` runs `mvn clean verify` against a real Postgres service container on every push/PR, then builds the Docker image — a ready starting point if you want to practice extending it (adding a push-to-registry step, deploy step, etc.).

## 8. API Endpoints

### Locations
| Method | Endpoint | Description |
|---|---|---|
| GET | /api/locations | List all locations |
| GET | /api/locations/{id} | Get one location |
| POST | /api/locations | Create location |
| PUT | /api/locations/{id} | Update location |
| DELETE | /api/locations/{id} | Delete location |

### Technicians
| Method | Endpoint | Description |
|---|---|---|
| GET | /api/technicians | List all technicians |
| GET | /api/technicians/{id} | Get one technician |
| POST | /api/technicians | Create technician |
| PUT | /api/technicians/{id} | Update technician |
| DELETE | /api/technicians/{id} | Delete technician |

### Assets
| Method | Endpoint | Description |
|---|---|---|
| GET | /api/assets | List all assets |
| GET | /api/assets/{id} | Get one asset |
| GET | /api/assets/status/{status} | Filter by status (OPERATIONAL, UNDER_MAINTENANCE, BREAKDOWN, RETIRED) |
| GET | /api/assets/location/{locationId} | Filter by location |
| POST | /api/assets | Create asset |
| PUT | /api/assets/{id} | Update asset |
| DELETE | /api/assets/{id} | Delete asset |

### Work Orders
| Method | Endpoint | Description |
|---|---|---|
| GET | /api/work-orders | List all work orders |
| GET | /api/work-orders/{id} | Get one work order |
| GET | /api/work-orders/status/{status} | Filter by status (OPEN, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED) |
| GET | /api/work-orders/priority/{priority} | Filter by priority (LOW, MEDIUM, HIGH, CRITICAL) |
| GET | /api/work-orders/asset/{assetId} | Filter by asset |
| GET | /api/work-orders/technician/{technicianId} | Filter by technician |
| POST | /api/work-orders | Create work order |
| PUT | /api/work-orders/{id} | Update work order |
| PATCH | /api/work-orders/{id}/status | Update status only, body: `{"status": "COMPLETED"}` |
| DELETE | /api/work-orders/{id} | Delete work order |

### Maintenance Schedules
| Method | Endpoint | Description |
|---|---|---|
| GET | /api/maintenance-schedules | List all schedules |
| GET | /api/maintenance-schedules/{id} | Get one schedule |
| GET | /api/maintenance-schedules/asset/{assetId} | Filter by asset |
| GET | /api/maintenance-schedules/due | Schedules due today or earlier |
| POST | /api/maintenance-schedules | Create schedule |
| PUT | /api/maintenance-schedules/{id} | Update schedule |
| DELETE | /api/maintenance-schedules/{id} | Delete schedule |

### Spare Parts
| Method | Endpoint | Description |
|---|---|---|
| GET | /api/spare-parts | List all spare parts |
| GET | /api/spare-parts/{id} | Get one spare part |
| GET | /api/spare-parts/low-stock | Parts at or below reorder level |
| POST | /api/spare-parts | Create spare part |
| PUT | /api/spare-parts/{id} | Update spare part |
| DELETE | /api/spare-parts/{id} | Delete spare part |

## 9. Sample requests

All examples below assume you've logged in and stored the token:
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)
```
(Windows PowerShell users can just copy the token value from the login response manually into `$TOKEN`.)

Create a location:
```bash
curl -X POST http://localhost:8080/api/locations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Plant A - Building 1","description":"Main manufacturing plant"}'
```

Create an asset:
```bash
curl -X POST http://localhost:8080/api/assets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "name":"CNC Milling Machine",
        "assetCode":"AST-001",
        "category":"Machinery",
        "manufacturer":"Haas",
        "model":"VF-2",
        "serialNumber":"SN-12345",
        "purchaseDate":"2022-01-15",
        "status":"OPERATIONAL",
        "location":{"id":1}
      }'
```

Create a work order:
```bash
curl -X POST http://localhost:8080/api/work-orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "title":"Replace worn spindle bearing",
        "description":"Bearing showing signs of wear during inspection",
        "asset":{"id":1},
        "type":"CORRECTIVE",
        "priority":"HIGH",
        "dueDate":"2026-08-10"
      }'
```

Update work order status:
```bash
curl -X PATCH http://localhost:8080/api/work-orders/1/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"COMPLETED"}'
```

Delete something (ADMIN only):
```bash
curl -X DELETE http://localhost:8080/api/assets/1 \
  -H "Authorization: Bearer $TOKEN"
```

## 10. Notes / possible next steps
- Add DTOs + MapStruct instead of exposing entities directly.
- Add pagination/sorting (`Pageable`) to list endpoints.
- Add Flyway/Liquibase for versioned migrations instead of `ddl-auto=update`.
- Add file/image attachments for work orders (e.g. photos of faults).

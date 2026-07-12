# TransitOps Backend

Production-oriented Spring Boot backend for the **TransitOps Smart Transport Operations Platform**.

## What is included

- Email/password authentication
- JWT access tokens
- Rotating database-backed refresh tokens
- Logout for one device or all devices
- Role-Based Access Control (RBAC)
- Admin user and role management
- Vehicle registry with search, filters, sorting, pagination, retirement, and safe deletion
- Driver management with license validity and safety score rules
- Draft, dispatch, complete, cancel, and delete trip workflow
- Transactional vehicle/driver status transitions
- Pessimistic locking to prevent double dispatch
- Maintenance workflow that automatically moves vehicles into and out of `IN_SHOP`
- Fuel logs and operational expenses
- Dashboard KPIs and distributions
- Vehicle and fleet analytics
- CSV and PDF report export
- Vehicle document upload/download/delete
- Expiring-license query and optional email reminders
- Centralized validation/error responses
- Request IDs for frontend/server troubleshooting
- MySQL + Flyway migrations
- Docker and Docker Compose
- Unit tests and a Spring context test
- PowerShell API smoke-test script

## Technology stack

- Java 21
- Spring Boot 3.5.16
- Spring Web, Spring Data JPA, Spring Security
- MySQL 8.x
- Flyway
- JJWT 0.13.0
- PDFBox 3.0.8
- Maven

## Backend component structure

```text
com.transitops.backend
├── auth          users, roles, login, refresh, logout, admin user management
├── security      JWT parsing, security filter chain, 401/403 JSON handlers
├── vehicle       vehicle CRUD, availability, retirement, filters
├── driver        driver CRUD, license compliance, availability, filters
├── trip          draft/dispatch/complete/cancel workflow and business rules
├── maintenance   active/closed maintenance workflow
├── fuel          fuel-log CRUD and trip linkage
├── expense       expense CRUD and categorization
├── dashboard     operational KPI calculations
├── report        fleet analytics, CSV export, PDF export
├── document      vehicle document storage and download
├── notification  license expiry reminders
├── common        shared errors, page response, auditing, request IDs
└── config        application properties, security, bootstrap data
```

## Roles

- `ADMIN`: complete access, user/role management
- `FLEET_MANAGER`: vehicle, trip, maintenance, fuel, expenses, dashboard, reports, documents
- `DRIVER`: trip creation and lifecycle actions; read operational data
- `SAFETY_OFFICER`: driver compliance and license monitoring
- `FINANCIAL_ANALYST`: fuel, expense, dashboard, and report access

The full endpoint matrix is in [RBAC_MATRIX.md](RBAC_MATRIX.md).

## Important business rules enforced

1. Vehicle registration numbers and driver license numbers are unique.
2. Only `AVAILABLE` vehicles can be selected and dispatched.
3. `IN_SHOP`, `RETIRED`, and `ON_TRIP` vehicles cannot be dispatched.
4. Only `AVAILABLE` drivers with non-expired licenses can be assigned.
5. Suspended, off-duty, expired-license, or already-on-trip drivers cannot be dispatched.
6. Cargo weight cannot exceed the selected vehicle capacity.
7. Dispatch changes both vehicle and driver to `ON_TRIP` in one transaction.
8. Completion updates the vehicle odometer and restores vehicle and driver to `AVAILABLE`.
9. Cancelling a dispatched trip restores resources to `AVAILABLE`.
10. Active maintenance changes the vehicle to `IN_SHOP` and hides it from available selections.
11. Closing maintenance restores the vehicle to `AVAILABLE`, unless it was retired.
12. Pessimistic database locks help prevent concurrent double assignment.

## Quick start using Docker

```powershell
cd path\to\transitops-backend
docker compose up --build
```

Backend: `http://localhost:8080/api`

MySQL: `localhost:3306`, database `transitops`

Default development administrator:

```text
Email:    admin@transitops.com
Password: Admin@123
```

Change the default credentials and JWT secret before deployment.

## Run locally without Docker

Requirements:

- Java 21
- Maven 3.9+
- MySQL 8.x

PowerShell setup:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/transitops?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "root"
$env:JWT_SECRET = "VHJhbnNpdE9wcy1EZXZlbG9wbWVudC1TZWNyZXQtS2V5LU11c3QtQmUtQ2hhbmdlZA=="
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173,http://localhost:3000"
$env:APP_ADMIN_EMAIL = "admin@transitops.com"
$env:APP_ADMIN_PASSWORD = "Admin@123"

mvn clean spring-boot:run
```

Run tests:

```powershell
mvn clean test
```

Build the JAR:

```powershell
mvn clean package
java -jar .\target\transitops-backend-1.0.0.jar
```

## Authentication flow

### Login

`POST /api/auth/login`

```json
{
  "email": "admin@transitops.com",
  "password": "Admin@123",
  "deviceInfo": "Chrome on Windows"
}
```

The response includes:

- `accessToken`: send as `Authorization: Bearer <token>`
- `refreshToken`: use only at `/api/auth/refresh`
- `user.roles`: use for frontend route/menu guards

### Refresh

`POST /api/auth/refresh`

```json
{
  "refreshToken": "<refresh-token>",
  "deviceInfo": "Chrome on Windows"
}
```

Refresh tokens rotate. Replace both stored tokens with the returned values.

### Logout

`POST /api/auth/logout` with an access token.

```json
{
  "refreshToken": "<refresh-token>",
  "allDevices": false
}
```

## Pagination and sorting

List APIs support Spring-style query parameters:

```text
?page=0&size=20&sort=registrationNumber,asc
```

Response shape:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

## Standard error shape

```json
{
  "timestamp": "2026-07-12T08:00:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "code": "CARGO_CAPACITY_EXCEEDED",
  "message": "Cargo weight exceeds the vehicle maximum load capacity of 500.00 kg",
  "path": "/api/trips/1/dispatch",
  "requestId": "d9cf...",
  "validationErrors": {}
}
```

Frontend behavior:

- `400`: show validation/input error
- `401`: try refresh once, then clear auth and redirect to login
- `403`: show access-denied page/toast
- `404`: show not-found message
- `409`: duplicate/reference conflict
- `422`: business-rule failure
- `500`: generic error using the `requestId` for debugging

## Database and migrations

Schema creation is handled by:

```text
src/main/resources/db/migration/V1__create_transitops_schema.sql
```

Hibernate is configured with `ddl-auto=validate`; it does not silently change production tables.

## Vehicle document storage

Files are stored under `UPLOAD_DIR` (default `./uploads`) while metadata is stored in MySQL.

Allowed content types:

- PDF
- JPEG
- PNG
- WebP

Default maximum size: 10 MB.

For cloud deployment, replace local storage with S3, Azure Blob Storage, or equivalent without changing controller contracts.

## Email reminders

Email sending is disabled by default. Enable it with:

```powershell
$env:LICENSE_REMINDER_ENABLED = "true"
$env:LICENSE_REMINDER_DAYS = "30"
$env:MAIL_HOST = "smtp.example.com"
$env:MAIL_PORT = "587"
$env:MAIL_USERNAME = "..."
$env:MAIL_PASSWORD = "..."
$env:MAIL_SMTP_AUTH = "true"
$env:MAIL_STARTTLS = "true"
```

The scheduled job runs daily at 08:00 server time. The safety officer can also query:

```text
GET /api/license-reminders/expiring?days=30
```

## Test the complete workflow

Run:

```powershell
.\scripts\api-smoke-test.ps1
```

The script logs in, creates a vehicle and driver, creates and dispatches a trip, completes it, creates/closes maintenance, adds an expense, reads dashboard analytics, and downloads CSV/PDF reports.

## Frontend integration

Use [FRONTEND_INTEGRATION.md](FRONTEND_INTEGRATION.md) for Axios interceptors, route guards, enum values, module APIs, and expected DTO shapes.

Use [API_ENDPOINTS.md](API_ENDPOINTS.md) for the complete endpoint catalogue.

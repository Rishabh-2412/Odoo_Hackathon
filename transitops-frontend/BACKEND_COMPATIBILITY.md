# TransitOps frontend/backend compatibility

This frontend is configured for the supplied Spring Boot backend.

## Run

```powershell
npm install
npm run dev
```

Backend must run at `http://localhost:8080` and expose `/api`.

## Implemented integration

- Public Driver signup (`POST /auth/signup`) with no client-controlled role
- Driver login and role-specific dashboard/navigation
- JWT access token attachment
- Refresh-token rotation and retry after 401
- Backend logout endpoint
- Multiple-role RBAC (`ADMIN`, `FLEET_MANAGER`, `DRIVER`, `SAFETY_OFFICER`, `FINANCIAL_ANALYST`)
- Backend paginated response unwrapping
- Vehicle, driver, trip, maintenance, fuel, expense, dashboard, reports, and admin-user endpoint mappings
- Request/response field adapters for the backend DTO contract

## Default admin

- Email: `admin@transitops.com`
- Password: `Admin@123`

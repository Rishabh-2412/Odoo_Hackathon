# TransitOps Frontend

React + Vite frontend integrated with the supplied TransitOps Spring Boot backend.

## Features

- Public Driver signup
- JWT login with access and refresh tokens
- Automatic access-token attachment and refresh-token rotation
- Multiple-role RBAC navigation and protected routes
- Driver dashboard and trip operations
- Admin, Fleet Manager, Safety Officer, and Financial Analyst views
- Real vehicle, driver, trip, maintenance, fuel, expense, report, and user APIs

## Run

```powershell
npm install
Copy-Item .env.example .env -ErrorAction SilentlyContinue
npm run dev
```

Frontend: `http://localhost:5173`

Backend: `http://localhost:8080/api`

## Environment

`.env`:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## Driver signup flow

1. Open `http://localhost:5173/signup`.
2. Enter name, email, password, and confirmation.
3. The frontend sends only `name`, `email`, and `password` to `POST /api/auth/signup`.
4. The backend assigns exactly the `DRIVER` role.
5. The user is redirected to login and can then open the Driver dashboard.

The signup page does not expose a role selector. Higher-privilege roles can only be assigned by an authenticated Admin from the Users & Roles page.

## Development Admin

- Email: `admin@transitops.com`
- Password: `Admin@123`

Change development credentials before deployment.

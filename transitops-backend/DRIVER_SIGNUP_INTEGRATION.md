# Driver signup and dashboard integration

## Public endpoint

`POST /api/auth/signup`

```json
{
  "name": "Alex Driver",
  "email": "alex.driver@example.com",
  "password": "Driver@123"
}
```

The server normalizes the email, checks uniqueness, hashes the password with BCrypt, enables the account, and assigns exactly the `DRIVER` role. Role data from the browser is never accepted.

## Driver access after login

A signed-in Driver can access:

- `GET /api/auth/me`
- `GET /api/dashboard`
- Vehicle and driver read endpoints
- Trip list/detail and trip create/update/dispatch/complete/cancel endpoints

A Driver cannot access Admin user management, vehicle/driver administration, maintenance, finance reports, or other restricted endpoints. The backend returns `403 Forbidden` for those calls.

## Verification

```powershell
.\scripts\signup-driver-smoke-test.ps1
```

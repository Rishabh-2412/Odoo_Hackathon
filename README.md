# TransitOps full-stack start guide

This package contains the backend and the updated frontend with public Driver signup.

## 1. Start MySQL

Ensure MySQL 8 is running and the `transitops` database is available. The backend can create the database automatically when the configured MySQL user has permission.

## 2. Start backend

Open PowerShell:

```powershell
cd .\transitops-backend

$env:DB_URL = "jdbc:mysql://localhost:3306/transitops?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "YOUR_MYSQL_PASSWORD"
$env:JWT_SECRET = "VHJhbnNpdE9wcy1EZXZlbG9wbWVudC1TZWNyZXQtS2V5LU11c3QtQmUtQ2hhbmdlZA=="
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173"

mvn clean spring-boot:run
```

Check health:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"
```

Expected: `status = UP`.

## 3. Start frontend

Open a second PowerShell window:

```powershell
cd .\transitops-frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## 4. Test Driver signup in the UI

1. Click **Create one** on the login page.
2. Enter name, email, password, and confirmation.
3. The backend creates an enabled user with exactly the `DRIVER` role.
4. Sign in using the new credentials.
5. The Driver can open Dashboard, Fleet, Drivers, and Trips. Write actions in Trips are available; restricted administration modules remain hidden and protected by the backend.

## 5. Test Driver signup and RBAC using PowerShell

```powershell
cd .\transitops-backend
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\scripts\signup-driver-smoke-test.ps1
```

Expected final line:

```text
Driver signup, login, dashboard access, and RBAC test completed successfully.
```

## Development Admin

```text
Email: admin@transitops.com
Password: Admin@123
```

Change the bootstrap credentials and JWT secret before deployment.

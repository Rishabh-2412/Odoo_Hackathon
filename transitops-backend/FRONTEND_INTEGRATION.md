# TransitOps Frontend Integration

Backend base URL:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## Recommended frontend service components

```text
src/
├── api/
│   ├── apiClient.ts
│   ├── authApi.ts
│   ├── vehicleApi.ts
│   ├── driverApi.ts
│   ├── tripApi.ts
│   ├── maintenanceApi.ts
│   ├── fuelApi.ts
│   ├── expenseApi.ts
│   ├── dashboardApi.ts
│   ├── reportApi.ts
│   └── documentApi.ts
├── auth/
│   ├── AuthProvider.tsx
│   ├── ProtectedRoute.tsx
│   ├── RoleGuard.tsx
│   └── authStorage.ts
├── types/
│   ├── auth.ts
│   ├── vehicle.ts
│   ├── driver.ts
│   ├── trip.ts
│   └── common.ts
└── features/
    ├── dashboard/
    ├── vehicles/
    ├── drivers/
    ├── trips/
    ├── maintenance/
    ├── finance/
    └── reports/
```


## Public Driver signup

The signup screen must send only:

```json
{
  "name": "Alex Driver",
  "email": "alex.driver@example.com",
  "password": "Driver@123"
}
```

Call `POST /api/auth/signup`. Do not send a role selector. The backend assigns `DRIVER` and returns a `UserSummary`. Redirect the user to login after a successful `201 Created` response.

## Axios client

```ts
import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";

const baseURL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api";

export const api = axios.create({
  baseURL,
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem("accessToken");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

let refreshPromise: Promise<string> | null = null;

async function refreshAccessToken(): Promise<string> {
  const refreshToken = localStorage.getItem("refreshToken");
  if (!refreshToken) throw new Error("No refresh token");

  const response = await axios.post(`${baseURL}/auth/refresh`, {
    refreshToken,
    deviceInfo: navigator.userAgent,
  });

  localStorage.setItem("accessToken", response.data.accessToken);
  localStorage.setItem("refreshToken", response.data.refreshToken);
  localStorage.setItem("currentUser", JSON.stringify(response.data.user));
  return response.data.accessToken;
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;
    const isAuthEndpoint = original?.url?.includes("/auth/login") || original?.url?.includes("/auth/refresh");

    if (error.response?.status === 401 && original && !original._retry && !isAuthEndpoint) {
      original._retry = true;
      try {
        refreshPromise ??= refreshAccessToken().finally(() => { refreshPromise = null; });
        const token = await refreshPromise;
        original.headers.Authorization = `Bearer ${token}`;
        return api(original);
      } catch {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("currentUser");
        window.location.assign("/login");
      }
    }
    return Promise.reject(error);
  },
);
```

## Login API

```ts
export interface LoginRequest {
  email: string;
  password: string;
  deviceInfo?: string;
}

export type Role =
  | "ADMIN"
  | "FLEET_MANAGER"
  | "DRIVER"
  | "SAFETY_OFFICER"
  | "FINANCIAL_ANALYST";

export interface CurrentUser {
  id: number;
  name: string;
  email: string;
  enabled: boolean;
  roles: Role[];
}

export interface AuthResponse {
  tokenType: "Bearer";
  accessToken: string;
  accessTokenExpiresInSeconds: number;
  refreshToken: string;
  refreshTokenExpiresInSeconds: number;
  user: CurrentUser;
}
```

## Protected route and role guard

```tsx
import { Navigate, Outlet } from "react-router";

export function ProtectedRoute() {
  return localStorage.getItem("accessToken") ? <Outlet /> : <Navigate to="/login" replace />;
}

export function RoleGuard({ allowed, children }: { allowed: Role[]; children: React.ReactNode }) {
  const user = JSON.parse(localStorage.getItem("currentUser") ?? "null") as CurrentUser | null;
  const permitted = user?.roles.some((role) => allowed.includes(role));
  return permitted ? children : null;
}
```

Do not rely on `RoleGuard` for security. The backend still checks every permission.

## TypeScript status enums

```ts
export type VehicleStatus = "AVAILABLE" | "ON_TRIP" | "IN_SHOP" | "RETIRED";
export type VehicleType = "VAN" | "TRUCK" | "BUS" | "CAR" | "BIKE" | "OTHER";
export type DriverStatus = "AVAILABLE" | "ON_TRIP" | "OFF_DUTY" | "SUSPENDED";
export type TripStatus = "DRAFT" | "DISPATCHED" | "COMPLETED" | "CANCELLED";
export type MaintenanceStatus = "ACTIVE" | "CLOSED";
```

## Selection components

Trip forms must load these endpoints rather than filtering cached master lists:

```text
GET /vehicles/available
GET /drivers/available
```

The backend repeats all validations at dispatch time because availability may change after the draft form loads.

## Trip action buttons

Recommended action visibility:

- `DRAFT`: Edit, Dispatch, Cancel, Delete
- `DISPATCHED`: Complete, Cancel
- `COMPLETED`: View only
- `CANCELLED`: View, Delete

Treat the returned trip status as the source of truth after every action.

## React Query keys

```ts
["dashboard", filters]
["vehicles", filters, page, sort]
["vehicle", id]
["availableVehicles", region, type]
["drivers", filters, page, sort]
["availableDrivers", region]
["trips", filters, page, sort]
["trip", id]
["maintenance", filters, page]
["fuelLogs", filters, page]
["expenses", filters, page]
["fleetReport"]
```

After mutations, invalidate related data. For example, dispatch/complete/cancel should invalidate `trips`, `trip`, `vehicles`, `availableVehicles`, `drivers`, `availableDrivers`, and `dashboard`.

## Error handling

```ts
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  code: string;
  message: string;
  path: string;
  requestId?: string;
  validationErrors: Record<string, string>;
}
```

Use `validationErrors` to call React Hook Form `setError`. Show `message` for business rules such as `CARGO_CAPACITY_EXCEEDED`, `LICENSE_EXPIRED`, or `VEHICLE_NOT_AVAILABLE`.

## File download example

```ts
const response = await api.get("/reports/export.pdf", { responseType: "blob" });
const url = URL.createObjectURL(response.data);
const anchor = document.createElement("a");
anchor.href = url;
anchor.download = "transitops-fleet-report.pdf";
anchor.click();
URL.revokeObjectURL(url);
```

## Vehicle document upload

```ts
const form = new FormData();
form.append("vehicleId", String(vehicleId));
form.append("documentType", "INSURANCE");
form.append("expiryDate", "2027-06-30");
form.append("file", file);

await api.post("/vehicle-documents", form, {
  headers: { "Content-Type": "multipart/form-data" },
});
```

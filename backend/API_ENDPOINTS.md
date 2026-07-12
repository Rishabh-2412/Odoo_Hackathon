# TransitOps API Endpoints

Base URL: `http://localhost:8080/api`

All protected calls require:

```http
Authorization: Bearer <access-token>
```

## Authentication

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/auth/login` | Login and receive access + refresh tokens |
| POST | `/auth/refresh` | Rotate refresh token and issue a new access token |
| POST | `/auth/logout` | Revoke one refresh token or all user sessions |
| GET | `/auth/me` | Current authenticated user and roles |

## Admin users

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/admin/users` | Create a user and assign roles |
| GET | `/admin/users` | Paginated user list |
| GET | `/admin/users/{id}` | User detail |
| PATCH | `/admin/users/{id}/roles` | Replace roles |
| PATCH | `/admin/users/{id}/status` | Enable or disable account |

## Vehicles

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/vehicles` | Register vehicle |
| GET | `/vehicles` | Search/filter/sort/page vehicles |
| GET | `/vehicles/available` | Dispatch-safe vehicle selection pool |
| GET | `/vehicles/{id}` | Vehicle detail |
| PUT | `/vehicles/{id}` | Update editable master data |
| PATCH | `/vehicles/{id}/status` | Manual `AVAILABLE`/`RETIRED` status change |
| POST | `/vehicles/{id}/retire` | Retire vehicle |
| DELETE | `/vehicles/{id}` | Hard delete only when no history exists |

Vehicle filters: `search`, `type`, `status`, `region`, `page`, `size`, `sort`.

Vehicle types: `VAN`, `TRUCK`, `BUS`, `CAR`, `BIKE`, `OTHER`.

Vehicle statuses: `AVAILABLE`, `ON_TRIP`, `IN_SHOP`, `RETIRED`.

## Drivers

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/drivers` | Register driver |
| GET | `/drivers` | Search/filter/sort/page drivers |
| GET | `/drivers/available` | Valid-license, available selection pool |
| GET | `/drivers/{id}` | Driver detail |
| PUT | `/drivers/{id}` | Update profile/compliance details |
| PATCH | `/drivers/{id}/status` | Change available/off-duty/suspended status |
| DELETE | `/drivers/{id}` | Delete only when no trip history exists |

Driver filters: `search`, `status`, `region`, `licenseValid`, `page`, `size`, `sort`.

Driver statuses: `AVAILABLE`, `ON_TRIP`, `OFF_DUTY`, `SUSPENDED`.

## Trips

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/trips` | Create draft |
| GET | `/trips` | Search/filter/sort/page trips |
| GET | `/trips/{id}` | Trip detail |
| PUT | `/trips/{id}` | Update draft |
| POST | `/trips/{id}/dispatch` | Validate and dispatch |
| POST | `/trips/{id}/complete` | Complete, update odometer/status, optionally create fuel log |
| POST | `/trips/{id}/cancel` | Cancel and restore resources when dispatched |
| DELETE | `/trips/{id}` | Delete draft/cancelled trip |

Trip filters: `search`, `status`, `vehicleId`, `driverId`, `from`, `to`, `page`, `size`, `sort`.

Trip statuses: `DRAFT`, `DISPATCHED`, `COMPLETED`, `CANCELLED`.

## Maintenance

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/maintenance` | Open active maintenance and mark vehicle `IN_SHOP` |
| GET | `/maintenance` | Filter/page maintenance records |
| GET | `/maintenance/{id}` | Maintenance detail |
| POST | `/maintenance/{id}/close` | Close and restore vehicle to `AVAILABLE` |
| DELETE | `/maintenance/{id}` | Delete closed record |

Maintenance filters: `vehicleId`, `status`, `from`, `to`, pagination/sorting.

## Fuel logs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/fuel-logs` | Record fuel |
| GET | `/fuel-logs` | Filter/page fuel logs |
| GET | `/fuel-logs/{id}` | Fuel-log detail |
| PUT | `/fuel-logs/{id}` | Update fuel log |
| DELETE | `/fuel-logs/{id}` | Delete fuel log |

Filters: `vehicleId`, `tripId`, `from`, `to`, pagination/sorting.

## Expenses

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/expenses` | Record expense |
| GET | `/expenses` | Filter/page expenses |
| GET | `/expenses/{id}` | Expense detail |
| PUT | `/expenses/{id}` | Update expense |
| DELETE | `/expenses/{id}` | Delete expense |

Expense types: `TOLL`, `MAINTENANCE`, `INSURANCE`, `REPAIR`, `PARKING`, `PERMIT`, `OTHER`.

## Dashboard and reports

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/dashboard` | KPI cards and status distributions |
| GET | `/reports/fleet` | Fleet totals, vehicle analytics, six-month cost trend |
| GET | `/reports/vehicles/{vehicleId}` | Single-vehicle analytics |
| GET | `/reports/export.csv` | Download CSV report |
| GET | `/reports/export.pdf` | Download PDF report |

Dashboard filters: `vehicleType`, `vehicleStatus`, `region`.

## Vehicle documents

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/vehicle-documents` | Multipart upload |
| GET | `/vehicle-documents?vehicleId=1` | List vehicle documents |
| GET | `/vehicle-documents/{id}/download` | Download document |
| DELETE | `/vehicle-documents/{id}` | Delete document + metadata |

Multipart fields: `vehicleId`, `documentType`, optional `expiryDate`, and `file`.

Document types: `REGISTRATION`, `INSURANCE`, `PERMIT`, `POLLUTION_CERTIFICATE`, `FITNESS_CERTIFICATE`, `OTHER`.

## License reminders

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/license-reminders/expiring?days=30` | Drivers whose licenses expire in the requested period |

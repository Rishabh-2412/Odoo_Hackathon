# TransitOps RBAC Matrix

Legend: `R` read, `C` create, `U` update/action, `D` delete.

| Module / action | ADMIN | FLEET_MANAGER | DRIVER | SAFETY_OFFICER | FINANCIAL_ANALYST |
|---|---:|---:|---:|---:|---:|
| Authentication / own profile | R | R | R | R | R |
| Admin user management | CRUD | - | - | - | - |
| Vehicle list/details/available pool | R | R | R | R | R |
| Vehicle create/update/status/retire | CRUD | CU | - | - | - |
| Vehicle hard delete | D | - | - | - | - |
| Driver list/details/available pool | R | R | R | R | R |
| Driver create/update/status | CRUD | - | - | CU | - |
| Driver hard delete | D | - | - | - | - |
| Trip list/details | R | R | R | R | R |
| Trip create/update/dispatch/complete/cancel | CU | CU | CU | - | - |
| Trip delete draft/cancelled | D | D | - | - | - |
| Maintenance | CRUD | CRUD | - | - | - |
| Fuel logs read/create | CRUD | RC | - | - | RC |
| Fuel log update/delete | UD | - | - | - | UD |
| Expenses read/create | CRUD | RC | - | - | RC |
| Expense update/delete | UD | - | - | - | UD |
| Dashboard | R | R | R | R | R |
| Analytics and CSV/PDF reports | R | R | - | - | R |
| Vehicle documents read/download | R | R | R | R | R |
| Vehicle documents upload/delete | CRUD | CRUD | - | - | - |
| Expiring-license alerts | R | - | - | R | - |

## Backend enforcement

Global URL rules secure every endpoint except Driver signup, login, refresh, health, and CORS preflight. Public signup never accepts a role from the client; the backend always assigns exactly `DRIVER`. Module permissions are enforced using Spring method security (`@PreAuthorize`). A hidden frontend button is only a usability feature; it is not considered security.

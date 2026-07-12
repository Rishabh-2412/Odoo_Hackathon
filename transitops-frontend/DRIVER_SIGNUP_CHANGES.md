# Driver signup changes

- Added `/signup` public route and Driver signup page.
- Signup sends `name`, `email`, and `password` only.
- Added Driver role to login messaging, RBAC navigation, and route guards.
- Driver can open Dashboard, Fleet read view, Drivers read view, and Trips.
- Financial dashboard requests are skipped for Driver accounts to prevent unnecessary `403` errors.
- Admin remains the only role that can manage application users and assign privileged roles.

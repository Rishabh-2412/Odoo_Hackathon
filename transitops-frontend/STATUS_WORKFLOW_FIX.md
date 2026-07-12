# Status workflow fix

The frontend no longer offers backend-controlled statuses during manual create/edit operations.

- New vehicles are always `AVAILABLE`.
- Vehicle `ON_TRIP` is set by trip dispatch and restored by trip completion/cancellation.
- Vehicle `IN_SHOP` is set by opening maintenance and restored by closing maintenance.
- New drivers are `AVAILABLE`, unless the licence is expired, in which case they are `OFF_DUTY`.
- Driver `ON_TRIP` is controlled by the trip workflow.
- Manual driver statuses are `AVAILABLE`, `OFF_DUTY`, and `SUSPENDED`.
- Manual vehicle statuses are `AVAILABLE` and `RETIRED`; an `IN_SHOP` vehicle can also be retired.

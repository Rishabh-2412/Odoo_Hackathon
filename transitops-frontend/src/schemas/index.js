import { z } from 'zod';

export const loginSchema = z.object({
  email: z.string().email('Enter a valid email'),
  password: z.string().min(1, 'Password is required'),
  remember: z.boolean().optional(),
});

// Public signup always creates a DRIVER account. Role selection is deliberately
// absent so a visitor cannot self-assign an administrative role.
export const signupSchema = z.object({
  name: z.string().trim().min(2, 'Name must contain at least 2 characters').max(120),
  email: z.string().trim().email('Enter a valid email').max(190),
  password: z.string().min(8, 'Password must be at least 8 characters').max(72),
  confirmPassword: z.string().min(1, 'Confirm your password'),
}).refine((data) => data.password === data.confirmPassword, {
  message: 'Passwords do not match',
  path: ['confirmPassword'],
});

export const vehicleSchema = z.object({
  registrationNumber: z.string().min(1, 'Registration number is required').max(50),
  nameModel: z.string().min(1, 'Vehicle name/model is required').max(120),
  type: z.enum(['VAN', 'TRUCK', 'BUS', 'CAR', 'BIKE', 'OTHER'], { required_error: 'Select a type' }),
  maximumLoadCapacity: z.coerce.number().positive('Must be greater than 0'),
  odometer: z.coerce.number().min(0, 'Must be at least 0'),
  acquisitionCost: z.coerce.number().min(0, 'Must be at least 0'),
  region: z.string().min(1, 'Select a region').max(100),
  status: z.enum(['AVAILABLE', 'ON_TRIP', 'IN_SHOP', 'RETIRED']).optional(),
});

export const driverSchema = z.object({
  name: z.string().min(1, 'Name is required').max(120),
  licenseNumber: z.string().min(1, 'License number is required').max(80),
  licenseCategory: z.string().min(1, 'Select a category').max(60),
  licenseExpiryDate: z.string().min(1, 'Expiry date is required'),
  contactNumber: z.string().min(5, 'Enter a valid phone number').max(30),
  email: z.union([z.string().email('Enter a valid email'), z.literal('')]).optional(),
  safetyScore: z.coerce.number().min(0).max(100),
  region: z.string().min(1, 'Select a region').max(100),
  status: z.enum(['AVAILABLE', 'ON_TRIP', 'OFF_DUTY', 'SUSPENDED']).optional(),
});

export const tripSchema = z.object({
  source: z.string().min(1, 'Source is required').max(180),
  destination: z.string().min(1, 'Destination is required').max(180),
  vehicleId: z.coerce.number().int().positive('Select a vehicle'),
  driverId: z.coerce.number().int().positive('Select a driver'),
  cargoWeight: z.coerce.number().positive('Cargo weight must be greater than 0'),
  plannedDistance: z.coerce.number().positive('Planned distance must be greater than 0'),
  notes: z.string().max(1000).optional(),
});

export const tripCompleteSchema = z.object({
  finalOdometerKm: z.coerce.number().min(0, 'Final odometer must be at least 0'),
  fuelConsumedLiters: z.coerce.number().min(0, 'Fuel consumed must be at least 0'),
  fuelCost: z.coerce.number().min(0, 'Fuel cost must be at least 0'),
  revenue: z.coerce.number().min(0, 'Revenue must be at least 0'),
  notes: z.string().max(1000).optional(),
});

export const maintenanceSchema = z.object({
  vehicleId: z.coerce.number().int().positive('Select a vehicle'),
  maintenanceType: z.string().min(1, 'Service type is required').max(120),
  description: z.string().max(1000).optional(),
  startDate: z.string().min(1, 'Start date is required'),
  cost: z.coerce.number().min(0, 'Must be at least 0'),
  odometerAtService: z.coerce.number().min(0, 'Must be at least 0').optional(),
});

export const fuelLogSchema = z.object({
  vehicleId: z.coerce.number().int().positive('Select a vehicle'),
  tripId: z.coerce.number().int().positive().optional().nullable().or(z.literal('')),
  liters: z.coerce.number().positive('Must be greater than 0'),
  cost: z.coerce.number().min(0, 'Must be at least 0'),
  fuelDate: z.string().min(1, 'Date is required'),
  odometerReading: z.coerce.number().min(0, 'Must be at least 0'),
  fuelStation: z.string().max(500).optional(),
});

export const expenseSchema = z.object({
  vehicleId: z.coerce.number().int().positive('Select a vehicle'),
  tripId: z.coerce.number().int().positive().optional().nullable().or(z.literal('')),
  expenseType: z.enum(['TOLL', 'MAINTENANCE', 'INSURANCE', 'REPAIR', 'PARKING', 'PERMIT', 'OTHER'], { required_error: 'Select a type' }),
  description: z.string().min(1, 'Description is required').max(500),
  amount: z.coerce.number().positive('Must be greater than 0'),
  expenseDate: z.string().min(1, 'Date is required'),
});

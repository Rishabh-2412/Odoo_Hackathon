param(
    [string]$BaseUrl = "http://localhost:8080/api",
    [string]$AdminEmail = "admin@transitops.com",
    [string]$AdminPassword = "Admin@123"
)

$ErrorActionPreference = "Stop"

function Invoke-JsonApi {
    param(
        [Parameter(Mandatory = $true)][string]$Uri,
        [Parameter(Mandatory = $true)][string]$Method,
        [hashtable]$Headers,
        $Body
    )

    $params = @{
        Uri         = $Uri
        Method      = $Method
        ContentType = "application/json"
    }
    if ($Headers) { $params.Headers = $Headers }
    if ($null -ne $Body) { $params.Body = ($Body | ConvertTo-Json -Depth 10) }

    try {
        return Invoke-RestMethod @params
    }
    catch {
        Write-Host "Request failed: $Method $Uri" -ForegroundColor Red
        if ($_.ErrorDetails.Message) {
            Write-Host $_.ErrorDetails.Message -ForegroundColor Red
        }
        throw
    }
}

Write-Host "1. Login" -ForegroundColor Cyan
$login = Invoke-JsonApi -Uri "$BaseUrl/auth/login" -Method POST -Body @{
    email      = $AdminEmail
    password   = $AdminPassword
    deviceInfo = "PowerShell smoke test"
}
$token = $login.accessToken
$refreshToken = $login.refreshToken
$headers = @{ Authorization = "Bearer $token" }
Write-Host "Logged in as $($login.user.email)" -ForegroundColor Green

$stamp = Get-Date -Format "yyyyMMddHHmmss"

Write-Host "2. Create vehicle" -ForegroundColor Cyan
$vehicle = Invoke-JsonApi -Uri "$BaseUrl/vehicles" -Method POST -Headers $headers -Body @{
    registrationNumber = "TNTEST$stamp"
    nameModel           = "Van-05"
    type                = "VAN"
    region              = "South"
    maxLoadCapacityKg   = 500
    odometerKm          = 1000
    acquisitionCost     = 1000000
}
Write-Host "Vehicle ID: $($vehicle.id), status: $($vehicle.status)" -ForegroundColor Green

Write-Host "3. Create driver" -ForegroundColor Cyan
$driver = Invoke-JsonApi -Uri "$BaseUrl/drivers" -Method POST -Headers $headers -Body @{
    name              = "Alex $stamp"
    licenseNumber     = "DL$stamp"
    licenseCategory   = "LMV"
    licenseExpiryDate = (Get-Date).AddYears(1).ToString("yyyy-MM-dd")
    contactNumber     = "9999999999"
    email             = "alex.$stamp@example.com"
    region            = "South"
    safetyScore       = 95
}
Write-Host "Driver ID: $($driver.id), status: $($driver.status)" -ForegroundColor Green

Write-Host "4. Create draft trip" -ForegroundColor Cyan
$trip = Invoke-JsonApi -Uri "$BaseUrl/trips" -Method POST -Headers $headers -Body @{
    source            = "Chennai"
    destination       = "Coimbatore"
    vehicleId         = $vehicle.id
    driverId          = $driver.id
    cargoWeightKg     = 450
    plannedDistanceKm = 500
    notes             = "Smoke test trip"
}
Write-Host "Trip ID: $($trip.id), status: $($trip.status)" -ForegroundColor Green

Write-Host "5. Dispatch trip" -ForegroundColor Cyan
$trip = Invoke-JsonApi -Uri "$BaseUrl/trips/$($trip.id)/dispatch" -Method POST -Headers $headers
Write-Host "Trip status: $($trip.status)" -ForegroundColor Green

Write-Host "6. Complete trip" -ForegroundColor Cyan
$trip = Invoke-JsonApi -Uri "$BaseUrl/trips/$($trip.id)/complete" -Method POST -Headers $headers -Body @{
    finalOdometerKm    = 1500
    fuelConsumedLiters = 45
    fuelCost            = 4500
    revenue             = 25000
    notes               = "Delivered successfully"
}
Write-Host "Trip status: $($trip.status), actual distance: $($trip.actualDistanceKm)" -ForegroundColor Green

Write-Host "7. Open maintenance" -ForegroundColor Cyan
$maintenance = Invoke-JsonApi -Uri "$BaseUrl/maintenance" -Method POST -Headers $headers -Body @{
    vehicleId             = $vehicle.id
    serviceType           = "Oil Change"
    description           = "Scheduled service after completed trip"
    startDate             = (Get-Date).ToString("yyyy-MM-dd")
    estimatedOrInitialCost = 2500
    odometerAtService     = 1500
}
Write-Host "Maintenance ID: $($maintenance.id), status: $($maintenance.status)" -ForegroundColor Green

Write-Host "8. Close maintenance" -ForegroundColor Cyan
$maintenance = Invoke-JsonApi -Uri "$BaseUrl/maintenance/$($maintenance.id)/close" -Method POST -Headers $headers -Body @{
    endDate      = (Get-Date).ToString("yyyy-MM-dd")
    finalCost    = 2800
    closingNotes = "Oil and filter replaced"
}
Write-Host "Maintenance status: $($maintenance.status)" -ForegroundColor Green

Write-Host "9. Add toll expense" -ForegroundColor Cyan
$expense = Invoke-JsonApi -Uri "$BaseUrl/expenses" -Method POST -Headers $headers -Body @{
    vehicleId   = $vehicle.id
    tripId      = $trip.id
    type        = "TOLL"
    amount      = 900
    expenseDate = (Get-Date).ToString("yyyy-MM-dd")
    description = "Highway tolls"
}
Write-Host "Expense ID: $($expense.id)" -ForegroundColor Green

Write-Host "10. Read dashboard" -ForegroundColor Cyan
$dashboard = Invoke-JsonApi -Uri "$BaseUrl/dashboard" -Method GET -Headers $headers
$dashboard | Format-List activeVehicles, availableVehicles, activeTrips, completedTrips, fleetUtilizationPercent

Write-Host "11. Read fleet report" -ForegroundColor Cyan
$report = Invoke-JsonApi -Uri "$BaseUrl/reports/fleet" -Method GET -Headers $headers
$report | Format-List totalDistanceKm, totalFuelLiters, totalOperationalCost, totalRevenue

$outDir = Join-Path $PSScriptRoot "output"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

Write-Host "12. Download reports" -ForegroundColor Cyan
Invoke-WebRequest -Uri "$BaseUrl/reports/export.csv" -Headers $headers -OutFile (Join-Path $outDir "transitops-report.csv")
Invoke-WebRequest -Uri "$BaseUrl/reports/export.pdf" -Headers $headers -OutFile (Join-Path $outDir "transitops-report.pdf")
Write-Host "Reports saved in $outDir" -ForegroundColor Green

Write-Host "13. Logout" -ForegroundColor Cyan
$logout = Invoke-JsonApi -Uri "$BaseUrl/auth/logout" -Method POST -Headers $headers -Body @{
    refreshToken = $refreshToken
    allDevices   = $false
}
Write-Host $logout.message -ForegroundColor Green

Write-Host "TransitOps smoke test completed successfully." -ForegroundColor Green

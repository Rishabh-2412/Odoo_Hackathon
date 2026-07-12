param(
    [string]$BaseUrl = "http://localhost:8080/api"
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

function Get-FailedStatusCode {
    param($ErrorRecord)

    if ($null -eq $ErrorRecord.Exception.Response) { return $null }
    try {
        return [int]$ErrorRecord.Exception.Response.StatusCode
    }
    catch {
        return $null
    }
}

$stamp = Get-Date -Format "yyyyMMddHHmmssfff"
$email = "driver.$stamp@example.com"
$password = "Driver@123"

Write-Host "1. Public Driver signup" -ForegroundColor Cyan
$registered = Invoke-JsonApi -Uri "$BaseUrl/auth/signup" -Method POST -Body @{
    name     = "Driver Signup Test"
    email    = $email
    password = $password
}

if (-not $registered.enabled) {
    throw "Signup created a disabled account."
}
if (-not ($registered.roles -contains "DRIVER")) {
    throw "Signup did not assign the DRIVER role."
}
if ($registered.roles.Count -ne 1) {
    throw "Public signup assigned more than the DRIVER role."
}
Write-Host "Created $($registered.email) with role DRIVER" -ForegroundColor Green

Write-Host "2. Login with the new Driver account" -ForegroundColor Cyan
$login = Invoke-JsonApi -Uri "$BaseUrl/auth/login" -Method POST -Body @{
    email      = $email
    password   = $password
    deviceInfo = "PowerShell Driver signup test"
}
$headers = @{ Authorization = "Bearer $($login.accessToken)" }
Write-Host "Driver login succeeded" -ForegroundColor Green

Write-Host "3. Read Driver profile" -ForegroundColor Cyan
$profile = Invoke-JsonApi -Uri "$BaseUrl/auth/me" -Method GET -Headers $headers
if (-not ($profile.roles -contains "DRIVER")) {
    throw "Authenticated profile does not contain DRIVER."
}
$profile | Format-List id, name, email, enabled, roles

Write-Host "4. Open Driver dashboard" -ForegroundColor Cyan
$dashboard = Invoke-JsonApi -Uri "$BaseUrl/dashboard" -Method GET -Headers $headers
$dashboard | Format-List activeVehicles, availableVehicles, activeTrips, pendingTrips, fleetUtilizationPercent
Write-Host "Dashboard access succeeded" -ForegroundColor Green

Write-Host "5. Read trips as Driver" -ForegroundColor Cyan
$trips = Invoke-JsonApi -Uri "$BaseUrl/trips?page=0&size=5" -Method GET -Headers $headers
Write-Host "Trips access succeeded. Returned $($trips.content.Count) item(s)." -ForegroundColor Green

Write-Host "6. Confirm Driver cannot access Admin users" -ForegroundColor Cyan
try {
    Invoke-WebRequest -Uri "$BaseUrl/admin/users" -Method GET -Headers $headers -ErrorAction Stop | Out-Null
    throw "RBAC failure: DRIVER unexpectedly accessed /admin/users."
}
catch {
    $statusCode = Get-FailedStatusCode -ErrorRecord $_
    if ($statusCode -eq 403) {
        Write-Host "Admin endpoint correctly blocked with 403" -ForegroundColor Green
    }
    elseif ($_.Exception.Message -like "RBAC failure:*") {
        throw
    }
    else {
        Write-Host "Expected 403 but received: $statusCode" -ForegroundColor Red
        if ($_.ErrorDetails.Message) { Write-Host $_.ErrorDetails.Message -ForegroundColor Red }
        throw
    }
}

Write-Host "7. Logout" -ForegroundColor Cyan
$logout = Invoke-JsonApi -Uri "$BaseUrl/auth/logout" -Method POST -Headers $headers -Body @{
    refreshToken = $login.refreshToken
    allDevices   = $false
}
Write-Host $logout.message -ForegroundColor Green

Write-Host "Driver signup, login, dashboard access, and RBAC test completed successfully." -ForegroundColor Green

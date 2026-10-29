$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'local-admin.ps1')
$adminPassword = Get-PeopleDeskAdminPassword
$body = @{username='admin';password=$adminPassword} | ConvertTo-Json
$login = Invoke-RestMethod -Uri 'http://localhost:8080/api/login' -Method Post -ContentType 'application/json' -Body $body -SessionVariable peopleSession
$headers = @{'X-CSRF-Token'=$login.csrfToken}
try {
    $existing = @(Invoke-RestMethod -Uri 'http://localhost:8080/api/employees' -WebSession $peopleSession)
    $samples = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'backend/database/sample-employees.json') -Raw | ConvertFrom-Json
    $added = 0
    foreach ($employee in $samples) {
        if ($existing.email -contains $employee.email) { continue }
        # The database allocates IDs; the frontend sample IDs are not copied.
        $record = $employee | Select-Object name,email,phone,department,designation,salary,joiningDate
        Invoke-RestMethod -Uri 'http://localhost:8080/api/employees' -Method Post -ContentType 'application/json' -Body ($record | ConvertTo-Json) -Headers $headers -WebSession $peopleSession | Out-Null
        $added++
    }
    Write-Host "Imported $added fictional employees. Existing employee emails were skipped."
} finally {
    Invoke-RestMethod -Uri 'http://localhost:8080/api/logout' -Method Post -Headers $headers -WebSession $peopleSession | Out-Null
}

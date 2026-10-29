$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/api'
. (Join-Path $PSScriptRoot 'local-admin.ps1')
$password = Get-PeopleDeskAdminPassword
$login = Invoke-RestMethod -Uri "$base/login" -Method Post -ContentType 'application/json' -Body (@{username='admin';password=$password}|ConvertTo-Json) -SessionVariable verificationSession
$headers = @{'X-CSRF-Token'=$login.csrfToken}
$testId = $null
try {
    $initial = Invoke-RestMethod -Uri "$base/employees" -WebSession $verificationSession
    $record = @{name='Database Verification';email=('verification.'+[Guid]::NewGuid().ToString('N')+'@example.com');phone='9876543999';department='Engineering';designation='Test Record';salary=12345.67;joiningDate='2026-10-29'}
    $created = Invoke-RestMethod -Uri "$base/employees" -Method Post -ContentType 'application/json' -Body ($record|ConvertTo-Json) -Headers $headers -WebSession $verificationSession
    $testId = $created.id
    $record.name = 'Updated Database Verification'
    Invoke-RestMethod -Uri "$base/employees/$testId" -Method Put -ContentType 'application/json' -Body ($record|ConvertTo-Json) -Headers $headers -WebSession $verificationSession | Out-Null
    Write-Host 'Create and update succeeded. Restarting MySQL to verify disk persistence...'
    & (Join-Path $PSScriptRoot 'stop-database.ps1')
    & (Join-Path $PSScriptRoot 'start-database.ps1')
    $afterRestart = Invoke-RestMethod -Uri "$base/employees" -WebSession $verificationSession
    $persisted = $afterRestart | Where-Object id -eq $testId
    if (-not $persisted -or $persisted.name -ne $record.name -or [decimal]$persisted.salary -ne [decimal]12345.67) { throw 'MySQL persistence verification failed.' }
    Invoke-RestMethod -Uri "$base/employees/$testId" -Method Delete -Headers $headers -WebSession $verificationSession | Out-Null
    $testId = $null
    $final = Invoke-RestMethod -Uri "$base/employees" -WebSession $verificationSession
    if (@($final).Count -ne @($initial).Count) { throw 'Record count changed unexpectedly during verification.' }
    Write-Host "PASS: real MySQL create, read, update, delete, and persistence after restart. $(@($final).Count) employee records retained."
} finally {
    if ($null -ne $testId) { Invoke-RestMethod -Uri "$base/employees/$testId" -Method Delete -Headers $headers -WebSession $verificationSession | Out-Null }
    Invoke-RestMethod -Uri "$base/logout" -Method Post -Headers $headers -WebSession $verificationSession | Out-Null
}

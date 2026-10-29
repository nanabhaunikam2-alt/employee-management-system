param([switch]$PhoneAccess)
$ErrorActionPreference = 'Stop'
$env:BIND_ADDRESS = '127.0.0.1'
if ($PhoneAccess) {
    $wifi = [Net.NetworkInformation.NetworkInterface]::GetAllNetworkInterfaces() | Where-Object { $_.NetworkInterfaceType -eq 'Wireless80211' -and $_.OperationalStatus -eq 'Up' } | Select-Object -First 1
    if (-not $wifi) { throw 'Connect the laptop to Wi-Fi before starting phone access.' }
    $address = $wifi.GetIPProperties().UnicastAddresses | Where-Object { $_.Address.AddressFamily -eq 'InterNetwork' -and -not $_.Address.ToString().StartsWith('169.254.') } | Select-Object -First 1
    if (-not $address) { throw 'The Wi-Fi adapter has no usable IPv4 address.' }
    $env:BIND_ADDRESS = $address.Address.ToString()
    Write-Host "Phone and laptop URL: http://$($env:BIND_ADDRESS):8080 (same Wi-Fi required)."
}
& (Join-Path $PSScriptRoot 'start-database.ps1')
$settings = Import-Clixml -LiteralPath (Join-Path $PSScriptRoot '.tools/database-settings.xml')
$env:DB_USER = 'peopledesk'
$env:DB_PASSWORD = [Net.NetworkCredential]::new('', $settings.AppPassword).Password
$env:DB_URL = 'jdbc:mysql://127.0.0.1:3307/employee_management?connectionTimeZone=Asia/Kolkata'
$env:ADMIN_USERNAME = 'admin'
. (Join-Path $PSScriptRoot 'local-admin.ps1')
$env:ADMIN_PASSWORD = Get-PeopleDeskAdminPassword -Create
& (Join-Path $PSScriptRoot 'start-backend.ps1')

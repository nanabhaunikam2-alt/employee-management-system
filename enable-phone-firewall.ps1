# Run once in an Administrator PowerShell window if Windows blocks phone access.
# This allows only this project's Java process, TCP port 8080, and local-subnet Wi-Fi traffic.
$ErrorActionPreference = 'Stop'
$name = 'PeopleDesk-Phone-WiFi-8080'
$java = Join-Path $PSScriptRoot '.tools/jdk/bin/java.exe'
if (-not (Test-Path -LiteralPath $java)) { throw 'Project Java is missing.' }
$existing = Get-NetFirewallRule -Name $name -ErrorAction SilentlyContinue
if (-not $existing) {
    New-NetFirewallRule -Name $name -DisplayName 'PeopleDesk phone access on Wi-Fi' -Direction Inbound -Action Allow -Protocol TCP -LocalPort 8080 -RemoteAddress LocalSubnet -InterfaceAlias 'Wi-Fi' -Program $java -Profile Any -ErrorAction Stop | Out-Null
}
Write-Host 'PeopleDesk Wi-Fi firewall rule is ready.'

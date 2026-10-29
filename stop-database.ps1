$ErrorActionPreference = 'Stop'
$tools = Join-Path $PSScriptRoot '.tools'
$settings = Import-Clixml -LiteralPath (Join-Path $tools 'database-settings.xml')
$password = [Net.NetworkCredential]::new('', $settings.RootPassword).Password
$clientPath = Join-Path $tools 'mysql-stop.ini'
try {
    @"
[client]
host=127.0.0.1
port=3307
user=root
password=$password
"@ | Set-Content -LiteralPath $clientPath -Encoding utf8
    & (Join-Path $tools 'mysql-8.4.13-winx64/bin/mysqladmin.exe') "--defaults-extra-file=$clientPath" shutdown
    if ($LASTEXITCODE -ne 0) { throw 'Could not stop the project database.' }
    Write-Host 'Project MySQL stopped safely.'
} finally { if (Test-Path -LiteralPath $clientPath) { Remove-Item -LiteralPath $clientPath }; $password = $null }

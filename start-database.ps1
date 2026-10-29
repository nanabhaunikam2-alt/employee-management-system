$ErrorActionPreference = 'Stop'
$tools = Join-Path $PSScriptRoot '.tools'
$mysqlHome = Join-Path $tools 'mysql-8.4.13-winx64'
$server = Join-Path $mysqlHome 'bin/mysqld.exe'
$client = Join-Path $mysqlHome 'bin/mysql.exe'
$data = Join-Path $tools 'mysql-data'
$settingsPath = Join-Path $tools 'database-settings.xml'
$optionsPath = Join-Path $tools 'mysql.ini'
$bootstrapPath = Join-Path $tools 'mysql-bootstrap.sql'
$clientPath = Join-Path $tools 'mysql-client.ini'
if (-not (Test-Path -LiteralPath $server)) { throw 'MySQL portable package is missing.' }
if (-not (Test-Path -LiteralPath $settingsPath)) {
    # Export-Clixml encrypts SecureString values with Windows DPAPI for this Windows account.
    $settings = [pscustomobject]@{
        RootPassword = ConvertTo-SecureString ([Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(24))) -AsPlainText -Force
        AppPassword = ConvertTo-SecureString ([Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(24))) -AsPlainText -Force
    }
    $settings | Export-Clixml -LiteralPath $settingsPath
}
$settings = Import-Clixml -LiteralPath $settingsPath
$rootPassword = [Net.NetworkCredential]::new('', $settings.RootPassword).Password
$appPassword = [Net.NetworkCredential]::new('', $settings.AppPassword).Password
if (-not (Test-Path -LiteralPath (Join-Path $data 'mysql'))) {
    New-Item -ItemType Directory -Path $data -Force | Out-Null
    Write-Host 'Initializing the project database...'
    & $server --no-defaults "--basedir=$mysqlHome" "--datadir=$data" --initialize-insecure --console
    if ($LASTEXITCODE -ne 0) { throw 'MySQL initialization failed. See the message above.' }
}
$forwardHome = $mysqlHome.Replace('\','/')
$forwardData = $data.Replace('\','/')
$log = (Join-Path $tools 'mysql-error.log').Replace('\','/')
@"
[mysqld]
basedir="$forwardHome"
datadir="$forwardData"
bind-address=127.0.0.1
port=3307
mysqlx=0
log-error="$log"
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
"@ | Set-Content -LiteralPath $optionsPath -Encoding utf8
@"
[client]
host=127.0.0.1
port=3307
user=root
password=$rootPassword
"@ | Set-Content -LiteralPath $clientPath -Encoding utf8
try {
    & $client "--defaults-extra-file=$clientPath" --connect-timeout=2 --batch --skip-column-names --execute='SELECT 1' 2>$null | Out-Null
    $running = $LASTEXITCODE -eq 0
    if (-not $running) {
        # Only generated hexadecimal passwords enter this SQL file. It is removed after startup.
        @"
ALTER USER 'root'@'localhost' IDENTIFIED BY '$rootPassword';
CREATE DATABASE IF NOT EXISTS employee_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'peopledesk'@'localhost' IDENTIFIED BY '$appPassword';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE ON employee_management.* TO 'peopledesk'@'localhost';
"@ | Set-Content -LiteralPath $bootstrapPath -Encoding utf8
        $owned = Start-Process -FilePath $server -ArgumentList @("--defaults-file=`"$optionsPath`"", "--init-file=`"$bootstrapPath`"") -WindowStyle Hidden -PassThru
        for ($attempt=0; $attempt -lt 60; $attempt++) {
            Start-Sleep -Milliseconds 1000
            if ($owned.HasExited) { throw 'MySQL stopped during startup. Inspect .tools/mysql-error.log.' }
            & $client "--defaults-extra-file=$clientPath" --connect-timeout=2 --batch --skip-column-names --execute='SELECT 1' 2>$null | Out-Null
            if ($LASTEXITCODE -eq 0) { $running = $true; break }
        }
        if (-not $running) { throw 'MySQL did not become ready. Inspect .tools/mysql-error.log.' }
        $owned.Id | Set-Content -LiteralPath (Join-Path $tools 'mysql.pid')
    }
    Write-Host 'MySQL is ready at 127.0.0.1:3307. Database: employee_management.'
} finally {
    if (Test-Path -LiteralPath $bootstrapPath) { Remove-Item -LiteralPath $bootstrapPath }
    if (Test-Path -LiteralPath $clientPath) { Remove-Item -LiteralPath $clientPath }
    $rootPassword = $null; $appPassword = $null
}

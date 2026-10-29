$ErrorActionPreference = 'Stop'
$tools = Join-Path $PSScriptRoot '.tools'
$directory = Join-Path $tools 'mysql-8.4.13-winx64'
if (Test-Path -LiteralPath (Join-Path $directory 'bin/mysqld.exe')) { Write-Host 'Project-local MySQL is already available.'; return }
New-Item -ItemType Directory -Path $tools -Force | Out-Null
$archive = Join-Path $tools 'mysql.zip'
Write-Host 'Downloading the official MySQL 8.4.13 Windows ZIP (about 268 MB)...'
Invoke-WebRequest -Uri 'https://cdn.mysql.com/Downloads/MySQL-8.4/mysql-8.4.13-winx64.zip' -OutFile $archive -TimeoutSec 600
# Published vendor checksum for archive integrity; download is over HTTPS.
if ((Get-FileHash -LiteralPath $archive -Algorithm MD5).Hash -ne '7b94489d5524dea9b27097068bea25a8') { throw 'MySQL archive checksum mismatch.' }
Expand-Archive -LiteralPath $archive -DestinationPath $tools -Force
Write-Host 'Project-local MySQL is ready.'

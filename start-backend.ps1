$ErrorActionPreference = 'Stop'
$jdk = Join-Path $PSScriptRoot '.tools/jdk'
$maven = Join-Path $PSScriptRoot '.tools/apache-maven-3.9.11/bin/mvn.cmd'
if (Test-Path -LiteralPath $jdk) { $env:JAVA_HOME = $jdk; $env:PATH = (Join-Path $jdk 'bin') + ';' + $env:PATH }
if (-not (Test-Path -LiteralPath $maven)) {
    $command = Get-Command mvn -ErrorAction SilentlyContinue
    if (-not $command) { throw 'Run setup-tools.ps1 first, or install Java and Maven.' }
    $maven = $command.Source
}
if (-not $env:DB_PASSWORD) { throw 'Set DB_PASSWORD first. See backend/README.md.' }
Push-Location (Join-Path $PSScriptRoot 'backend')
try { & $maven -B '-Dmaven.repo.local=../.tools/m2' compile exec:java; if ($LASTEXITCODE -ne 0) { throw 'The backend could not start. See the error above.' } }
finally { Pop-Location }

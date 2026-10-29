$ErrorActionPreference = 'Stop'
$jdk = Join-Path $PSScriptRoot '.tools/jdk'
$maven = Join-Path $PSScriptRoot '.tools/apache-maven-3.9.11/bin/mvn.cmd'
if (Test-Path -LiteralPath $jdk) { $env:JAVA_HOME = $jdk; $env:PATH = (Join-Path $jdk 'bin') + ';' + $env:PATH }
if (-not (Test-Path -LiteralPath $maven)) { throw 'Run setup-tools.ps1 first.' }
Push-Location (Join-Path $PSScriptRoot 'backend')
try { & $maven -B '-Dmaven.repo.local=../.tools/m2' test; if ($LASTEXITCODE -ne 0) { throw 'Backend tests failed.' } }
finally { Pop-Location }

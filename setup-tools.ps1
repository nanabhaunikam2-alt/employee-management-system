$ErrorActionPreference = 'Stop'
$toolRoot = Join-Path $PSScriptRoot '.tools'
New-Item -ItemType Directory -Path $toolRoot -Force | Out-Null
if (-not (Test-Path -LiteralPath (Join-Path $toolRoot 'jdk'))) {
    Write-Host 'Downloading a portable JDK 21 into this project...'
    $assets = Invoke-RestMethod -Uri 'https://api.adoptium.net/v3/assets/latest/21/hotspot?architecture=x64&image_type=jdk&os=windows&vendor=eclipse' -TimeoutSec 60
    $package = $assets[0].binary.package
    $archive = Join-Path $toolRoot 'jdk.zip'
    Invoke-WebRequest -Uri $package.link -OutFile $archive -TimeoutSec 600
    if ((Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash -ne $package.checksum) { throw 'JDK checksum mismatch' }
    $jdkStage = Join-Path $toolRoot 'jdk-extracted'
    Expand-Archive -LiteralPath $archive -DestinationPath $jdkStage -Force
    $jdkFolder = Get-ChildItem -LiteralPath $jdkStage -Directory | Select-Object -First 1
    Move-Item -LiteralPath $jdkFolder.FullName -Destination (Join-Path $toolRoot 'jdk')
}
if (-not (Test-Path -LiteralPath (Join-Path $toolRoot 'apache-maven-3.9.11'))) {
    Write-Host 'Downloading portable Apache Maven...'
    $mavenUrl = 'https://archive.apache.org/dist/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.zip'
    $archive = Join-Path $toolRoot 'maven.zip'
    Invoke-WebRequest -Uri $mavenUrl -OutFile $archive -TimeoutSec 180
    $checksum = (Invoke-WebRequest -Uri ($mavenUrl + '.sha512') -TimeoutSec 60).Content
    if ($checksum -is [byte[]]) { $checksum = [Text.Encoding]::UTF8.GetString($checksum) }
    $expected = ([regex]::Match($checksum, '[a-fA-F0-9]{128}')).Value
    if (-not $expected -or (Get-FileHash -LiteralPath $archive -Algorithm SHA512).Hash -ne $expected) { throw 'Maven checksum mismatch' }
    Expand-Archive -LiteralPath $archive -DestinationPath $toolRoot -Force
}
Write-Host 'Project-local Java and Maven are ready. No system PATH changes were made.'

# Dot-source this file; no credentials are printed when it is loaded.
function Get-PeopleDeskAdminPassword {
    param([switch]$Create)
    if ($env:ADMIN_PASSWORD) { return $env:ADMIN_PASSWORD }
    $path = Join-Path $PSScriptRoot '.tools/database-settings.xml'
    $settings = Import-Clixml -LiteralPath $path
    if ($settings.PSObject.Properties.Name -contains 'AdminPassword') {
        return [Net.NetworkCredential]::new('', $settings.AdminPassword).Password
    }
    if (-not $Create) { throw 'Set ADMIN_PASSWORD or run start-website.ps1 to configure your local administrator password.' }
    $secure = Read-Host 'Choose your administrator password (12-128 characters)' -AsSecureString
    $plain = [Net.NetworkCredential]::new('', $secure).Password
    if ($plain.Length -lt 12 -or $plain.Length -gt 128) { throw 'Administrator password must contain 12-128 characters.' }
    $settings | Add-Member -NotePropertyName AdminPassword -NotePropertyValue $secure
    $settings | Export-Clixml -LiteralPath $path
    return $plain
}

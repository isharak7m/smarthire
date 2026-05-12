$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $projectRoot "backend"
$frontendDir = Join-Path $projectRoot "frontend"
$runtimeDir = Join-Path $projectRoot "runtime"
$backendLog = Join-Path $runtimeDir "backend.log"
$backendErrLog = Join-Path $runtimeDir "backend.err.log"
$frontendLog = Join-Path $runtimeDir "frontend.log"
$frontendErrLog = Join-Path $runtimeDir "frontend.err.log"
$mvnPath = (Get-Command mvn).Source
$pythonPath = (Get-Command python).Source

New-Item -ItemType Directory -Force -Path $runtimeDir | Out-Null
Remove-Item -Force -ErrorAction SilentlyContinue $backendLog, $backendErrLog, $frontendLog, $frontendErrLog

Write-Host "Starting SmartHire backend on http://localhost:8081 ..."
Start-Process -FilePath $mvnPath `
  -ArgumentList "spring-boot:run" `
  -WorkingDirectory $backendDir `
  -RedirectStandardOutput $backendLog `
  -RedirectStandardError $backendErrLog `
  -WindowStyle Hidden

Write-Host "Starting SmartHire frontend on http://localhost:5500 ..."
Start-Process -FilePath $pythonPath `
  -ArgumentList "-m", "http.server", "5500" `
  -WorkingDirectory $frontendDir `
  -RedirectStandardOutput $frontendLog `
  -RedirectStandardError $frontendErrLog `
  -WindowStyle Hidden

Write-Host ""
Write-Host "SmartHire should be available at:"
Write-Host "  Frontend: http://localhost:5500"
Write-Host "  Backend:  http://localhost:8081/api"
Write-Host ""
Write-Host "Demo login: admin / Admin@123"
Write-Host "Logs:"
Write-Host "  runtime\\backend.log"
Write-Host "  runtime\\backend.err.log"
Write-Host "  runtime\\frontend.log"
Write-Host "  runtime\\frontend.err.log"

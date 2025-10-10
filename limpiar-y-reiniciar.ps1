#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Limpia cache de MSAL y reinicia la aplicacion

.DESCRIPTION
    Este script detiene el servidor Angular, abre la herramienta de limpieza,
    y luego reinicia el servidor limpio.
#>

Write-Host ""
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "        LIMPIEZA DE MSAL Y REINICIO DE APLICACION              " -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host ""

# Paso 1: Detener cualquier proceso de Angular que esté corriendo
Write-Host "Deteniendo procesos de Angular..." -ForegroundColor Yellow
$ngProcesses = Get-Process | Where-Object { $_.ProcessName -like "*node*" -and $_.CommandLine -like "*ng serve*" }
if ($ngProcesses) {
    Stop-Process -Name node -Force -ErrorAction SilentlyContinue
    Write-Host "Procesos detenidos" -ForegroundColor Green
    Start-Sleep -Seconds 2
} else {
    Write-Host "No hay procesos de Angular ejecutandose" -ForegroundColor Gray
}

# Paso 2: Abrir herramienta de limpieza
Write-Host ""
Write-Host "Abriendo herramienta de limpieza..." -ForegroundColor Cyan
$htmlPath = Join-Path $PSScriptRoot "limpiar-msal-rapido.html"

if (Test-Path $htmlPath) {
    Start-Process $htmlPath
    Write-Host ""
    Write-Host "INSTRUCCIONES:" -ForegroundColor Yellow
    Write-Host "1. Se abrira una pagina en tu navegador" -ForegroundColor White
    Write-Host "2. Haz clic en el boton 'Limpiar TODO'" -ForegroundColor White
    Write-Host "3. Vuelve aqui y presiona ENTER para continuar" -ForegroundColor White
    Write-Host ""
    
    Read-Host "Presiona ENTER cuando hayas limpiado la cache..."
} else {
    Write-Host "Archivo limpiar-msal-rapido.html no encontrado" -ForegroundColor Red
    Write-Host "Limpia manualmente el localStorage del navegador" -ForegroundColor Yellow
    Read-Host "Presiona ENTER cuando hayas limpiado..."
}

# Paso 3: Iniciar Angular
Write-Host ""
Write-Host "Iniciando servidor Angular..." -ForegroundColor Cyan
Write-Host "Servidor en: http://localhost:4200" -ForegroundColor Green
Write-Host ""
Write-Host "Presiona Ctrl+C para detener el servidor" -ForegroundColor Yellow
Write-Host ""

Start-Process "ng" -ArgumentList "serve" -NoNewWindow -Wait


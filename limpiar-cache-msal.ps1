#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Limpia la cache de MSAL y cookies del navegador para evitar redirecciones no deseadas

.DESCRIPTION
    Este script proporciona instrucciones para limpiar manualmente el localStorage
    y las cookies del navegador que MSAL usa para almacenar tokens y estado.

.NOTES
    Autor: Sistema de Autenticacion Dual
    Fecha: 2025-10-10
#>

Write-Host ""
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "        LIMPIEZA DE CACHE DE MSAL Y AZURE AD                    " -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Este script te ayudara a limpiar la configuracion de MSAL" -ForegroundColor Yellow
Write-Host ""

# Verificar si Chrome esta en ejecucion
$chromeProcess = Get-Process -Name chrome -ErrorAction SilentlyContinue

if ($chromeProcess) {
    Write-Host "Chrome esta actualmente ejecutandose" -ForegroundColor Yellow
    Write-Host "   Es recomendable cerrar Chrome antes de limpiar la cache" -ForegroundColor Yellow
    Write-Host ""
    
    $response = Read-Host "Deseas cerrar Chrome ahora? (S/N)"
    
    if ($response -eq 'S' -or $response -eq 's') {
        Write-Host "Cerrando Chrome..." -ForegroundColor Cyan
        Stop-Process -Name chrome -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 2
        Write-Host "Chrome cerrado" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "INSTRUCCIONES PARA LIMPIAR MSAL:" -ForegroundColor Cyan
Write-Host ""
Write-Host "1.  Abre Chrome y presiona F12 (DevTools)" -ForegroundColor White
Write-Host "2.  Ve a la pestana 'Application' o 'Aplicacion'" -ForegroundColor White
Write-Host "3.  En el menu izquierdo, expande 'Local Storage'" -ForegroundColor White
Write-Host "4.  Selecciona 'http://localhost:4200'" -ForegroundColor White
Write-Host "5.  Busca y elimina todas las claves que contengan:" -ForegroundColor White
Write-Host "    - msal" -ForegroundColor Yellow
Write-Host "    - login.windows.net" -ForegroundColor Yellow
Write-Host "    - login.microsoftonline.com" -ForegroundColor Yellow
Write-Host "6.  Tambien en 'Session Storage', elimina las mismas claves" -ForegroundColor White
Write-Host "7.  Ve a 'Cookies' y elimina cookies de:" -ForegroundColor White
Write-Host "    - login.microsoftonline.com" -ForegroundColor Yellow
Write-Host "    - localhost:4200" -ForegroundColor Yellow
Write-Host ""

Write-Host "ALTERNATIVA RAPIDA:" -ForegroundColor Cyan
Write-Host ""
Write-Host "Puedes abrir la consola del navegador (F12) y ejecutar:" -ForegroundColor White
Write-Host ""
Write-Host "  localStorage.clear();" -ForegroundColor Yellow
Write-Host "  sessionStorage.clear();" -ForegroundColor Yellow
Write-Host "  location.reload();" -ForegroundColor Yellow
Write-Host ""

$response = Read-Host "Presiona ENTER cuando hayas limpiado la cache..."

Write-Host ""
Write-Host "Cache limpiada. Ahora puedes:" -ForegroundColor Green
Write-Host "   1. Iniciar la aplicacion Angular (ng serve)" -ForegroundColor White
Write-Host "   2. Abrir http://localhost:4200" -ForegroundColor White
Write-Host "   3. Deberias ser redirigido a /login (autenticacion local)" -ForegroundColor White
Write-Host ""
Write-Host "Si aun experimentas problemas:" -ForegroundColor Yellow
Write-Host "   - Verifica que el backend este ejecutandose" -ForegroundColor White
Write-Host "   - Confirma que JWT_LOCAL_HABILITADO=true en application.properties" -ForegroundColor White
Write-Host "   - Verifica la consola del navegador para ver logs" -ForegroundColor White
Write-Host ""

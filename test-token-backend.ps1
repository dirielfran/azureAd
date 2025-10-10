#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Prueba el endpoint de autenticacion con token invalido
#>

Write-Host ""
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "        TEST DE AUTENTICACION JWT BACKEND                       " -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host ""

# Paso 1: Verificar que el backend este corriendo
Write-Host "1. Verificando que el backend este corriendo..." -ForegroundColor Yellow
try {
    $statusResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/config/auth/status" -Method GET -UseBasicParsing -ErrorAction Stop
    Write-Host "   Backend respondiendo correctamente" -ForegroundColor Green
    $status = $statusResponse.Content | ConvertFrom-Json
    Write-Host "   - JWT Local: $($status.jwtLocalHabilitado)" -ForegroundColor $(if($status.jwtLocalHabilitado) { "Green" } else { "Red" })
    Write-Host "   - Azure AD: $($status.azureAdHabilitado)" -ForegroundColor $(if($status.azureAdHabilitado) { "Yellow" } else { "Green" })
} catch {
    Write-Host "   ERROR: Backend no esta corriendo" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Paso 2: Intentar login
Write-Host "2. Intentando login con credenciales correctas..." -ForegroundColor Yellow
try {
    $loginBody = @{
        email = "admin@local.com"
        password = "admin123"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/local/login" -Method POST -Body $loginBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "   Login exitoso!" -ForegroundColor Green
    $token = $loginResponse.token
    Write-Host "   Token obtenido (primeros 50 chars): $($token.Substring(0, [Math]::Min(50, $token.Length)))..." -ForegroundColor Cyan
} catch {
    Write-Host "   ERROR en login: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Paso 3: Probar endpoint de informacion de usuario con token VALIDO
Write-Host "3. Probando endpoint de informacion con token VALIDO..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $token"
    }
    
    $userInfoResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/autorizacion/informacion-usuario" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "   Informacion del usuario obtenida!" -ForegroundColor Green
    Write-Host "   - Email: $($userInfoResponse.email)" -ForegroundColor Cyan
    Write-Host "   - Nombre: $($userInfoResponse.nombre)" -ForegroundColor Cyan
    Write-Host "   - Permisos: $($userInfoResponse.codigosPermisos.Count)" -ForegroundColor Cyan
} catch {
    Write-Host "   ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
}

Write-Host ""

# Paso 4: Probar endpoint con token INVALIDO
Write-Host "4. Probando endpoint con token INVALIDO..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer token.invalido.aqui"
    }
    
    $invalidResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/autorizacion/informacion-usuario" -Method GET -Headers $headers -ErrorAction Stop
    Write-Host "   ADVERTENCIA: El backend acepto un token invalido!" -ForegroundColor Yellow
} catch {
    Write-Host "   Token invalido correctamente rechazado" -ForegroundColor Green
    Write-Host "   Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "Test completado" -ForegroundColor Green
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host ""


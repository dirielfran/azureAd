# ================================================================
# 🔍 SCRIPT DE DIAGNÓSTICO - AUTENTICACIÓN LOCAL
# ================================================================

Write-Host "🔍 Iniciando diagnóstico de autenticación..." -ForegroundColor Cyan
Write-Host ""

# ================================================================
# 1. VERIFICAR QUE EL BACKEND ESTÉ CORRIENDO
# ================================================================
Write-Host "1️⃣ Verificando que el backend esté corriendo..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/config/auth/status" -Method GET -ErrorAction Stop
    Write-Host "   ✅ Backend está corriendo" -ForegroundColor Green
    Write-Host "   📊 Estado de autenticación:" -ForegroundColor Cyan
    Write-Host "      - Azure AD: $($response.azureAdHabilitado)" -ForegroundColor White
    Write-Host "      - JWT Local: $($response.jwtLocalHabilitado)" -ForegroundColor White
    Write-Host ""
    
    # Verificar configuración
    if ($response.azureAdHabilitado -eq $true -and $response.jwtLocalHabilitado -eq $true) {
        Write-Host "   ⚠️  ADVERTENCIA: Ambos métodos están habilitados" -ForegroundColor Yellow
        Write-Host "      Solo uno debe estar activo a la vez" -ForegroundColor Yellow
        Write-Host ""
    }
    
    if ($response.jwtLocalHabilitado -eq $false) {
        Write-Host "   ❌ JWT Local está deshabilitado" -ForegroundColor Red
        Write-Host "      Ejecuta: .\habilitar-jwt-local.ps1" -ForegroundColor Yellow
        Write-Host ""
        exit 1
    }
} catch {
    Write-Host "   ❌ Error: No se puede conectar al backend" -ForegroundColor Red
    Write-Host "      Verifica que esté corriendo en http://localhost:8080" -ForegroundColor Yellow
    Write-Host "      Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    exit 1
}

# ================================================================
# 2. PROBAR LOGIN CON CREDENCIALES
# ================================================================
Write-Host "2️⃣ Probando login con credenciales de prueba..." -ForegroundColor Yellow

$credentials = @(
    @{ email = "admin@local.com"; password = "admin123" },
    @{ email = "user@local.com"; password = "admin123" },
    @{ email = "guest@local.com"; password = "admin123" }
)

$loginSuccess = $false

foreach ($cred in $credentials) {
    Write-Host "   🔐 Probando: $($cred.email) / $($cred.password)" -ForegroundColor Cyan
    
    $base64Creds = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("$($cred.email):$($cred.password)"))
    
    try {
        $headers = @{
            "Authorization" = "Basic $base64Creds"
            "Content-Type" = "application/json"
        }
        
        $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Headers $headers -Body "{}" -ErrorAction Stop
        
        Write-Host "      ✅ Login exitoso!" -ForegroundColor Green
        Write-Host "      🎫 Token recibido: $($loginResponse.token.Substring(0, 50))..." -ForegroundColor Green
        Write-Host ""
        $loginSuccess = $true
        $workingCredentials = $cred
        break
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq 401) {
            Write-Host "      ❌ Credenciales incorrectas" -ForegroundColor Red
        } else {
            Write-Host "      ❌ Error: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host ""

if (-not $loginSuccess) {
    Write-Host "❌ Ninguna credencial funcionó" -ForegroundColor Red
    Write-Host ""
    Write-Host "🔧 Posibles soluciones:" -ForegroundColor Yellow
    Write-Host "   1. Verifica que los usuarios existan en la base de datos" -ForegroundColor White
    Write-Host "      Accede a: http://localhost:8080/api/h2-console" -ForegroundColor Cyan
    Write-Host "      JDBC URL: jdbc:h2:mem:testdb" -ForegroundColor Cyan
    Write-Host "      Usuario: sa | Password: password" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "   2. Ejecuta esta query en H2 Console:" -ForegroundColor White
    Write-Host "      SELECT email, password, activo FROM usuarios WHERE email LIKE '%@local.com';" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "   3. Si no hay usuarios, verifica que data.sql se esté ejecutando" -ForegroundColor White
    Write-Host ""
    exit 1
}

# ================================================================
# 3. PROBAR ENDPOINT DE PERMISOS
# ================================================================
Write-Host "3️⃣ Probando carga de permisos del usuario..." -ForegroundColor Yellow

try {
    $headers = @{
        "Authorization" = $loginResponse.token
        "Content-Type" = "application/json"
    }
    
    $userInfo = Invoke-RestMethod -Uri "http://localhost:8080/api/autorizacion/informacion-usuario" -Method GET -Headers $headers -ErrorAction Stop
    
    Write-Host "   ✅ Permisos cargados correctamente" -ForegroundColor Green
    Write-Host "   👤 Usuario: $($userInfo.nombre)" -ForegroundColor Cyan
    Write-Host "   📧 Email: $($userInfo.email)" -ForegroundColor Cyan
    Write-Host "   🔑 Permisos: $($userInfo.codigosPermisos.Count)" -ForegroundColor Cyan
    Write-Host ""
} catch {
    Write-Host "   ❌ Error al cargar permisos" -ForegroundColor Red
    Write-Host "      $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "   🔧 Posible solución:" -ForegroundColor Yellow
    Write-Host "      El usuario debe tener perfiles asignados en la BD" -ForegroundColor White
    Write-Host ""
}

# ================================================================
# 4. VERIFICAR CORS
# ================================================================
Write-Host "4️⃣ Verificando configuración CORS..." -ForegroundColor Yellow

try {
    $headers = @{
        "Origin" = "http://localhost:4200"
    }
    
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/config/auth/status" -Method OPTIONS -Headers $headers -ErrorAction Stop
    
    $corsHeader = $response.Headers["Access-Control-Allow-Origin"]
    if ($corsHeader) {
        Write-Host "   ✅ CORS configurado correctamente" -ForegroundColor Green
        Write-Host "      Allow-Origin: $corsHeader" -ForegroundColor Cyan
    } else {
        Write-Host "   ⚠️  No se encontró header CORS" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ⚠️  No se pudo verificar CORS" -ForegroundColor Yellow
}

Write-Host ""

# ================================================================
# RESUMEN
# ================================================================
Write-Host "📋 RESUMEN DEL DIAGNÓSTICO" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Gray
Write-Host ""

if ($loginSuccess) {
    Write-Host "✅ El backend está funcionando correctamente" -ForegroundColor Green
    Write-Host ""
    Write-Host "🔐 Credenciales válidas:" -ForegroundColor Green
    Write-Host "   Email: $($workingCredentials.email)" -ForegroundColor White
    Write-Host "   Password: $($workingCredentials.password)" -ForegroundColor White
    Write-Host ""
    Write-Host "🎯 SIGUIENTE PASO:" -ForegroundColor Yellow
    Write-Host "   1. Abre la consola del navegador (F12)" -ForegroundColor White
    Write-Host "   2. Ve a la pestaña 'Network' o 'Red'" -ForegroundColor White
    Write-Host "   3. Intenta hacer login en el frontend" -ForegroundColor White
    Write-Host "   4. Busca errores en las peticiones HTTP" -ForegroundColor White
    Write-Host ""
    Write-Host "   Si ves error de CORS:" -ForegroundColor Cyan
    Write-Host "   - Verifica que el frontend esté en http://localhost:4200" -ForegroundColor White
    Write-Host "   - Reinicia el backend si es necesario" -ForegroundColor White
    Write-Host ""
    Write-Host "   Si la petición no se completa:" -ForegroundColor Cyan
    Write-Host "   - Verifica que no haya errores de red" -ForegroundColor White
    Write-Host "   - Revisa los logs del backend" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host "❌ Hay problemas con el backend" -ForegroundColor Red
    Write-Host "   Revisa los pasos anteriores para más detalles" -ForegroundColor Yellow
    Write-Host ""
}

Write-Host "=" * 50 -ForegroundColor Gray


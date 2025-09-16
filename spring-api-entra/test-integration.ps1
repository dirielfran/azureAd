# =============================================================================
# SCRIPT DE PRUEBA DE INTEGRACIÓN - API SPRING BOOT + AZURE AD
# =============================================================================

Write-Host "🚀 Iniciando pruebas de integración..." -ForegroundColor Green

# Variables de configuración
$API_BASE_URL = "http://localhost:8080/api"
$FRONTEND_URL = "http://localhost:4200"

# Función para hacer peticiones HTTP
function Invoke-ApiRequest {
    param(
        [string]$Url,
        [string]$Method = "GET",
        [hashtable]$Headers = @{},
        [string]$Body = $null
    )
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            Headers = $Headers
            ContentType = "application/json"
        }
        
        if ($Body) {
            $params.Body = $Body
        }
        
        $response = Invoke-RestMethod @params
        return @{
            Success = $true
            Data = $response
            StatusCode = 200
        }
    }
    catch {
        return @{
            Success = $false
            Error = $_.Exception.Message
            StatusCode = $_.Exception.Response.StatusCode.value__
        }
    }
}

# Función para mostrar resultados
function Show-TestResult {
    param(
        [string]$TestName,
        [hashtable]$Result
    )
    
    if ($Result.Success) {
        Write-Host "✅ $TestName - EXITOSO" -ForegroundColor Green
        if ($Result.Data) {
            Write-Host "   Respuesta: $($Result.Data | ConvertTo-Json -Depth 2)" -ForegroundColor Gray
        }
    } else {
        Write-Host "❌ $TestName - FALLÓ" -ForegroundColor Red
        Write-Host "   Error: $($Result.Error)" -ForegroundColor Red
        Write-Host "   Status: $($Result.StatusCode)" -ForegroundColor Red
    }
    Write-Host ""
}

# =============================================================================
# PRUEBAS BÁSICAS DE CONECTIVIDAD
# =============================================================================

Write-Host "🔍 1. PRUEBAS DE CONECTIVIDAD BÁSICA" -ForegroundColor Yellow
Write-Host "=====================================" -ForegroundColor Yellow

# Prueba 1: Información de la API (endpoint público)
$result1 = Invoke-ApiRequest -Url "$API_BASE_URL/auth/info"
Show-TestResult "Información de la API" $result1

# Prueba 2: Health Check
$result2 = Invoke-ApiRequest -Url "$API_BASE_URL/actuator/health"
Show-TestResult "Health Check" $result2

# =============================================================================
# PRUEBAS DE AUTENTICACIÓN
# =============================================================================

Write-Host "🔐 2. PRUEBAS DE AUTENTICACIÓN" -ForegroundColor Yellow
Write-Host "=============================" -ForegroundColor Yellow

# Prueba 3: Endpoint protegido sin token (debe fallar)
$result3 = Invoke-ApiRequest -Url "$API_BASE_URL/auth/user-info"
Show-TestResult "Endpoint protegido sin token (debe fallar)" $result3

# Prueba 4: Endpoint protegido con token inválido (debe fallar)
$invalidHeaders = @{
    "Authorization" = "Bearer token-invalido"
}
$result4 = Invoke-ApiRequest -Url "$API_BASE_URL/auth/user-info" -Headers $invalidHeaders
Show-TestResult "Endpoint protegido con token inválido (debe fallar)" $result4

# =============================================================================
# PRUEBAS DE ENDPOINTS DE AUTORIZACIÓN
# =============================================================================

Write-Host "🎯 3. PRUEBAS DE ENDPOINTS DE AUTORIZACIÓN" -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Yellow

# Prueba 5: Información de usuario sin token
$result5 = Invoke-ApiRequest -Url "$API_BASE_URL/autorizacion/informacion-usuario"
Show-TestResult "Información de usuario sin token (debe fallar)" $result5

# Prueba 6: Permisos de usuario sin token
$result6 = Invoke-ApiRequest -Url "$API_BASE_URL/autorizacion/permisos"
Show-TestResult "Permisos de usuario sin token (debe fallar)" $result6

# =============================================================================
# PRUEBAS DE CORS
# =============================================================================

Write-Host "🌐 4. PRUEBAS DE CORS" -ForegroundColor Yellow
Write-Host "====================" -ForegroundColor Yellow

# Prueba 7: Verificar headers CORS
try {
    $corsHeaders = @{
        "Origin" = "http://localhost:4200"
        "Access-Control-Request-Method" = "GET"
        "Access-Control-Request-Headers" = "Authorization"
    }
    
    $result7 = Invoke-WebRequest -Uri "$API_BASE_URL/auth/info" -Method OPTIONS -Headers $corsHeaders
    Write-Host "✅ Prueba CORS - EXITOSO" -ForegroundColor Green
    Write-Host "   Headers CORS encontrados:" -ForegroundColor Gray
    $result7.Headers | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
} catch {
    Write-Host "❌ Prueba CORS - FALLÓ" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# =============================================================================
# VERIFICACIÓN DE CONFIGURACIÓN
# =============================================================================

Write-Host "⚙️ 5. VERIFICACIÓN DE CONFIGURACIÓN" -ForegroundColor Yellow
Write-Host "===================================" -ForegroundColor Yellow

# Verificar que el puerto esté en uso
$port8080 = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($port8080) {
    Write-Host "✅ Puerto 8080 está en uso - API probablemente ejecutándose" -ForegroundColor Green
} else {
    Write-Host "❌ Puerto 8080 no está en uso - API no está ejecutándose" -ForegroundColor Red
}

# Verificar que el puerto 4200 esté en uso (Angular)
$port4200 = Get-NetTCPConnection -LocalPort 4200 -ErrorAction SilentlyContinue
if ($port4200) {
    Write-Host "✅ Puerto 4200 está en uso - Angular probablemente ejecutándose" -ForegroundColor Green
} else {
    Write-Host "⚠️ Puerto 4200 no está en uso - Angular no está ejecutándose" -ForegroundColor Yellow
}

Write-Host ""

# =============================================================================
# INSTRUCCIONES PARA PRUEBAS MANUALES
# =============================================================================

Write-Host "📋 6. INSTRUCCIONES PARA PRUEBAS MANUALES" -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Yellow

Write-Host "Para probar la integración completa con Azure AD:" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. 🌐 Abre el navegador y ve a: $FRONTEND_URL" -ForegroundColor White
Write-Host "2. 🔐 Inicia sesión con tu cuenta de Azure AD" -ForegroundColor White
Write-Host "3. 📊 Verifica que se muestren los permisos correctos" -ForegroundColor White
Write-Host "4. 🎯 Prueba diferentes funcionalidades según tu rol" -ForegroundColor White
Write-Host ""
Write-Host "Endpoints importantes para probar:" -ForegroundColor Cyan
Write-Host "• $API_BASE_URL/auth/info - Información de la API" -ForegroundColor Gray
Write-Host "• $API_BASE_URL/auth/user-info - Info del usuario (requiere token)" -ForegroundColor Gray
Write-Host "• $API_BASE_URL/autorizacion/informacion-usuario - Permisos del usuario" -ForegroundColor Gray
Write-Host "• $API_BASE_URL/autorizacion/permisos - Lista de permisos" -ForegroundColor Gray
Write-Host ""

# =============================================================================
# RESUMEN FINAL
# =============================================================================

Write-Host "📊 RESUMEN DE PRUEBAS" -ForegroundColor Yellow
Write-Host "====================" -ForegroundColor Yellow

$totalTests = 6
$passedTests = 0

if ($result1.Success) { $passedTests++ }
if ($result2.Success) { $passedTests++ }
if (-not $result3.Success) { $passedTests++ } # Debe fallar
if (-not $result4.Success) { $passedTests++ } # Debe fallar
if (-not $result5.Success) { $passedTests++ } # Debe fallar
if (-not $result6.Success) { $passedTests++ } # Debe fallar

Write-Host "Pruebas ejecutadas: $totalTests" -ForegroundColor White
Write-Host "Pruebas exitosas: $passedTests" -ForegroundColor Green
Write-Host "Pruebas fallidas: $($totalTests - $passedTests)" -ForegroundColor Red

if ($passedTests -eq $totalTests) {
    Write-Host ""
    Write-Host "🎉 ¡TODAS LAS PRUEBAS BÁSICAS PASARON!" -ForegroundColor Green
    Write-Host "La API está configurada correctamente y lista para pruebas con Azure AD." -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "⚠️ Algunas pruebas fallaron. Revisa la configuración." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🚀 Para ejecutar la API:" -ForegroundColor Cyan
Write-Host "   cd spring-api-entra" -ForegroundColor Gray
Write-Host "   ./mvnw spring-boot:run" -ForegroundColor Gray
Write-Host ""
Write-Host "🌐 Para ejecutar Angular:" -ForegroundColor Cyan
Write-Host "   ng serve" -ForegroundColor Gray
Write-Host ""


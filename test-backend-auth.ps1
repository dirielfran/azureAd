# ================================================================
# 🧪 TEST COMPLETO DE AUTENTICACIÓN JWT LOCAL
# ================================================================

Write-Host "🧪 Iniciando test de autenticación..." -ForegroundColor Cyan
Write-Host ""

# ================================================================
# 1. LOGIN
# ================================================================
Write-Host "1️⃣ Haciendo login..." -ForegroundColor Yellow

$email = "admin@local.com"
$password = "admin123"
$base64Creds = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${email}:${password}"))

try {
    $headers = @{
        "Authorization" = "Basic $base64Creds"
        "Content-Type" = "application/json"
    }
    
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Headers $headers -Body "{}" -ErrorAction Stop
    
    Write-Host "   ✅ Login exitoso" -ForegroundColor Green
    Write-Host "   🎫 Token: $($loginResponse.token.Substring(0, 50))..." -ForegroundColor Cyan
    Write-Host ""
    
    $token = $loginResponse.token
    
} catch {
    Write-Host "   ❌ Error en login" -ForegroundColor Red
    Write-Host "   $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# ================================================================
# 2. OBTENER INFORMACIÓN DEL USUARIO
# ================================================================
Write-Host "2️⃣ Obteniendo información del usuario..." -ForegroundColor Yellow

try {
    $headers = @{
        "Authorization" = $token
        "Content-Type" = "application/json"
    }
    
    Write-Host "   📡 Llamando a: http://localhost:8080/api/autorizacion/informacion-usuario" -ForegroundColor Cyan
    Write-Host "   🔑 Con token: $($token.Substring(0, 50))..." -ForegroundColor Cyan
    
    $userInfo = Invoke-RestMethod -Uri "http://localhost:8080/api/autorizacion/informacion-usuario" -Method GET -Headers $headers -TimeoutSec 10 -ErrorAction Stop
    
    Write-Host "   ✅ Información obtenida" -ForegroundColor Green
    Write-Host "   👤 Usuario: $($userInfo.nombre)" -ForegroundColor Cyan
    Write-Host "   📧 Email: $($userInfo.email)" -ForegroundColor Cyan
    Write-Host "   🔑 Permisos: $($userInfo.codigosPermisos.Count)" -ForegroundColor Cyan
    Write-Host "   📋 Permisos:" -ForegroundColor Cyan
    foreach ($permiso in $userInfo.codigosPermisos) {
        Write-Host "      - $permiso" -ForegroundColor White
    }
    Write-Host ""
    
    Write-Host "✅ TEST COMPLETADO EXITOSAMENTE" -ForegroundColor Green
    
} catch {
    Write-Host "   ❌ Error al obtener información del usuario" -ForegroundColor Red
    Write-Host "   Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "   Mensaje: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    
    # Intentar leer el cuerpo de la respuesta de error
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "   📄 Respuesta del servidor:" -ForegroundColor Yellow
            Write-Host "   $responseBody" -ForegroundColor White
        } catch {
            Write-Host "   No se pudo leer la respuesta del servidor" -ForegroundColor Yellow
        }
    }
    
    Write-Host ""
    Write-Host "🔧 POSIBLES CAUSAS:" -ForegroundColor Yellow
    Write-Host "   1. El backend no está procesando correctamente el token JWT local" -ForegroundColor White
    Write-Host "   2. El filtro DualAuthenticationFilter no está funcionando" -ForegroundColor White
    Write-Host "   3. El usuario no tiene perfiles asignados en la BD" -ForegroundColor White
    Write-Host ""
    Write-Host "🔍 REVISA LOS LOGS DEL BACKEND para ver el error exacto" -ForegroundColor Yellow
    Write-Host ""
}


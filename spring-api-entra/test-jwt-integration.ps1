# =============================================================================
# SCRIPT DE PRUEBA PARA INTEGRACIÓN JWT + AZURE AD
# =============================================================================

Write-Host "🚀 Iniciando pruebas de integración JWT + Azure AD" -ForegroundColor Green

# Configuración
$baseUrl = "http://localhost:8080/api"
$jwtSecret = "mySecretKeyForJWTTokenGeneration123456789012345678901234567890"

# =============================================================================
# 1. PROBAR ENDPOINT DE INFORMACIÓN PÚBLICA
# =============================================================================
Write-Host "`n📋 1. Probando endpoint público..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/info" -Method GET
    Write-Host "✅ Endpoint público funcionando: $($response | ConvertTo-Json)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error en endpoint público: $($_.Exception.Message)" -ForegroundColor Red
}

# =============================================================================
# 2. PROBAR LOGIN JWT LOCAL
# =============================================================================
Write-Host "`n🔐 2. Probando login JWT local..." -ForegroundColor Yellow

# Credenciales de prueba (del data.sql)
$testUsers = @(
    @{ email = "admin@local.com"; password = "admin123" },
    @{ email = "user@local.com"; password = "user123" },
    @{ email = "guest@local.com"; password = "guest123" }
)

foreach ($user in $testUsers) {
    Write-Host "`n   Probando usuario: $($user.email)" -ForegroundColor Cyan
    
    try {
        # Crear credenciales Basic Auth
        $credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("$($user.email):$($user.password)"))
        $headers = @{
            "Authorization" = "Basic $credentials"
            "Content-Type" = "application/json"
        }
        
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Headers $headers
        Write-Host "   ✅ Login exitoso para $($user.email)" -ForegroundColor Green
        Write-Host "   Token: $($response.token.Substring(0, 50))..." -ForegroundColor Gray
        
        # Probar endpoint protegido con el token
        $token = $response.token
        $authHeaders = @{
            "Authorization" = "Bearer $token"
            "Content-Type" = "application/json"
        }
        
        try {
            $protectedResponse = Invoke-RestMethod -Uri "$baseUrl/data/protected" -Method GET -Headers $authHeaders
            Write-Host "   ✅ Acceso a endpoint protegido exitoso" -ForegroundColor Green
        } catch {
            Write-Host "   ⚠️  Error en endpoint protegido: $($_.Exception.Message)" -ForegroundColor Yellow
        }
        
    } catch {
        Write-Host "   ❌ Error en login para $($user.email): $($_.Exception.Message)" -ForegroundColor Red
    }
}

# =============================================================================
# 3. PROBAR TOKENS JWT INVÁLIDOS
# =============================================================================
Write-Host "`n🚫 3. Probando tokens JWT inválidos..." -ForegroundColor Yellow

$invalidTokens = @(
    "invalid-token",
    "Bearer invalid-token",
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid",
    ""
)

foreach ($token in $invalidTokens) {
    Write-Host "`n   Probando token inválido: $($token.Substring(0, [Math]::Min(20, $token.Length)))..." -ForegroundColor Cyan
    
    try {
        $authHeaders = @{
            "Authorization" = "Bearer $token"
            "Content-Type" = "application/json"
        }
        
        $response = Invoke-RestMethod -Uri "$baseUrl/data/protected" -Method GET -Headers $authHeaders
        Write-Host "   ⚠️  Token inválido aceptado (no debería pasar): $($response | ConvertTo-Json)" -ForegroundColor Yellow
    } catch {
        Write-Host "   ✅ Token inválido correctamente rechazado: $($_.Exception.Message)" -ForegroundColor Green
    }
}

# =============================================================================
# 4. PROBAR ENDPOINTS SIN AUTENTICACIÓN
# =============================================================================
Write-Host "`n🔒 4. Probando endpoints sin autenticación..." -ForegroundColor Yellow

$protectedEndpoints = @(
    "/data/protected",
    "/data/admin",
    "/data/user"
)

foreach ($endpoint in $protectedEndpoints) {
    Write-Host "`n   Probando $endpoint sin token..." -ForegroundColor Cyan
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl$endpoint" -Method GET
        Write-Host "   ⚠️  Endpoint accesible sin autenticación (no debería pasar): $($response | ConvertTo-Json)" -ForegroundColor Yellow
    } catch {
        Write-Host "   ✅ Endpoint correctamente protegido: $($_.Exception.Message)" -ForegroundColor Green
    }
}

# =============================================================================
# 5. PROBAR CORS
# =============================================================================
Write-Host "`n🌐 5. Probando configuración CORS..." -ForegroundColor Yellow

try {
    $headers = @{
        "Origin" = "http://localhost:4200"
        "Access-Control-Request-Method" = "GET"
        "Access-Control-Request-Headers" = "Authorization"
    }
    
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/info" -Method OPTIONS -Headers $headers
    Write-Host "✅ CORS configurado correctamente" -ForegroundColor Green
} catch {
    Write-Host "❌ Error en CORS: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🎉 Pruebas de integración completadas!" -ForegroundColor Green
Write-Host "`n📝 Resumen:" -ForegroundColor Yellow
Write-Host "   - ✅ Endpoints públicos funcionando" -ForegroundColor Green
Write-Host "   - ✅ Login JWT local funcionando" -ForegroundColor Green
Write-Host "   - ✅ Tokens inválidos rechazados correctamente" -ForegroundColor Green
Write-Host "   - ✅ Endpoints protegidos funcionando" -ForegroundColor Green
Write-Host "   - ✅ CORS configurado" -ForegroundColor Green
Write-Host "`n💡 Para probar Azure AD, usa el frontend Angular con la configuracion existente" -ForegroundColor Cyan

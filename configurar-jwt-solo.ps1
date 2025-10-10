# ================================================================
# 🔧 CONFIGURAR SOLO JWT LOCAL (DESHABILITAR AZURE AD)
# ================================================================

Write-Host "🔧 Configurando autenticación..." -ForegroundColor Cyan
Write-Host ""

$headers = @{
    "X-Admin-Token" = "ADMIN_SECRET_TOKEN_2024"
    "Content-Type" = "application/json"
}

$body = @{
    azureEnabled = $false
    jwtLocalEnabled = $true
} | ConvertTo-Json

Write-Host "📡 Enviando configuración al backend..." -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/config/auth/config/admin" -Method POST -Headers $headers -Body $body -ErrorAction Stop
    
    Write-Host "✅ Configuración actualizada exitosamente" -ForegroundColor Green
    Write-Host ""
    Write-Host "📊 Estado actual:" -ForegroundColor Cyan
    Write-Host "   - Azure AD: $($response.azureAdHabilitado)" -ForegroundColor $(if($response.azureAdHabilitado) { "Yellow" } else { "Green" })
    Write-Host "   - JWT Local: $($response.jwtLocalHabilitado)" -ForegroundColor $(if($response.jwtLocalHabilitado) { "Green" } else { "Yellow" })
    Write-Host ""
    
    if ($response.jwtLocalHabilitado -and -not $response.azureAdHabilitado) {
        Write-Host "✨ Configuración correcta: Solo JWT Local está habilitado" -ForegroundColor Green
        Write-Host ""
        Write-Host "🎯 AHORA PUEDES:" -ForegroundColor Yellow
        Write-Host "   1. Recargar la página del frontend (Ctrl+R)" -ForegroundColor White
        Write-Host "   2. Usar las credenciales:" -ForegroundColor White
        Write-Host "      Email: admin@local.com" -ForegroundColor Cyan
        Write-Host "      Password: admin123" -ForegroundColor Cyan
        Write-Host ""
    } else {
        Write-Host "⚠️  Advertencia: La configuración no es la esperada" -ForegroundColor Yellow
        Write-Host ""
    }
} catch {
    Write-Host "❌ Error al actualizar configuración" -ForegroundColor Red
    Write-Host "   $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "🔧 Verifica que:" -ForegroundColor Yellow
    Write-Host "   1. El backend esté corriendo en http://localhost:8080" -ForegroundColor White
    Write-Host "   2. El endpoint /api/config/auth/config/admin esté disponible" -ForegroundColor White
    Write-Host ""
}


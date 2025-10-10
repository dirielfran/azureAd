# ================================================================
# 🔧 SCRIPT PARA HABILITAR AUTENTICACIÓN JWT LOCAL
# ================================================================

Write-Host "🔧 Habilitando autenticación JWT local..." -ForegroundColor Cyan
Write-Host ""

$headers = @{
    "X-Admin-Token" = "ADMIN_SECRET_TOKEN_2024"
    "Content-Type" = "application/json"
}

$body = @{
    azureEnabled = $false
    jwtLocalEnabled = $true
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/config/auth/config/admin" -Method POST -Headers $headers -Body $body -ErrorAction Stop
    
    Write-Host "✅ Configuración actualizada exitosamente" -ForegroundColor Green
    Write-Host ""
    Write-Host "📊 Estado actual:" -ForegroundColor Cyan
    Write-Host "   - Azure AD: $($response.azureAdHabilitado)" -ForegroundColor White
    Write-Host "   - JWT Local: $($response.jwtLocalHabilitado)" -ForegroundColor White
    Write-Host ""
    Write-Host "✨ JWT Local está ahora habilitado" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Error al actualizar configuración" -ForegroundColor Red
    Write-Host "   $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "🔧 Verifica que:" -ForegroundColor Yellow
    Write-Host "   1. El backend esté corriendo" -ForegroundColor White
    Write-Host "   2. El token de admin sea correcto" -ForegroundColor White
    Write-Host ""
}


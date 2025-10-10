# ================================================================
# 🔄 REINICIAR BACKEND PARA APLICAR CAMBIOS EN BASE DE DATOS
# ================================================================

Write-Host "🔄 Reiniciando backend..." -ForegroundColor Cyan
Write-Host ""

Write-Host "📋 INSTRUCCIONES:" -ForegroundColor Yellow
Write-Host "   1. Ve a la terminal donde está corriendo el backend" -ForegroundColor White
Write-Host "   2. Presiona Ctrl+C para detenerlo" -ForegroundColor White
Write-Host "   3. Ejecuta el script de inicio del backend:" -ForegroundColor White
Write-Host ""
Write-Host "      cd spring-api-entra" -ForegroundColor Cyan
Write-Host "      .\run-api.ps1" -ForegroundColor Cyan
Write-Host ""
Write-Host "   O si estás en la raíz del proyecto:" -ForegroundColor White
Write-Host ""
Write-Host "      .\spring-api-entra\run-api.ps1" -ForegroundColor Cyan
Write-Host ""

Write-Host "✅ CAMBIOS APLICADOS:" -ForegroundColor Green
Write-Host "   - Usuarios locales ahora tienen perfiles asignados" -ForegroundColor White
Write-Host "   - admin@local.com → Perfil 'Usuario Básico'" -ForegroundColor White
Write-Host "   - user@local.com → Perfil 'Usuario Básico'" -ForegroundColor White
Write-Host "   - guest@local.com → Perfil 'Gestor' (más permisos)" -ForegroundColor White
Write-Host ""

Write-Host "🎯 DESPUÉS DE REINICIAR:" -ForegroundColor Yellow
Write-Host "   1. El backend iniciará con los nuevos datos" -ForegroundColor White
Write-Host "   2. Los usuarios tendrán perfiles y permisos asignados" -ForegroundColor White
Write-Host "   3. El login desde el frontend debería funcionar completamente" -ForegroundColor White
Write-Host ""

Write-Host "💡 TIP:" -ForegroundColor Cyan
Write-Host "   Despues de que el backend inicie, recarga el frontend (Ctrl+R)" -ForegroundColor White
Write-Host "   y prueba el login con:" -ForegroundColor White
Write-Host ""
Write-Host "      Email: admin@local.com" -ForegroundColor Green
Write-Host "      Password: admin123" -ForegroundColor Green
Write-Host ""


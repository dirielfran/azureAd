# Script para reiniciar los servicios con los nuevos IDs de grupos
Write-Host "REINICIANDO SERVICIOS CON NUEVOS IDS DE GRUPOS" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "PASO 1: Reiniciar Backend" -ForegroundColor Yellow
Write-Host "El backend se reiniciara para cargar los nuevos IDs de grupos en la BD"
Write-Host ""

Write-Host "EJECUTA ESTOS COMANDOS EN TERMINALES SEPARADOS:" -ForegroundColor Green
Write-Host ""
Write-Host "TERMINAL 1 - BACKEND:" -ForegroundColor Yellow
Write-Host "cd spring-api-entra"
Write-Host "# Presiona Ctrl+C si esta ejecutandose"
Write-Host "./run-api.ps1"
Write-Host ""

Write-Host "TERMINAL 2 - FRONTEND:" -ForegroundColor Yellow  
Write-Host "# Presiona Ctrl+C si esta ejecutandose"
Write-Host "ng serve"
Write-Host ""

Write-Host "VERIFICACION DESPUES DEL REINICIO:" -ForegroundColor Green
Write-Host "1. Backend iniciado en puerto 8080"
Write-Host "2. Frontend iniciado en puerto 4200"
Write-Host "3. Hacer login en http://localhost:4200"
Write-Host "4. Verificar logs en consola del backend"
Write-Host "5. Verificar permisos en el frontend"
Write-Host ""

Write-Host "QUE BUSCAR EN LOS LOGS DEL BACKEND:" -ForegroundColor Yellow
Write-Host "- 'Grupos de Azure AD: [bdff3193-e802-41d9-a5c6-edc6fb0db732, ...]'"
Write-Host "- 'Perfil encontrado: Administrador -> ROLE_ADMINISTRADOR'"
Write-Host "- 'Authorities finales: [GROUP_xxx, ROLE_xxx, SCOPE_access_as_user]'"
Write-Host ""

Write-Host "ENDPOINTS PARA PROBAR:" -ForegroundColor Green
Write-Host "GET http://localhost:8080/api/autorizacion/informacion-usuario"
Write-Host "GET http://localhost:8080/api/autorizacion/permisos"
Write-Host ""

Read-Host "Presiona Enter para continuar..."

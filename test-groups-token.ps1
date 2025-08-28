# Script para probar la configuracion de grupos en el token
Write-Host "VERIFICACION DE CONFIGURACION DE GRUPOS" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "PASOS PARA VERIFICAR:" -ForegroundColor Yellow
Write-Host "1. Configura el groups claim en Azure AD"
Write-Host "2. Reinicia tu aplicacion Angular (ng serve)"
Write-Host "3. Reinicia tu API Spring Boot"
Write-Host "4. Haz login nuevamente"
Write-Host "5. Verifica los logs en ambas consolas"
Write-Host ""

Write-Host "QUE BUSCAR EN LOS LOGS:" -ForegroundColor Yellow
Write-Host ""
Write-Host "EN LA CONSOLA DEL BACKEND (Spring Boot):"
Write-Host "   - 'Grupos de Azure AD: [lista-de-ids-de-grupos]'"
Write-Host "   - 'Perfil encontrado: [nombre-perfil] -> ROLE_[NOMBRE]'"
Write-Host "   - 'Authorities finales: [lista-completa-de-authorities]'"
Write-Host ""

Write-Host "EN LA CONSOLA DEL FRONTEND (Angular):"
Write-Host "   - 'Informacion del usuario obtenida: [objeto-con-permisos]'"
Write-Host "   - 'Permisos cargados desde sessionStorage'"
Write-Host ""

Write-Host "COMANDOS PARA REINICIAR:" -ForegroundColor Green
Write-Host ""
Write-Host "# Terminal 1 - Backend:"
Write-Host "cd spring-api-entra"
Write-Host "Ctrl+C (para detener)"
Write-Host "./run-api.ps1"
Write-Host ""
Write-Host "# Terminal 2 - Frontend:"
Write-Host "Ctrl+C (para detener)"
Write-Host "ng serve"
Write-Host ""

Write-Host "ENDPOINTS PARA PROBAR:" -ForegroundColor Yellow
Write-Host "http://localhost:8080/api/autorizacion/informacion-usuario"
Write-Host "http://localhost:8080/api/autorizacion/permisos"
Write-Host "http://localhost:8080/api/autorizacion/codigos-permisos"
Write-Host ""

Write-Host "SI AUN NO FUNCIONA:" -ForegroundColor Red
Write-Host "1. Verifica que el usuario este en grupos de Azure AD"
Write-Host "2. Verifica que los grupos tengan perfiles asociados en la BD"
Write-Host "3. Revisa los logs detallados del AzureAdGroupsJwtConverter"
Write-Host ""

Read-Host "Presiona Enter para continuar..."

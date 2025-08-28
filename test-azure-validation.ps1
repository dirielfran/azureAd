# =============================================================================
# SCRIPT DE VALIDACION DE AZURE AD - PERMISOS Y GRUPOS
# =============================================================================
# Este script te ayuda a validar la configuracion de Azure AD
# y los permisos del usuario actual

Write-Host "VALIDACION DE AZURE AD - PERMISOS Y GRUPOS" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Configuracion de tu aplicacion
$ClientId = "4a12fbd8-bf63-4c12-be4c-9678b207fbe7"
$TenantId = "f128ae87-3797-42d7-8490-82c6b570f832"
$FrontendUrl = "http://localhost:4200"
$BackendUrl = "http://localhost:8080"

Write-Host "CONFIGURACION ACTUAL:" -ForegroundColor Yellow
Write-Host "   Client ID: $ClientId"
Write-Host "   Tenant ID: $TenantId"
Write-Host "   Frontend: $FrontendUrl"
Write-Host "   Backend: $BackendUrl"
Write-Host ""

# Funcion para verificar si un puerto esta en uso
function Test-Port {
    param($Port)
    try {
        $connection = New-Object System.Net.Sockets.TcpClient
        $connection.Connect("localhost", $Port)
        $connection.Close()
        return $true
    } catch {
        return $false
    }
}

# Verificar servicios
Write-Host "VERIFICANDO SERVICIOS:" -ForegroundColor Yellow

$frontendRunning = Test-Port -Port 4200
$backendRunning = Test-Port -Port 8080

Write-Host "   Frontend (Puerto 4200): $(if($frontendRunning){'[OK] EJECUTANDOSE'}else{'[X] DETENIDO'})"
Write-Host "   Backend (Puerto 8080): $(if($backendRunning){'[OK] EJECUTANDOSE'}else{'[X] DETENIDO'})"
Write-Host ""

if (-not $frontendRunning -or -not $backendRunning) {
    Write-Host "ADVERTENCIA: Algunos servicios no estan ejecutandose." -ForegroundColor Red
    Write-Host "   Para una validacion completa, inicia ambos servicios:" -ForegroundColor Red
    Write-Host "   - Frontend: ng serve" -ForegroundColor Red
    Write-Host "   - Backend: ./run-api.ps1" -ForegroundColor Red
    Write-Host ""
}

# URLs importantes para validacion manual
Write-Host "URLS DE VALIDACION EN AZURE PORTAL:" -ForegroundColor Yellow
Write-Host "   Portal Principal: https://portal.azure.com"
Write-Host "   Azure AD: https://portal.azure.com/#view/Microsoft_AAD_IAM/ActiveDirectoryMenuBlade"
Write-Host "   App Registrations: https://portal.azure.com/#view/Microsoft_AAD_RegisteredApps/ApplicationsListBlade"
Write-Host "   Tu Aplicacion: https://portal.azure.com/#view/Microsoft_AAD_RegisteredApps/ApplicationMenuBlade/appId/$ClientId"
Write-Host ""

# Checklist de validacion
Write-Host "CHECKLIST DE VALIDACION:" -ForegroundColor Green
Write-Host ""
Write-Host "EN TU APLICACION REGISTRADA:" -ForegroundColor Yellow
Write-Host "   [ ] Application ID coincide: $ClientId"
Write-Host "   [ ] Directory ID coincide: $TenantId"
Write-Host "   [ ] Redirect URI configurado: $FrontendUrl"
Write-Host "   [ ] API Permissions incluyen:"
Write-Host "     [ ] Microsoft Graph: User.Read"
Write-Host "     [ ] Microsoft Graph: GroupMember.Read.All"
Write-Host "     [ ] Tu API: access_as_user"
Write-Host "   [ ] Admin consent otorgado para todos los permisos"
Write-Host ""

Write-Host "EN GRUPOS DE AZURE AD:" -ForegroundColor Yellow
Write-Host "   [ ] Grupos creados para diferentes roles (Admin, Manager, User)"
Write-Host "   [ ] Usuario de prueba agregado a grupos apropiados"
Write-Host "   [ ] IDs de grupos documentados para la base de datos"
Write-Host ""

Write-Host "EN TOKEN CONFIGURATION:" -ForegroundColor Yellow
Write-Host "   [ ] Optional claims configurados:"
Write-Host "     [ ] email"
Write-Host "     [ ] groups"
Write-Host "     [ ] given_name"
Write-Host "     [ ] family_name"
Write-Host ""

# Comandos utiles
Write-Host "COMANDOS UTILES PARA PRUEBAS:" -ForegroundColor Yellow
Write-Host ""
Write-Host "   # Iniciar frontend:"
Write-Host "   ng serve"
Write-Host ""
Write-Host "   # Iniciar backend:"
Write-Host "   cd spring-api-entra"
Write-Host "   ./run-api.ps1"
Write-Host ""
Write-Host "   # Probar endpoint de autorizacion:"
Write-Host "   # (Despues de autenticarte en el frontend)"
Write-Host "   # GET http://localhost:8080/api/autorizacion/informacion-usuario"
Write-Host ""

if ($frontendRunning -and $backendRunning) {
    Write-Host "PRUEBA RAPIDA:" -ForegroundColor Green
    Write-Host "   1. Ve a: $FrontendUrl"
    Write-Host "   2. Inicia sesion con tu usuario de Azure AD"
    Write-Host "   3. Verifica que se muestren tus permisos y grupos"
    Write-Host "   4. Revisa la consola del navegador para logs detallados"
    Write-Host ""
}

Write-Host "ENDPOINTS DE VALIDACION:" -ForegroundColor Yellow
Write-Host "   GET /api/autorizacion/informacion-usuario - Informacion completa del usuario"
Write-Host "   GET /api/autorizacion/permisos - Lista de permisos"
Write-Host "   GET /api/autorizacion/codigos-permisos - Codigos de permisos"
Write-Host "   GET /api/autorizacion/tiene-permiso/{codigo} - Verificar permiso especifico"
Write-Host ""

Write-Host "SIGUIENTE PASO:" -ForegroundColor Green
Write-Host "   Sigue la guia para validar cada punto en Azure Portal"
Write-Host "   Luego ejecuta las pruebas con los servicios ejecutandose"
Write-Host ""

Write-Host "PASOS DETALLADOS EN AZURE PORTAL:" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. ACCEDER AL PORTAL:"
Write-Host "   - Ve a https://portal.azure.com"
Write-Host "   - Inicia sesion con tu cuenta de administrador"
Write-Host ""
Write-Host "2. NAVEGAR A AZURE AD:"
Write-Host "   - Busca 'Azure Active Directory'"
Write-Host "   - Selecciona el servicio"
Write-Host ""
Write-Host "3. VALIDAR APLICACION:"
Write-Host "   - Ve a 'App registrations'"
Write-Host "   - Busca tu app: $ClientId"
Write-Host "   - Verifica configuracion basica"
Write-Host ""
Write-Host "4. VERIFICAR PERMISOS:"
Write-Host "   - En tu app, ve a 'API permissions'"
Write-Host "   - Verifica permisos de Microsoft Graph"
Write-Host "   - Asegurate de tener 'Admin consent granted'"
Write-Host ""
Write-Host "5. VALIDAR GRUPOS:"
Write-Host "   - En Azure AD, ve a 'Groups'"
Write-Host "   - Verifica que existan los grupos necesarios"
Write-Host "   - Confirma membresia de usuarios de prueba"
Write-Host ""
Write-Host "6. CONFIGURAR CLAIMS:"
Write-Host "   - En tu app, ve a 'Token configuration'"
Write-Host "   - Agrega claims opcionales: email, groups, name"
Write-Host ""

Read-Host "Presiona Enter para continuar..."
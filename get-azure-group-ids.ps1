# Script para obtener los IDs de grupos de Azure AD
Write-Host "OBTENCION DE IDS DE GRUPOS DE AZURE AD" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "METODO 1: DESDE AZURE PORTAL" -ForegroundColor Yellow
Write-Host "1. Ve a https://portal.azure.com"
Write-Host "2. Navega a Azure Active Directory > Groups"
Write-Host "3. Haz clic en cada grupo que quieras usar"
Write-Host "4. En la pagina del grupo, copia el 'Object ID'"
Write-Host ""

Write-Host "METODO 2: DESDE POWERSHELL (Si tienes AzureAD module)" -ForegroundColor Yellow
Write-Host "# Instalar modulo si no lo tienes:"
Write-Host "Install-Module AzureAD"
Write-Host ""
Write-Host "# Conectar y obtener grupos:"
Write-Host "Connect-AzureAD"
Write-Host "Get-AzureADGroup | Select-Object DisplayName, ObjectId"
Write-Host ""

Write-Host "METODO 3: DESDE EL TOKEN JWT" -ForegroundColor Yellow
Write-Host "1. Inicia sesion en tu aplicacion"
Write-Host "2. Abre Developer Tools (F12)"
Write-Host "3. Ve a la consola y busca los logs del backend"
Write-Host "4. Busca: 'Grupos de Azure AD: [lista-de-ids]'"
Write-Host ""

Write-Host "EJEMPLO DE LO QUE DEBES BUSCAR:" -ForegroundColor Green
Write-Host "Grupos de Azure AD: [12345678-1234-1234-1234-123456789012, 87654321-4321-4321-4321-210987654321]"
Write-Host ""

Write-Host "LUEGO ACTUALIZA LA BASE DE DATOS:" -ForegroundColor Yellow
Write-Host "UPDATE perfiles SET azure_group_id = 'ID-REAL-DEL-GRUPO' WHERE nombre = 'Administrador';"
Write-Host "UPDATE perfiles SET azure_group_id = 'ID-REAL-DEL-GRUPO' WHERE nombre = 'Gestor';"
Write-Host "UPDATE perfiles SET azure_group_id = 'ID-REAL-DEL-GRUPO' WHERE nombre = 'Usuario';"
Write-Host ""

Write-Host "PASOS SIGUIENTES:" -ForegroundColor Green
Write-Host "1. Configura el groups claim en Azure AD (como ya hiciste)"
Write-Host "2. Obtén los IDs reales de tus grupos"
Write-Host "3. Actualiza la base de datos con los IDs correctos"
Write-Host "4. Reinicia la aplicacion"
Write-Host "5. Haz login y verifica los logs"
Write-Host ""

Read-Host "Presiona Enter para continuar..."

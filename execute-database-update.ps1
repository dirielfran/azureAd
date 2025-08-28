# Script para ejecutar la actualización de la base de datos
Write-Host "ACTUALIZACION DE IDS DE GRUPOS EN BASE DE DATOS" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "IDs DE GRUPOS PROPORCIONADOS:" -ForegroundColor Yellow
Write-Host "  Grupo 1: bdff3193-e802-41d9-a5c6-edc6fb0db732"
Write-Host "  Grupo 2: 1ae2b90c-5c46-4639-84d0-809d66cdd809"
Write-Host ""

# Verificar si el backend está ejecutándose
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

$backendRunning = Test-Port -Port 8080

if (-not $backendRunning) {
    Write-Host "ADVERTENCIA: El backend no está ejecutándose." -ForegroundColor Red
    Write-Host "Necesitas iniciar el backend primero para que la base de datos H2 esté disponible." -ForegroundColor Red
    Write-Host ""
    Write-Host "Para iniciar el backend:" -ForegroundColor Yellow
    Write-Host "cd spring-api-entra"
    Write-Host "./run-api.ps1"
    Write-Host ""
    Write-Host "Despues de iniciar el backend, ejecuta este script nuevamente." -ForegroundColor Yellow
    Read-Host "Presiona Enter para continuar..."
    exit
}

Write-Host "Backend detectado ejecutandose en puerto 8080" -ForegroundColor Green
Write-Host ""

Write-Host "OPCIONES PARA EJECUTAR EL SCRIPT SQL:" -ForegroundColor Yellow
Write-Host ""
Write-Host "OPCION 1: H2 Console (Recomendado)" -ForegroundColor Green
Write-Host "1. Ve a: http://localhost:8080/h2-console"
Write-Host "2. Configuracion de conexion:"
Write-Host "   - JDBC URL: jdbc:h2:mem:testdb"
Write-Host "   - User Name: sa"
Write-Host "   - Password: (dejar vacio)"
Write-Host "3. Haz clic en 'Connect'"
Write-Host "4. Copia y pega el contenido del archivo 'update-database-group-ids.sql'"
Write-Host "5. Ejecuta el script"
Write-Host ""

Write-Host "OPCION 2: Desde linea de comandos (Alternativa)" -ForegroundColor Yellow
Write-Host "Si tienes H2 client instalado localmente:"
Write-Host "java -cp h2*.jar org.h2.tools.Shell -url jdbc:h2:mem:testdb -user sa"
Write-Host ""

Write-Host "CONTENIDO DEL SCRIPT A EJECUTAR:" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Get-Content -Path "update-database-group-ids.sql" | Write-Host
Write-Host ""

Write-Host "DESPUES DE EJECUTAR EL SCRIPT:" -ForegroundColor Green
Write-Host "1. Verifica que los IDs se actualizaron correctamente"
Write-Host "2. Reinicia el backend (Ctrl+C y luego ./run-api.ps1)"
Write-Host "3. Reinicia el frontend (Ctrl+C y luego ng serve)"
Write-Host "4. Haz login nuevamente y verifica los permisos"
Write-Host ""

Write-Host "ABRIENDO H2 CONSOLE..." -ForegroundColor Green
Start-Process "http://localhost:8080/h2-console"

Write-Host ""
Read-Host "Presiona Enter despues de ejecutar el script SQL en H2 Console..."

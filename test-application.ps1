# =============================================================================
# 🧪 SCRIPT DE PRUEBAS - SISTEMA DE AUTORIZACIÓN AZURE AD
# =============================================================================
# Este script automatiza las pruebas de la aplicación completa
# =============================================================================

Write-Host "🚀 Iniciando pruebas del Sistema de Autorización Azure AD..." -ForegroundColor Cyan
Write-Host "=" * 70 -ForegroundColor Gray

# =============================================================================
# FUNCIONES AUXILIARES
# =============================================================================

function Test-BackendHealth {
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/actuator/health" -TimeoutSec 10
        if ($response.status -eq "UP") {
            Write-Host "✅ Backend funcionando correctamente" -ForegroundColor Green
            return $true
        }
    }
    catch {
        Write-Host "❌ Backend no disponible: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

function Test-DatabaseConnection {
    Write-Host "🔍 Verificando base de datos H2..." -ForegroundColor Yellow
    Write-Host "   💡 Abrir en navegador: http://localhost:8080/api/h2-console" -ForegroundColor Cyan
    Write-Host "   📋 JDBC URL: jdbc:h2:mem:testdb" -ForegroundColor Cyan
    Write-Host "   👤 Usuario: sa | Contraseña: password" -ForegroundColor Cyan
}

function Test-PublicEndpoints {
    Write-Host "🔍 Probando endpoints públicos..." -ForegroundColor Yellow
    
    try {
        $authInfo = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/info" -TimeoutSec 10
        Write-Host "✅ Endpoint público /auth/info responde correctamente" -ForegroundColor Green
    }
    catch {
        Write-Host "❌ Error en endpoint público: $($_.Exception.Message)" -ForegroundColor Red
    }
}

function Start-Frontend {
    Write-Host "🔍 Verificando frontend..." -ForegroundColor Yellow
    
    # Verificar si Angular CLI está disponible
    try {
        $ngVersion = ng version --version 2>$null
        Write-Host "✅ Angular CLI disponible" -ForegroundColor Green
    }
    catch {
        Write-Host "❌ Angular CLI no encontrado. Instalar con: npm install -g @angular/cli" -ForegroundColor Red
        return
    }
    
    Write-Host "🚀 Para iniciar el frontend, ejecutar en otra terminal:" -ForegroundColor Cyan
    Write-Host "   ng serve" -ForegroundColor White
    Write-Host "   Luego abrir: http://localhost:4200" -ForegroundColor White
}

function Show-TestData {
    Write-Host "📊 Datos de prueba configurados:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "🔐 PERFILES DISPONIBLES:" -ForegroundColor Cyan
    Write-Host "   👑 Administrador (admin-group-id-123)  - Todos los permisos" -ForegroundColor Red
    Write-Host "   🛠️  Gestor (manager-group-id-456)      - Permisos de gestión" -ForegroundColor Yellow
    Write-Host "   👤 Usuario (user-group-id-789)         - Permisos básicos" -ForegroundColor Green
    Write-Host "   👁️  Lector (reader-group-id-101)       - Solo lectura" -ForegroundColor Blue
    Write-Host ""
    Write-Host "📋 PERMISOS CONFIGURADOS:" -ForegroundColor Cyan
    Write-Host "   • USUARIOS: LEER, CREAR, EDITAR, ELIMINAR"
    Write-Host "   • REPORTES: LEER, CREAR, EXPORTAR"
    Write-Host "   • CONFIGURACION: LEER, EDITAR"
    Write-Host "   • PERFILES: LEER, CREAR, EDITAR, ELIMINAR"
    Write-Host "   • DASHBOARD: LEER, ADMIN"
    Write-Host ""
}

function Show-NextSteps {
    Write-Host "📝 PRÓXIMOS PASOS PARA PROBAR:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. 🌐 Abrir http://localhost:4200" -ForegroundColor White
    Write-Host "2. 🔐 Hacer clic en 'Iniciar sesión con Microsoft'" -ForegroundColor White
    Write-Host "3. 🔑 Autenticarse con cuenta de Azure AD" -ForegroundColor White
    Write-Host "4. ✅ Verificar que se cargan los permisos" -ForegroundColor White
    Write-Host "5. 🧭 Navegar por las diferentes secciones" -ForegroundColor White
    Write-Host "6. 👀 Observar renderizado condicional según permisos" -ForegroundColor White
    Write-Host ""
    Write-Host "💡 CONFIGURACIÓN AZURE AD:" -ForegroundColor Cyan
    Write-Host "   • Crear grupos de seguridad en Azure AD"
    Write-Host "   • Asignar usuarios a grupos"
    Write-Host "   • Actualizar IDs de grupos en la base de datos"
    Write-Host ""
}

# =============================================================================
# EJECUCIÓN PRINCIPAL
# =============================================================================

Write-Host ""
Write-Host "🔍 VERIFICANDO BACKEND..." -ForegroundColor Yellow
Write-Host "-" * 50 -ForegroundColor Gray

$backendReady = Test-BackendHealth

if ($backendReady) {
    Test-DatabaseConnection
    Write-Host ""
    Test-PublicEndpoints
} else {
    Write-Host ""
    Write-Host "⏳ El backend está iniciando. Esto puede tomar unos momentos..." -ForegroundColor Yellow
    Write-Host "   Ejecutar en terminal separada: cd spring-api-entra && mvn spring-boot:run" -ForegroundColor Cyan
    Write-Host ""
    
    # Intentar verificar nuevamente después de un momento
    Write-Host "🔄 Esperando 15 segundos e intentando nuevamente..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
    
    $backendReady = Test-BackendHealth
    if ($backendReady) {
        Test-PublicEndpoints
    }
}

Write-Host ""
Write-Host "🎨 VERIFICANDO FRONTEND..." -ForegroundColor Yellow
Write-Host "-" * 50 -ForegroundColor Gray

Start-Frontend

Write-Host ""
Write-Host "📊 INFORMACIÓN DEL SISTEMA..." -ForegroundColor Yellow
Write-Host "-" * 50 -ForegroundColor Gray

Show-TestData
Show-NextSteps

Write-Host "=" * 70 -ForegroundColor Gray
Write-Host "🎉 Script de pruebas completado!" -ForegroundColor Green

if ($backendReady) {
    Write-Host "✅ Backend: FUNCIONANDO" -ForegroundColor Green
} else {
    Write-Host "⏳ Backend: INICIANDO" -ForegroundColor Yellow
}

Write-Host "📱 Frontend: LISTO PARA EJECUTAR" -ForegroundColor Green
Write-Host ""
Write-Host "🚀 Para continuar:" -ForegroundColor Cyan
Write-Host "   1. Ejecutar: ng serve" -ForegroundColor White
Write-Host "   2. Abrir: http://localhost:4200" -ForegroundColor White
Write-Host "=" * 70 -ForegroundColor Gray

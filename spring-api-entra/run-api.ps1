Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   🚀 INICIANDO API SPRING BOOT 🚀" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "🔧 Compilando proyecto..." -ForegroundColor Green
& mvn clean compile

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error en la compilación" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🏃‍♂️ Ejecutando aplicación..." -ForegroundColor Green
Write-Host ""
Write-Host "📡 La API estará disponible en: " -NoNewline -ForegroundColor White
Write-Host "http://localhost:8080/api" -ForegroundColor Cyan
Write-Host "🔒 Autenticación: " -NoNewline -ForegroundColor White
Write-Host "Microsoft Entra ID" -ForegroundColor Yellow
Write-Host "🌐 CORS configurado para: " -NoNewline -ForegroundColor White
Write-Host "http://localhost:4200" -ForegroundColor Cyan
Write-Host "💾 Base de datos H2: " -NoNewline -ForegroundColor White
Write-Host "http://localhost:8080/api/h2-console" -ForegroundColor Cyan
Write-Host "📊 Health check: " -NoNewline -ForegroundColor White
Write-Host "http://localhost:8080/api/actuator/health" -ForegroundColor Cyan
Write-Host ""
Write-Host "⏹️  Para detener la aplicación, presiona Ctrl+C" -ForegroundColor Red
Write-Host ""

& mvn spring-boot:run

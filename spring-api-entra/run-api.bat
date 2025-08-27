@echo off
echo.
echo ========================================
echo   🚀 INICIANDO API SPRING BOOT 🚀
echo ========================================
echo.
echo 🔧 Compilando proyecto...
call mvn clean compile

echo.
echo 🏃‍♂️ Ejecutando aplicación...
echo.
echo 📡 La API estará disponible en: http://localhost:8080/api
echo 🔒 Autenticación: Microsoft Entra ID
echo 🌐 CORS configurado para: http://localhost:4200
echo 💾 Base de datos H2: http://localhost:8080/api/h2-console
echo 📊 Health check: http://localhost:8080/api/actuator/health
echo.
echo ⏹️  Para detener la aplicación, presiona Ctrl+C
echo.

call mvn spring-boot:run

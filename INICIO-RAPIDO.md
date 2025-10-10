# 🚀 Inicio Rápido - Autenticación Dual

## ⚡ Comandos Rápidos

### 1. Instalar Dependencias (si es necesario)
```bash
npm install
```

### 2. Iniciar el Backend
```bash
cd spring-api-entra
./run-api.bat
# O en PowerShell:
./run-api.ps1
```

### 3. Iniciar el Frontend
```bash
# En la raíz del proyecto
ng serve
```

### 4. Acceder a la Aplicación
Abrir navegador en: `http://localhost:4200`

---

## 🔄 Cambiar Método de Autenticación

### Opción 1: Mediante Variables de Entorno

#### Activar Autenticación Local (JWT)
```bash
# Windows CMD
set JWT_LOCAL_ENABLED=true
set AZURE_AD_ENABLED=false

# PowerShell
$env:JWT_LOCAL_ENABLED="true"
$env:AZURE_AD_ENABLED="false"

# Linux/Mac
export JWT_LOCAL_ENABLED=true
export AZURE_AD_ENABLED=false
```

#### Activar Autenticación Azure AD
```bash
# Windows CMD
set AZURE_AD_ENABLED=true
set JWT_LOCAL_ENABLED=false

# PowerShell
$env:AZURE_AD_ENABLED="true"
$env:JWT_LOCAL_ENABLED="false"

# Linux/Mac
export AZURE_AD_ENABLED=true
export JWT_LOCAL_ENABLED=false
```

### Opción 2: Mediante API REST

#### Script PowerShell para cambiar a JWT Local
```powershell
$headers = @{
    "X-Admin-Token" = "ADMIN_SECRET_TOKEN_2024"
    "Content-Type" = "application/json"
}

$body = @{
    azureEnabled = $false
    jwtLocalEnabled = $true
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/config/auth/config/admin" -Method POST -Headers $headers -Body $body
```

#### Script PowerShell para cambiar a Azure AD
```powershell
$headers = @{
    "X-Admin-Token" = "ADMIN_SECRET_TOKEN_2024"
    "Content-Type" = "application/json"
}

$body = @{
    azureEnabled = $true
    jwtLocalEnabled = $false
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/config/auth/config/admin" -Method POST -Headers $headers -Body $body
```

### Opción 3: Mediante cURL

#### Cambiar a JWT Local
```bash
curl -X POST http://localhost:8080/api/config/auth/config/admin \
  -H "X-Admin-Token: ADMIN_SECRET_TOKEN_2024" \
  -H "Content-Type: application/json" \
  -d '{"azureEnabled": false, "jwtLocalEnabled": true}'
```

#### Cambiar a Azure AD
```bash
curl -X POST http://localhost:8080/api/config/auth/config/admin \
  -H "X-Admin-Token: ADMIN_SECRET_TOKEN_2024" \
  -H "Content-Type: application/json" \
  -d '{"azureEnabled": true, "jwtLocalEnabled": false}'
```

---

## 👥 Usuarios de Prueba (JWT Local)

### Verificar Usuarios en Base de Datos

Los usuarios están configurados en `spring-api-entra/src/main/resources/data.sql`

Usuarios de ejemplo (verificar en el archivo):
- **Email**: `admin@test.com` o similar
- **Contraseña**: La configurada en el script SQL (debe estar hasheada con BCrypt)

### Crear Nuevo Usuario de Prueba

1. Hashear la contraseña:
```bash
curl -X POST http://localhost:8080/api/auth/generate-hash-temp \
  -H "Content-Type: application/json" \
  -d '{"password": "tu_contraseña"}'
```

2. Agregar el usuario en `data.sql`:
```sql
INSERT INTO usuario (email, nombre, password, activo) 
VALUES ('usuario@test.com', 'Usuario Prueba', 'hash_generado', true);
```

3. Reiniciar el backend

---

## 🔍 Verificar Estado de Autenticación

### Consultar Método Activo
```bash
curl http://localhost:8080/api/config/auth/status
```

Respuesta esperada:
```json
{
  "azureAdHabilitado": false,
  "jwtLocalHabilitado": true,
  "timestamp": 1234567890
}
```

---

## 🐛 Solución Rápida de Problemas

### Error: No se puede conectar con el servidor

✅ **Verificar que el backend esté corriendo**
```bash
curl http://localhost:8080/api/config/auth/status
```

### Error: Credenciales incorrectas

✅ **Verificar usuario en base de datos**
- Acceder a H2 Console: `http://localhost:8080/api/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Usuario: `sa`
- Contraseña: `password`

Ejecutar:
```sql
SELECT * FROM usuario;
```

### Error: Permisos no se cargan

✅ **Verificar perfiles y permisos**
```sql
-- Ver perfiles del usuario
SELECT * FROM perfil;

-- Ver permisos de un perfil
SELECT * FROM permiso WHERE perfil_id = 1;
```

---

## 📱 Flujo de Uso Completo

### Con Autenticación Local (JWT)

1. **Configurar backend**:
   ```bash
   # PowerShell
   $env:JWT_LOCAL_ENABLED="true"
   $env:AZURE_AD_ENABLED="false"
   ```

2. **Iniciar servicios**:
   ```bash
   # Terminal 1: Backend
   cd spring-api-entra
   ./run-api.ps1
   
   # Terminal 2: Frontend
   ng serve
   ```

3. **Acceder**: `http://localhost:4200`

4. **Login**:
   - Hacer clic en "Iniciar sesión"
   - Ingresar email y contraseña
   - ✅ Listo!

### Con Autenticación Azure AD

1. **Configurar backend**:
   ```bash
   # PowerShell
   $env:AZURE_AD_ENABLED="true"
   $env:JWT_LOCAL_ENABLED="false"
   ```

2. **Iniciar servicios** (igual que arriba)

3. **Acceder**: `http://localhost:4200`

4. **Login**:
   - Hacer clic en "Iniciar sesión con Microsoft"
   - Autenticarse con cuenta Microsoft
   - ✅ Listo!

---

## 🎯 Comandos Útiles

### Ver Logs del Backend
```bash
# En el directorio del backend
tail -f logs/application.log
```

### Limpiar y Reconstruir Frontend
```bash
rm -rf node_modules dist
npm install
ng build
```

### Reiniciar Todo desde Cero
```bash
# PowerShell
# 1. Detener servicios
# Ctrl+C en ambos terminales

# 2. Limpiar
cd spring-api-entra
mvn clean

cd ..
rm -rf node_modules dist

# 3. Reinstalar
npm install

# 4. Reiniciar
cd spring-api-entra
./run-api.ps1

# En otra terminal
cd ..
ng serve
```

---

## 📞 Contacto y Soporte

Si encuentras problemas:

1. Revisa la **Consola del Navegador** (F12)
2. Revisa los **Logs del Backend**
3. Consulta `GUIA-AUTENTICACION-DUAL.md` para más detalles
4. Verifica la sección de Troubleshooting

---

**¡Todo listo para usar!** 🎉


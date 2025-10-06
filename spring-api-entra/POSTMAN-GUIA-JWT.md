# 📮 Guía para Probar Autenticación JWT con Postman

## 🚀 Configuración Inicial

### 1. Asegúrate de que la aplicación esté ejecutándose

```powershell
cd spring-api-entra
mvn spring-boot:run
```

**Base URL**: `http://localhost:8080/api`

---

## 🧪 PRUEBA 1: Endpoint Público (Sin Autenticación)

### ✅ Verificar que la API esté funcionando

**Request:**
- **Método**: `GET`
- **URL**: `http://localhost:8080/api/auth/info`
- **Headers**: Ninguno necesario

**Respuesta Esperada** (200 OK):
```json
{
    "api": "API Protegida con Microsoft Entra ID",
    "version": "1.0.0",
    "descripcion": "API REST segura integrada con Azure AD",
    "endpoints": {
        "usuarios": "/users",
        "datos": "/data",
        "perfil": "/profile",
        "autenticacion": "/auth"
    },
    "autenticacion": "Microsoft Entra ID (Azure AD)",
    "scopes_requeridos": "access_as_user"
}
```

---

## 🔐 PRUEBA 2: Login JWT (Obtener Token)

### 🎯 Login con Usuario Local

**Request:**
- **Método**: `POST`
- **URL**: `http://localhost:8080/api/auth/login`
- **Headers**:
  - `Content-Type`: `application/json`
  - `Authorization`: `Basic YWRtaW5AbG9jYWwuY29tOmFkbWluMTIz`
    - *(Este es el Base64 de `admin@local.com:admin123`)*

**Cómo configurar en Postman:**

1. **Selecciona el método**: `POST`
2. **Pega la URL**: `http://localhost:8080/api/auth/login`
3. **Ve a la pestaña "Authorization"**:
   - Type: `Basic Auth`
   - Username: `admin@local.com`
   - Password: `admin123`
4. **Ve a la pestaña "Headers"**:
   - Key: `Content-Type`
   - Value: `application/json`
5. **Click en "Send"**

**Respuesta Esperada** (200 OK):
```json
{
    "jwt": "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJBUEktVFlDIiwic3ViIjoiYWRtaW5AbG9jYWwuY29tIiwicGVyZmlsIjoiVXN1YXJpbyBCXHUwMGU5c2ljbyIsImF1dGhvcml0aWVzIjpbIlVTVUFSSU9TX0xFRVIiLCJEQVNIQk9BUkRfTEVFUiJdLCJpYXQiOjE3MjgxNjQ2MTUsImV4cCI6MTcyODI1MTAxNX0.XYZ..."
}
```

**✨ IMPORTANTE**: Copia el valor completo de `jwt` (incluyendo "Bearer "). Lo necesitarás para el siguiente paso.

---

## 🎫 PRUEBA 3: Usar el Token en Endpoint Protegido

### 📊 Acceder a Datos Protegidos

**Request:**
- **Método**: `GET`
- **URL**: `http://localhost:8080/api/data`
- **Headers**:
  - `Authorization`: `Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9...` *(Usa el token que obtuviste)*
  - `Content-Type`: `application/json`

**Cómo configurar en Postman:**

1. **Selecciona el método**: `GET`
2. **Pega la URL**: `http://localhost:8080/api/data`
3. **Ve a la pestaña "Headers"**:
   - Key: `Authorization`
   - Value: Pega el token completo que copiaste (debe empezar con "Bearer ")
   - Key: `Content-Type`
   - Value: `application/json`
4. **Click en "Send"**

**Respuesta Esperada** (200 OK):
```json
{
    "message": "Datos protegidos",
    "timestamp": "2025-10-06T13:30:00",
    "data": [...]
}
```

---

## 👥 Usuarios de Prueba Disponibles

| Email | Contraseña | Base64 para Authorization Header |
|-------|-----------|----------------------------------|
| `admin@local.com` | `admin123` | `Basic YWRtaW5AbG9jYWwuY29tOmFkbWluMTIz` |
| `user@local.com` | `user123` | `Basic dXNlckBsb2NhbC5jb206dXNlcjEyMw==` |
| `guest@local.com` | `guest123` | `Basic Z3Vlc3RAbG9jYWwuY29tOmd1ZXN0MTIz` |

---

## 🎯 PRUEBA 4: Endpoint con Información de Usuario

### 📋 Obtener Información del Usuario Autenticado

**Request:**
- **Método**: `GET`
- **URL**: `http://localhost:8080/api/autorizacion/informacion-usuario`
- **Headers**:
  - `Authorization`: `Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9...` *(Tu token)*
  - `Content-Type`: `application/json`

**Respuesta Esperada** (200 OK):
```json
{
    "email": "admin@local.com",
    "perfiles": [...],
    "permisos": [...]
}
```

---

## 📦 Colección de Postman

### Crear una Colección

1. **Crea una nueva colección** llamada "API JWT Local"
2. **Agrega estas 4 requests**:
   - ✅ GET - Health Check
   - 🔐 POST - Login JWT
   - 📊 GET - Data (Protected)
   - 👤 GET - User Info

### Variables de Colección

Configura estas variables para reutilizar:

- `baseUrl`: `http://localhost:8080/api`
- `jwtToken`: (Se actualiza manualmente después del login)

Luego usa `{{baseUrl}}` y `{{jwtToken}}` en tus requests.

---

## 🔍 Troubleshooting

### ❌ Error 401 - No autorizado

**Posibles causas:**

1. **Token expirado**: Los tokens JWT expiran después de 24 horas. Haz login nuevamente.
2. **Token mal copiado**: Asegúrate de incluir "Bearer " al inicio.
3. **Credenciales incorrectas**: Verifica usuario y contraseña.
4. **Endpoint no permitido**: El endpoint `/auth/login` debe estar en `permitAll()`.

### ❌ Error 500 - Internal Server Error

**Revisa los logs de la aplicación** para ver el error específico.

### ✅ Cómo generar el Base64 manualmente

Si quieres probar con otras credenciales:

1. Ve a: https://www.base64encode.org/
2. Ingresa: `email@ejemplo.com:contraseña`
3. Codifica y usa: `Basic [resultado]`

O en PowerShell:
```powershell
$credenciales = "admin@local.com:admin123"
$bytes = [System.Text.Encoding]::UTF8.GetBytes($credenciales)
$base64 = [System.Convert]::ToBase64String($bytes)
Write-Host "Basic $base64"
```

---

## 🎉 Resultado Final

Ahora tienes **DOS sistemas de autenticación** funcionando:

1. **🔵 Azure AD (OAuth2)**: Para usuarios corporativos
   - Usa tokens de Microsoft
   - Se configura con el cliente de Azure

2. **🟢 JWT Local**: Para usuarios de base de datos
   - Usa email y contraseña
   - Tokens generados por tu API

Ambos sistemas coexisten sin problemas. 🚀

---

## 📸 Capturas Sugeridas

### Login Request
```
POST http://localhost:8080/api/auth/login
Authorization: Basic Auth
  Username: admin@local.com
  Password: admin123
```

### Protected Endpoint
```
GET http://localhost:8080/api/data
Headers:
  Authorization: Bearer eyJ0eXAiOiJKV1Qi...
  Content-Type: application/json
```

---

## 💡 Tips Adicionales

1. **Guarda el token**: Usa una variable de Postman para no copiarlo cada vez
2. **Crea tests**: Agrega scripts de prueba en la pestaña "Tests"
3. **Automatiza**: Usa Pre-request Scripts para refrescar el token automáticamente
4. **Exporta**: Guarda tu colección para compartir con el equipo

¡Listo para probar! 🎯


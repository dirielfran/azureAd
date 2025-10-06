# 🧪 Guía para Probar Autenticación JWT Local

## 📋 Requisitos Previos

1. **Aplicación ejecutándose**: La aplicación Spring Boot debe estar corriendo en `http://localhost:8080/api`
2. **PowerShell**: Para ejecutar los comandos de prueba

---

## 🚀 Paso 1: Ejecutar la Aplicación

Abre una terminal en el directorio del proyecto y ejecuta:

```powershell
cd spring-api-entra
mvn spring-boot:run
```

**Espera** a que veas el mensaje:
```
🚀 API PROTEGIDA INICIADA 🚀
```

---

## 🧪 Paso 2: Probar el Endpoint Público

En **otra terminal** (deja la primera con la aplicación ejecutándose), ejecuta:

```powershell
cd spring-api-entra
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/info" -Method GET
```

**Resultado esperado:**
```
api               : API Protegida con Microsoft Entra ID
version           : 1.0.0
descripcion       : API REST segura integrada con Azure AD
```

---

## 🔑 Paso 3: Probar el Login JWT

### Usuarios de Prueba Disponibles:

| Email | Contraseña | Rol |
|-------|-----------|-----|
| `admin@local.com` | `admin123` | Administrador |
| `user@local.com` | `user123` | Usuario |
| `guest@local.com` | `guest123` | Invitado |

### Comando de Prueba:

```powershell
# Probar login con admin@local.com
$headers = @{
    "Authorization" = "Basic YWRtaW5AbG9jYWwuY29tOmFkbWluMTIz"
    "Content-Type" = "application/json"
}

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Headers $headers
Write-Host "Token JWT: $($response.jwt)"
```

**Resultado esperado:**
```
Token JWT: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9...
```

---

## ✅ Paso 4: Usar el Token en un Endpoint Protegido

Una vez que obtengas el token, guárdalo y pruébalo:

```powershell
# Guardar el token
$token = $response.jwt

# Crear headers con el token
$authHeaders = @{
    "Authorization" = $token
    "Content-Type" = "application/json"
}

# Probar en un endpoint protegido
Invoke-RestMethod -Uri "http://localhost:8080/api/data" -Method GET -Headers $authHeaders
```

**Resultado esperado:**
```
Deberías recibir los datos protegidos del endpoint.
```

---

## 🔍 Paso 5: Ver los Logs Detallados

En la terminal donde está ejecutándose la aplicación, deberías ver logs como:

```
🔍 [UsuarioService] Buscando usuario por email: admin@local.com
✅ [UsuarioService] Usuario encontrado: admin@local.com - Activo: true
🔐 [UsuarioService] Verificación de contraseña para admin@local.com: true
✅ [UsuarioService] Usuario autenticado exitosamente: admin@local.com
```

---

## 🛠️ Troubleshooting

### Error 401 (No autorizado)

Si recibes error 401, revisa los logs de la aplicación para ver:

1. **¿El usuario existe en la base de datos?**
   ```
   ✅ [UsuarioService] Usuario encontrado: admin@local.com
   ```

2. **¿La contraseña es correcta?**
   ```
   🔐 [UsuarioService] Verificación de contraseña: true
   ```

3. **¿El usuario está activo?**
   ```
   ✅ [UsuarioService] Usuario encontrado: admin@local.com - Activo: true
   ```

### Verificar Datos en H2 Console

Puedes verificar que los usuarios existen visitando:

1. Ve a: `http://localhost:8080/api/h2-console`
2. JDBC URL: `jdbc:h2:mem:testdb`
3. Usuario: `sa`
4. Contraseña: (dejar vacío)
5. Ejecuta: `SELECT * FROM usuarios WHERE email = 'admin@local.com';`

---

## 📝 Script de Prueba Completo

O simplemente ejecuta el script de prueba:

```powershell
.\test-jwt-simple.ps1
```

Este script probará automáticamente:
1. Endpoint público
2. Login JWT
3. Uso del token en endpoint protegido

---

## ✨ Resumen

**Dos métodos de autenticación funcionando:**

1. **Azure AD (OAuth2)**: Para usuarios corporativos con tokens de Microsoft
2. **JWT Local**: Para usuarios de la base de datos con email/contraseña

Ambos sistemas funcionan en paralelo sin interferirse. 🎉


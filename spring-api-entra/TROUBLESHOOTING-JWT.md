# 🔧 Troubleshooting - Autenticación JWT

## ❌ Error 401 - No Autorizado

### Causa 1: Contraseña Incorrecta o Usuario No Existe

**Síntomas:**
- Postman devuelve 401
- En los logs de la aplicación ves:
  ```
  ❌ [UsuarioService] Usuario no encontrado con email: admin@local.com
  ```
  o
  ```
  ❌ [UsuarioService] Contraseña incorrecta para usuario: admin@local.com
  ```

**Solución:**
1. Verifica que estés usando las credenciales correctas:
   - Email: `admin@local.com`
   - Contraseña: `admin123`

2. Verifica en H2 Console que el usuario existe:
   ```sql
   SELECT * FROM usuarios WHERE email = 'admin@local.com';
   ```

### Causa 2: Header Authorization Mal Configurado

**Síntomas:**
- Error 401
- En logs: `Header de autorización inválido`

**Solución en Postman:**
1. Ve a la pestaña **"Authorization"**
2. Type: **"Basic Auth"** (NO "Bearer Token")
3. Username: `admin@local.com`
4. Password: `admin123`
5. Postman generará automáticamente el header correcto

### Causa 3: Usuario Inactivo

**Síntomas:**
- En logs: `❌ [UsuarioService] Usuario inactivo: admin@local.com`

**Solución:**
```sql
UPDATE usuarios SET activo = true WHERE email = 'admin@local.com';
```

---

## ❌ Error 500 - Internal Server Error

### Causa 1: Problema con BCrypt

**Síntomas:**
- Error 500
- En logs: Error relacionado con `passwordEncoder`

**Solución:**
Verifica que el bean `PasswordEncoder` esté configurado en `SecurityConfig.java`

### Causa 2: Usuario Sin Contraseña

**Síntomas:**
- En logs: `❌ [UsuarioService] Usuario sin contraseña: admin@local.com`

**Solución:**
El usuario en la base de datos no tiene el campo `password` configurado. Verifica el archivo `data.sql`.

---

## ❌ Error 404 - Not Found

### Síntomas:
- Postman dice "404 Not Found"

**Solución:**
Verifica la URL. Debe ser EXACTAMENTE:
```
http://localhost:8080/api/auth/login
```

Nota el `/api` en la URL (es el context path de la aplicación).

---

## ❌ Error de Conexión

### Síntomas:
- "Could not get any response"
- "Connection refused"

**Solución:**
1. Verifica que la aplicación esté ejecutándose:
   ```powershell
   netstat -ano | findstr :8080
   ```

2. Si no está ejecutándose, iníciala:
   ```powershell
   cd spring-api-entra
   mvn spring-boot:run
   ```

---

## 🔍 Verificar Logs de la Aplicación

Los logs te dirán EXACTAMENTE qué está pasando:

```
✅ CORRECTO - Login Exitoso:
🔍 [UsuarioService] Buscando usuario por email: admin@local.com
✅ [UsuarioService] Usuario encontrado: admin@local.com - Activo: true
🔐 [UsuarioService] Verificación de contraseña para admin@local.com: true
✅ [UsuarioService] Usuario autenticado exitosamente: admin@local.com

❌ INCORRECTO - Usuario No Existe:
🔍 [UsuarioService] Buscando usuario por email: admin@local.com
❌ [UsuarioService] Usuario no encontrado con email: admin@local.com

❌ INCORRECTO - Contraseña Incorrecta:
🔍 [UsuarioService] Buscando usuario por email: admin@local.com
✅ [UsuarioService] Usuario encontrado: admin@local.com - Activo: true
🔐 [UsuarioService] Verificación de contraseña para admin@local.com: false
❌ [UsuarioService] Contraseña incorrecta para usuario: admin@local.com
```

---

## 📋 Checklist de Verificación

Antes de probar, verifica:

- [ ] La aplicación está ejecutándose (puerto 8080)
- [ ] La URL es correcta: `http://localhost:8080/api/auth/login`
- [ ] El método es `POST` (no GET)
- [ ] Authorization Type es "Basic Auth"
- [ ] Username es `admin@local.com`
- [ ] Password es `admin123`
- [ ] Content-Type header es `application/json`

---

## 🧪 Prueba Manual con cURL

Si Postman sigue fallando, prueba con cURL:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -u admin@local.com:admin123 \
  -v
```

O en PowerShell:
```powershell
$headers = @{
    "Authorization" = "Basic YWRtaW5AbG9jYWwuY29tOmFkbWluMTIz"
    "Content-Type" = "application/json"
}
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Headers $headers
```

---

## 🔐 Verificar Hash de Contraseña

Las contraseñas en la base de datos deben estar hasheadas con BCrypt.

**Hash correcto de "admin123":**
```
$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
```

Verifica en H2 Console:
```sql
SELECT email, password, activo FROM usuarios WHERE email = 'admin@local.com';
```

---

## 💡 Tips Adicionales

1. **Reinicia la aplicación** después de cambios en `data.sql`
2. **Revisa los logs** - son tu mejor amigo
3. **Usa H2 Console** para verificar datos: `http://localhost:8080/api/h2-console`
4. **Limpia los headers** - A veces Postman guarda headers viejos

---

## 🆘 Si Nada Funciona

1. **Para la aplicación**
2. **Limpia y recompila:**
   ```powershell
   mvn clean compile
   ```
3. **Ejecuta nuevamente:**
   ```powershell
   mvn spring-boot:run
   ```
4. **Revisa los logs de inicio** - ¿Hay algún error?
5. **Verifica el puerto:** ¿Está el 8080 libre?

---

## 📞 Información de Debug

Si sigues teniendo problemas, proporciona:

1. **Código de error HTTP** (401, 500, etc.)
2. **Mensaje de error** del body de Postman
3. **Logs de la aplicación** (últimas 20 líneas)
4. **Screenshot de la configuración de Postman**

¡Con esto podremos identificar el problema exacto! 🎯


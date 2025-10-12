# 🎛️ Guía de Flags de Autenticación

## 📋 **Sistema de Configuración Dinámica**

Tu aplicación ahora tiene **flags en la base de datos** que permiten habilitar/deshabilitar los métodos de autenticación **sin reiniciar la aplicación**.

---

## 🔧 **Flags Disponibles**

| Flag | Descripción | Valor por Defecto |
|------|-------------|-------------------|
| `auth.azure.enabled` | Habilita/deshabilita Azure AD | `true` |
| `auth.jwt.local.enabled` | Habilita/deshabilita JWT Local | `true` |
| `auth.require.mfa` | Requiere MFA (futuro) | `false` |
| `auth.session.timeout` | Timeout de sesión (segundos) | `3600` |

---

## 📮 **ENDPOINTS DE CONFIGURACIÓN**

### **1. Consultar Estado (Público - Sin Auth)**

```
GET http://localhost:8080/api/config/auth/status
```

**Respuesta:**
```json
{
    "azureAdHabilitado": true,
    "jwtLocalHabilitado": true,
    "timestamp": 1728234567890
}
```

---

### **2. Deshabilitar Azure AD (Requiere Auth)**

```
POST http://localhost:8080/api/config/auth/azure/toggle

Headers:
  Authorization: Bearer [tu-token]
  Content-Type: application/json

Body:
{
    "habilitado": false
}
```

**Respuesta:**
```json
{
    "mensaje": "Azure AD deshabilitado",
    "azureAdHabilitado": false,
    "jwtLocalHabilitado": true
}
```

**Efecto:** Los tokens de Azure AD serán rechazados con error 403.

---

### **3. Deshabilitar JWT Local (Requiere Auth)**

```
POST http://localhost:8080/api/config/auth/jwt-local/toggle

Headers:
  Authorization: Bearer [tu-token]
  Content-Type: application/json

Body:
{
    "habilitado": false
}
```

**Respuesta:**
```json
{
    "mensaje": "JWT Local deshabilitado",
    "azureAdHabilitado": true,
    "jwtLocalHabilitado": false
}
```

**Efecto:** Los intentos de login JWT local serán rechazados con error 403.

---

### **4. Ver Todas las Configuraciones de Autenticación**

```
GET http://localhost:8080/api/config/auth/all

Headers:
  Authorization: Bearer [tu-token]
```

**Respuesta:**
```json
[
    {
        "id": 1,
        "clave": "auth.azure.enabled",
        "valor": "true",
        "descripcion": "Habilita/deshabilita autenticación con Azure AD",
        "tipo": "BOOLEAN",
        "categoria": "AUTENTICACION",
        "activo": true,
        "fechaCreacion": "2025-10-06T14:00:00",
        "fechaActualizacion": "2025-10-06T14:00:00"
    },
    ...
]
```

---

## 🎯 **ESCENARIOS DE USO**

### **Escenario 1: Solo Azure AD**

Desactiva JWT Local:

```json
POST /config/auth/jwt-local/toggle
{
    "habilitado": false
}
```

**Resultado:**
- ✅ Usuarios de Azure AD pueden autenticarse
- ❌ Login JWT local rechazado con 403

---

### **Escenario 2: Solo JWT Local**

Desactiva Azure AD:

```json
POST /config/auth/azure/toggle
{
    "habilitado": false
}
```

**Resultado:**
- ❌ Tokens de Azure AD rechazados con 403
- ✅ Login JWT local funciona

---

### **Escenario 3: Ambos Habilitados** (Por Defecto)

```json
POST /config/auth/azure/toggle
{
    "habilitado": true
}

POST /config/auth/jwt-local/toggle
{
    "habilitado": true
}
```

**Resultado:**
- ✅ Ambos métodos de autenticación funcionan
- ✅ Convivencia perfecta

---

### **Escenario 4: Ambos Deshabilitados** (Modo Mantenimiento)

```json
POST /config/auth/azure/toggle
{"habilitado": false}

POST /config/auth/jwt-local/toggle
{"habilitado": false}
```

**Resultado:**
- ❌ Ninguna autenticación funciona
- ⚠️ Solo endpoints públicos accesibles

---

## 🧪 **PRUEBAS EN POSTMAN**

### **Colección de Pruebas Sugerida:**

1. **📊 Ver Estado Actual** (Público)
   ```
   GET /config/auth/status
   ```

2. **🔐 Login JWT** (Para obtener token)
   ```
   POST /auth/login
   Basic Auth: admin@local.com / admin123
   ```

3. **🔴 Deshabilitar JWT Local**
   ```
   POST /config/auth/jwt-local/toggle
   Body: {"habilitado": false}
   ```

4. **🧪 Probar Login JWT** (Debería fallar con 403)
   ```
   POST /auth/login
   Basic Auth: admin@local.com / admin123
   Esperado: 403 Forbidden
   ```

5. **🟢 Habilitar JWT Local**
   ```
   POST /config/auth/jwt-local/toggle
   Body: {"habilitado": true}
   ```

6. **✅ Probar Login JWT** (Ahora debería funcionar)
   ```
   POST /auth/login
   Basic Auth: admin@local.com / admin123
   Esperado: 200 OK + Token
   ```

---

## 🔍 **CÓMO FUNCIONA INTERNAMENTE**

### Verificación en DualAuthenticationFilter:

```java
// Para JWT Local
if (!configuracionService.esJwtLocalHabilitado()) {
    return 403 Forbidden
}

// Para Azure AD
if (!configuracionService.esAzureAdHabilitado()) {
    return 403 Forbidden
}
```

### Caché para Performance:

- ✅ Las configuraciones se cachean para evitar consultas repetidas a la BD
- 🔄 El caché se invalida automáticamente al actualizar configuraciones
- ⚡ Rendimiento óptimo sin overhead

---

## 💾 **PERSISTENCIA EN BASE DE DATOS**

Los flags se almacenan en la tabla `configuracion_sistema`:

```sql
SELECT * FROM configuracion_sistema WHERE categoria = 'AUTENTICACION';
```

Puedes actualizar directamente en la BD:

```sql
UPDATE configuracion_sistema 
SET valor = 'false' 
WHERE clave = 'auth.jwt.local.enabled';
```

**⚠️ NOTA**: Si actualizas directamente en la BD, el caché puede no refrescarse inmediatamente. Usa los endpoints REST para actualizaciones.

---

## 🛡️ **SEGURIDAD**

Los endpoints de cambio de configuración están protegidos:

- ✅ Requieren autenticación (Azure AD o JWT Local)
- ✅ Solo usuarios con permisos `ADMIN` o `SCOPE_access_as_user`
- ✅ Logs detallados de todos los cambios
- ✅ Auditoría con fecha de actualización

---

## 📊 **MONITOREO**

Ver en los logs cuando se consultan o cambian flags:

```
📊 Consultando estado de métodos de autenticación
🔧 [ADMIN] Cambiando estado de Azure AD a: false
⚠️ Token JWT local detectado pero JWT local está DESHABILITADO
```

---

## 🎯 **CASOS DE USO REALES**

### **Mantenimiento Programado:**
```
1. Deshabilitar ambos métodos
2. Realizar mantenimiento
3. Habilitar nuevamente
```

### **Migración Gradual:**
```
1. Habilitar ambos métodos (coexistencia)
2. Migrar usuarios progresivamente
3. Deshabilitar método antiguo
```

### **Testing / Debugging:**
```
1. Aislar un método de autenticación
2. Probar cambios
3. Reactivar el otro método
```

---

## 📝 **EJEMPLO COMPLETO EN POSTMAN**

```javascript
// 1. Verificar estado inicial
GET /config/auth/status
// Resultado: ambos true

// 2. Hacer login JWT y guardar token
POST /auth/login
// Guardar token en variable: {{jwt_token}}

// 3. Deshabilitar JWT Local
POST /config/auth/jwt-local/toggle
Headers: Authorization: {{jwt_token}}
Body: {"habilitado": false}

// 4. Intentar login nuevamente
POST /auth/login
// Resultado: 403 Forbidden - "Autenticación JWT local deshabilitada"

// 5. Reactivar JWT Local
POST /config/auth/jwt-local/toggle
Headers: Authorization: {{jwt_token}}
Body: {"habilitado": true}

// 6. Login funciona nuevamente
POST /auth/login
// Resultado: 200 OK + Token
```

---

## ✨ **VENTAJAS DEL SISTEMA**

✅ **Sin Reinicio** - Cambios dinámicos sin downtime  
✅ **Auditable** - Todos los cambios quedan registrados  
✅ **Flexible** - Habilita/deshabilita según necesidad  
✅ **Seguro** - Solo administradores pueden cambiar  
✅ **Performance** - Caché para consultas rápidas  
✅ **Escalable** - Fácil agregar nuevos flags  

---

**¡El sistema de flags está completamente implementado y listo para usar!** 🎉




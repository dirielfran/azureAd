# 🎉 GUÍA COMPLETA - API PROTEGIDA CON SPRING BOOT Y ANGULAR

¡Felicidades! Has creado exitosamente una integración completa entre Angular y Spring Boot con Microsoft Entra ID. Todo está configurado y listo para usar.

## 📁 Estructura del Proyecto

```
angular-entra-auth/
├── 🌐 Frontend Angular (Puerto 4200)
│   ├── src/app/
│   │   ├── components/protected-data.component.ts
│   │   ├── services/api.service.ts
│   │   ├── config/api.config.ts
│   │   └── ...
│   └── ...
└── spring-api-entra/
    ├── 🚀 Backend Spring Boot (Puerto 8080)
    ├── src/main/java/com/example/apiprotegida/
    │   ├── controller/
    │   ├── model/
    │   ├── repository/
    │   └── config/
    └── ...
```

## 🔑 Credenciales Configuradas

**✅ YA ESTÁN CONFIGURADAS EN EL CÓDIGO:**
- **Client ID**: `4a12fbd8-bf63-4c12-be4c-9678b207fbe7`
- **Tenant ID**: `f128ae87-3797-42d7-8490-82c6b570f832`
- **App Secret**: `wTu8Q~nC5v31H6fYqV8HKX9M3zIz1b.UQSt0hcr5`
- **App ID URI**: `api://4a12fbd8-bf63-4c12-be4c-9678b207fbe7`
- **Scope**: `access_as_user`

## 🚀 CÓMO EJECUTAR TODO

### Paso 1: Ejecutar la API Spring Boot

```powershell
# Navegar al directorio de la API
cd spring-api-entra

# Ejecutar con el script de PowerShell (recomendado)
.\run-api.ps1

# O ejecutar directamente con Maven
mvn spring-boot:run
```

**La API estará disponible en:** `http://localhost:8080/api`

### Paso 2: Ejecutar Angular (en otra terminal)

```powershell
# Navegar al directorio raíz del proyecto Angular
cd ..

# Ejecutar Angular
npm start
```

**Angular estará disponible en:** `http://localhost:4200`

## 🧪 PROBAR LA INTEGRACIÓN

### 1. Abrir Angular
- Ve a `http://localhost:4200`
- Deberías ver la interfaz con navegación

### 2. Iniciar Sesión
- Haz clic en "Iniciar sesión con Microsoft"
- Usa tus credenciales de Microsoft/Azure AD
- Serás redirigido de vuelta a la aplicación

### 3. Probar los Endpoints
Una vez autenticado, prueba estos botones:

#### 🔵 **"Obtener Perfil de Usuario"**
- Llama a Microsoft Graph API
- Muestra tu información de perfil de Azure AD

#### 🔴 **"Obtener Datos Protegidos"**
- Llama a tu API Spring Boot: `/api/data`
- Muestra datos de ejemplo con información del usuario

#### 🟢 **"Obtener Dashboard"**
- Llama a: `/api/data/dashboard`
- Muestra métricas y gráficos de ejemplo

#### 🟡 **"Info de la API"**
- Llama a: `/api/auth/info` (endpoint público)
- Muestra información sobre la API

#### 🔵 **"Ver Token de Acceso"**
- Muestra tu JWT token de acceso
- Útil para debugging

## 📊 Endpoints Adicionales Disponibles

### 🔓 Públicos (sin autenticación)
```http
GET http://localhost:8080/api/auth/info
GET http://localhost:8080/api/actuator/health
GET http://localhost:8080/api/h2-console
```

### 🔒 Protegidos (requieren autenticación)
```http
# Autenticación
GET http://localhost:8080/api/auth/user-info
GET http://localhost:8080/api/auth/test
GET http://localhost:8080/api/auth/token-claims

# Datos
GET http://localhost:8080/api/data
GET http://localhost:8080/api/data/dashboard
GET http://localhost:8080/api/data/proceso-lento
GET http://localhost:8080/api/data/config
GET http://localhost:8080/api/data/reportes/ventas

# Usuarios
GET http://localhost:8080/api/users
POST http://localhost:8080/api/users
GET http://localhost:8080/api/users/mi-perfil
GET http://localhost:8080/api/users/estadisticas
```

## 💾 Base de Datos H2

**Acceso a la consola H2:**
- URL: `http://localhost:8080/api/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Usuario: `sa`
- Contraseña: `password`

**Datos precargados:**
- 10 usuarios de ejemplo
- Diferentes departamentos
- Datos para testing

## 🔍 DEBUGGING Y VERIFICACIÓN

### Verificar que Angular funciona:
1. Ve a `http://localhost:4200`
2. Deberías ver la interfaz de la aplicación
3. El botón "Iniciar sesión" debe estar visible

### Verificar que Spring Boot funciona:
1. Ve a `http://localhost:8080/api/auth/info`
2. Deberías ver información de la API en JSON
3. Ve a `http://localhost:8080/api/actuator/health` para health check

### Verificar la integración:
1. Inicia sesión en Angular
2. Haz clic en "Obtener Datos Protegidos"
3. Deberías ver datos de la API Spring Boot
4. Revisa la consola del navegador para logs detallados

## 🐛 Solución de Problemas

### ❌ Error: "CORS policy"
**Solución:** Verifica que Angular esté en `http://localhost:4200`

### ❌ Error: "Invalid JWT token"
**Solución:** 
1. Cierra sesión y vuelve a iniciar
2. Verifica que las credenciales en Azure AD sean correctas

### ❌ Error: "Connection refused"
**Solución:** 
1. Verifica que Spring Boot esté corriendo en puerto 8080
2. Verifica que Angular esté corriendo en puerto 4200

### ❌ Error en compilación de Spring Boot
**Solución:** 
1. Verifica que tengas Java 17+ instalado
2. Ejecuta `mvn clean install`

## 📈 CARACTERÍSTICAS IMPLEMENTADAS

### ✅ Frontend Angular
- [x] Autenticación Microsoft Entra ID
- [x] Interceptor MSAL automático
- [x] Servicio API centralizado
- [x] Componente de demostración
- [x] Manejo de errores
- [x] Estados de carga
- [x] Interfaz responsive

### ✅ Backend Spring Boot
- [x] Autenticación JWT
- [x] Autorización por scopes
- [x] CORS configurado
- [x] Múltiples controladores
- [x] Base de datos H2
- [x] Datos de ejemplo
- [x] Actuator para monitoring
- [x] Logs detallados

### ✅ Integración
- [x] Tokens automáticos
- [x] Scopes configurados
- [x] Headers Authorization
- [x] Manejo de errores HTTP
- [x] Debugging completo

## 🎯 PRÓXIMOS PASOS

1. **Personalizar endpoints** según tus necesidades
2. **Agregar más funcionalidades** de negocio
3. **Cambiar a base de datos** persistente (PostgreSQL, MySQL)
4. **Agregar tests** unitarios e integración
5. **Desplegar a producción** (Azure, AWS, etc.)
6. **Agregar más scopes** y permisos según necesites

## 🎊 ¡FELICIDADES!

Has creado exitosamente:
- ✅ Una aplicación Angular con autenticación Microsoft Entra ID
- ✅ Una API Spring Boot protegida con JWT
- ✅ Integración completa y funcional
- ✅ Todos los endpoints configurados y probados
- ✅ Base de datos con datos de ejemplo
- ✅ Documentación completa

**Tu aplicación está 100% funcional y lista para usar.** 🚀

## 📞 Soporte

Si encuentras algún problema:
1. Revisa los logs en la consola del navegador
2. Revisa los logs de Spring Boot en la terminal
3. Verifica que ambos servicios estén corriendo
4. Confirma que las URLs sean correctas

¡Disfruta tu nueva aplicación integrada con Microsoft Entra ID! 🎉

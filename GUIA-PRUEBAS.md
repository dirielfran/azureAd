# 🧪 Guía de Pruebas - Sistema de Autorización Azure AD

## 📋 Checklist de Verificación

### ✅ **1. Backend (Spring Boot)**

#### **Compilación y Ejecución**
- [x] ✅ Proyecto compila sin errores
- [ ] 🔄 Backend ejecutándose en puerto 8080
- [ ] 🔄 Base de datos H2 inicializada con datos de ejemplo
- [ ] 🔄 Endpoints REST respondiendo correctamente

#### **Verificar Backend**
```bash
# 1. Verificar que el backend está ejecutándose
curl http://localhost:8080/api/actuator/health

# 2. Verificar base de datos H2 (en navegador)
http://localhost:8080/api/h2-console
# JDBC URL: jdbc:h2:mem:testdb
# Usuario: sa
# Password: password

# 3. Probar endpoint público
curl http://localhost:8080/api/auth/info
```

### ✅ **2. Frontend (Angular)**

#### **Instalación y Compilación**
- [x] ✅ Dependencias instaladas
- [ ] 🔄 Proyecto Angular compila sin errores
- [ ] 🔄 Frontend ejecutándose en puerto 4200
- [ ] 🔄 Integración MSAL configurada

#### **Verificar Frontend**
```bash
# 1. Compilar proyecto Angular
ng build

# 2. Ejecutar en modo desarrollo
ng serve

# 3. Abrir en navegador
http://localhost:4200
```

### ✅ **3. Integración Completa**

#### **Flujo de Autenticación**
- [ ] 🔄 Login con Azure AD funciona
- [ ] 🔄 Token JWT se obtiene correctamente
- [ ] 🔄 Grupos de Azure AD se extraen del token
- [ ] 🔄 Permisos se cargan desde la base de datos
- [ ] 🔄 UI se renderiza según permisos

## 🚀 **Pasos para Probar la Aplicación**

### **Paso 1: Verificar Backend**

1. **Abrir nueva terminal** y navegar al directorio del backend:
```bash
cd spring-api-entra
```

2. **Verificar que está ejecutándose**:
```bash
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8080/api/actuator/health"

# Respuesta esperada:
# status : UP
```

3. **Verificar base de datos**:
- Abrir: http://localhost:8080/api/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Usuario: `sa`, Password: `password`
- Conectar y verificar tablas: `perfiles`, `permisos`, `perfil_permisos`, `usuarios`

4. **Probar endpoints**:
```bash
# Endpoint público
curl http://localhost:8080/api/auth/info

# Verificar que devuelve información sin autenticación
```

### **Paso 2: Ejecutar Frontend**

1. **En el directorio raíz del proyecto**:
```bash
ng serve
```

2. **Verificar compilación**:
- Debe compilar sin errores
- Mostrar mensaje: "✅ Compiled successfully"

3. **Abrir navegador**:
```
http://localhost:4200
```

### **Paso 3: Probar Flujo Completo**

#### **3.1 Página de Inicio**
- ✅ Se muestra la página principal
- ✅ Botón "Iniciar sesión con Microsoft" visible
- ✅ Navegación oculta (usuario no autenticado)

#### **3.2 Proceso de Login**
1. **Hacer clic en "Iniciar sesión con Microsoft"**
2. **Redirección a Azure AD**
3. **Autenticarse con cuenta de Azure AD**
4. **Redirección de vuelta a la aplicación**

#### **3.3 Estado Autenticado**
- ✅ Navegación principal visible
- ✅ Información del usuario mostrada
- ✅ Menús dinámicos según permisos
- ✅ Estadísticas de perfiles y permisos

#### **3.4 Dashboard de Permisos**
- **Navegar a "Mis Permisos"**
- ✅ Información personal del usuario
- ✅ Perfiles asignados (basados en grupos de Azure AD)
- ✅ Lista de permisos disponibles
- ✅ Ejemplos de renderizado condicional

#### **3.5 Navegación Condicional**
- **Probar acceso a diferentes secciones**:
  - `/datos-protegidos` - Requiere `DASHBOARD_LEER`
  - `/usuarios` - Requiere `USUARIOS_LEER`
  - `/reportes` - Requiere permisos del módulo `REPORTES`
  - `/admin` - Solo administradores
  - `/gestion` - Gestores y administradores

#### **3.6 Renderizado Condicional**
- ✅ Botones visibles según permisos
- ✅ Secciones mostradas/ocultas dinámicamente
- ✅ Menús adaptativos al rol del usuario

## 🔧 **Configuración de Pruebas**

### **Datos de Prueba en la Base de Datos**

Los siguientes perfiles están preconfigurados:

| Perfil | Azure Group ID | Permisos |
|--------|---------------|----------|
| **Administrador** | `admin-group-id-123` | TODOS los permisos |
| **Gestor** | `manager-group-id-456` | Gestión limitada |
| **Usuario** | `user-group-id-789` | Permisos básicos |
| **Lector** | `reader-group-id-101` | Solo lectura |

### **Configurar Grupos Reales en Azure AD**

Para probar con grupos reales:

1. **Ir al Azure Portal**
2. **Azure Active Directory > Grupos**
3. **Crear grupos de seguridad**:
   - `Administradores`
   - `Gestores`
   - `Usuarios`
   - `Lectores`

4. **Actualizar IDs en la base de datos**:
```sql
-- Reemplazar con IDs reales de Azure AD
UPDATE perfiles SET azure_group_id = 'real-admin-group-id' WHERE nombre = 'Administrador';
UPDATE perfiles SET azure_group_id = 'real-manager-group-id' WHERE nombre = 'Gestor';
UPDATE perfiles SET azure_group_id = 'real-user-group-id' WHERE nombre = 'Usuario';
UPDATE perfiles SET azure_group_id = 'real-reader-group-id' WHERE nombre = 'Lector';
```

5. **Asignar usuarios a grupos en Azure AD**

## 🐛 **Troubleshooting**

### **Problemas Comunes**

#### **Backend no inicia**
```bash
# Verificar puerto disponible
netstat -ano | findstr :8080

# Verificar logs
mvn spring-boot:run
```

#### **Frontend no compila**
```bash
# Limpiar cache
ng build --delete-output-path

# Verificar dependencias
npm install
```

#### **Error de CORS**
- Verificar configuración en `application.properties`
- Asegurar que Angular está en puerto 4200

#### **Token no contiene grupos**
1. **Verificar configuración en Azure AD**
2. **Habilitar "groups" claim en el token**
3. **Verificar que el usuario pertenece a grupos**

#### **Permisos no se cargan**
1. **Verificar logs del backend**
2. **Comprobar mapeo de grupos en la base de datos**
3. **Verificar que los perfiles tienen permisos asignados**

## 📊 **Endpoints para Debugging**

### **Información del Usuario**
```bash
# Obtener información completa (requiere autenticación)
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/autorizacion/informacion-usuario
```

### **Verificar Permisos**
```bash
# Verificar permiso específico
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/autorizacion/tiene-permiso/USUARIOS_LEER

# Verificar múltiples permisos
curl -X POST -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_TOKEN" \
     -d '["USUARIOS_LEER","REPORTES_CREAR"]' \
     http://localhost:8080/api/autorizacion/verificar-permisos
```

## ✅ **Criterios de Éxito**

La aplicación está funcionando correctamente si:

1. ✅ **Backend responde** en puerto 8080
2. ✅ **Frontend carga** en puerto 4200
3. ✅ **Login con Azure AD** funciona
4. ✅ **Permisos se obtienen** del backend
5. ✅ **UI se adapta** según permisos del usuario
6. ✅ **Navegación funciona** con guards de permisos
7. ✅ **Renderizado condicional** opera correctamente
8. ✅ **SessionStorage** contiene permisos del usuario

## 🎯 **Casos de Prueba**

### **Caso 1: Usuario Administrador**
- Login exitoso
- Acceso a todas las secciones
- Todos los botones y menús visibles
- Dashboard completo de permisos

### **Caso 2: Usuario Gestor**
- Login exitoso
- Acceso limitado a secciones de gestión
- Algunos botones ocultos
- Permisos de gestión visibles

### **Caso 3: Usuario Estándar**
- Login exitoso
- Acceso básico a la aplicación
- Mayoría de funciones administrativas ocultas
- Solo permisos de lectura

### **Caso 4: Usuario Sin Grupos**
- Login exitoso
- Rol por defecto asignado
- Acceso mínimo a la aplicación
- Mensaje de permisos limitados

---

**¡Sigue esta guía paso a paso para verificar que todo funcione correctamente! 🚀**

# 📚 Explicación Detallada de los Cambios Implementados

## 🎯 Objetivo

Implementar un sistema seguro de recuperación de contraseña para usuarios de autenticación local, usando tokens únicos enviados por email.

---

## 📋 Índice

1. [Cambios en Backend (Spring Boot)](#backend)
2. [Cambios en Frontend (Angular)](#frontend)
3. [Configuración](#configuración)
4. [Flujo Completo](#flujo-completo)
5. [Seguridad Implementada](#seguridad)

---

## 🔧 Backend (Spring Boot)

### 1. Dependencia Agregada

**Archivo:** `spring-api-entra/pom.xml`

**Cambio:**
```xml
<!-- Spring Boot Mail (para recuperación de contraseña) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

**Explicación:**
- Agrega la capacidad de enviar emails desde Spring Boot
- Proporciona `JavaMailSender` para envío SMTP
- Necesario para enviar emails de recuperación

---

### 2. Nueva Entidad: PasswordResetToken

**Archivo:** `spring-api-entra/src/main/java/com/example/apiprotegida/model/PasswordResetToken.java`

**¿Qué es?**
Una entidad JPA que almacena tokens de recuperación de contraseña en la base de datos.

**Campos:**
- `id`: Identificador único
- `token`: Token único de 32 caracteres (Base64)
- `usuario`: Relación ManyToOne con Usuario
- `fechaExpiracion`: Cuándo expira el token (1 hora por defecto)
- `usado`: Si el token ya fue utilizado (un solo uso)
- `fechaCreacion`: Cuándo se creó el token

**Métodos importantes:**
- `isExpirado()`: Verifica si el token expiró
- `esValido()`: Verifica si el token es válido (no usado y no expirado)

**Por qué:**
- Permite almacenar tokens de forma segura
- Permite invalidar tokens usados
- Permite verificar expiración
- Permite rate limiting (contar solicitudes recientes)

---

### 3. Nuevo Repositorio: PasswordResetTokenRepository

**Archivo:** `spring-api-entra/src/main/java/com/example/apiprotegida/repository/PasswordResetTokenRepository.java`

**Funcionalidades:**
1. `findByToken(String token)`: Busca un token por su valor
2. `findTokenValidoPorUsuario()`: Busca tokens válidos para un usuario
3. `invalidarTokensDelUsuario()`: Marca todos los tokens de un usuario como usados
4. `eliminarTokensExpirados()`: Limpia tokens expirados (limpieza)
5. `contarTokensRecientes()`: Cuenta tokens recientes (para rate limiting)

**Por qué:**
- Abstrae el acceso a la base de datos
- Permite consultas optimizadas
- Facilita rate limiting y limpieza

---

### 4. Nuevo Servicio: EmailService

**Archivo:** `spring-api-entra/src/main/java/com/example/apiprotegida/service/EmailService.java`

**Responsabilidades:**
- Enviar emails de recuperación de contraseña
- Manejar errores de envío
- Proporcionar fallback (loguear token si no hay email configurado)

**Características:**
1. **Modo Desarrollo:**
   - Si no hay `JavaMailSender` configurado, solo loguea el token
   - Permite probar sin configurar SMTP

2. **Modo Producción:**
   - Envía email real con link de recuperación
   - Maneja errores gracefully

3. **Manejo de Errores:**
   - No lanza excepción si falla el envío
   - Loguea el token como backup
   - Muestra mensajes informativos

**Método principal:**
```java
public void enviarEmailRecuperacion(String emailDestino, String token, String nombreUsuario)
```

**Flujo:**
1. Verifica si hay `mailSender` configurado
2. Si no hay → loguea token (modo desarrollo)
3. Si hay → crea mensaje de email
4. Configura remitente, destinatario, asunto y cuerpo
5. Envía email
6. Si falla → loguea token como backup

---

### 5. Nuevo Servicio: PasswordResetService

**Archivo:** `spring-api-entra/src/main/java/com/example/apiprotegida/service/PasswordResetService.java`

**Responsabilidades:**
- Generar tokens seguros
- Validar tokens
- Procesar solicitudes de recuperación
- Resetear contraseñas
- Rate limiting

**Métodos principales:**

#### `solicitarRecuperacion(String email)`
1. Busca usuario por email
2. Verifica que tenga contraseña local (no solo Azure AD)
3. Verifica rate limiting (máximo 3 solicitudes por hora)
4. Invalida tokens anteriores del usuario
5. Genera nuevo token seguro
6. Guarda token en base de datos
7. Envía email con token

**Seguridad:**
- Siempre retorna éxito (no revela si el email existe)
- Rate limiting previene abuso
- Tokens anteriores se invalidan

#### `resetearPassword(String token, String nuevaPassword)`
1. Busca token en base de datos
2. Valida que el token sea válido (no usado, no expirado)
3. Valida nueva contraseña (mínimo 6 caracteres)
4. Actualiza contraseña del usuario
5. Marca token como usado

#### `validarToken(String token)`
- Verifica si un token es válido sin usarlo
- Útil para validar antes de mostrar formulario

#### `generarTokenSeguro()`
- Usa `SecureRandom` para generar bytes aleatorios
- Convierte a Base64 URL-safe
- Genera tokens de 32 caracteres
- No predecible, criptográficamente seguro

---

### 6. Método Agregado: UsuarioService.actualizarPassword()

**Archivo:** `spring-api-entra/src/main/java/com/example/apiprotegida/service/UsuarioService.java`

**Método:**
```java
public void actualizarPassword(Usuario usuario, String nuevaPassword)
```

**Funcionalidad:**
- Valida que la contraseña no esté vacía
- Valida longitud mínima (6 caracteres)
- Codifica contraseña con BCrypt
- Guarda en base de datos

**Por qué:**
- Centraliza la lógica de actualización de contraseña
- Asegura que siempre se codifique con BCrypt
- Valida antes de guardar

---

### 7. Endpoints Agregados: LocalAuthController

**Archivo:** `spring-api-entra/src/main/java/com/example/apiprotegida/controller/LocalAuthController.java`

#### Endpoint 1: `POST /auth/local/forgot-password`

**Request:**
```json
{
  "email": "usuario@ejemplo.com"
}
```

**Response:**
```json
{
  "message": "Si el email existe en nuestro sistema, recibirás un enlace de recuperación"
}
```

**Funcionalidad:**
- Recibe email del usuario
- Procesa solicitud de recuperación
- Siempre retorna el mismo mensaje (por seguridad)

**Seguridad:**
- No revela si el email existe
- Siempre retorna éxito

#### Endpoint 2: `POST /auth/local/reset-password`

**Request:**
```json
{
  "token": "token-generado",
  "newPassword": "nuevaContraseña123"
}
```

**Response (éxito):**
```json
{
  "message": "Contraseña actualizada exitosamente"
}
```

**Response (error):**
```json
{
  "error": "Token inválido o expirado"
}
```

**Funcionalidad:**
- Valida token
- Valida nueva contraseña
- Actualiza contraseña
- Marca token como usado

#### Endpoint 3: `POST /auth/local/validate-reset-token`

**Request:**
```json
{
  "token": "token-generado"
}
```

**Response:**
```json
{
  "valid": true
}
```

**Funcionalidad:**
- Valida token sin usarlo
- Útil para verificar antes de mostrar formulario

---

### 8. Configuración: application.properties

**Archivo:** `spring-api-entra/src/main/resources/application.properties`

**Cambios agregados:**

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=ccscoffeeshopar@gmail.com
spring.mail.password=wtamefxdvrztwiin
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=10000
spring.mail.properties.mail.smtp.timeout=10000
spring.mail.properties.mail.smtp.writetimeout=10000
spring.mail.properties.mail.smtp.from=ccscoffeeshopar@gmail.com

# URL de la aplicación
app.url=http://localhost:4200

# Password Reset Configuration
password.reset.token.expiration.hours=1
password.reset.rate.limit.max=3
```

**Explicación:**
- `spring.mail.*`: Configuración SMTP para Gmail
- `app.url`: URL base para links en emails
- `password.reset.*`: Configuración de tokens (expiración, rate limiting)

---

### 9. SecurityConfig (Sin cambios necesarios)

**Archivo:** `spring-api-entra/src/main/java/com/example/apiprotegida/config/SecurityConfig.java`

**Estado:**
- Ya tenía `/auth/local/**` en `permitAll()`
- Los nuevos endpoints son accesibles sin autenticación
- Correcto para recuperación de contraseña

---

## 🎨 Frontend (Angular)

### 1. Nuevo Servicio: PasswordResetService

**Archivo:** `src/app/services/password-reset.service.ts`

**Funcionalidad:**
- Comunica con los endpoints del backend
- Proporciona métodos TypeScript para recuperación

**Métodos:**
1. `solicitarRecuperacion(email)`: Solicita recuperación
2. `resetearPassword(token, newPassword)`: Resetea contraseña
3. `validarToken(token)`: Valida token

**Por qué:**
- Centraliza comunicación con API
- Facilita reutilización
- Proporciona tipado TypeScript

---

### 2. Nuevo Componente: ForgotPasswordComponent

**Archivos:**
- `src/app/components/forgot-password.component.ts`
- `src/app/components/forgot-password.component.html`
- `src/app/components/forgot-password.component.scss`

**Funcionalidad:**
- Formulario para solicitar recuperación
- Validación de email
- Mensajes de éxito/error
- Navegación al login

**Flujo:**
1. Usuario ingresa email
2. Valida formato de email
3. Envía solicitud al backend
4. Muestra mensaje de éxito (siempre el mismo, por seguridad)
5. Opción de volver al login

**Características:**
- Validación de email en frontend
- Loading state durante petición
- Mensajes informativos
- Diseño consistente con login

---

### 3. Nuevo Componente: ResetPasswordComponent

**Archivos:**
- `src/app/components/reset-password.component.ts`
- `src/app/components/reset-password.component.html`
- `src/app/components/reset-password.component.scss`

**Funcionalidad:**
- Lee token de query params de URL
- Valida token antes de mostrar formulario
- Formulario para nueva contraseña
- Confirmación de contraseña
- Reseteo de contraseña
- Redirección al login después de éxito

**Flujo:**
1. Lee `?token=...` de la URL
2. Valida token con backend
3. Si válido → muestra formulario
4. Si inválido → muestra error y opción de solicitar nuevo
5. Usuario ingresa nueva contraseña
6. Valida que coincidan
7. Envía al backend
8. Muestra éxito
9. Redirige al login después de 3 segundos

**Características:**
- Validación de token automática
- Validación de contraseñas (mínimo 6 caracteres, coincidencia)
- Toggle para mostrar/ocultar contraseñas
- Manejo de errores
- Redirección automática

---

### 4. Modificación: LocalLoginComponent

**Archivo:** `src/app/components/local-login.component.html`

**Cambio:**
Agregado enlace "¿Olvidaste tu contraseña?" en el footer del formulario de login.

**Código agregado:**
```html
<p class="help-text">
  <a routerLink="/forgot-password" class="forgot-password-link">
    ¿Olvidaste tu contraseña?
  </a>
</p>
```

**Por qué:**
- Facilita acceso a recuperación
- UX mejorada
- Ubicación estándar (footer del login)

---

### 5. Modificación: LocalLoginComponent (SCSS)

**Archivo:** `src/app/components/local-login.component.scss`

**Cambio:**
Agregados estilos para el enlace de recuperación.

**Estilos:**
- Color azul (#667eea)
- Hover con underline
- Transición suave
- Consistente con diseño

---

### 6. Modificación: AppRoutingModule

**Archivo:** `src/app/app-routing.module.ts`

**Cambios:**
Agregadas dos nuevas rutas:

```typescript
{
  path: 'forgot-password',
  component: ForgotPasswordComponent,
  data: { title: 'Recuperar contraseña' }
},
{
  path: 'reset-password',
  component: ResetPasswordComponent,
  data: { title: 'Restablecer contraseña' }
}
```

**Por qué:**
- Permite navegación a componentes
- URLs amigables
- Sin guards (rutas públicas)

---

### 7. Modificación: AppModule

**Archivo:** `src/app/app.module.ts`

**Cambios:**
1. Importados nuevos componentes
2. Agregados a `declarations`

**Código:**
```typescript
import { ForgotPasswordComponent } from './components/forgot-password.component';
import { ResetPasswordComponent } from './components/reset-password.component';

// En declarations:
ForgotPasswordComponent,
ResetPasswordComponent,
```

**Por qué:**
- Registra componentes en Angular
- Necesario para usar en rutas

---

### 8. Modificación: AppComponent

**Archivo:** `src/app/app.component.ts`

**Cambio crítico:**
Agregadas rutas públicas para evitar redirección al login.

**Antes:**
```typescript
const publicRoutes = ['/auth-selector', '/login'];
if (!publicRoutes.includes(currentUrl)) {
  // Redirigir a login
}
```

**Después:**
```typescript
const publicRoutes = ['/auth-selector', '/login', '/forgot-password', '/reset-password'];
const isPublicRoute = publicRoutes.some(route => currentUrl.startsWith(route));
if (!isPublicRoute) {
  // Redirigir a login
}
```

**Por qué:**
- Sin esto, `/reset-password?token=...` redirigía al login
- `startsWith` permite query params
- Permite acceso sin autenticación

---

## 🔄 Flujo Completo

### Flujo de Usuario:

1. **Usuario olvida contraseña:**
   - Va a `/login`
   - Hace clic en "¿Olvidaste tu contraseña?"
   - Va a `/forgot-password`

2. **Solicita recuperación:**
   - Ingresa email
   - Hace clic en "Enviar Enlace"
   - Frontend llama a `POST /auth/local/forgot-password`
   - Backend genera token y envía email
   - Usuario ve mensaje de éxito

3. **Recibe email:**
   - Abre email
   - Hace clic en link: `http://localhost:4200/reset-password?token=ABC123...`
   - Angular navega a `/reset-password` con token en query params

4. **Resetea contraseña:**
   - Componente lee token de URL
   - Valida token con backend
   - Si válido → muestra formulario
   - Usuario ingresa nueva contraseña
   - Frontend llama a `POST /auth/local/reset-password`
   - Backend valida token, actualiza contraseña, marca token como usado
   - Usuario ve mensaje de éxito
   - Redirección automática a `/login` después de 3 segundos

5. **Inicia sesión:**
   - Usuario va a `/login`
   - Ingresa email y nueva contraseña
   - Login exitoso

---

## 🔐 Seguridad Implementada

### 1. Tokens Seguros
- **Generación:** `SecureRandom` + Base64 URL-safe
- **Longitud:** 32 caracteres
- **No predecible:** Criptográficamente seguro
- **Único:** Cada token es diferente

### 2. Expiración
- **Duración:** 1 hora (configurable)
- **Validación:** Se verifica en cada uso
- **Limpieza:** Tokens expirados pueden eliminarse

### 3. Un Solo Uso
- **Invalidación:** Token se marca como usado después de resetear
- **Prevención:** No se puede reutilizar el mismo token

### 4. Rate Limiting
- **Límite:** Máximo 3 solicitudes por hora por email
- **Prevención:** Evita abuso del sistema
- **Implementación:** Cuenta tokens recientes en base de datos

### 5. No Revelación de Información
- **Respuesta uniforme:** Siempre el mismo mensaje
- **No revela:** Si el email existe o no
- **Seguridad:** Previene enumeración de usuarios

### 6. Validación de Contraseña
- **Longitud mínima:** 6 caracteres
- **Confirmación:** Debe coincidir con confirmación
- **Codificación:** Siempre se codifica con BCrypt

### 7. Invalidación de Tokens Anteriores
- **Al generar nuevo:** Tokens anteriores se invalidan
- **Prevención:** Solo el último token es válido
- **Seguridad:** Previene uso de tokens antiguos

---

## 📊 Estructura de Base de Datos

### Nueva Tabla: `password_reset_tokens`

```sql
CREATE TABLE password_reset_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(64) UNIQUE NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
```

**Índices:**
- `token` (único) - Para búsqueda rápida
- `usuario_id` - Para consultas por usuario
- `fecha_creacion` - Para rate limiting

---

## 🎯 Puntos Clave de la Implementación

### 1. Modo Desarrollo vs Producción

**Desarrollo:**
- Si no hay email configurado → token se loguea
- Permite probar sin SMTP
- Token disponible en logs

**Producción:**
- Email configurado → envía email real
- Token también en logs (backup)
- Mejor experiencia de usuario

### 2. Manejo de Errores

**Backend:**
- No lanza excepciones que rompan el flujo
- Loguea tokens como backup
- Mensajes informativos en logs

**Frontend:**
- Maneja errores gracefully
- Muestra mensajes claros
- Permite reintentar

### 3. UX/UI

**Consistencia:**
- Mismo diseño que login
- Colores y estilos consistentes
- Animaciones y transiciones

**Accesibilidad:**
- Enlaces claros
- Mensajes informativos
- Navegación intuitiva

---

## 📝 Archivos Modificados/Creados

### Backend (9 archivos):
1. `pom.xml` - Dependencia agregada
2. `PasswordResetToken.java` - Nueva entidad
3. `PasswordResetTokenRepository.java` - Nuevo repositorio
4. `EmailService.java` - Nuevo servicio
5. `PasswordResetService.java` - Nuevo servicio
6. `UsuarioService.java` - Método agregado
7. `LocalAuthController.java` - Endpoints agregados
8. `application.properties` - Configuración agregada
9. `SecurityConfig.java` - Sin cambios (ya permitía rutas)

### Frontend (8 archivos):
1. `password-reset.service.ts` - Nuevo servicio
2. `forgot-password.component.ts` - Nuevo componente
3. `forgot-password.component.html` - Template
4. `forgot-password.component.scss` - Estilos
5. `reset-password.component.ts` - Nuevo componente
6. `reset-password.component.html` - Template
7. `reset-password.component.scss` - Estilos
8. `local-login.component.html` - Enlace agregado
9. `local-login.component.scss` - Estilos agregados
10. `app-routing.module.ts` - Rutas agregadas
11. `app.module.ts` - Componentes registrados
12. `app.component.ts` - Rutas públicas agregadas

**Total: 21 archivos modificados/creados**

---

## ✅ Resultado Final

Un sistema completo de recuperación de contraseña que:
- ✅ Es seguro (tokens únicos, expiración, rate limiting)
- ✅ Es funcional (envía emails, resetea contraseñas)
- ✅ Tiene buena UX (interfaz clara, mensajes informativos)
- ✅ Maneja errores (fallbacks, logging)
- ✅ Está documentado (guías y explicaciones)

---

**¿Tienes alguna pregunta sobre algún cambio específico?** 🤔







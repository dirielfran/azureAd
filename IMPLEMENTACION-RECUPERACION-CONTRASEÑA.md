# ✅ Implementación: Recuperación de Contraseña (Token + Email)

## 📋 Resumen

Se ha implementado exitosamente el sistema de recuperación de contraseña usando **tokens seguros enviados por email** (Opción 1).

---

## 🎯 Funcionalidades Implementadas

### Backend (Spring Boot)

1. **Entidad `PasswordResetToken`**
   - Almacena tokens de recuperación
   - Campos: token, usuario, fechaExpiracion, usado, fechaCreacion
   - Validación de expiración y uso

2. **Repositorio `PasswordResetTokenRepository`**
   - Búsqueda de tokens
   - Invalidación de tokens
   - Limpieza de tokens expirados
   - Rate limiting (contar solicitudes recientes)

3. **Servicio `EmailService`**
   - Envío de emails de recuperación
   - Modo desarrollo: loguea tokens en consola si no hay SMTP configurado
   - Genera links de recuperación

4. **Servicio `PasswordResetService`**
   - Generación de tokens seguros (32 caracteres)
   - Validación de tokens
   - Rate limiting (máximo 3 solicitudes por hora)
   - Invalidación de tokens usados
   - Actualización de contraseñas

5. **Controlador `LocalAuthController`**
   - `POST /auth/local/forgot-password` - Solicitar recuperación
   - `POST /auth/local/reset-password` - Resetear contraseña
   - `POST /auth/local/validate-reset-token` - Validar token

6. **Configuración**
   - Propiedades de email en `application.properties`
   - Endpoints públicos en `SecurityConfig`
   - Dependencia Spring Mail agregada

### Frontend (Angular)

1. **Servicio `PasswordResetService`**
   - Métodos para solicitar recuperación, resetear y validar tokens

2. **Componente `ForgotPasswordComponent`**
   - Formulario para solicitar recuperación
   - Validación de email
   - Mensajes de éxito/error

3. **Componente `ResetPasswordComponent`**
   - Validación de token desde URL
   - Formulario para nueva contraseña
   - Confirmación de contraseña
   - Redirección automática al login

4. **Integración en Login**
   - Enlace "¿Olvidaste tu contraseña?" en componente de login
   - Rutas configuradas en `app-routing.module.ts`

---

## 🔐 Características de Seguridad

✅ **Tokens seguros**: Generados con `SecureRandom` y Base64 (32 caracteres)  
✅ **Expiración**: Tokens expiran en 1 hora (configurable)  
✅ **Un solo uso**: Tokens se invalidan después de usar  
✅ **Rate limiting**: Máximo 3 solicitudes por hora por email  
✅ **No revelación de información**: Siempre retorna el mismo mensaje (no revela si el email existe)  
✅ **Validación de contraseña**: Mínimo 6 caracteres  
✅ **Invalidación automática**: Tokens anteriores se invalidan al generar uno nuevo  

---

## 📁 Archivos Creados/Modificados

### Backend

**Nuevos archivos:**
- `spring-api-entra/src/main/java/com/example/apiprotegida/model/PasswordResetToken.java`
- `spring-api-entra/src/main/java/com/example/apiprotegida/repository/PasswordResetTokenRepository.java`
- `spring-api-entra/src/main/java/com/example/apiprotegida/service/EmailService.java`
- `spring-api-entra/src/main/java/com/example/apiprotegida/service/PasswordResetService.java`

**Archivos modificados:**
- `spring-api-entra/pom.xml` - Agregada dependencia Spring Mail
- `spring-api-entra/src/main/resources/application.properties` - Configuración de email
- `spring-api-entra/src/main/java/com/example/apiprotegida/controller/LocalAuthController.java` - Endpoints agregados
- `spring-api-entra/src/main/java/com/example/apiprotegida/service/UsuarioService.java` - Método `actualizarPassword` agregado

### Frontend

**Nuevos archivos:**
- `src/app/services/password-reset.service.ts`
- `src/app/components/forgot-password.component.ts`
- `src/app/components/forgot-password.component.html`
- `src/app/components/forgot-password.component.scss`
- `src/app/components/reset-password.component.ts`
- `src/app/components/reset-password.component.html`
- `src/app/components/reset-password.component.scss`

**Archivos modificados:**
- `src/app/app.module.ts` - Componentes registrados
- `src/app/app-routing.module.ts` - Rutas agregadas
- `src/app/components/local-login.component.html` - Enlace agregado
- `src/app/components/local-login.component.scss` - Estilos agregados

---

## ⚙️ Configuración

### Email (Opcional - Para Producción)

Para habilitar envío real de emails, configura en `application.properties`:

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Nota:** En desarrollo, si no se configura email, los tokens se loguean en la consola del servidor.

### Variables de Entorno (Recomendado)

```properties
# Usar variables de entorno para seguridad
spring.mail.host=${MAIL_HOST:}
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
```

---

## 🚀 Uso

### Flujo de Usuario

1. **Usuario olvida contraseña:**
   - Hace clic en "¿Olvidaste tu contraseña?" en la página de login
   - Ingresa su email
   - Recibe email con link de recuperación (o ve token en consola en desarrollo)

2. **Usuario recibe email:**
   - Hace clic en el link (ej: `http://localhost:4200/reset-password?token=ABC123...`)
   - Se valida el token automáticamente
   - Si es válido, puede ingresar nueva contraseña

3. **Usuario resetea contraseña:**
   - Ingresa nueva contraseña (mínimo 6 caracteres)
   - Confirma contraseña
   - Se actualiza la contraseña
   - Redirección automática al login

### Endpoints API

#### Solicitar Recuperación
```http
POST /api/auth/local/forgot-password
Content-Type: application/json

{
  "email": "usuario@ejemplo.com"
}
```

**Respuesta:**
```json
{
  "message": "Si el email existe en nuestro sistema, recibirás un enlace de recuperación"
}
```

#### Resetear Contraseña
```http
POST /api/auth/local/reset-password
Content-Type: application/json

{
  "token": "token-generado",
  "newPassword": "nuevaContraseña123"
}
```

**Respuesta exitosa:**
```json
{
  "message": "Contraseña actualizada exitosamente"
}
```

#### Validar Token
```http
POST /api/auth/local/validate-reset-token
Content-Type: application/json

{
  "token": "token-generado"
}
```

**Respuesta:**
```json
{
  "valid": true
}
```

---

## 🧪 Pruebas

### Modo Desarrollo (Sin Email Configurado)

1. Solicita recuperación de contraseña
2. Revisa los logs del servidor Spring Boot
3. Busca el mensaje: `🔑 [EmailService] Token de recuperación para...`
4. Copia el token y ve a: `http://localhost:4200/reset-password?token=TOKEN_COPIADO`
5. Ingresa nueva contraseña

### Modo Producción (Con Email Configurado)

1. Configura SMTP en `application.properties`
2. Solicita recuperación
3. Revisa tu email
4. Haz clic en el link recibido
5. Ingresa nueva contraseña

---

## 📊 Configuración Avanzada

### Cambiar Expiración de Token

En `application.properties`:
```properties
password.reset.token.expiration.hours=2  # Cambiar a 2 horas
```

### Cambiar Rate Limiting

En `application.properties`:
```properties
password.reset.rate.limit.max=5  # Máximo 5 solicitudes por hora
```

### Cambiar URL de la Aplicación

En `application.properties`:
```properties
app.url=https://tu-dominio.com
```

---

## ✅ Estado de Implementación

- ✅ Backend completo
- ✅ Frontend completo
- ✅ Integración con login
- ✅ Seguridad implementada
- ✅ Rate limiting
- ✅ Validaciones
- ✅ Manejo de errores
- ✅ Modo desarrollo (sin email)
- ⚠️ Configuración de email (opcional, para producción)

---

## 📝 Notas Importantes

1. **Seguridad**: Los endpoints siempre retornan el mismo mensaje para no revelar si un email existe
2. **Rate Limiting**: Previene abuso del sistema (máximo 3 solicitudes por hora)
3. **Tokens Únicos**: Cada token se genera con `SecureRandom` y Base64
4. **Invalidación**: Los tokens anteriores se invalidan al generar uno nuevo
5. **Expiración**: Los tokens expiran automáticamente después de 1 hora
6. **Modo Desarrollo**: Si no hay email configurado, los tokens se loguean en consola

---

## 🔄 Próximos Pasos (Opcional)

1. **Configurar email SMTP** para producción
2. **Agregar tests unitarios** para los servicios
3. **Implementar limpieza automática** de tokens expirados (scheduled task)
4. **Agregar métricas** de uso del sistema de recuperación
5. **Personalizar templates de email** (HTML en lugar de texto plano)

---

## 🐛 Troubleshooting

### El token no funciona
- Verifica que el token no haya expirado (1 hora)
- Verifica que el token no haya sido usado ya
- Revisa los logs del servidor para más detalles

### No recibo emails
- Verifica configuración SMTP en `application.properties`
- En desarrollo, revisa los logs del servidor (tokens se loguean)
- Verifica que el email del usuario exista en la base de datos

### Error al resetear contraseña
- Verifica que la contraseña tenga al menos 6 caracteres
- Verifica que ambas contraseñas coincidan
- Revisa los logs del servidor para errores específicos

---

**Implementación completada exitosamente** ✅








# 🔐 Opciones para Recuperación de Contraseña Segura

## Análisis del Proyecto

El proyecto actualmente tiene:
- ✅ Autenticación local con JWT
- ✅ Entidad Usuario con campo `email`
- ✅ PasswordEncoder (BCrypt) configurado
- ❌ No tiene sistema de recuperación de contraseña
- ❌ No tiene dependencia de email configurada

---

## 📋 Opciones Disponibles

### **Opción 1: Token de Reseteo con Email** ⭐ (RECOMENDADA)

**Descripción:**
- Genera un token único y aleatorio cuando el usuario solicita recuperación
- Envía el token por email con un link de reseteo
- El token tiene expiración (ej: 1 hora)
- Almacena el token en base de datos con timestamp

**Ventajas:**
- ✅ Muy segura (token único, no predecible)
- ✅ Estándar de la industria
- ✅ El usuario no necesita recordar nada
- ✅ Token con expiración limita el riesgo
- ✅ Se puede invalidar tokens usados

**Desventajas:**
- ❌ Requiere configurar servidor de email (SMTP)
- ❌ Requiere crear tabla para almacenar tokens
- ❌ Dependencia adicional (Spring Mail)

**Implementación:**
- Nueva entidad: `PasswordResetToken`
- Nuevo servicio: `PasswordResetService`
- Nuevo controlador: Endpoints `/auth/local/forgot-password` y `/auth/local/reset-password`
- Configuración SMTP en `application.properties`
- Componente Angular para solicitar y resetear contraseña

**Seguridad:**
- Token aleatorio de 32+ caracteres
- Expiración de 1 hora
- Un solo uso (se invalida después de usar)
- Rate limiting (máximo 3 intentos por hora por email)

---

### **Opción 2: Código OTP (One-Time Password)**

**Descripción:**
- Genera código numérico de 6 dígitos
- Envía por email o SMS
- Código con expiración corta (15 minutos)
- Almacena código en base de datos

**Ventajas:**
- ✅ Más fácil de ingresar para el usuario
- ✅ Expiración corta reduce riesgo
- ✅ Similar seguridad a tokens

**Desventajas:**
- ❌ Requiere email o SMS
- ❌ Código más corto (menos seguro que token largo)
- ❌ Puede ser vulnerable a fuerza bruta si no hay rate limiting

**Implementación:**
- Similar a Opción 1 pero con código numérico
- Validación de intentos fallidos
- Rate limiting más estricto

---

### **Opción 3: Preguntas de Seguridad**

**Descripción:**
- Usuario responde preguntas predefinidas al registrarse
- Al recuperar contraseña, debe responder correctamente
- No requiere email

**Ventajas:**
- ✅ No requiere configuración de email
- ✅ Funciona sin infraestructura adicional
- ✅ Útil para entornos sin email

**Desventajas:**
- ❌ Menos seguro (preguntas pueden ser adivinadas)
- ❌ Requiere modificar entidad Usuario
- ❌ Usuario debe recordar respuestas
- ❌ No es estándar moderno

**Implementación:**
- Agregar campos `pregunta_seguridad` y `respuesta_seguridad` a Usuario
- Hash de respuesta (similar a contraseña)
- Endpoint para validar respuesta y permitir cambio

---

### **Opción 4: JWT para Reseteo**

**Descripción:**
- Usa la infraestructura JWT existente
- Genera JWT especial con claim `type: password-reset`
- Envía link con JWT por email
- Valida JWT al hacer reset

**Ventajas:**
- ✅ Reutiliza infraestructura existente
- ✅ No requiere tabla adicional
- ✅ Token firmado y verificado automáticamente

**Desventajas:**
- ❌ Requiere email
- ❌ JWT puede ser más largo en URL
- ❌ Menos control sobre invalidación (hasta que expire)

**Implementación:**
- Modificar `JWTTokenProvider` para generar tokens de reseteo
- Endpoint para validar y procesar reseteo
- Configuración de email

---

## 🎯 Recomendación

**Opción 1: Token de Reseteo con Email** es la más recomendada porque:
1. Es el estándar de la industria
2. Ofrece el mejor balance seguridad/facilidad
3. Es lo que los usuarios esperan
4. Permite control granular (invalidación, rate limiting)

---

## 📦 Dependencias Necesarias

Para implementar Opción 1 o 2, necesitarás agregar:

```xml
<!-- Spring Boot Mail -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

---

## 🔧 Configuración SMTP Necesaria

En `application.properties`:

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Nota:** Para Gmail, necesitarás generar una "App Password" en lugar de usar tu contraseña normal.

---

## 📊 Comparación Rápida

| Opción | Seguridad | Facilidad | Requiere Email | Complejidad |
|--------|-----------|-----------|----------------|-------------|
| Token + Email | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ✅ | Media |
| OTP | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ✅ | Media |
| Preguntas | ⭐⭐⭐ | ⭐⭐⭐ | ❌ | Baja |
| JWT | ⭐⭐⭐⭐ | ⭐⭐⭐ | ✅ | Baja |

---

## 🚀 Próximos Pasos

1. Decidir qué opción implementar
2. Si es Opción 1 o 2: Configurar email
3. Crear entidades y servicios necesarios
4. Implementar endpoints en backend
5. Crear componentes en frontend
6. Agregar tests de seguridad



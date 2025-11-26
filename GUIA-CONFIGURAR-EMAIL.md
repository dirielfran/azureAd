# 📧 Guía Paso a Paso: Configurar Email SMTP

## 🎯 Objetivo

Configurar el envío de emails reales para la recuperación de contraseña.

---

## 📋 Opción 1: Gmail (Recomendado para Desarrollo/Pruebas)

### Paso 1: Habilitar Verificación en 2 Pasos

1. Ve a tu cuenta de Google: https://myaccount.google.com/
2. Ve a **Seguridad** → **Verificación en 2 pasos**
3. Actívala si no está activada

### Paso 2: Generar App Password

1. En la misma página de Seguridad, busca **"Contraseñas de aplicaciones"**
2. O ve directamente a: https://myaccount.google.com/apppasswords
3. Selecciona **"Aplicación"**: "Correo"
4. Selecciona **"Dispositivo"**: "Otro (nombre personalizado)"
5. Escribe: "Spring Boot App"
6. Haz clic en **"Generar"**
7. **Copia la contraseña de 16 caracteres** (se muestra solo una vez)

### Paso 3: Configurar en application.properties

Edita el archivo: `spring-api-entra/src/main/resources/application.properties`

```properties
# Email Configuration (Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=TU-EMAIL@gmail.com
spring.mail.password=TU-APP-PASSWORD-AQUI
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Reemplaza:**
- `TU-EMAIL@gmail.com` → Tu email de Gmail
- `TU-APP-PASSWORD-AQUI` → La contraseña de 16 caracteres que copiaste

### Paso 4: Probar

1. Reinicia el servidor Spring Boot
2. Solicita recuperación de contraseña desde la aplicación
3. Revisa tu bandeja de entrada (y spam)

---

## 📋 Opción 2: Outlook/Office 365

### Configuración

```properties
# Email Configuration (Outlook)
spring.mail.host=smtp.office365.com
spring.mail.port=587
spring.mail.username=TU-EMAIL@outlook.com
spring.mail.password=TU-CONTRASEÑA
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Nota:** Para Outlook, puedes usar tu contraseña normal (no necesitas App Password).

---

## 📋 Opción 3: Servidor Corporativo

Si tu empresa tiene un servidor SMTP:

```properties
# Email Configuration (Servidor Corporativo)
spring.mail.host=smtp.empresa.com
spring.mail.port=587
spring.mail.username=tu-usuario
spring.mail.password=tu-contraseña
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Pregunta a tu administrador de IT:**
- Host SMTP
- Puerto (generalmente 587 o 465)
- Si requiere autenticación
- Si usa TLS/SSL

---

## 📋 Opción 4: Variables de Entorno (Recomendado para Producción)

En lugar de poner credenciales en el archivo, usa variables de entorno:

### En application.properties:

```properties
# Email Configuration (usando variables de entorno)
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=${MAIL_SMTP_AUTH:true}
spring.mail.properties.mail.smtp.starttls.enable=${MAIL_SMTP_STARTTLS:true}
```

### Configurar variables de entorno:

**Windows (PowerShell):**
```powershell
$env:MAIL_HOST="smtp.gmail.com"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="tu-email@gmail.com"
$env:MAIL_PASSWORD="tu-app-password"
```

**Windows (CMD):**
```cmd
set MAIL_HOST=smtp.gmail.com
set MAIL_PORT=587
set MAIL_USERNAME=tu-email@gmail.com
set MAIL_PASSWORD=tu-app-password
```

**Linux/Mac:**
```bash
export MAIL_HOST="smtp.gmail.com"
export MAIL_PORT="587"
export MAIL_USERNAME="tu-email@gmail.com"
export MAIL_PASSWORD="tu-app-password"
```

---

## 🧪 Verificar Configuración

### Test Rápido

1. **Inicia el servidor Spring Boot**
2. **Revisa los logs al iniciar:**
   ```
   ✅ [EmailService] Email configurado correctamente
   ```
   O si no está configurado:
   ```
   ⚠️ [EmailService] JavaMailSender no configurado. Modo desarrollo.
   ```

3. **Solicita recuperación de contraseña:**
   - Ve a: http://localhost:4200/login
   - Haz clic en "¿Olvidaste tu contraseña?"
   - Ingresa un email válido
   - Revisa los logs del servidor

4. **Si está configurado correctamente:**
   - Verás: `✅ [EmailService] Email de recuperación enviado exitosamente`
   - Revisa tu bandeja de entrada

5. **Si NO está configurado:**
   - Verás: `🔑 [EmailService] Token de recuperación para...`
   - El token aparecerá en los logs (modo desarrollo)

---

## 🐛 Troubleshooting

### Error: "Authentication failed"

**Causa:** Credenciales incorrectas o App Password no generada (Gmail)

**Solución:**
- Verifica que estés usando App Password (no tu contraseña normal) en Gmail
- Verifica que el email y contraseña sean correctos
- Asegúrate de que la verificación en 2 pasos esté activada (Gmail)

### Error: "Connection refused"

**Causa:** Puerto bloqueado o host incorrecto

**Solución:**
- Verifica que el puerto 587 no esté bloqueado por firewall
- Verifica el host SMTP (smtp.gmail.com, smtp.office365.com, etc.)
- Prueba con puerto 465 (SSL) si 587 no funciona

### No recibo emails

**Causa:** Email en spam, configuración incorrecta, o email no existe

**Solución:**
- Revisa carpeta de spam
- Verifica que el email del usuario exista en la base de datos
- Revisa los logs del servidor para errores
- Verifica que el email de destino sea válido

### Error: "Could not connect to SMTP host"

**Causa:** Problema de red o configuración incorrecta

**Solución:**
- Verifica tu conexión a internet
- Verifica que el host SMTP sea correcto
- Prueba con otro servidor SMTP (Gmail, Outlook)

---

## 📝 Ejemplo Completo (Gmail)

### application.properties:

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=mi-email@gmail.com
spring.mail.password=abcd efgh ijkl mnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# URL de la aplicación (para links en emails)
app.url=http://localhost:4200
```

**Nota:** El App Password de Gmail tiene espacios, pero puedes ponerlo sin espacios o con espacios, ambos funcionan.

---

## ✅ Checklist de Configuración

- [ ] Verificación en 2 pasos activada (Gmail)
- [ ] App Password generada (Gmail)
- [ ] Credenciales agregadas en `application.properties`
- [ ] Servidor reiniciado
- [ ] Prueba de envío realizada
- [ ] Email recibido (o token en logs si no está configurado)

---

## 🚀 Siguiente Paso

Una vez configurado, prueba el flujo completo:

1. Ve a http://localhost:4200/login
2. Haz clic en "¿Olvidaste tu contraseña?"
3. Ingresa un email válido de tu base de datos
4. Revisa tu email (o logs del servidor)
5. Haz clic en el link recibido
6. Ingresa nueva contraseña
7. Inicia sesión con la nueva contraseña

---

**¿Necesitas ayuda con algún paso específico?** 🆘








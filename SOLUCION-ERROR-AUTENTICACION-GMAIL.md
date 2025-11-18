# 🔧 Solución: Error de Autenticación Gmail

## ❌ Error Actual

```
535-5.7.8 Username and Password not accepted
Authentication failed
```

**Causa:** Estás usando tu contraseña normal de Gmail en lugar de un **App Password**.

---

## ✅ Solución: Generar App Password de Gmail

### Paso 1: Activar Verificación en 2 Pasos

1. Ve a: https://myaccount.google.com/security
2. Busca **"Verificación en 2 pasos"**
3. Si no está activada, actívala (es obligatorio para App Passwords)

### Paso 2: Generar App Password

1. Ve a: https://myaccount.google.com/apppasswords
   - O desde Seguridad → "Contraseñas de aplicaciones"
2. Si no ves la opción, primero activa verificación en 2 pasos
3. Selecciona:
   - **Aplicación**: "Correo"
   - **Dispositivo**: "Otro (nombre personalizado)"
   - Escribe: "Spring Boot App"
4. Haz clic en **"Generar"**
5. **Copia la contraseña de 16 caracteres** (aparece solo una vez)
   - Formato: `abcd efgh ijkl mnop` (con espacios) o `abcdefghijklmnop` (sin espacios)

### Paso 3: Actualizar application.properties

Edita: `spring-api-entra/src/main/resources/application.properties`

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
- `TU-EMAIL@gmail.com` → Tu email completo de Gmail
- `TU-APP-PASSWORD-AQUI` → La contraseña de 16 caracteres que copiaste

**Ejemplo:**
```properties
spring.mail.username=mi-email@gmail.com
spring.mail.password=abcd efgh ijkl mnop
```

**Nota:** Puedes poner el App Password con o sin espacios, ambos funcionan.

### Paso 4: Reiniciar Servidor

1. Detén el servidor Spring Boot
2. Inícialo nuevamente
3. Prueba solicitar recuperación de contraseña

---

## 🔍 Verificar Configuración

### Verificar que está configurado:

Revisa los logs al iniciar el servidor. Deberías ver:
```
✅ [EmailService] Email configurado correctamente
```

### Probar envío:

1. Solicita recuperación de contraseña
2. Revisa los logs:
   - ✅ Si funciona: `✅ [EmailService] Email de recuperación enviado exitosamente`
   - ❌ Si falla: Verás el error específico

---

## 🐛 Troubleshooting

### Error: "Username and Password not accepted"

**Causas posibles:**
1. ❌ Estás usando tu contraseña normal (no App Password)
2. ❌ El App Password está mal copiado
3. ❌ Verificación en 2 pasos no está activada

**Solución:**
- Genera un nuevo App Password
- Asegúrate de copiar los 16 caracteres completos
- Verifica que la verificación en 2 pasos esté activada

### Error: "Could not connect to SMTP host"

**Causa:** Problema de red o firewall

**Solución:**
- Verifica tu conexión a internet
- Verifica que el puerto 587 no esté bloqueado

### No recibo emails

**Causas:**
1. Email en spam
2. Email incorrecto en la base de datos
3. Configuración incorrecta

**Solución:**
- Revisa carpeta de spam
- Verifica que el email del usuario exista en la BD
- Revisa los logs del servidor

---

## 📝 Ejemplo Completo

### application.properties:

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=mi-email@gmail.com
spring.mail.password=abcd efgh ijkl mnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# URL de la aplicación
app.url=http://localhost:4200
```

---

## ✅ Checklist

- [ ] Verificación en 2 pasos activada en Gmail
- [ ] App Password generado (16 caracteres)
- [ ] Credenciales actualizadas en `application.properties`
- [ ] Servidor reiniciado
- [ ] Prueba de envío realizada
- [ ] Email recibido (o token en logs si falla)

---

## 🎯 Resultado Esperado

Después de configurar correctamente:

1. **Logs al iniciar:**
   ```
   ✅ [EmailService] Email configurado correctamente
   ```

2. **Al solicitar recuperación:**
   ```
   ✅ [EmailService] Email de recuperación enviado exitosamente a: usuario@ejemplo.com
   ```

3. **Usuario recibe email** con link de recuperación

---

## 💡 Nota Importante

**El sistema ahora tiene fallback mejorado:**
- Si el email falla, el token se loguea en consola
- Puedes usar el token desde los logs para desarrollo
- En producción, deberías corregir la configuración SMTP

---

**¿Necesitas ayuda con algún paso específico?** 🆘


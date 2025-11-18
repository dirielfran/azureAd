/**
 * =============================================================================
 * 📋 APP MODULE - CONFIGURACIÓN PRINCIPAL DE ANGULAR CON MICROSOFT ENTRA ID
 * =============================================================================
 * 
 * Este módulo configura la integración completa entre Angular y Microsoft Entra ID
 * usando la biblioteca MSAL (Microsoft Authentication Library).
 * 
 * FUNCIONALIDADES PRINCIPALES:
 * ✅ Autenticación automática con Microsoft Entra ID
 * ✅ Interceptor que agrega tokens JWT automáticamente a las peticiones HTTP
 * ✅ Guards para proteger rutas
 * ✅ Configuración de scopes y recursos protegidos
 * ✅ Manejo de redirecciones después del login
 * 
 * CREDENCIALES CONFIGURADAS:
 * - Client ID: 4a12fbd8-bf63-4c12-be4c-9678b207fbe7
 * - Tenant ID: f128ae87-3797-42d7-8490-82c6b570f832
 * - API Scope: api://4a12fbd8-bf63-4c12-be4c-9678b207fbe7/access_as_user
 * 
 * @author Sistema de Autenticación Angular-Entra
 * @version 1.0.0
 * =============================================================================
 */

import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ProtectedDataComponent } from './components/protected-data.component';
import { AccessDeniedComponent } from './components/access-denied.component';
import { UserPermissionsComponent } from './components/user-permissions.component';
import { AuthSelectorComponent } from './components/auth-selector.component';
import { LocalLoginComponent } from './components/local-login.component';
import { ForgotPasswordComponent } from './components/forgot-password.component';
import { ResetPasswordComponent } from './components/reset-password.component';

// Importar servicios de autorización
import { AuthorizationService } from './services/authorization.service';
import { AuthConfigService } from './services/auth-config.service';
import { LocalAuthService } from './services/local-auth.service';

// Importar guards
import { PermissionGuard, AdminGuard, ManagerGuard } from './guards/permission.guard';
import { AuthGuard } from './guards/auth.guard';

// Importar directivas
import { 
  HasPermissionDirective, 
  IsAdminDirective, 
  IsManagerDirective 
} from './directives/has-permission.directive';

// Componente de estado de autenticación
import { AuthStatusComponent } from './components/auth-status.component';

// Importar interceptor JWT
import { JwtAuthInterceptor } from './interceptors/jwt-auth.interceptor';

// Importaciones de MSAL para Microsoft Entra ID
import { 
  MsalModule, 
  MsalRedirectComponent, 
  MsalGuard, 
  MsalInterceptor,
  MsalService, 
  MSAL_INSTANCE, 
  MSAL_GUARD_CONFIG, 
  MSAL_INTERCEPTOR_CONFIG, 
  MsalGuardConfiguration, 
  MsalInterceptorConfiguration 
} from '@azure/msal-angular';

import { 
  IPublicClientApplication, 
  PublicClientApplication, 
  InteractionType 
} from '@azure/msal-browser';

import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { environment } from '../environments/environment';

/**
 * =============================================================================
 * 🏭 FACTORY FUNCTIONS - CONFIGURACIÓN DE MSAL
 * =============================================================================
 */

/**
 * 🏗️ MSAL INSTANCE FACTORY
 * 
 * Crea la instancia principal de MSAL usando la configuración del environment.
 * Esta instancia maneja toda la autenticación con Microsoft Entra ID.
 * 
 * CONFIGURACIÓN INCLUIDA:
 * - Client ID de tu aplicación registrada en Azure AD
 * - Authority (URL del tenant de Azure AD)
 * - Redirect URI (donde regresar después del login)
 * - Cache configuration (localStorage para persistir tokens)
 * 
 * IMPORTANTE: Esta instancia se crea al iniciar la app. Para evitar
 * redirecciones no deseadas, se debe limpiar el localStorage de MSAL
 * cuando se cambia de Azure AD a JWT Local.
 * 
 * @returns {IPublicClientApplication} Instancia de MSAL configurada
 */
export function MSALInstanceFactory(): IPublicClientApplication {
  // Verificar si hay configuración guardada de autenticación
  const authConfig = localStorage.getItem('auth_config');
  if (authConfig) {
    try {
      const config = JSON.parse(authConfig);
      // Si JWT Local está activo y Azure deshabilitado, limpiar MSAL del localStorage
      if (config.jwtLocalHabilitado && !config.azureAdHabilitado) {
        console.log('🧹 [MSALInstanceFactory] JWT Local activo, limpiando cache de MSAL...');
        cleanMSALCache();
      }
    } catch (error) {
      console.warn('⚠️ [MSALInstanceFactory] Error al leer configuración:', error);
    }
  }
  
  // Limpiar el flag de "interaction_in_progress" si existe
  // Esto previene errores después de hot reloads de Angular
  try {
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && key.includes('interaction.status')) {
        console.warn('⚠️ [MSALInstanceFactory] Limpiando flag de interacción en progreso:', key);
        localStorage.removeItem(key);
      }
    }
  } catch (error) {
    console.warn('⚠️ [MSALInstanceFactory] Error al limpiar flags de interacción');
  }
  
  return new PublicClientApplication(environment.msalConfig);
}

/**
 * Limpia el cache de MSAL del localStorage y sessionStorage
 */
function cleanMSALCache(): void {
  // Limpiar localStorage
  const msalKeys: string[] = [];
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i);
    if (key && (
      key.includes('msal') || 
      key.includes('login.windows.net') ||
      key.includes('login.microsoftonline.com')
    )) {
      msalKeys.push(key);
    }
  }
  msalKeys.forEach(key => {
    localStorage.removeItem(key);
    console.log('  🗑️ Removido:', key);
  });
  
  // Limpiar sessionStorage
  const sessionKeys: string[] = [];
  for (let i = 0; i < sessionStorage.length; i++) {
    const key = sessionStorage.key(i);
    if (key && (
      key.includes('msal') || 
      key.includes('login.windows.net') ||
      key.includes('login.microsoftonline.com')
    )) {
      sessionKeys.push(key);
    }
  }
  sessionKeys.forEach(key => sessionStorage.removeItem(key));
  
  console.log(`✅ [MSALInstanceFactory] ${msalKeys.length + sessionKeys.length} elementos de MSAL limpiados`);
}


/**
 * 🛡️ MSAL GUARD CONFIGURATION FACTORY
 * 
 * Configura el guard de MSAL que protege rutas y define qué scopes solicitar
 * cuando un usuario necesita autenticarse.
 * 
 * SCOPES CONFIGURADOS:
 * - 'user.read': Permiso básico para leer el perfil del usuario desde Microsoft Graph
 * - 'api://4a12fbd8-bf63-4c12-be4c-9678b207fbe7/access_as_user': 
 *   Permiso personalizado para acceder a tu API Spring Boot
 * 
 * INTERACTION TYPE:
 * - Redirect: El usuario será redirigido a Microsoft para autenticarse
 *   (alternativa sería Popup, pero Redirect es más confiable)
 * 
 * @returns {MsalGuardConfiguration} Configuración del guard de MSAL
 */
export function MSALGuardConfigFactory(): MsalGuardConfiguration {
  return {
    interactionType: InteractionType.Redirect,
    authRequest: {
      scopes: [
        'user.read', // Microsoft Graph - Perfil básico del usuario
        'api://4a12fbd8-bf63-4c12-be4c-9678b207fbe7/access_as_user' // Tu API Spring Boot
      ]
    }
  };
}

/**
 * 🔄 MSAL INTERCEPTOR CONFIGURATION FACTORY
 * 
 * ¡ESTA ES LA MAGIA! 🪄
 * 
 * Configura el interceptor de MSAL que AUTOMÁTICAMENTE agrega el header
 * "Authorization: Bearer <token>" a todas las peticiones HTTP que coincidan
 * con las URLs configuradas en el protectedResourceMap.
 * 
 * CÓMO FUNCIONA:
 * 1. Cuando haces una petición HTTP a una URL protegida
 * 2. El interceptor intercepta la petición
 * 3. Obtiene el token de acceso apropiado según los scopes
 * 4. Agrega el header Authorization automáticamente
 * 5. Envía la petición con el token incluido
 * 
 * RECURSOS PROTEGIDOS CONFIGURADOS:
 * - Microsoft Graph API: https://graph.microsoft.com/v1.0/me → ['user.read']
 * - Tu API Spring Boot: http://localhost:8080/api → ['api://...../access_as_user']
 * 
 * VENTAJAS:
 * ✅ No necesitas manejar tokens manualmente
 * ✅ Renovación automática de tokens
 * ✅ Manejo de errores integrado
 * ✅ Fácil agregar nuevas APIs
 * 
 * @returns {MsalInterceptorConfiguration} Configuración del interceptor
 */
export function MSALInterceptorConfigFactory(): MsalInterceptorConfiguration {
  // Mapa que define qué URLs necesitan qué scopes
  const protectedResourceMap = new Map<string, Array<string>>();
  
  // Verificar si JWT Local está activo
  const authConfig = localStorage.getItem('auth_config');
  let isJwtLocalActive = false;
  
  if (authConfig) {
    try {
      const config = JSON.parse(authConfig);
      isJwtLocalActive = config.jwtLocalHabilitado && !config.azureAdHabilitado;
    } catch (e) {
      console.warn('Error al leer configuración de autenticación');
    }
  }
  
  // Solo configurar rutas protegidas si Azure AD está activo
  if (!isJwtLocalActive) {
    console.log('🔧 [MSALInterceptor] Configurando rutas protegidas para Azure AD');
    
    // 📊 Microsoft Graph API - Para obtener perfil de usuario
    protectedResourceMap.set('https://graph.microsoft.com/v1.0/me', ['user.read']);
    
    // 🚀 Tu API Spring Boot protegida - CONFIGURACIÓN PRINCIPAL
    protectedResourceMap.set('http://localhost:8080/api/data', ['api://4a12fbd8-bf63-4c12-be4c-9678b207fbe7/access_as_user']);
    protectedResourceMap.set('http://localhost:8080/api/autorizacion', ['api://4a12fbd8-bf63-4c12-be4c-9678b207fbe7/access_as_user']);
  } else {
    console.log('ℹ️ [MSALInterceptor] JWT Local activo, MsalInterceptor desactivado (mapa vacío)');
  }

  return {
    interactionType: InteractionType.Redirect,
    protectedResourceMap
  };
}

/**
 * =============================================================================
 * 🎯 ANGULAR MODULE - CONFIGURACIÓN PRINCIPAL DE LA APLICACIÓN
 * =============================================================================
 */

@NgModule({
  /**
   * 📦 DECLARACIONES - Componentes que pertenecen a este módulo
   */
  declarations: [
    AppComponent,              // Componente principal con navegación y login/logout
    ProtectedDataComponent,    // Componente de demostración para probar APIs protegidas
    AccessDeniedComponent,     // Componente para mostrar errores de acceso denegado
    UserPermissionsComponent,  // Componente para mostrar permisos del usuario
    AuthStatusComponent,       // Componente para mostrar estado de autenticación
    AuthSelectorComponent,     // Componente selector de método de autenticación
    LocalLoginComponent,       // Componente de login local con JWT
    ForgotPasswordComponent,   // Componente para solicitar recuperación de contraseña
    ResetPasswordComponent,    // Componente para resetear contraseña
    // Directivas personalizadas
    HasPermissionDirective,    // Directiva para renderizado condicional por permisos
    IsAdminDirective,         // Directiva para contenido solo de administradores
    IsManagerDirective        // Directiva para contenido de gestores
  ],
  
  /**
   * 📥 IMPORTS - Módulos que esta aplicación necesita
   */
  imports: [
    BrowserModule,       // Módulo base para aplicaciones que corren en el navegador
    CommonModule,        // Directivas comunes de Angular (ngIf, ngFor, etc.)
    FormsModule,         // Módulo para formularios (ngModel)
    AppRoutingModule,    // Configuración de rutas de la aplicación
    HttpClientModule,    // Cliente HTTP para hacer peticiones a APIs
    MsalModule          // Módulo de MSAL para autenticación con Microsoft
  ],
  
  /**
   * 🔧 PROVIDERS - Servicios y configuraciones inyectables
   */
  providers: [
    /**
     * 🔄 HTTP INTERCEPTOR - JwtAuthInterceptor (PRIMERO)
     * 
     * Interceptor para autenticación JWT local:
     * - Agrega tokens JWT a las peticiones cuando la autenticación local está activa
     * - Maneja errores 401/403 y redirige al login
     * - Se ejecuta ANTES que MsalInterceptor para tener prioridad
     */
    {
      provide: HTTP_INTERCEPTORS,
      useClass: JwtAuthInterceptor,
      multi: true
    },
    
    /**
     * 🔄 HTTP INTERCEPTOR - MsalInterceptor (SEGUNDO)
     * 
     * Este interceptor maneja tokens de Azure AD:
     * - Solo se activa para endpoints específicos configurados
     * - Agrega automáticamente tokens de Azure AD
     * - Usa la configuración de MSALInterceptorConfigFactory
     * - multi: true permite múltiples interceptors
     */
    {
      provide: HTTP_INTERCEPTORS,
      useClass: MsalInterceptor,
      multi: true
    },
    
    /**
     * 🏗️ MSAL INSTANCE - Instancia principal de autenticación
     * 
     * Proporciona la instancia configurada de PublicClientApplication
     * que maneja toda la comunicación con Microsoft Entra ID
     */
    {
      provide: MSAL_INSTANCE,
      useFactory: MSALInstanceFactory
    },
    
    /**
     * 🛡️ MSAL GUARD CONFIG - Configuración del guard de rutas
     * 
     * Define cómo comportarse cuando una ruta protegida requiere autenticación:
     * - Qué scopes solicitar
     * - Tipo de interacción (redirect/popup)
     */
    {
      provide: MSAL_GUARD_CONFIG,
      useFactory: MSALGuardConfigFactory
    },
    
    /**
     * 🔄 MSAL INTERCEPTOR CONFIG - Configuración del interceptor HTTP
     * 
     * Define qué URLs están protegidas y qué scopes necesitan:
     * - Mapa de URLs → Scopes
     * - Configuración de comportamiento del interceptor
     */
    {
      provide: MSAL_INTERCEPTOR_CONFIG,
      useFactory: MSALInterceptorConfigFactory
    },
    
    // 🔧 Servicios de MSAL
    MsalService,    // Servicio principal para operaciones de autenticación
    MsalGuard,      // Guard para proteger rutas que requieren autenticación
    
    // 🛡️ Servicios y Guards de Autorización
    AuthorizationService,  // Servicio para gestión de permisos
    AuthConfigService,     // Servicio para obtener configuración de autenticación
    LocalAuthService,      // Servicio para autenticación local con JWT
    PermissionGuard,      // Guard para verificar permisos específicos
    AdminGuard,           // Guard para acceso de administradores
    ManagerGuard,         // Guard para acceso de gestores
    AuthGuard             // Guard combinado para ambos tipos de autenticación
  ],
  
  /**
   * 🚀 BOOTSTRAP - Componentes que se cargan al iniciar la aplicación
   * 
   * - AppComponent: Componente principal de la aplicación
   * - MsalRedirectComponent: Solo debe activarse cuando Azure AD está habilitado
   *   (se removió del bootstrap para evitar redirecciones automáticas)
   */
  bootstrap: [AppComponent]
})
export class AppModule { 
  
  /**
   * =============================================================================
   * 📝 NOTAS IMPORTANTES PARA DESARROLLO
   * =============================================================================
   * 
   * 🔑 FLUJO DE AUTENTICACIÓN:
   * 1. Usuario hace clic en "Iniciar sesión"
   * 2. Se ejecuta loginRedirect() → Redirección a Microsoft
   * 3. Usuario se autentica en Microsoft
   * 4. Microsoft redirige de vuelta con código de autorización
   * 5. MsalRedirectComponent procesa la respuesta
   * 6. Tokens se guardan automáticamente en localStorage
   * 7. MsalInterceptor agrega tokens a peticiones HTTP automáticamente
   * 
   * 🔧 PERSONALIZACIÓN:
   * - Para agregar nuevas APIs: Modifica MSALInterceptorConfigFactory
   * - Para cambiar scopes: Modifica MSALGuardConfigFactory
   * - Para cambiar configuración: Modifica environment.ts
   * 
   * 🐛 DEBUGGING:
   * - Tokens se muestran en console.log automáticamente
   * - Usa el botón "Ver Token de Acceso" en ProtectedDataComponent
   * - Revisa Network tab para ver headers Authorization
   * 
   * 📚 DOCUMENTACIÓN:
   * - MSAL Angular: https://github.com/AzureAD/microsoft-authentication-library-for-js
   * - Azure AD: https://docs.microsoft.com/en-us/azure/active-directory/develop/
   * 
   * =============================================================================
   */
}
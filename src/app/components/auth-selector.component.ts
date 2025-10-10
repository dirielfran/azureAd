import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MsalService } from '@azure/msal-angular';
import { AuthConfigService, AuthStatus } from '../services/auth-config.service';

/**
 * Componente Selector de Método de Autenticación
 * Determina qué método de autenticación usar basado en la configuración del backend
 */
@Component({
  selector: 'app-auth-selector',
  templateUrl: './auth-selector.component.html',
  styleUrls: ['./auth-selector.component.scss']
})
export class AuthSelectorComponent implements OnInit {
  
  authStatus: AuthStatus | null = null;
  isLoading = true;
  errorMessage = '';

  constructor(
    private authConfigService: AuthConfigService,
    private msalService: MsalService,
    private router: Router
  ) {}

  ngOnInit(): void {
    console.log('🔍 [AuthSelector] Verificando métodos de autenticación disponibles...');
    this.loadAuthConfig();
  }

  /**
   * Carga la configuración de autenticación desde el backend
   */
  async loadAuthConfig(): Promise<void> {
    try {
      console.log('📡 [AuthSelector] Iniciando carga de configuración...');
      console.log('📍 [AuthSelector] URL:', `${this.getApiUrl()}/config/auth/status`);
      
      const status = await this.authConfigService.getAuthStatus();
      
      this.authStatus = status;
      this.isLoading = false;
      console.log('✅ [AuthSelector] Configuración cargada:', status);
      
      // Redirigir automáticamente según el método activo
      this.autoRedirect(status);
    } catch (error) {
      console.error('❌ [AuthSelector] Error al cargar configuración:', error);
      console.error('❌ [AuthSelector] Detalles del error:', JSON.stringify(error, null, 2));
      this.isLoading = false;
      this.errorMessage = `No se pudo conectar con el servidor. Error: ${error}`;
    }
  }

  private getApiUrl(): string {
    return 'http://localhost:8080/api';
  }

  /**
   * Redirige automáticamente según el método de autenticación activo
   * Solo un método puede estar activo a la vez
   */
  private autoRedirect(status: AuthStatus): void {
    const azureEnabled = status.azureAdHabilitado;
    const localEnabled = status.jwtLocalHabilitado;
    
    // Solo redirigir automáticamente si JWT Local está habilitado
    // Para Azure AD, el usuario debe hacer clic manualmente para evitar errores de MSAL
    if (localEnabled && !azureEnabled) {
      // Para JWT Local: Redirigir al formulario de login
      console.log('🔀 [AuthSelector] JWT Local activo (solo), redirigiendo al login...');
      setTimeout(() => {
        this.router.navigate(['/login']);
      }, 500);
    } else if (azureEnabled && !localEnabled) {
      // Para Azure AD: Mostrar mensaje y esperar a que el usuario haga clic
      console.log('🔀 [AuthSelector] Azure AD activo (solo) - Esperando acción del usuario');
      // NO redirigir automáticamente para evitar el error de MSAL
      // El usuario debe hacer clic en el botón de Azure
    } else if (azureEnabled && localEnabled) {
      // Ambos habilitados - mostrar advertencia
      console.warn('⚠️ [AuthSelector] Ambos métodos están habilitados - Se debe configurar solo uno');
      this.errorMessage = '⚠️ Configuración incorrecta: Ambos métodos de autenticación están habilitados. Solo uno debe estar activo.';
    } else {
      this.errorMessage = '⚠️ Ningún método de autenticación está activo. Contacte al administrador.';
    }
  }

  /**
   * Inicia login con Azure AD
   */
  loginWithAzure(): void {
    console.log('🔐 [AuthSelector] Iniciando login con Azure AD...');
    
    try {
      // Limpiar cualquier estado de interacción previo
      const interactionStatus = sessionStorage.getItem('msal.interaction.status');
      if (interactionStatus) {
        console.log('🧹 [AuthSelector] Limpiando estado de interacción previo');
        sessionStorage.removeItem('msal.interaction.status');
        localStorage.removeItem('msal.interaction.status');
      }
      
      // Iniciar login con Azure AD
      this.msalService.loginRedirect({
        scopes: ['user.read', 'api://4a12fbd8-bf63-4c12-be4c-9678b207fbe7/access_as_user']
      });
    } catch (error) {
      console.error('❌ [AuthSelector] Error al iniciar login Azure:', error);
      this.errorMessage = 'Error al iniciar sesión con Azure AD. Recarga la página e intenta de nuevo.';
    }
  }

  /**
   * Navega al formulario de login local
   */
  loginWithLocal(): void {
    console.log('🔐 [AuthSelector] Navegando a login local...');
    this.router.navigate(['/login']);
  }
}


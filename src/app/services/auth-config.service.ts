import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { environment } from '../../environments/environment';

/**
 * Interfaz para el estado de autenticación del sistema
 */
export interface AuthStatus {
  azureAdHabilitado: boolean;
  jwtLocalHabilitado: boolean;
  timestamp: number;
}

/**
 * Servicio de Configuración de Autenticación
 * Consulta al backend qué métodos de autenticación están activos
 */
@Injectable({
  providedIn: 'root'
})
export class AuthConfigService {
  
  private readonly API_URL = environment.apiUrl || 'http://localhost:8080/api';
  private readonly CONFIG_KEY = 'auth_config';
  
  // Observable para el estado de configuración
  private authStatusSubject = new BehaviorSubject<AuthStatus | null>(this.getStoredConfig());
  public authStatus$ = this.authStatusSubject.asObservable();

  constructor(private http: HttpClient) {
    console.log('🔧 [AuthConfig] Servicio inicializado');
  }

  /**
   * Obtiene el estado de los métodos de autenticación desde el backend
   * Usa fetch para evitar interceptores que requieren autenticación
   */
  getAuthStatus(): Promise<AuthStatus> {
    const url = `${this.API_URL}/config/auth/status`;
    console.log('📡 [AuthConfig] Consultando estado de autenticación al backend...');
    console.log('📍 [AuthConfig] URL completa:', url);
    
    return fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
      mode: 'cors',
      credentials: 'omit' // No enviar cookies ni credenciales
    })
    .then(response => {
      console.log('📥 [AuthConfig] Respuesta recibida:', response.status, response.statusText);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    })
    .then((status: AuthStatus) => {
      console.log('✅ [AuthConfig] Estado recibido:', status);
      this.saveConfig(status);
      this.authStatusSubject.next(status);
      return status;
    })
    .catch(error => {
      console.error('❌ [AuthConfig] Error al obtener estado:', error);
      console.error('❌ [AuthConfig] Tipo de error:', error.constructor.name);
      console.error('❌ [AuthConfig] Mensaje:', error.message);
      throw error;
    });
  }

  /**
   * Verifica si Azure AD está habilitado
   */
  isAzureAdEnabled(): boolean {
    const config = this.authStatusSubject.value;
    return config?.azureAdHabilitado || false;
  }

  /**
   * Verifica si JWT Local está habilitado
   */
  isJwtLocalEnabled(): boolean {
    const config = this.authStatusSubject.value;
    return config?.jwtLocalHabilitado || false;
  }

  /**
   * Obtiene el método de autenticación activo
   * Solo uno puede estar activo a la vez
   * Retorna: 'azure' | 'local' | 'none'
   */
  getActiveAuthMethod(): 'azure' | 'local' | 'none' {
    const azureEnabled = this.isAzureAdEnabled();
    const localEnabled = this.isJwtLocalEnabled();
    
    // Prioridad: Si ambos están habilitados (no debería pasar), preferir Azure
    if (azureEnabled) {
      return 'azure';
    } else if (localEnabled) {
      return 'local';
    } else {
      return 'none';
    }
  }

  /**
   * Guarda la configuración en localStorage
   */
  private saveConfig(status: AuthStatus): void {
    localStorage.setItem(this.CONFIG_KEY, JSON.stringify(status));
  }

  /**
   * Obtiene la configuración almacenada
   */
  private getStoredConfig(): AuthStatus | null {
    const stored = localStorage.getItem(this.CONFIG_KEY);
    if (stored) {
      return JSON.parse(stored);
    }
    return null;
  }

  /**
   * Limpia la configuración almacenada
   */
  clearConfig(): void {
    localStorage.removeItem(this.CONFIG_KEY);
    this.authStatusSubject.next(null);
  }
}


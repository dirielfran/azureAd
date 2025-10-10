import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MsalService } from '@azure/msal-angular';
import { API_CONFIG, GRAPH_CONFIG } from '../config/api.config';
import { AuthConfigService } from './auth-config.service';
import { LocalAuthService } from './local-auth.service';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiBaseUrl = API_CONFIG.baseUrl;

  constructor(
    private http: HttpClient,
    private msalService: MsalService,
    private authConfigService: AuthConfigService,
    private localAuthService: LocalAuthService
  ) {}

  /**
   * Obtiene datos de un endpoint protegido
   * @param endpoint - El endpoint de la API (ej: '/users', '/data')
   * @returns Observable con la respuesta de la API
   */
  getData(endpoint: string): Observable<any> {
    const url = `${this.apiBaseUrl}${endpoint}`;
    
    return this.http.get(url).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Envía datos a un endpoint protegido via POST
   * @param endpoint - El endpoint de la API
   * @param data - Los datos a enviar
   * @returns Observable con la respuesta de la API
   */
  postData(endpoint: string, data: any): Observable<any> {
    const url = `${this.apiBaseUrl}${endpoint}`;
    
    return this.http.post(url, data).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Actualiza datos en un endpoint protegido via PUT
   * @param endpoint - El endpoint de la API
   * @param data - Los datos a actualizar
   * @returns Observable con la respuesta de la API
   */
  updateData(endpoint: string, data: any): Observable<any> {
    const url = `${this.apiBaseUrl}${endpoint}`;
    
    return this.http.put(url, data).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Elimina datos de un endpoint protegido via DELETE
   * @param endpoint - El endpoint de la API
   * @returns Observable con la respuesta de la API
   */
  deleteData(endpoint: string): Observable<any> {
    const url = `${this.apiBaseUrl}${endpoint}`;
    
    return this.http.delete(url).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Obtiene información del usuario desde Microsoft Graph API
   * @returns Observable con la información del usuario
   */
  getUserProfile(): Observable<any> {
    const url = `${GRAPH_CONFIG.baseUrl}${GRAPH_CONFIG.endpoints.me}`;
    return this.http.get(url).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Maneja errores de las llamadas HTTP
   * @param error - El error recibido
   * @returns Observable que emite el error
   */
  private handleError(error: any): Observable<never> {
    console.error('Error en la API:', error);
    
    let errorMessage = 'Ocurrió un error desconocido';
    
    if (error.error instanceof ErrorEvent) {
      // Error del lado del cliente
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Error del lado del servidor
      switch (error.status) {
        case 401:
          errorMessage = 'No autorizado. Por favor, inicia sesión nuevamente.';
          break;
        case 403:
          errorMessage = 'Acceso denegado. No tienes permisos para esta acción.';
          break;
        case 404:
          errorMessage = 'Recurso no encontrado.';
          break;
        case 500:
          errorMessage = 'Error interno del servidor.';
          break;
        default:
          errorMessage = `Error ${error.status}: ${error.message}`;
      }
    }
    
    return throwError(() => new Error(errorMessage));
  }

  /**
   * Verifica si el usuario está autenticado
   * @returns true si está autenticado, false en caso contrario
   */
  isAuthenticated(): boolean {
    // Verificar autenticación según el método activo
    const authMethod = this.authConfigService.getActiveAuthMethod();
    
    if (authMethod === 'azure') {
      const accounts = this.msalService.instance.getAllAccounts();
      const isAuth = accounts.length > 0;
      console.log('🔍 [ApiService] Verificando autenticación Azure AD:');
      console.log('  - Cuentas encontradas:', accounts.length);
      console.log('  - ¿Autenticado?', isAuth);
      if (isAuth) {
        console.log('  - Primera cuenta:', accounts[0].name || accounts[0].username);
      }
      return isAuth;
    } else if (authMethod === 'local') {
      const isAuth = this.localAuthService.isAuthenticated();
      console.log('🔍 [ApiService] Verificando autenticación JWT Local:');
      console.log('  - ¿Autenticado?', isAuth);
      return isAuth;
    }
    
    console.log('🔍 [ApiService] No hay método de autenticación configurado');
    return false;
  }

  /**
   * Obtiene el token de acceso actual (para debugging)
   * @returns Promise con el token de acceso
   */
  async getAccessToken(): Promise<string | null> {
    console.log('🔑 [ApiService] Solicitando token de acceso...');
    
    // Verificar método de autenticación activo
    const authMethod = this.authConfigService.getActiveAuthMethod();
    console.log('🔐 [ApiService] Método de autenticación:', authMethod);
    
    if (authMethod === 'local') {
      // Obtener token JWT local
      const token = this.localAuthService.getToken();
      if (token) {
        console.log('✅ [ApiService] Token JWT local obtenido');
        console.log('🔑 [ApiService] Token (primeros 50 chars):', token.substring(0, 50) + '...');
        return token;
      } else {
        console.error('❌ [ApiService] No hay token JWT local');
        return null;
      }
    } else if (authMethod === 'azure') {
      // Obtener token de Azure AD
      try {
        const accounts = this.msalService.instance.getAllAccounts();
        if (accounts.length === 0) {
          console.error('❌ [ApiService] No hay cuentas autenticadas en Azure AD');
          return null;
        }

        const tokenRequest = {
          scopes: ['user.read', 'api://4a12fbd8-bf63-4c12-be4c-9678b207fbe7/access_as_user'],
          account: accounts[0]
        };

        console.log('📋 [ApiService] Scopes solicitados:', tokenRequest.scopes);
        console.log('👤 [ApiService] Cuenta utilizada:', accounts[0].name || accounts[0].username);
        
        const response = await this.msalService.instance.acquireTokenSilent(tokenRequest);
        console.log('✅ [ApiService] Token obtenido exitosamente (silencioso)');
        console.log('🔑 [ApiService] Token (primeros 50 chars):', response.accessToken.substring(0, 50) + '...');
        return response.accessToken;
      } catch (error) {
        console.error('❌ [ApiService] Error obteniendo token silenciosamente:', error);
        
        // Si falla el token silencioso, intentar con popup
        try {
          console.log('🔄 [ApiService] Intentando obtener token con popup...');
          const accounts = this.msalService.instance.getAllAccounts();
          const tokenRequest = {
            scopes: ['user.read', 'api://4a12fbd8-bf63-4c12-be4c-9678b207fbe7/access_as_user'],
            account: accounts[0]
          };
          
          const response = await this.msalService.instance.acquireTokenPopup(tokenRequest);
          console.log('✅ [ApiService] Token obtenido con popup exitosamente');
          console.log('🔑 [ApiService] Token (primeros 50 chars):', response.accessToken.substring(0, 50) + '...');
          return response.accessToken;
        } catch (popupError) {
          console.error('❌ [ApiService] Error obteniendo token con popup:', popupError);
          return null;
        }
      }
    }
    
    // Si no hay método de autenticación configurado
    console.error('❌ [ApiService] No hay método de autenticación configurado');
    return null;
  }
}

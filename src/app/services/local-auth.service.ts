import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

/**
 * Interfaz para la respuesta del login JWT
 */
export interface JwtLoginResponse {
  token: string;
  type: string;
  message: string;
}

/**
 * Interfaz para datos del usuario local
 */
export interface LocalUser {
  email: string;
  nombre: string;
  permisos: string[];
  perfil: string;
}

/**
 * Servicio de Autenticación Local (JWT)
 * Maneja login/logout con usuarios de base de datos
 */
@Injectable({
  providedIn: 'root'
})
export class LocalAuthService {
  
  private readonly TOKEN_KEY = 'local_jwt_token';
  private readonly USER_KEY = 'local_user_data';
  private readonly API_URL = environment.apiUrl || 'http://localhost:8080/api';
  
  // Observable para el estado de autenticación
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();
  
  // Observable para los datos del usuario
  private currentUserSubject = new BehaviorSubject<LocalUser | null>(this.getUserData());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    console.log('🔧 LocalAuthService inicializado');
    // Limpiar tokens con nombres de clave antiguos
    this.cleanLegacyTokenKeys();
    // Limpiar tokens antiguos al iniciar
    this.cleanOldTokens();
  }

  /**
   * Limpia tokens guardados con nombres de clave antiguos
   */
  private cleanLegacyTokenKeys(): void {
    // Limpiar claves antiguas que puedan existir
    const legacyKeys = ['jwt_token', 'current_user', 'token', 'auth_token'];
    legacyKeys.forEach(key => {
      if (localStorage.getItem(key)) {
        console.warn(`⚠️ [LocalAuth] Encontrada clave antigua "${key}", limpiando...`);
        localStorage.removeItem(key);
      }
    });
  }

  /**
   * Limpia tokens que sean más viejos que 5 minutos (probablemente de un backend reiniciado)
   */
  private cleanOldTokens(): void {
    const token = this.getToken();
    if (token) {
      try {
        // Remover "Bearer " si existe
        const cleanToken = token.replace('Bearer ', '');
        // Decodificar el payload del JWT
        const payload = JSON.parse(atob(cleanToken.split('.')[1]));
        
        if (payload && payload.iat) {
          const tokenAge = Date.now() - (payload.iat * 1000);
          const maxAge = 5 * 60 * 1000; // 5 minutos
          
          if (tokenAge > maxAge) {
            console.warn('⚠️ [LocalAuth] Token antiguo detectado (más de 5 minutos), limpiando...');
            console.warn(`   Token creado hace: ${Math.floor(tokenAge / 1000 / 60)} minutos`);
            this.logout();
          } else {
            console.log(`✅ [LocalAuth] Token válido (creado hace ${Math.floor(tokenAge / 1000)} segundos)`);
          }
        }
      } catch (error) {
        console.error('❌ [LocalAuth] Error al verificar edad del token, limpiando por seguridad');
        this.logout();
      }
    }
  }

  /**
   * Login con credenciales de usuario local
   * @param email Email del usuario
   * @param password Contraseña del usuario
   */
  login(email: string, password: string): Observable<JwtLoginResponse> {
    console.log('🔐 [LocalAuth] Intentando login para:', email);
    console.log('📍 [LocalAuth] URL:', `${this.API_URL}/auth/login`);
    
    // Crear credenciales en formato Basic Auth
    const credentials = btoa(`${email}:${password}`);
    console.log('🔑 [LocalAuth] Credenciales Base64 generadas');
    
    const headers = new HttpHeaders({
      'Authorization': `Basic ${credentials}`,
      'Content-Type': 'application/json'
    });

    console.log('📡 [LocalAuth] Enviando petición POST...');
    
    return this.http.post<JwtLoginResponse>(
      `${this.API_URL}/auth/login`,
      {},
      { headers }
    ).pipe(
      tap(response => {
        console.log('✅ [LocalAuth] Login exitoso');
        console.log('🎫 [LocalAuth] Respuesta completa:', response);
        console.log('🎫 [LocalAuth] Token en respuesta:', response.token ? 'SÍ' : 'NO');
        
        if (!response.token) {
          console.error('❌ [LocalAuth] No hay token en la respuesta!');
          throw new Error('No se recibió token en la respuesta');
        }
        
        // Guardar token
        this.saveToken(response.token);
        console.log('💾 [LocalAuth] Token guardado en localStorage con clave:', this.TOKEN_KEY);
        
        // Verificar que se guardó
        const savedToken = localStorage.getItem(this.TOKEN_KEY);
        console.log('🔍 [LocalAuth] Token verificado en localStorage:', savedToken ? 'SÍ' : 'NO');
        
        // Decodificar y guardar datos del usuario
        this.decodeAndSaveUser(response.token);
        // Notificar que el usuario está autenticado
        this.isAuthenticatedSubject.next(true);
        console.log('✅ [LocalAuth] Proceso de login completo');
      }),
      catchError(error => {
        console.error('❌ [LocalAuth] Error en login:', error);
        console.error('❌ [LocalAuth] Status:', error.status);
        console.error('❌ [LocalAuth] Message:', error.message);
        console.error('❌ [LocalAuth] Error completo:', JSON.stringify(error, null, 2));
        throw error;
      })
    );
  }

  /**
   * Logout - Elimina token y datos del usuario
   */
  logout(): void {
    console.log('🚪 [LocalAuth] Cerrando sesión');
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.isAuthenticatedSubject.next(false);
    this.currentUserSubject.next(null);
  }

  /**
   * Obtiene el token JWT almacenado
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Verifica si hay un token almacenado
   */
  hasToken(): boolean {
    return !!this.getToken();
  }

  /**
   * Verifica si el usuario está autenticado
   */
  isAuthenticated(): boolean {
    return this.hasToken();
  }

  /**
   * Obtiene los datos del usuario actual
   */
  getCurrentUser(): LocalUser | null {
    return this.getUserData();
  }

  /**
   * Guarda el token en localStorage
   */
  private saveToken(token: string): void {
    // El token viene en formato "Bearer eyJhbGc..."
    localStorage.setItem(this.TOKEN_KEY, token);
    console.log('💾 [LocalAuth] Token guardado');
  }

  /**
   * Decodifica el token JWT y extrae los datos del usuario
   */
  private decodeAndSaveUser(token: string): void {
    try {
      // Remover "Bearer " si existe
      const cleanToken = token.replace('Bearer ', '');
      
      // Decodificar el payload del JWT (parte central del token)
      const payload = JSON.parse(atob(cleanToken.split('.')[1]));
      
      console.log('📋 [LocalAuth] Payload decodificado:', payload);
      
      // Crear objeto de usuario con los datos del token
      const user: LocalUser = {
        email: payload.sub || payload.email,
        nombre: payload.nombre || payload.name || 'Usuario',
        permisos: payload.authorities || payload.permisos || [],
        perfil: payload.perfil || 'Usuario'
      };
      
      // Guardar datos del usuario
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
      this.currentUserSubject.next(user);
      
      console.log('👤 [LocalAuth] Usuario guardado:', user);
      console.log('🔑 [LocalAuth] Permisos detectados:', user.permisos);
    } catch (error) {
      console.error('❌ [LocalAuth] Error decodificando token:', error);
    }
  }

  /**
   * Obtiene los datos del usuario desde localStorage
   */
  private getUserData(): LocalUser | null {
    const userData = localStorage.getItem(this.USER_KEY);
    if (userData) {
      return JSON.parse(userData);
    }
    return null;
  }

  /**
   * Verifica si el usuario tiene un permiso específico
   */
  hasPermission(permission: string): boolean {
    const user = this.getCurrentUser();
    return user?.permisos?.includes(permission) || false;
  }

  /**
   * Verifica si el usuario tiene alguno de los permisos especificados
   */
  hasAnyPermission(permissions: string[]): boolean {
    return permissions.some(permission => this.hasPermission(permission));
  }

  /**
   * Verifica si el usuario tiene todos los permisos especificados
   */
  hasAllPermissions(permissions: string[]): boolean {
    return permissions.every(permission => this.hasPermission(permission));
  }
}


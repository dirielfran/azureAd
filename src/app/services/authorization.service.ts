/**
 * =============================================================================
 * 🔐 AUTHORIZATION SERVICE - GESTIÓN DE PERMISOS Y AUTORIZACIÓN
 * =============================================================================
 * 
 * Este servicio maneja toda la lógica de autorización del frontend:
 * ✅ Obtención de permisos del usuario desde el backend
 * ✅ Almacenamiento en sessionStorage para acceso rápido
 * ✅ Validación de permisos para componentes y rutas
 * ✅ Gestión del estado de autorización
 * ✅ Integración con MSAL para tokens de acceso
 * 
 * @author Sistema de Autorización Angular-Entra
 * @version 1.0.0
 * =============================================================================
 */

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { MsalService } from '@azure/msal-angular';

// Interfaces para tipado
export interface UserPermission {
  codigo: string;
  nombre: string;
  modulo: string;
  accion: string;
  descripcion: string;
}

export interface UserProfile {
  id: number;
  nombre: string;
  descripcion: string;
  azureGroupId: string;
  azureGroupName: string;
}

export interface UserInfo {
  email: string;
  nombre: string;
  grupos: string[];
  perfiles: UserProfile[];
  permisos: UserPermission[];
  codigosPermisos: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AuthorizationService {
  private readonly baseUrl = 'http://localhost:8080/api/autorizacion';
  private readonly storageKey = 'permisos_usuario';
  private readonly userInfoKey = 'informacion_usuario';

  // Estado reactivo de los permisos
  private permissionsSubject = new BehaviorSubject<string[]>([]);
  private userInfoSubject = new BehaviorSubject<UserInfo | null>(null);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public permissions$ = this.permissionsSubject.asObservable();
  public userInfo$ = this.userInfoSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();

  constructor(
    private http: HttpClient,
    private msalService: MsalService
  ) {
    console.log('🔧 [AuthorizationService] Inicializando servicio de autorización...');
    this.loadPermissionsFromStorage();
  }

  /**
   * =============================================================================
   * 🔄 MÉTODOS DE INICIALIZACIÓN Y CARGA
   * =============================================================================
   */

  /**
   * Inicializa los permisos del usuario obteniendo la información del backend
   */
  initializeUserPermissions(): Observable<UserInfo> {
    console.log('🔄 [AuthorizationService] Inicializando permisos del usuario...');
    console.log('🌐 [AuthorizationService] URL del endpoint:', `${this.baseUrl}/informacion-usuario`);
    this.loadingSubject.next(true);
    
    return this.http.get<UserInfo>(`${this.baseUrl}/informacion-usuario`).pipe(
      tap(userInfo => {
        console.log('✅ [AuthorizationService] Información del usuario obtenida del backend:', userInfo);
        console.log('📊 [AuthorizationService] Detalles del usuario:');
        console.log('  - Email:', userInfo.email);
        console.log('  - Nombre:', userInfo.nombre);
        console.log('  - Grupos:', userInfo.grupos);
        console.log('  - Perfiles:', userInfo.perfiles?.length || 0);
        console.log('  - Permisos:', userInfo.permisos?.length || 0);
        console.log('  - Códigos de permisos:', userInfo.codigosPermisos);
        
        this.storeUserInfo(userInfo);
        this.updatePermissionsState(userInfo.codigosPermisos);
        this.loadingSubject.next(false);
      }),
      catchError(error => {
        console.error('❌ [AuthorizationService] Error al obtener información del usuario:', error);
        console.error('🔍 [AuthorizationService] Detalles del error:', {
          status: error.status,
          message: error.message,
          url: error.url
        });
        this.clearStoredData();
        this.loadingSubject.next(false);
        return throwError(() => error);
      })
    );
  }

  /**
   * Carga los permisos desde sessionStorage al inicializar el servicio
   */
  private loadPermissionsFromStorage(): void {
    console.log('🔍 [AuthorizationService] Verificando permisos almacenados en sessionStorage...');
    try {
      const storedUserInfo = sessionStorage.getItem(this.userInfoKey);
      if (storedUserInfo) {
        console.log('📦 [AuthorizationService] Información del usuario encontrada en sessionStorage');
        const userInfo: UserInfo = JSON.parse(storedUserInfo);
        console.log('👤 [AuthorizationService] Usuario cargado desde storage:', userInfo.nombre);
        console.log('🔑 [AuthorizationService] Permisos cargados desde storage:', userInfo.codigosPermisos);
        this.userInfoSubject.next(userInfo);
        this.updatePermissionsState(userInfo.codigosPermisos);
        console.log('✅ [AuthorizationService] Permisos cargados exitosamente desde sessionStorage');
      } else {
        console.log('📭 [AuthorizationService] No hay información del usuario en sessionStorage');
      }
    } catch (error) {
      console.error('❌ [AuthorizationService] Error al cargar permisos desde storage:', error);
      this.clearStoredData();
    }
  }

  /**
   * Almacena la información del usuario en sessionStorage
   */
  private storeUserInfo(userInfo: UserInfo): void {
    try {
      sessionStorage.setItem(this.userInfoKey, JSON.stringify(userInfo));
      sessionStorage.setItem(this.storageKey, JSON.stringify(userInfo.codigosPermisos));
      this.userInfoSubject.next(userInfo);
      console.log('💾 Información del usuario almacenada en sessionStorage');
    } catch (error) {
      console.error('❌ Error al almacenar información del usuario:', error);
    }
  }

  /**
   * Actualiza el estado reactivo de los permisos
   */
  private updatePermissionsState(permissions: string[]): void {
    this.permissionsSubject.next(permissions);
  }

  /**
   * =============================================================================
   * 🔍 MÉTODOS DE VALIDACIÓN DE PERMISOS
   * =============================================================================
   */

  /**
   * Verifica si el usuario tiene un permiso específico
   */
  hasPermission(permissionCode: string): boolean {
    const permissions = this.permissionsSubject.value;
    const hasPermission = permissions.includes(permissionCode);
    console.log(`🔍 Verificando permiso "${permissionCode}": ${hasPermission ? '✅' : '❌'}`);
    return hasPermission;
  }

  /**
   * Verifica si el usuario tiene alguno de los permisos especificados
   */
  hasAnyPermission(permissionCodes: string[]): boolean {
    const permissions = this.permissionsSubject.value;
    const hasAny = permissionCodes.some(code => permissions.includes(code));
    console.log(`🔍 Verificando permisos ${permissionCodes}: ${hasAny ? '✅' : '❌'}`);
    return hasAny;
  }

  /**
   * Verifica si el usuario tiene todos los permisos especificados
   */
  hasAllPermissions(permissionCodes: string[]): boolean {
    const permissions = this.permissionsSubject.value;
    const hasAll = permissionCodes.every(code => permissions.includes(code));
    console.log(`🔍 Verificando todos los permisos ${permissionCodes}: ${hasAll ? '✅' : '❌'}`);
    return hasAll;
  }

  /**
   * Verifica si el usuario tiene permisos para un módulo específico
   */
  hasModulePermission(module: string): boolean {
    const userInfo = this.userInfoSubject.value;
    if (!userInfo) return false;
    
    const hasModule = userInfo.permisos.some(permiso => permiso.modulo === module);
    console.log(`🔍 Verificando permisos para módulo "${module}": ${hasModule ? '✅' : '❌'}`);
    return hasModule;
  }

  /**
   * Verifica si el usuario tiene permisos para una acción específica
   */
  hasActionPermission(action: string): boolean {
    const userInfo = this.userInfoSubject.value;
    if (!userInfo) return false;
    
    const hasAction = userInfo.permisos.some(permiso => permiso.accion === action);
    console.log(`🔍 Verificando permisos para acción "${action}": ${hasAction ? '✅' : '❌'}`);
    return hasAction;
  }

  /**
   * Verifica si el usuario tiene permisos para una acción en un módulo específico
   */
  hasModuleActionPermission(module: string, action: string): boolean {
    const userInfo = this.userInfoSubject.value;
    if (!userInfo) return false;
    
    const hasModuleAction = userInfo.permisos.some(
      permiso => permiso.modulo === module && permiso.accion === action
    );
    console.log(`🔍 Verificando permiso "${module}.${action}": ${hasModuleAction ? '✅' : '❌'}`);
    return hasModuleAction;
  }

  /**
   * =============================================================================
   * 📡 MÉTODOS DE COMUNICACIÓN CON EL BACKEND
   * =============================================================================
   */

  /**
   * Obtiene los permisos del usuario desde el backend
   */
  getUserPermissions(): Observable<UserPermission[]> {
    return this.http.get<UserPermission[]>(`${this.baseUrl}/permisos`);
  }

  /**
   * Obtiene solo los códigos de permisos del usuario
   */
  getPermissionCodes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/codigos-permisos`);
  }

  /**
   * Verifica un permiso específico en el backend
   */
  checkPermissionOnServer(permissionCode: string): Observable<boolean> {
    return this.http.get<{tienePermiso: boolean}>(`${this.baseUrl}/tiene-permiso/${permissionCode}`)
      .pipe(map(response => response.tienePermiso));
  }

  /**
   * Verifica múltiples permisos en el backend
   */
  checkMultiplePermissions(permissionCodes: string[]): Observable<{[key: string]: boolean}> {
    return this.http.post<{[key: string]: boolean}>(`${this.baseUrl}/verificar-permisos`, permissionCodes);
  }

  /**
   * Valida acceso complejo en el backend
   */
  validateAccess(validationRequest: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/validar-acceso`, validationRequest);
  }

  /**
   * =============================================================================
   * 📊 MÉTODOS DE INFORMACIÓN Y ESTADO
   * =============================================================================
   */

  /**
   * Obtiene la información completa del usuario
   */
  getCurrentUserInfo(): UserInfo | null {
    return this.userInfoSubject.value;
  }

  /**
   * Obtiene los permisos actuales del usuario
   */
  getCurrentPermissions(): string[] {
    return this.permissionsSubject.value;
  }

  /**
   * Obtiene los perfiles del usuario actual
   */
  getCurrentUserProfiles(): UserProfile[] {
    const userInfo = this.userInfoSubject.value;
    return userInfo ? userInfo.perfiles : [];
  }

  /**
   * Verifica si el usuario está autenticado y tiene permisos cargados
   */
  isAuthorized(): boolean {
    const permissions = this.permissionsSubject.value;
    const isAuth = permissions.length > 0;
    console.log(`🔍 Usuario autorizado: ${isAuth ? '✅' : '❌'}`);
    return isAuth;
  }

  /**
   * Obtiene el estado actual de carga
   */
  isLoading(): boolean {
    return this.loadingSubject.value;
  }

  /**
   * Obtiene los permisos agrupados por módulo
   */
  getPermissionsByModule(): {[module: string]: UserPermission[]} {
    const userInfo = this.userInfoSubject.value;
    if (!userInfo) return {};

    return userInfo.permisos.reduce((acc, permiso) => {
      if (!acc[permiso.modulo]) {
        acc[permiso.modulo] = [];
      }
      acc[permiso.modulo].push(permiso);
      return acc;
    }, {} as {[module: string]: UserPermission[]});
  }

  /**
   * =============================================================================
   * 🔧 MÉTODOS DE UTILIDAD Y LIMPIEZA
   * =============================================================================
   */

  /**
   * Refresca los permisos del usuario desde el backend
   */
  refreshPermissions(): Observable<UserInfo> {
    console.log('🔄 Refrescando permisos del usuario...');
    return this.initializeUserPermissions();
  }

  /**
   * Limpia todos los datos almacenados
   */
  clearStoredData(): void {
    try {
      sessionStorage.removeItem(this.storageKey);
      sessionStorage.removeItem(this.userInfoKey);
      this.permissionsSubject.next([]);
      this.userInfoSubject.next(null);
      console.log('🧹 Datos de autorización limpiados');
    } catch (error) {
      console.error('❌ Error al limpiar datos:', error);
    }
  }

  /**
   * Maneja el logout limpiando los permisos
   */
  logout(): void {
    console.log('👋 Cerrando sesión y limpiando permisos...');
    this.clearStoredData();
  }

  /**
   * Verifica si el token de MSAL es válido
   */
  private isTokenValid(): boolean {
    try {
      const accounts = this.msalService.instance.getAllAccounts();
      return accounts.length > 0;
    } catch (error) {
      console.error('❌ Error al verificar token:', error);
      return false;
    }
  }

  /**
   * =============================================================================
   * 🎯 MÉTODOS DE CONVENIENCIA PARA COMPONENTES
   * =============================================================================
   */

  /**
   * Verifica permisos comunes del sistema
   */
  canReadUsers(): boolean { return this.hasPermission('USUARIOS_LEER'); }
  canCreateUsers(): boolean { return this.hasPermission('USUARIOS_CREAR'); }
  canEditUsers(): boolean { return this.hasPermission('USUARIOS_EDITAR'); }
  canDeleteUsers(): boolean { return this.hasPermission('USUARIOS_ELIMINAR'); }
  
  canReadReports(): boolean { return this.hasPermission('REPORTES_LEER'); }
  canCreateReports(): boolean { return this.hasPermission('REPORTES_CREAR'); }
  canExportReports(): boolean { return this.hasPermission('REPORTES_EXPORTAR'); }
  
  canReadConfig(): boolean { return this.hasPermission('CONFIG_LEER'); }
  canEditConfig(): boolean { return this.hasPermission('CONFIG_EDITAR'); }
  
  canAccessDashboard(): boolean { return this.hasPermission('DASHBOARD_LEER'); }
  canAccessAdminDashboard(): boolean { return this.hasPermission('DASHBOARD_ADMIN'); }
  
  canManageProfiles(): boolean { 
    return this.hasAnyPermission(['PERFILES_CREAR', 'PERFILES_EDITAR', 'PERFILES_ELIMINAR']); 
  }
}

/**
 * =============================================================================
 * 🛡️ PERMISSION GUARD - PROTECCIÓN DE RUTAS BASADA EN PERMISOS
 * =============================================================================
 * 
 * Este guard protege las rutas de la aplicación verificando que el usuario
 * tenga los permisos necesarios antes de permitir el acceso.
 * 
 * FUNCIONALIDADES:
 * ✅ Verificación de permisos específicos por ruta
 * ✅ Verificación de permisos por módulo
 * ✅ Verificación de permisos por acción
 * ✅ Redirección automática en caso de acceso denegado
 * ✅ Integración con el AuthorizationService
 * 
 * USO EN RUTAS:
 * {
 *   path: 'usuarios',
 *   component: UsuariosComponent,
 *   canActivate: [PermissionGuard],
 *   data: { 
 *     permissions: ['USUARIOS_LEER'],
 *     requireAll: false // true = requiere todos, false = requiere al menos uno
 *   }
 * }
 * 
 * @author Sistema de Autorización Angular-Entra
 * @version 1.0.0
 * =============================================================================
 */

import { Injectable } from '@angular/core';
import { 
  CanActivate, 
  CanActivateChild, 
  ActivatedRouteSnapshot, 
  RouterStateSnapshot, 
  Router 
} from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { AuthorizationService } from '../services/authorization.service';
import { MsalService } from '@azure/msal-angular';

@Injectable({
  providedIn: 'root'
})
export class PermissionGuard implements CanActivate, CanActivateChild {

  constructor(
    private authorizationService: AuthorizationService,
    private msalService: MsalService,
    private router: Router
  ) {}

  /**
   * Protege la activación de rutas principales
   */
  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {
    return this.checkPermissions(route, state);
  }

  /**
   * Protege la activación de rutas hijas
   */
  canActivateChild(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {
    return this.checkPermissions(route, state);
  }

  /**
   * Lógica principal de verificación de permisos
   */
  private checkPermissions(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {
    
    console.log(`🛡️ [PermissionGuard] Verificando acceso a ${state.url}`);
    console.log(`📍 [PermissionGuard] Ruta completa:`, route.routeConfig?.path);

    // Verificar si el usuario está autenticado en MSAL
    if (!this.isUserAuthenticated()) {
      console.log('❌ [PermissionGuard] Usuario no autenticado, redirigiendo al login');
      this.msalService.loginRedirect();
      return false;
    }
    console.log('✅ [PermissionGuard] Usuario autenticado en MSAL');

    // Obtener configuración de permisos desde la ruta
    const routeData = route.data;
    const permissions = routeData['permissions'] as string[];
    const module = routeData['module'] as string;
    const action = routeData['action'] as string;
    const requireAll = routeData['requireAll'] as boolean || false;

    console.log('🔍 [PermissionGuard] Configuración de la ruta:');
    console.log('  - Permisos requeridos:', permissions);
    console.log('  - Módulo:', module);
    console.log('  - Acción:', action);
    console.log('  - Requerir todos:', requireAll);

    // Si no hay restricciones específicas, permitir acceso
    if (!permissions && !module && !action) {
      console.log('✅ [PermissionGuard] Ruta sin restricciones específicas, acceso permitido');
      return true;
    }

    // Verificar si los permisos están cargados
    if (!this.authorizationService.isAuthorized()) {
      console.log('⏳ [PermissionGuard] Permisos no cargados, inicializando...');
      return this.authorizationService.initializeUserPermissions().pipe(
        map(() => {
          console.log('🔄 [PermissionGuard] Permisos cargados, evaluando acceso...');
          return this.evaluatePermissions(permissions, module, action, requireAll, state.url);
        }),
        catchError(error => {
          console.error('❌ [PermissionGuard] Error al cargar permisos:', error);
          this.handleAccessDenied(state.url);
          return of(false);
        })
      );
    }

    // Evaluar permisos
    console.log('🔍 [PermissionGuard] Evaluando permisos específicos...');
    const hasAccess = this.evaluatePermissions(permissions, module, action, requireAll, state.url);
    
    if (!hasAccess) {
      this.handleAccessDenied(state.url);
    }

    return hasAccess;
  }

  /**
   * Evalúa los permisos según los criterios especificados
   */
  private evaluatePermissions(
    permissions?: string[],
    module?: string,
    action?: string,
    requireAll?: boolean,
    url?: string
  ): boolean {
    
    // Verificar permisos específicos
    if (permissions && permissions.length > 0) {
      const hasPermission = requireAll 
        ? this.authorizationService.hasAllPermissions(permissions)
        : this.authorizationService.hasAnyPermission(permissions);
      
      if (!hasPermission) {
        console.log(`❌ Acceso denegado por permisos: ${permissions.join(', ')}`);
        return false;
      }
    }

    // Verificar permisos por módulo
    if (module && !this.authorizationService.hasModulePermission(module)) {
      console.log(`❌ Acceso denegado al módulo: ${module}`);
      return false;
    }

    // Verificar permisos por acción
    if (action && !this.authorizationService.hasActionPermission(action)) {
      console.log(`❌ Acceso denegado para la acción: ${action}`);
      return false;
    }

    // Verificar combinación módulo + acción
    if (module && action && !this.authorizationService.hasModuleActionPermission(module, action)) {
      console.log(`❌ Acceso denegado para ${module}.${action}`);
      return false;
    }

    console.log(`✅ Acceso permitido a ${url}`);
    return true;
  }

  /**
   * Verifica si el usuario está autenticado en MSAL
   */
  private isUserAuthenticated(): boolean {
    try {
      const accounts = this.msalService.instance.getAllAccounts();
      return accounts.length > 0;
    } catch (error) {
      console.error('❌ Error al verificar autenticación:', error);
      return false;
    }
  }

  /**
   * Maneja el acceso denegado redirigiendo a una página apropiada
   */
  private handleAccessDenied(attemptedUrl: string): void {
    console.log(`🚫 Acceso denegado a: ${attemptedUrl}`);
    
    // Guardar la URL intentada para redirección posterior
    sessionStorage.setItem('attempted_url', attemptedUrl);
    
    // Redirigir a página de acceso denegado o dashboard principal
    this.router.navigate(['/acceso-denegado'], {
      queryParams: { returnUrl: attemptedUrl }
    }).catch(() => {
      // Si no existe la ruta de acceso denegado, ir al dashboard
      this.router.navigate(['/']);
    });
  }
}

/**
 * =============================================================================
 * 🔧 GUARD ESPECÍFICO PARA ADMINISTRADORES
 * =============================================================================
 */
@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(
    private authorizationService: AuthorizationService,
    private router: Router
  ) {}

  canActivate(): boolean {
    const hasAdminAccess = this.authorizationService.canAccessAdminDashboard();
    
    if (!hasAdminAccess) {
      console.log('❌ Acceso de administrador denegado');
      this.router.navigate(['/acceso-denegado']);
      return false;
    }

    console.log('✅ Acceso de administrador permitido');
    return true;
  }
}

/**
 * =============================================================================
 * 🔧 GUARD ESPECÍFICO PARA GESTORES
 * =============================================================================
 */
@Injectable({
  providedIn: 'root'
})
export class ManagerGuard implements CanActivate {

  constructor(
    private authorizationService: AuthorizationService,
    private router: Router
  ) {}

  canActivate(): boolean {
    const hasManagerAccess = this.authorizationService.hasAnyPermission([
      'DASHBOARD_ADMIN', 
      'PERFILES_LEER', 
      'USUARIOS_CREAR', 
      'USUARIOS_EDITAR'
    ]);
    
    if (!hasManagerAccess) {
      console.log('❌ Acceso de gestor denegado');
      this.router.navigate(['/acceso-denegado']);
      return false;
    }

    console.log('✅ Acceso de gestor permitido');
    return true;
  }
}

/**
 * =============================================================================
 * 🎯 HAS-PERMISSION DIRECTIVE - RENDERIZADO CONDICIONAL BASADO EN PERMISOS
 * =============================================================================
 * 
 * Esta directiva permite mostrar u ocultar elementos del DOM basándose en
 * los permisos del usuario autenticado.
 * 
 * FUNCIONALIDADES:
 * ✅ Mostrar/ocultar elementos según permisos específicos
 * ✅ Verificación de múltiples permisos (AND/OR)
 * ✅ Verificación por módulo y acción
 * ✅ Reactividad automática cuando cambian los permisos
 * ✅ Optimización de rendimiento con OnPush
 * 
 * EJEMPLOS DE USO:
 * 
 * <!-- Mostrar si tiene permiso específico -->
 * <button *hasPermission="'USUARIOS_CREAR'">Crear Usuario</button>
 * 
 * <!-- Mostrar si tiene alguno de los permisos -->
 * <div *hasPermission="['USUARIOS_LEER', 'USUARIOS_EDITAR']; requireAll: false">
 *   Lista de usuarios
 * </div>
 * 
 * <!-- Mostrar si tiene todos los permisos -->
 * <div *hasPermission="['USUARIOS_CREAR', 'USUARIOS_EDITAR']; requireAll: true">
 *   Panel de administración
 * </div>
 * 
 * <!-- Mostrar si tiene permisos para un módulo -->
 * <nav *hasPermission="null; module: 'REPORTES'">
 *   Menú de reportes
 * </nav>
 * 
 * <!-- Mostrar si tiene permisos para una acción -->
 * <button *hasPermission="null; action: 'CREAR'">Crear</button>
 * 
 * <!-- Combinación de módulo y acción -->
 * <button *hasPermission="null; module: 'USUARIOS'; action: 'ELIMINAR'">
 *   Eliminar Usuario
 * </button>
 * 
 * @author Sistema de Autorización Angular-Entra
 * @version 1.0.0
 * =============================================================================
 */

import { 
  Directive, 
  Input, 
  TemplateRef, 
  ViewContainerRef, 
  OnInit, 
  OnDestroy 
} from '@angular/core';
import { Subscription } from 'rxjs';
import { AuthorizationService } from '../services/authorization.service';

@Directive({
  selector: '[hasPermission]'
})
export class HasPermissionDirective implements OnInit, OnDestroy {
  
  private subscription: Subscription = new Subscription();
  private hasView = false;

  // Inputs para la directiva
  @Input() hasPermission: string | string[] | null = null;
  @Input() hasPermissionRequireAll: boolean = false;
  @Input() hasPermissionModule: string | null = null;
  @Input() hasPermissionAction: string | null = null;

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private authorizationService: AuthorizationService
  ) {}

  ngOnInit(): void {
    // Suscribirse a cambios en los permisos
    this.subscription.add(
      this.authorizationService.permissions$.subscribe(() => {
        this.updateView();
      })
    );

    // Evaluación inicial
    this.updateView();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  /**
   * Actualiza la visibilidad del elemento basándose en los permisos
   */
  private updateView(): void {
    const shouldShow = this.evaluatePermissions();

    if (shouldShow && !this.hasView) {
      // Mostrar el elemento
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
      this.logPermissionCheck('Elemento mostrado', true);
    } else if (!shouldShow && this.hasView) {
      // Ocultar el elemento
      this.viewContainer.clear();
      this.hasView = false;
      this.logPermissionCheck('Elemento ocultado', false);
    }
  }

  /**
   * Evalúa si el usuario tiene los permisos necesarios
   */
  private evaluatePermissions(): boolean {
    // Si no hay permisos cargados, ocultar por defecto
    if (!this.authorizationService.isAuthorized()) {
      return false;
    }

    // Verificar permisos específicos
    if (this.hasPermission) {
      if (typeof this.hasPermission === 'string') {
        return this.authorizationService.hasPermission(this.hasPermission);
      } else if (Array.isArray(this.hasPermission)) {
        return this.hasPermissionRequireAll
          ? this.authorizationService.hasAllPermissions(this.hasPermission)
          : this.authorizationService.hasAnyPermission(this.hasPermission);
      }
    }

    // Verificar permisos por módulo y/o acción
    if (this.hasPermissionModule || this.hasPermissionAction) {
      // Verificar combinación módulo + acción
      if (this.hasPermissionModule && this.hasPermissionAction) {
        return this.authorizationService.hasModuleActionPermission(
          this.hasPermissionModule, 
          this.hasPermissionAction
        );
      }

      // Verificar solo módulo
      if (this.hasPermissionModule) {
        return this.authorizationService.hasModulePermission(this.hasPermissionModule);
      }

      // Verificar solo acción
      if (this.hasPermissionAction) {
        return this.authorizationService.hasActionPermission(this.hasPermissionAction);
      }
    }

    // Si no se especificaron criterios, mostrar por defecto
    return true;
  }

  /**
   * Log para debug de permisos
   */
  private logPermissionCheck(action: string, result: boolean): void {
    const criteria = this.buildCriteriaString();
    console.log(`🎯 HasPermissionDirective: ${action} - ${criteria} = ${result ? '✅' : '❌'}`);
  }

  /**
   * Construye una cadena descriptiva de los criterios de permisos
   */
  private buildCriteriaString(): string {
    const parts: string[] = [];

    if (this.hasPermission) {
      if (typeof this.hasPermission === 'string') {
        parts.push(`permiso: "${this.hasPermission}"`);
      } else if (Array.isArray(this.hasPermission)) {
        const operator = this.hasPermissionRequireAll ? 'AND' : 'OR';
        parts.push(`permisos: [${this.hasPermission.join(', ')}] (${operator})`);
      }
    }

    if (this.hasPermissionModule) {
      parts.push(`módulo: "${this.hasPermissionModule}"`);
    }

    if (this.hasPermissionAction) {
      parts.push(`acción: "${this.hasPermissionAction}"`);
    }

    return parts.length > 0 ? parts.join(', ') : 'sin criterios';
  }
}

/**
 * =============================================================================
 * 🔧 DIRECTIVA DE CONVENIENCIA PARA ROLES ESPECÍFICOS
 * =============================================================================
 */

@Directive({
  selector: '[isAdmin]'
})
export class IsAdminDirective implements OnInit, OnDestroy {
  
  private subscription: Subscription = new Subscription();
  private hasView = false;

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private authorizationService: AuthorizationService
  ) {}

  ngOnInit(): void {
    this.subscription.add(
      this.authorizationService.permissions$.subscribe(() => {
        this.updateView();
      })
    );
    this.updateView();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private updateView(): void {
    const isAdmin = this.authorizationService.canAccessAdminDashboard();

    if (isAdmin && !this.hasView) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
    } else if (!isAdmin && this.hasView) {
      this.viewContainer.clear();
      this.hasView = false;
    }
  }
}

/**
 * =============================================================================
 * 🔧 DIRECTIVA DE CONVENIENCIA PARA GESTORES
 * =============================================================================
 */

@Directive({
  selector: '[isManager]'
})
export class IsManagerDirective implements OnInit, OnDestroy {
  
  private subscription: Subscription = new Subscription();
  private hasView = false;

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private authorizationService: AuthorizationService
  ) {}

  ngOnInit(): void {
    this.subscription.add(
      this.authorizationService.permissions$.subscribe(() => {
        this.updateView();
      })
    );
    this.updateView();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private updateView(): void {
    const isManager = this.authorizationService.hasAnyPermission([
      'DASHBOARD_ADMIN', 
      'PERFILES_LEER', 
      'USUARIOS_CREAR', 
      'USUARIOS_EDITAR'
    ]);

    if (isManager && !this.hasView) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
    } else if (!isManager && this.hasView) {
      this.viewContainer.clear();
      this.hasView = false;
    }
  }
}

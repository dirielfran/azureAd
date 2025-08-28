/**
 * =============================================================================
 * 👤 USER PERMISSIONS COMPONENT - DASHBOARD DE PERMISOS DEL USUARIO
 * =============================================================================
 * 
 * Componente que muestra los permisos y perfiles del usuario autenticado,
 * junto con ejemplos de renderizado condicional basado en permisos.
 * 
 * @author Sistema de Autorización Angular-Entra
 * @version 1.0.0
 * =============================================================================
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { AuthorizationService, UserInfo } from '../services/authorization.service';

@Component({
  selector: 'app-user-permissions',
  template: `
    <div class="permissions-container">
      <div class="header">
        <h1>
          <i class="fas fa-user-shield"></i>
          Mis Permisos y Accesos
        </h1>
        <p class="subtitle">
          Información sobre tus permisos y perfiles en el sistema
        </p>
      </div>

      <!-- Loading State -->
      <div *ngIf="isLoading" class="loading-container">
        <div class="spinner"></div>
        <p>Cargando información de permisos...</p>
      </div>

      <!-- User Info -->
      <div *ngIf="!isLoading && userInfo" class="user-info-section">
        <div class="info-card">
          <h2>
            <i class="fas fa-user"></i>
            Información Personal
          </h2>
          <div class="user-details">
            <p><strong>Nombre:</strong> {{ userInfo.nombre }}</p>
            <p><strong>Email:</strong> {{ userInfo.email }}</p>
            <p><strong>Grupos de Azure AD:</strong> 
              <span *ngFor="let grupo of userInfo.grupos; let last = last">
                {{ grupo }}<span *ngIf="!last">, </span>
              </span>
            </p>
          </div>
        </div>

        <!-- User Profiles -->
        <div class="profiles-card" *ngIf="userInfo.perfiles.length > 0">
          <h2>
            <i class="fas fa-id-badge"></i>
            Perfiles Asignados
          </h2>
          <div class="profiles-grid">
            <div 
              *ngFor="let perfil of userInfo.perfiles" 
              class="profile-item"
            >
              <div class="profile-header">
                <h3>{{ perfil.nombre }}</h3>
                <span class="azure-group">{{ perfil.azureGroupName }}</span>
              </div>
              <p class="profile-description">{{ perfil.descripcion }}</p>
              <div class="profile-meta">
                <small>ID Grupo Azure: {{ perfil.azureGroupId }}</small>
              </div>
            </div>
          </div>
        </div>

        <!-- User Permissions -->
        <div class="permissions-card" *ngIf="userInfo.permisos.length > 0">
          <h2>
            <i class="fas fa-key"></i>
            Permisos Disponibles
          </h2>
          
          <!-- Permissions by Module -->
          <div class="permissions-by-module">
            <div 
              *ngFor="let moduleGroup of permissionsByModule | keyvalue" 
              class="module-group"
            >
              <h3 class="module-title">
                <i class="fas fa-folder"></i>
                {{ moduleGroup.key }}
              </h3>
              <div class="permissions-list">
                <div 
                  *ngFor="let permiso of moduleGroup.value" 
                  class="permission-item"
                  [class]="'action-' + permiso.accion.toLowerCase()"
                >
                  <div class="permission-header">
                    <span class="permission-code">{{ permiso.codigo }}</span>
                    <span class="permission-action">{{ permiso.accion }}</span>
                  </div>
                  <p class="permission-name">{{ permiso.nombre }}</p>
                  <p class="permission-description">{{ permiso.descripcion }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Permission Examples -->
        <div class="examples-card">
          <h2>
            <i class="fas fa-code"></i>
            Ejemplos de Renderizado Condicional
          </h2>
          
          <div class="examples-grid">
            <!-- Admin Only -->
            <div class="example-section">
              <h3>Solo Administradores</h3>
              <div *isAdmin class="admin-content">
                <i class="fas fa-crown"></i>
                <p>¡Eres administrador! Puedes ver este contenido exclusivo.</p>
                <button class="btn btn-danger">Panel de Administración</button>
              </div>
              <div *ngIf="!authService.canAccessAdminDashboard()" class="no-access">
                <i class="fas fa-lock"></i>
                <p>Contenido solo para administradores</p>
              </div>
            </div>

            <!-- Manager or Admin -->
            <div class="example-section">
              <h3>Gestores y Administradores</h3>
              <div *isManager class="manager-content">
                <i class="fas fa-users-cog"></i>
                <p>Tienes permisos de gestión</p>
                <button class="btn btn-warning">Panel de Gestión</button>
              </div>
              <div *ngIf="!hasManagerAccess()" class="no-access">
                <i class="fas fa-lock"></i>
                <p>Contenido para gestores</p>
              </div>
            </div>

            <!-- Specific Permissions -->
            <div class="example-section">
              <h3>Gestión de Usuarios</h3>
              <div class="permission-examples">
                <button 
                  *hasPermission="'USUARIOS_LEER'" 
                  class="btn btn-info"
                >
                  <i class="fas fa-eye"></i>
                  Ver Usuarios
                </button>
                
                <button 
                  *hasPermission="'USUARIOS_CREAR'" 
                  class="btn btn-success"
                >
                  <i class="fas fa-plus"></i>
                  Crear Usuario
                </button>
                
                <button 
                  *hasPermission="'USUARIOS_EDITAR'" 
                  class="btn btn-primary"
                >
                  <i class="fas fa-edit"></i>
                  Editar Usuario
                </button>
                
                <button 
                  *hasPermission="'USUARIOS_ELIMINAR'" 
                  class="btn btn-danger"
                >
                  <i class="fas fa-trash"></i>
                  Eliminar Usuario
                </button>
              </div>
            </div>

            <!-- Module Permissions -->
            <div class="example-section">
              <h3>Acceso por Módulos</h3>
              <div class="module-examples">
                <div 
                  *hasPermission="null; module: 'REPORTES'" 
                  class="module-access"
                >
                  <i class="fas fa-chart-bar"></i>
                  <p>Tienes acceso al módulo de Reportes</p>
                </div>
                
                <div 
                  *hasPermission="null; module: 'CONFIGURACION'" 
                  class="module-access"
                >
                  <i class="fas fa-cog"></i>
                  <p>Tienes acceso a Configuración</p>
                </div>
                
                <div 
                  *hasPermission="null; module: 'PERFILES'" 
                  class="module-access"
                >
                  <i class="fas fa-id-card"></i>
                  <p>Tienes acceso a gestión de Perfiles</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="actions-card">
          <h2>
            <i class="fas fa-tools"></i>
            Acciones
          </h2>
          <div class="actions-buttons">
            <button 
              class="btn btn-primary" 
              (click)="refreshPermissions()"
              [disabled]="isRefreshing"
            >
              <i class="fas fa-sync-alt" [class.fa-spin]="isRefreshing"></i>
              {{ isRefreshing ? 'Actualizando...' : 'Actualizar Permisos' }}
            </button>
            
            <button 
              class="btn btn-info" 
              (click)="downloadPermissionsReport()"
            >
              <i class="fas fa-download"></i>
              Descargar Reporte
            </button>
            
            <button 
              class="btn btn-secondary" 
              (click)="copyPermissionsToClipboard()"
            >
              <i class="fas fa-copy"></i>
              Copiar Permisos
            </button>
          </div>
        </div>
      </div>

      <!-- No Permissions State -->
      <div *ngIf="!isLoading && (!userInfo || userInfo.permisos.length === 0)" class="no-permissions">
        <div class="no-permissions-content">
          <i class="fas fa-exclamation-triangle"></i>
          <h2>Sin Permisos Asignados</h2>
          <p>No tienes permisos asignados en el sistema. Contacta al administrador.</p>
          <button class="btn btn-primary" (click)="refreshPermissions()">
            <i class="fas fa-sync-alt"></i>
            Verificar Permisos
          </button>
        </div>
      </div>
    </div>
  `,
  styleUrls: ['./user-permissions.component.scss']
})
export class UserPermissionsComponent implements OnInit, OnDestroy {
  
  userInfo: UserInfo | null = null;
  permissionsByModule: {[module: string]: any[]} = {};
  isLoading = true;
  isRefreshing = false;
  private subscription = new Subscription();

  constructor(
    public authService: AuthorizationService
  ) {}

  ngOnInit(): void {
    this.loadUserPermissions();
    
    // Suscribirse a cambios en la información del usuario
    this.subscription.add(
      this.authService.userInfo$.subscribe(userInfo => {
        this.userInfo = userInfo;
        if (userInfo) {
          this.permissionsByModule = this.authService.getPermissionsByModule();
          this.isLoading = false;
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  /**
   * Carga los permisos del usuario
   */
  private loadUserPermissions(): void {
    if (this.authService.isAuthorized()) {
      this.userInfo = this.authService.getCurrentUserInfo();
      this.permissionsByModule = this.authService.getPermissionsByModule();
      this.isLoading = false;
    } else {
      this.authService.initializeUserPermissions().subscribe({
        next: (userInfo) => {
          this.userInfo = userInfo;
          this.permissionsByModule = this.authService.getPermissionsByModule();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error al cargar permisos:', error);
          this.isLoading = false;
        }
      });
    }
  }

  /**
   * Refresca los permisos del usuario
   */
  refreshPermissions(): void {
    this.isRefreshing = true;
    
    this.authService.refreshPermissions().subscribe({
      next: (userInfo) => {
        this.userInfo = userInfo;
        this.permissionsByModule = this.authService.getPermissionsByModule();
        this.isRefreshing = false;
      },
      error: (error) => {
        console.error('Error al refrescar permisos:', error);
        this.isRefreshing = false;
      }
    });
  }

  /**
   * Verifica si tiene acceso de gestor
   */
  hasManagerAccess(): boolean {
    return this.authService.hasAnyPermission([
      'DASHBOARD_ADMIN', 
      'PERFILES_LEER', 
      'USUARIOS_CREAR', 
      'USUARIOS_EDITAR'
    ]);
  }

  /**
   * Descarga un reporte de permisos
   */
  downloadPermissionsReport(): void {
    if (!this.userInfo) return;

    const report = {
      usuario: {
        nombre: this.userInfo.nombre,
        email: this.userInfo.email,
        fecha_reporte: new Date().toISOString()
      },
      perfiles: this.userInfo.perfiles,
      permisos: this.userInfo.permisos,
      grupos_azure: this.userInfo.grupos
    };

    const blob = new Blob([JSON.stringify(report, null, 2)], {
      type: 'application/json'
    });
    
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `permisos_${this.userInfo.email}_${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  /**
   * Copia los permisos al portapapeles
   */
  copyPermissionsToClipboard(): void {
    if (!this.userInfo) return;

    const permissionsList = this.userInfo.codigosPermisos.join(', ');
    navigator.clipboard.writeText(permissionsList).then(() => {
      console.log('Permisos copiados al portapapeles');
      // Aquí podrías mostrar un toast de confirmación
    });
  }
}

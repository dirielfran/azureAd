import { Component, OnInit, OnDestroy } from '@angular/core';
import { MsalService } from '@azure/msal-angular';
import { AuthorizationService, UserInfo } from './services/authorization.service';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'Sistema de Autorización Azure AD';
  userInfo: UserInfo | null = null;
  isInitializingPermissions = false;
  private subscription = new Subscription();

  constructor(
    public msalService: MsalService,
    public authorizationService: AuthorizationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Suscribirse a cambios en la información del usuario
    this.subscription.add(
      this.authorizationService.userInfo$.subscribe(userInfo => {
        this.userInfo = userInfo;
      })
    );

    // Inicializar permisos si el usuario ya está autenticado
    if (this.isLoggedIn && !this.authorizationService.isAuthorized()) {
      this.initializePermissions();
    }
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  /**
   * Inicia sesión con Microsoft Entra ID
   */
  login() {
    this.msalService.loginRedirect();
  }

  /**
   * Cierra sesión y limpia los permisos
   */
  logout() {
    this.authorizationService.logout();
    this.msalService.logoutRedirect();
  }

  /**
   * Verifica si el usuario está autenticado
   */
  get isLoggedIn(): boolean {
    return this.msalService.instance.getAllAccounts().length > 0;
  }

  /**
   * Obtiene el nombre del usuario actual
   */
  get userName(): string {
    if (this.userInfo) {
      return this.userInfo.nombre;
    }
    
    const accounts = this.msalService.instance.getAllAccounts();
    if (accounts.length > 0) {
      return accounts[0].name || accounts[0].username || 'Usuario';
    }
    
    return 'Usuario';
  }

  /**
   * Obtiene el email del usuario actual
   */
  get userEmail(): string {
    if (this.userInfo) {
      return this.userInfo.email;
    }
    
    const accounts = this.msalService.instance.getAllAccounts();
    if (accounts.length > 0) {
      return accounts[0].username || 'usuario@empresa.com';
    }
    
    return '';
  }

  /**
   * Inicializa los permisos del usuario
   */
  initializePermissions(): void {
    if (this.isInitializingPermissions) return;
    
    this.isInitializingPermissions = true;
    console.log('🔄 Inicializando permisos del usuario...');
    
    this.authorizationService.initializeUserPermissions().subscribe({
      next: (userInfo) => {
        console.log('✅ Permisos inicializados correctamente:', userInfo);
        this.isInitializingPermissions = false;
      },
      error: (error) => {
        console.error('❌ Error al inicializar permisos:', error);
        this.isInitializingPermissions = false;
      }
    });
  }

  /**
   * Navega a una ruta específica
   */
  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  /**
   * Verifica si el usuario tiene un permiso específico
   */
  hasPermission(permission: string): boolean {
    return this.authorizationService.hasPermission(permission);
  }

  /**
   * Verifica si el usuario es administrador
   */
  get isAdmin(): boolean {
    return this.authorizationService.canAccessAdminDashboard();
  }

  /**
   * Verifica si el usuario es gestor
   */
  get isManager(): boolean {
    return this.authorizationService.hasAnyPermission([
      'DASHBOARD_ADMIN', 
      'PERFILES_LEER', 
      'USUARIOS_CREAR', 
      'USUARIOS_EDITAR'
    ]);
  }

  /**
   * Obtiene el número de permisos del usuario
   */
  get permissionsCount(): number {
    return this.authorizationService.getCurrentPermissions().length;
  }

  /**
   * Obtiene el número de perfiles del usuario
   */
  get profilesCount(): number {
    return this.authorizationService.getCurrentUserProfiles().length;
  }
}
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
    console.log('🚀 [AppComponent] Inicializando aplicación...');
    
    // Suscribirse a cambios en la información del usuario
    this.subscription.add(
      this.authorizationService.userInfo$.subscribe(userInfo => {
        console.log('👤 [AppComponent] Información del usuario actualizada:', userInfo);
        this.userInfo = userInfo;
      })
    );

    // Verificar estado de autenticación
    console.log('🔍 [AppComponent] Verificando estado de autenticación...');
    console.log('🔐 [AppComponent] ¿Usuario logueado?', this.isLoggedIn);
    console.log('🛡️ [AppComponent] ¿Usuario autorizado?', this.authorizationService.isAuthorized());

    // Inicializar permisos si el usuario ya está autenticado
    if (this.isLoggedIn && !this.authorizationService.isAuthorized()) {
      console.log('⚡ [AppComponent] Usuario autenticado pero sin permisos, inicializando...');
      this.initializePermissions();
    } else if (this.isLoggedIn && this.authorizationService.isAuthorized()) {
      console.log('✅ [AppComponent] Usuario completamente autenticado y autorizado');
    } else {
      console.log('❌ [AppComponent] Usuario no autenticado, mostrando pantalla de login');
    }
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  /**
   * Inicia sesión con Microsoft Entra ID
   */
  login() {
    console.log('🔑 [AppComponent] Iniciando proceso de login...');
    console.log('🌐 [AppComponent] Redirigiendo a Microsoft Entra ID...');
    this.msalService.loginRedirect();
  }

  /**
   * Cierra sesión y limpia los permisos
   */
  logout() {
    console.log('👋 [AppComponent] Iniciando proceso de logout...');
    console.log('🧹 [AppComponent] Limpiando permisos del usuario...');
    this.authorizationService.logout();
    console.log('🌐 [AppComponent] Redirigiendo a Microsoft para cerrar sesión...');
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
    if (this.isInitializingPermissions) {
      console.log('⏳ [AppComponent] Ya se están inicializando permisos, omitiendo...');
      return;
    }
    
    this.isInitializingPermissions = true;
    console.log('🔄 [AppComponent] Inicializando permisos del usuario...');
    console.log('📡 [AppComponent] Llamando al backend para obtener información del usuario...');
    
    this.authorizationService.initializeUserPermissions().subscribe({
      next: (userInfo) => {
        console.log('✅ [AppComponent] Permisos inicializados correctamente:', userInfo);
        console.log('📊 [AppComponent] Perfiles del usuario:', userInfo.perfiles);
        console.log('🔑 [AppComponent] Permisos del usuario:', userInfo.permisos);
        console.log('📋 [AppComponent] Códigos de permisos:', userInfo.codigosPermisos);
        this.isInitializingPermissions = false;
      },
      error: (error) => {
        console.error('❌ [AppComponent] Error al inicializar permisos:', error);
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
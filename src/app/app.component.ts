import { Component, OnInit, OnDestroy } from '@angular/core';
import { MsalService } from '@azure/msal-angular';
import { AuthorizationService, UserInfo } from './services/authorization.service';
import { AuthConfigService } from './services/auth-config.service';
import { LocalAuthService } from './services/local-auth.service';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'Sistema de Autorización';
  userInfo: UserInfo | null = null;
  isInitializingPermissions = false;
  authMethod: 'azure' | 'local' | 'none' = 'none';
  isLoadingAuthConfig = true;
  private subscription = new Subscription();

  constructor(
    public msalService: MsalService,
    public authorizationService: AuthorizationService,
    public authConfigService: AuthConfigService,
    public localAuthService: LocalAuthService,
    private router: Router
  ) {
    // Con popup no necesitamos capturar hash - todo se maneja en la ventana popup
  }

  async ngOnInit(): Promise<void> {
    console.log('🚀 [AppComponent] Inicializando aplicación...');
    
    // Suscribirse a cambios en la información del usuario
    this.subscription.add(
      this.authorizationService.userInfo$.subscribe(userInfo => {
        console.log('👤 [AppComponent] Información del usuario actualizada:', userInfo);
        this.userInfo = userInfo;
      })
    );

    // Cargar configuración de autenticación desde el backend
    await this.loadAuthConfiguration();
    
    // Con popup no necesitamos manejar redirects - la ventana popup maneja todo
    if (this.authMethod === 'azure') {
      console.log('✅ [AppComponent] Azure AD habilitado (modo popup)');
      
      // Verificar si ya hay cuentas autenticadas
      const accounts = this.msalService.instance.getAllAccounts();
      console.log('📊 [AppComponent] Cuentas totales en MSAL:', accounts.length);
      if (accounts.length > 0) {
        console.log('👥 [AppComponent] Usuario ya autenticado:', accounts[0].username);
      }
    } else if (this.authMethod === 'local') {
      console.log('✅ [AppComponent] JWT Local habilitado');
    } else {
      console.log('ℹ️ [AppComponent] Ningún método de autenticación configurado');
    }
    
    // Verificar estado de autenticación según el método activo
    console.log('🔍 [AppComponent] Verificando estado de autenticación...');
    console.log('🔐 [AppComponent] Método de autenticación activo:', this.authMethod);
    console.log('🔐 [AppComponent] ¿Usuario logueado?', this.isLoggedIn);
    console.log('🛡️ [AppComponent] ¿Usuario autorizado?', this.authorizationService.isAuthorized());

    // Inicializar permisos si el usuario ya está autenticado
    if (this.isLoggedIn && !this.authorizationService.isAuthorized()) {
      console.log('⚡ [AppComponent] Usuario autenticado pero sin permisos, inicializando...');
      this.initializePermissions();
    } else if (this.isLoggedIn && this.authorizationService.isAuthorized()) {
      console.log('✅ [AppComponent] Usuario completamente autenticado y autorizado');
    } else {
      console.log('❌ [AppComponent] Usuario no autenticado -->', this.router.url);
      
      // Verificar si estamos procesando un callback de Microsoft (tiene parámetros de OAuth)
      const currentUrl = this.router.url;
      const isOAuthCallback = currentUrl.includes('code=') || currentUrl.includes('state=') || currentUrl.includes('error=');
      
      if (isOAuthCallback && this.authMethod === 'azure') {
        console.log('🔄 [AppComponent] Procesando callback de Microsoft, no redirigir...');
        // No hacer nada, dejar que MSAL termine de procesar
        return;
      }
      
      // Redirigir al login apropiado si no está autenticado
      const publicRoutes = ['/auth-selector', '/login'];
      
      // Si no está en una ruta pública, redirigir según el método de autenticación
      if (!publicRoutes.includes(currentUrl)) {
        if (this.authMethod === 'local') {
          console.log('🔀 [AppComponent] Redirigiendo a login local...');
          this.router.navigate(['/login']);
        } else if (this.authMethod === 'azure') {
          console.log('🔀 [AppComponent] Azure AD activo pero no autenticado, redirigiendo a selector...');
          this.router.navigate(['/auth-selector']);
        } else {
          console.log('🔀 [AppComponent] Sin método configurado, redirigiendo a selector...');
          this.router.navigate(['/auth-selector']);
        }
      }
    }
  }

  /**
   * Carga la configuración de autenticación desde el backend
   */
  async loadAuthConfiguration(): Promise<void> {
    try {
      console.log('📡 [AppComponent] Cargando configuración de autenticación...');
      this.isLoadingAuthConfig = true;
      
      await this.authConfigService.getAuthStatus();
      this.authMethod = this.authConfigService.getActiveAuthMethod();
      
      console.log('✅ [AppComponent] Configuración cargada:', this.authMethod);
      this.isLoadingAuthConfig = false;
    } catch (error) {
      console.error('❌ [AppComponent] Error al cargar configuración:', error);
      this.isLoadingAuthConfig = false;
      this.authMethod = 'none';
    }
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  /**
   * Inicia sesión según el método de autenticación activo
   */
  login() {
    console.log('🔑 [AppComponent] Iniciando proceso de login...');
    
    if (this.authMethod === 'azure') {
      console.log('🌐 [AppComponent] Redirigiendo a Microsoft Entra ID...');
      this.msalService.loginRedirect();
    } else if (this.authMethod === 'local') {
      console.log('🔐 [AppComponent] Redirigiendo al login local...');
      this.router.navigate(['/login']);
    } else {
      console.log('⚠️ [AppComponent] No hay método de autenticación activo');
      this.router.navigate(['/auth-selector']);
    }
  }

  /**
   * Cierra sesión según el método de autenticación activo
   */
  logout() {
    console.log('👋 [AppComponent] Iniciando proceso de logout...');
    console.log('🧹 [AppComponent] Limpiando permisos del usuario...');
    this.authorizationService.logout();
    
    if (this.authMethod === 'azure') {
      console.log('🌐 [AppComponent] Cerrando sesión de Microsoft...');
      this.msalService.logoutRedirect();
    } else if (this.authMethod === 'local') {
      console.log('🔐 [AppComponent] Cerrando sesión local...');
      this.localAuthService.logout();
      this.router.navigate(['/login']);
    } else {
      this.router.navigate(['/auth-selector']);
    }
  }

  /**
   * Verifica si el usuario está autenticado según el método activo
   */
  get isLoggedIn(): boolean {
    if (this.authMethod === 'azure') {
      return this.msalService.instance.getAllAccounts().length > 0;
    } else if (this.authMethod === 'local') {
      return this.localAuthService.isAuthenticated();
    }
    return false;
  }





  

  /**
   * Obtiene el nombre del usuario actual según el método de autenticación
   */
  get userName(): string {
    if (this.userInfo) {
      return this.userInfo.nombre;
    }
    
    if (this.authMethod === 'azure') {
      const accounts = this.msalService.instance.getAllAccounts();
      if (accounts.length > 0) {
        return accounts[0].name || accounts[0].username || 'Usuario';
      }
    } else if (this.authMethod === 'local') {
      const user = this.localAuthService.getCurrentUser();
      if (user) {
        return user.nombre;
      }
    }
    
    return 'Usuario';
  }

  /**
   * Obtiene el email del usuario actual según el método de autenticación
   */
  get userEmail(): string {
    if (this.userInfo) {
      return this.userInfo.email;
    }
    
    if (this.authMethod === 'azure') {
      const accounts = this.msalService.instance.getAllAccounts();
      if (accounts.length > 0) {
        return accounts[0].username || 'usuario@empresa.com';
      }
    } else if (this.authMethod === 'local') {
      const user = this.localAuthService.getCurrentUser();
      if (user) {
        return user.email;
      }
    }
    
    return '';
  }
  
  /**
   * Obtiene el método de autenticación en formato legible
   */
  get authMethodName(): string {
    if (this.authMethod === 'azure') {
      return 'Microsoft Entra ID';
    } else if (this.authMethod === 'local') {
      return 'Autenticación Local';
    }
    return 'No configurado';
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
        
        // Redirigir al dashboard si estamos en una ruta pública (auth-selector o login)
        const currentUrl = this.router.url;
        const publicRoutes = ['/auth-selector', '/login', '/'];
        
        if (publicRoutes.some(route => currentUrl === route || currentUrl.startsWith(route + '?'))) {
          console.log('🔀 [AppComponent] Permisos cargados, redirigiendo al dashboard...');
          this.router.navigate(['/mis-permisos']);
        }
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
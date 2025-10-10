import { Injectable } from '@angular/core';
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  Router,
  UrlTree
} from '@angular/router';
import { Observable } from 'rxjs';
import { MsalService } from '@azure/msal-angular';
import { LocalAuthService } from '../services/local-auth.service';
import { AuthConfigService } from '../services/auth-config.service';

/**
 * Guard de Autenticación Combinado
 * Verifica autenticación según el método activo (Azure AD o JWT Local)
 */
@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private msalService: MsalService,
    private localAuthService: LocalAuthService,
    private authConfigService: AuthConfigService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    
    const authMethod = this.authConfigService.getActiveAuthMethod();
    
    console.log('🛡️ [AuthGuard] Verificando acceso a:', state.url);
    console.log('🔐 [AuthGuard] Método de autenticación activo:', authMethod);
    
    // Verificar según el método de autenticación activo
    if (authMethod === 'azure') {
      // Verificar autenticación con Azure AD
      const isAuthenticated = this.msalService.instance.getAllAccounts().length > 0;
      console.log('🔐 [AuthGuard] Azure AD - Autenticado:', isAuthenticated);
      
      if (!isAuthenticated) {
        console.log('❌ [AuthGuard] No autenticado con Azure, iniciando login...');
        this.msalService.loginRedirect();
        return false;
      }
      
      return true;
      
    } else if (authMethod === 'local') {
      // Verificar autenticación con JWT Local
      const isAuthenticated = this.localAuthService.isAuthenticated();
      console.log('🔐 [AuthGuard] JWT Local - Autenticado:', isAuthenticated);
      
      if (!isAuthenticated) {
        console.log('❌ [AuthGuard] No autenticado con JWT Local, redirigiendo a login...');
        return this.router.createUrlTree(['/login'], {
          queryParams: { returnUrl: state.url }
        });
      }
      
      return true;
      
    } else {
      // Ningún método de autenticación activo
      console.error('❌ [AuthGuard] No hay métodos de autenticación activos');
      return this.router.createUrlTree(['/auth-selector']);
    }
  }
}


import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { LocalAuthService } from '../services/local-auth.service';
import { AuthConfigService } from '../services/auth-config.service';

/**
 * Interceptor para Autenticación JWT Local
 * Agrega el token JWT a las peticiones HTTP cuando la autenticación local está activa
 */
@Injectable()
export class JwtAuthInterceptor implements HttpInterceptor {

  constructor(
    private localAuthService: LocalAuthService,
    private authConfigService: AuthConfigService,
    private router: Router
  ) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    
    // Verificar si JWT Local está habilitado
    const authMethod = this.authConfigService.getActiveAuthMethod();
    
    // Endpoints públicos que NO necesitan token
    const publicEndpoints = [
      '/auth/login',
      '/auth/local/login',
      '/auth/generate-hash-temp',
      '/config/auth/status',
      '/config/auth/config/admin'
    ];
    
    // Verificar si la URL es pública
    const isPublicEndpoint = publicEndpoints.some(endpoint => 
      request.url.includes(endpoint)
    );
    
    // Solo agregar token si JWT Local está activo y NO es un endpoint público
    if (authMethod === 'local' && !isPublicEndpoint) {
      const token = this.localAuthService.getToken();
      
      if (token) {
        // Agregar el token al header Authorization
        request = request.clone({
          setHeaders: {
            Authorization: token // El token ya viene en formato "Bearer xxx"
          }
        });
        
        console.log('🔑 [JwtInterceptor] Token agregado a la petición:', request.url);
        console.log('🔑 [JwtInterceptor] Token (primeros 50 chars):', token.substring(0, 50) + '...');
        console.log('🔑 [JwtInterceptor] Header Authorization:', request.headers.get('Authorization')?.substring(0, 50) + '...');
      }
    } else if (isPublicEndpoint) {
      console.log('🔓 [JwtInterceptor] Endpoint público, no se agrega token:', request.url);
    }

    // Log antes de enviar la petición
    console.log('📤 [JwtInterceptor] Enviando petición:', request.method, request.url);
    
    // Manejar errores
    return next.handle(request).pipe(
      tap({
        next: (event) => {
          if (event.type === 4) { // HttpResponse
            console.log('✅ [JwtInterceptor] Respuesta recibida:', request.url);
          }
        },
        error: (error) => {
          console.error('🚨 [JwtInterceptor] Error en petición:', {
            url: request.url,
            error: error
          });
        }
      }),
      catchError((error: HttpErrorResponse) => {
        console.log('🔍 [JwtInterceptor] Error detectado:', {
          status: error.status,
          url: request.url,
          authMethod: authMethod,
          isPublicEndpoint: isPublicEndpoint
        });
        
        // Si es error 401 o 403 y estamos usando JWT local (y NO es un endpoint público)
        if (authMethod === 'local' && !isPublicEndpoint && (error.status === 401 || error.status === 403)) {
          console.error('❌ [JwtInterceptor] Error de autenticación:', error.status);
          console.error('🔐 [JwtInterceptor] Token expirado o inválido');
          console.error('🧹 [JwtInterceptor] Limpiando sesión y redirigiendo al login...');
          
          // Limpiar TODA la sesión
          this.localAuthService.logout();
          
          // Limpiar también sessionStorage
          sessionStorage.clear();
          
          // Limpiar cualquier configuración de autenticación antigua
          localStorage.removeItem('auth_config');
          
          console.log('✅ [JwtInterceptor] Sesión limpiada completamente');
          
          // Redirigir al login
          this.router.navigate(['/login']).then(() => {
            console.log('🔄 [JwtInterceptor] Redirigido al login');
          });
        }
        
        return throwError(() => error);
      })
    );
  }
}


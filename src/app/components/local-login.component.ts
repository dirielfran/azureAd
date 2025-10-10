import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LocalAuthService } from '../services/local-auth.service';
import { AuthorizationService } from '../services/authorization.service';

/**
 * Componente de Login Local
 * Formulario de autenticación con usuario y contraseña
 */
@Component({
  selector: 'app-local-login',
  templateUrl: './local-login.component.html',
  styleUrls: ['./local-login.component.scss']
})
export class LocalLoginComponent implements OnInit {
  
  email: string = '';
  password: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';
  showPassword: boolean = false;

  constructor(
    private localAuthService: LocalAuthService,
    private authorizationService: AuthorizationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    console.log('🔐 [LocalLogin] Componente inicializado');
    
    // Si ya está autenticado, redirigir al home
    if (this.localAuthService.isAuthenticated()) {
      console.log('✅ [LocalLogin] Usuario ya autenticado, redirigiendo...');
      this.router.navigate(['/']);
    }
  }

  /**
   * Maneja el envío del formulario de login
   */
  onSubmit(): void {
    // Validar campos
    if (!this.email || !this.password) {
      this.errorMessage = 'Por favor, ingresa tu email y contraseña';
      return;
    }

    // Validar formato de email
    if (!this.isValidEmail(this.email)) {
      this.errorMessage = 'Por favor, ingresa un email válido';
      return;
    }

    this.login();
  }

  /**
   * Realiza el login con las credenciales
   */
  private login(): void {
    this.isLoading = true;
    this.errorMessage = '';

    console.log('🔐 [LocalLogin] Intentando autenticar:', this.email);

    this.localAuthService.login(this.email, this.password).subscribe({
      next: (response) => {
        console.log('✅ [LocalLogin] Autenticación exitosa');
        console.log('🎫 [LocalLogin] Respuesta recibida:', response);
        console.log('🔑 [LocalLogin] ¿Usuario autenticado?', this.localAuthService.isAuthenticated());
        
        // Cargar permisos del usuario
        console.log('📞 [LocalLogin] Llamando a loadUserPermissions()...');
        this.loadUserPermissions();
      },
      error: (error) => {
        console.error('❌ [LocalLogin] Error en autenticación:', error);
        this.isLoading = false;
        
        // Manejar diferentes tipos de errores
        if (error.status === 401 || error.status === 403) {
          this.errorMessage = 'Usuario o contraseña incorrectos';
        } else if (error.status === 0) {
          this.errorMessage = 'No se pudo conectar con el servidor';
        } else {
          this.errorMessage = error.error?.message || 'Error en la autenticación';
        }
      }
    });
  }

  /**
   * Carga los permisos del usuario después de autenticar
   */
  private loadUserPermissions(): void {
    console.log('🔑 [LocalLogin] Cargando permisos del usuario...');
    
    this.authorizationService.initializeUserPermissions().subscribe({
      next: (userInfo) => {
        console.log('✅ [LocalLogin] Permisos cargados:', userInfo);
        this.isLoading = false;
        
        // Redirigir al home
        console.log('🔀 [LocalLogin] Redirigiendo al home...');
        this.router.navigate(['/']);
      },
      error: (error) => {
        console.error('❌ [LocalLogin] Error al cargar permisos:', error);
        this.isLoading = false;
        this.errorMessage = 'Autenticación exitosa, pero error al cargar permisos';
      }
    });
  }

  /**
   * Valida formato de email
   */
  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  /**
   * Alterna la visibilidad de la contraseña
   */
  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  /**
   * Navega de vuelta al selector de autenticación
   */
  goBack(): void {
    this.router.navigate(['/auth-selector']);
  }

  /**
   * Limpia el mensaje de error
   */
  clearError(): void {
    this.errorMessage = '';
  }
}


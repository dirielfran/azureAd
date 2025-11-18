import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PasswordResetService } from '../services/password-reset.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {
  
  token: string = '';
  newPassword: string = '';
  confirmPassword: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';
  showPassword: boolean = false;
  showConfirmPassword: boolean = false;
  tokenValid: boolean = false;
  tokenValidated: boolean = false;

  constructor(
    private passwordResetService: PasswordResetService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    // Obtener token de la URL
    this.route.queryParams.subscribe(params => {
      this.token = params['token'] || '';
      
      if (this.token) {
        this.validarToken();
      } else {
        this.errorMessage = 'Token de recuperación no proporcionado';
        this.tokenValidated = true;
      }
    });
  }

  validarToken(): void {
    if (!this.token) {
      return;
    }

    this.isLoading = true;
    this.passwordResetService.validarToken(this.token).subscribe({
      next: (response) => {
        this.tokenValid = response.valid;
        this.tokenValidated = true;
        this.isLoading = false;
        
        if (!this.tokenValid) {
          this.errorMessage = 'El token de recuperación es inválido o ha expirado. Por favor solicita uno nuevo.';
        }
      },
      error: (error) => {
        console.error('❌ [ResetPassword] Error al validar token:', error);
        this.tokenValid = false;
        this.tokenValidated = true;
        this.isLoading = false;
        this.errorMessage = 'Error al validar el token. Por favor intenta nuevamente.';
      }
    });
  }

  onSubmit(): void {
    // Validaciones
    if (!this.newPassword || this.newPassword.trim().length < 6) {
      this.errorMessage = 'La contraseña debe tener al menos 6 caracteres';
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Las contraseñas no coinciden';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.passwordResetService.resetearPassword(this.token, this.newPassword).subscribe({
      next: (response) => {
        console.log('✅ [ResetPassword] Contraseña actualizada:', response);
        this.isLoading = false;
        this.successMessage = response.message || 'Contraseña actualizada exitosamente';
        
        // Redirigir al login después de 3 segundos
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 3000);
      },
      error: (error) => {
        console.error('❌ [ResetPassword] Error:', error);
        this.isLoading = false;
        this.errorMessage = error.error?.error || 'Error al actualizar la contraseña. Por favor intenta nuevamente.';
      }
    });
  }

  goToForgotPassword(): void {
    this.router.navigate(['/forgot-password']);
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  clearError(): void {
    this.errorMessage = '';
  }

  clearSuccess(): void {
    this.successMessage = '';
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }
}



import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PasswordResetService } from '../services/password-reset.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss']
})
export class ForgotPasswordComponent implements OnInit {
  
  email: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';
  showPassword: boolean = false;

  constructor(
    private passwordResetService: PasswordResetService,
    private router: Router
  ) { }

  ngOnInit(): void {
  }

  onSubmit(): void {
    if (!this.email || !this.email.trim()) {
      this.errorMessage = 'Por favor ingresa tu email';
      return;
    }

    if (!this.isValidEmail(this.email)) {
      this.errorMessage = 'Por favor ingresa un email válido';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.passwordResetService.solicitarRecuperacion(this.email.trim()).subscribe({
      next: (response) => {
        console.log('✅ [ForgotPassword] Solicitud procesada:', response);
        this.isLoading = false;
        this.successMessage = response.message || 'Si el email existe en nuestro sistema, recibirás un enlace de recuperación';
        this.email = ''; // Limpiar campo
      },
      error: (error) => {
        console.error('❌ [ForgotPassword] Error:', error);
        this.isLoading = false;
        // Por seguridad, mostrar mensaje genérico
        this.successMessage = 'Si el email existe en nuestro sistema, recibirás un enlace de recuperación';
        this.errorMessage = '';
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/login']);
  }

  clearError(): void {
    this.errorMessage = '';
  }

  clearSuccess(): void {
    this.successMessage = '';
  }

  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }
}








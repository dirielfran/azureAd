import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

/**
 * Interfaz para respuesta de solicitud de recuperación
 */
export interface ForgotPasswordResponse {
  message: string;
}

/**
 * Interfaz para respuesta de reseteo de contraseña
 */
export interface ResetPasswordResponse {
  message?: string;
  error?: string;
}

/**
 * Interfaz para validación de token
 */
export interface ValidateTokenResponse {
  valid: boolean;
}

/**
 * Servicio para recuperación de contraseña
 */
@Injectable({
  providedIn: 'root'
})
export class PasswordResetService {

  private readonly API_URL = environment.apiUrl || 'http://localhost:8080/api';

  constructor(private http: HttpClient) {
    console.log('🔧 PasswordResetService inicializado');
  }

  /**
   * Solicita recuperación de contraseña
   * @param email Email del usuario
   */
  solicitarRecuperacion(email: string): Observable<ForgotPasswordResponse> {
    console.log('📧 [PasswordReset] Solicitando recuperación para:', email);
    
    return this.http.post<ForgotPasswordResponse>(
      `${this.API_URL}/auth/local/forgot-password`,
      { email }
    );
  }

  /**
   * Resetea la contraseña con un token
   * @param token Token de recuperación
   * @param newPassword Nueva contraseña
   */
  resetearPassword(token: string, newPassword: string): Observable<ResetPasswordResponse> {
    console.log('🔄 [PasswordReset] Reseteando contraseña');
    
    return this.http.post<ResetPasswordResponse>(
      `${this.API_URL}/auth/local/reset-password`,
      { token, newPassword }
    );
  }

  /**
   * Valida si un token de recuperación es válido
   * @param token Token a validar
   */
  validarToken(token: string): Observable<ValidateTokenResponse> {
    console.log('🔍 [PasswordReset] Validando token');
    
    return this.http.post<ValidateTokenResponse>(
      `${this.API_URL}/auth/local/validate-reset-token`,
      { token }
    );
  }
}








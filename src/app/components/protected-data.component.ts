import { Component, OnInit } from '@angular/core';
import { ApiService } from '../services/api.service';
import { MsalService } from '@azure/msal-angular';
import { AuthConfigService } from '../services/auth-config.service';
import { LocalAuthService } from '../services/local-auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-protected-data',
  template: `
    <div class="protected-data-container">
      <h2>Datos de API Protegida</h2>
      
      <div class="auth-status" [ngClass]="{'authenticated': isAuthenticated, 'not-authenticated': !isAuthenticated}">
        <p>Estado de autenticación: 
          <strong>{{ isAuthenticated ? 'Autenticado' : 'No autenticado' }}</strong>
        </p>
      </div>

      <div class="actions" *ngIf="isAuthenticated">
        <button (click)="getUserProfile()" [disabled]="loading" class="btn btn-primary">
          {{ loading ? 'Cargando...' : 'Obtener Perfil de Usuario' }}
        </button>
        
        <button (click)="getProtectedData()" [disabled]="loading" class="btn btn-secondary">
          {{ loading ? 'Cargando...' : 'Obtener Datos Protegidos' }}
        </button>

        <button (click)="getDashboardData()" [disabled]="loading" class="btn btn-success">
          {{ loading ? 'Cargando...' : 'Obtener Dashboard' }}
        </button>

        <button (click)="getApiInfo()" [disabled]="loading" class="btn btn-warning">
          {{ loading ? 'Cargando...' : 'Info de la API' }}
        </button>

        <button (click)="getAccessToken()" [disabled]="loading" class="btn btn-info">
          {{ loading ? 'Cargando...' : 'Ver Token de Acceso' }}
        </button>

        <button (click)="debugAuth()" [disabled]="loading" class="btn btn-danger">
          {{ loading ? 'Cargando...' : 'Diagnosticar Autenticación' }}
        </button>
      </div>

      <div class="login-prompt" *ngIf="!isAuthenticated">
        <p>Por favor, inicia sesión para acceder a los datos protegidos.</p>
        <button (click)="login()" class="btn btn-primary">Iniciar Sesión</button>
      </div>

      <div class="results" *ngIf="data || error">
        <div class="success" *ngIf="data && !error">
          <h3>Datos obtenidos exitosamente:</h3>
          <pre>{{ data | json }}</pre>
        </div>

        <div class="error" *ngIf="error">
          <h3>Error:</h3>
          <p class="error-message">{{ error }}</p>
        </div>
      </div>

      <div class="token-info" *ngIf="accessToken">
        <h3>🔑 Token de Acceso JWT</h3>
        <div class="token-preview">
          <p><strong>Primeros 50 caracteres:</strong></p>
          <p class="token">{{ accessToken.substring(0, 50) }}...</p>
        </div>
        <div class="token-actions">
          <button (click)="showFullToken()" class="btn btn-sm btn-outline">
            {{ showToken ? 'Ocultar Token Completo' : 'Mostrar Token Completo' }}
          </button>
          <button (click)="copyTokenToClipboard()" class="btn btn-sm btn-outline">
            📋 Copiar Token
          </button>
          <button (click)="decodeToken()" class="btn btn-sm btn-outline">
            🔍 Decodificar JWT
          </button>
        </div>
        <div class="full-token" *ngIf="showToken">
          <p><strong>Token completo:</strong></p>
          <textarea class="token-textarea" readonly>{{ accessToken }}</textarea>
        </div>
        <div class="decoded-token" *ngIf="decodedToken">
          <p><strong>Token decodificado:</strong></p>
          <pre>{{ decodedToken | json }}</pre>
        </div>
        <small class="token-note">
          💡 El token también se muestra en la consola del navegador para debugging
        </small>
      </div>
    </div>
  `,
  styles: [`
    .protected-data-container {
      max-width: 800px;
      margin: 20px auto;
      padding: 20px;
      font-family: Arial, sans-serif;
    }

    .auth-status {
      padding: 10px;
      margin: 10px 0;
      border-radius: 5px;
    }

    .authenticated {
      background-color: #d4edda;
      border: 1px solid #c3e6cb;
      color: #155724;
    }

    .not-authenticated {
      background-color: #f8d7da;
      border: 1px solid #f5c6cb;
      color: #721c24;
    }

    .actions {
      margin: 20px 0;
    }

    .btn {
      padding: 10px 15px;
      margin: 5px;
      border: none;
      border-radius: 5px;
      cursor: pointer;
      font-size: 14px;
    }

    .btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .btn-primary {
      background-color: #007bff;
      color: white;
    }

    .btn-secondary {
      background-color: #6c757d;
      color: white;
    }

    .btn-info {
      background-color: #17a2b8;
      color: white;
    }

    .btn-success {
      background-color: #28a745;
      color: white;
    }

    .btn-warning {
      background-color: #ffc107;
      color: #212529;
    }

    .btn-danger {
      background-color: #dc3545;
      color: white;
    }

    .btn:hover:not(:disabled) {
      opacity: 0.8;
    }

    .results {
      margin: 20px 0;
    }

    .success {
      background-color: #d4edda;
      border: 1px solid #c3e6cb;
      padding: 15px;
      border-radius: 5px;
    }

    .error {
      background-color: #f8d7da;
      border: 1px solid #f5c6cb;
      padding: 15px;
      border-radius: 5px;
    }

    .error-message {
      color: #721c24;
      font-weight: bold;
    }

    pre {
      background-color: #f8f9fa;
      padding: 10px;
      border-radius: 3px;
      overflow-x: auto;
      white-space: pre-wrap;
    }

    .token-info {
      background-color: #e2e3e5;
      padding: 15px;
      border-radius: 5px;
      margin: 20px 0;
    }

    .token {
      font-family: monospace;
      background-color: #f8f9fa;
      padding: 5px;
      border-radius: 3px;
    }

    .login-prompt {
      text-align: center;
      padding: 20px;
      background-color: #fff3cd;
      border: 1px solid #ffeaa7;
      border-radius: 5px;
    }

    .token-actions {
      margin: 10px 0;
    }

    .btn-sm {
      padding: 5px 10px;
      font-size: 12px;
    }

    .btn-outline {
      background-color: transparent;
      border: 1px solid #007bff;
      color: #007bff;
    }

    .btn-outline:hover {
      background-color: #007bff;
      color: white;
    }

    .token-textarea {
      width: 100%;
      height: 100px;
      font-family: monospace;
      font-size: 11px;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 3px;
      resize: vertical;
    }

    .full-token, .decoded-token {
      margin: 15px 0;
      padding: 10px;
      background-color: #f8f9fa;
      border-radius: 5px;
      border: 1px solid #dee2e6;
    }

    .token-note {
      color: #6c757d;
      font-style: italic;
    }
  `]
})
export class ProtectedDataComponent implements OnInit {
  data: any = null;
  error: string = '';
  loading: boolean = false;
  accessToken: string = '';
  showToken: boolean = false;
  decodedToken: any = null;

  constructor(
    private apiService: ApiService,
    private msalService: MsalService,
    private authConfigService: AuthConfigService,
    private localAuthService: LocalAuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Puedes cargar datos automáticamente si el usuario ya está autenticado
    // if (this.isAuthenticated) {
    //   this.getUserProfile();
    // }
  }

  get isAuthenticated(): boolean {
    // Verificar autenticación según el método activo
    const authMethod = this.authConfigService.getActiveAuthMethod();
    
    if (authMethod === 'azure') {
      return this.msalService.instance.getAllAccounts().length > 0;
    } else if (authMethod === 'local') {
      return this.localAuthService.isAuthenticated();
    }
    
    return false;
  }

  login() {
    // Iniciar sesión según el método activo
    const authMethod = this.authConfigService.getActiveAuthMethod();
    
    if (authMethod === 'azure') {
      console.log('🌐 [ProtectedData] Redirigiendo a Microsoft Entra ID...');
      this.msalService.loginRedirect();
    } else if (authMethod === 'local') {
      console.log('🔐 [ProtectedData] Redirigiendo al login local...');
      this.router.navigate(['/login']);
    } else {
      console.log('⚠️ [ProtectedData] No hay método de autenticación configurado');
      this.router.navigate(['/auth-selector']);
    }
  }

  async getUserProfile() {
    this.loading = true;
    this.error = '';
    this.data = null;

    try {
      this.apiService.getUserProfile().subscribe({
        next: (profile) => {
          this.data = profile;
          this.loading = false;
          console.log('Perfil de usuario:', profile);
        },
        error: (err) => {
          this.error = err.message;
          this.loading = false;
          console.error('Error obteniendo perfil:', err);
        }
      });
    } catch (err: any) {
      this.error = err.message || 'Error desconocido';
      this.loading = false;
    }
  }

  async getProtectedData() {
    this.loading = true;
    this.error = '';
    this.data = null;

    try {
      // Llamada a tu API Spring Boot
      this.apiService.getData('/data').subscribe({
        next: (response) => {
          this.data = response;
          this.loading = false;
          console.log('Datos protegidos:', response);
        },
        error: (err) => {
          this.error = err.message;
          this.loading = false;
          console.error('Error obteniendo datos protegidos:', err);
        }
      });
    } catch (err: any) {
      this.error = err.message || 'Error desconocido';
      this.loading = false;
    }
  }

  async getDashboardData() {
    this.loading = true;
    this.error = '';
    this.data = null;

    try {
      this.apiService.getData('/data/dashboard').subscribe({
        next: (response) => {
          this.data = response;
          this.loading = false;
          console.log('Datos del dashboard:', response);
        },
        error: (err) => {
          this.error = err.message;
          this.loading = false;
          console.error('Error obteniendo dashboard:', err);
        }
      });
    } catch (err: any) {
      this.error = err.message || 'Error desconocido';
      this.loading = false;
    }
  }

  async getApiInfo() {
    this.loading = true;
    this.error = '';
    this.data = null;

    try {
      this.apiService.getData('/auth/info').subscribe({
        next: (response) => {
          this.data = response;
          this.loading = false;
          console.log('Información de la API:', response);
        },
        error: (err) => {
          this.error = err.message;
          this.loading = false;
          console.error('Error obteniendo info de API:', err);
        }
      });
    } catch (err: any) {
      this.error = err.message || 'Error desconocido';
      this.loading = false;
    }
  }

  async debugAuth() {
    this.loading = true;
    this.error = '';
    this.data = null;

    try {
      // Llamada al endpoint de diagnóstico
      this.apiService.getData('/data/debug-auth').subscribe({
        next: (response) => {
          this.data = response;
          this.loading = false;
          console.log('🔍 Diagnóstico de autenticación:', response);
          
          // Mostrar información relevante en la consola
          console.log('🔑 Authorities:', response.authorities);
          console.log('🏢 Groups claim:', response.groups_claim);
          console.log('🎭 Roles claim:', response.roles_claim);
          console.log('📋 Scopes:', response.scopes);
        },
        error: (err) => {
          this.error = err.message;
          this.loading = false;
          console.error('❌ Error en diagnóstico:', err);
        }
      });
    } catch (err: any) {
      this.error = err.message || 'Error desconocido';
      this.loading = false;
    }
  }

  async getAccessToken() {
    this.loading = true;
    this.error = '';
    this.data = null;
    this.showToken = false;
    this.decodedToken = null;

    try {
      const token = await this.apiService.getAccessToken();
      if (token) {
        this.accessToken = token;
        console.log('Token de acceso completo:', token);
      } else {
        this.error = 'No se pudo obtener el token de acceso';
      }
      this.loading = false;
    } catch (err: any) {
      this.error = err.message || 'Error obteniendo token';
      this.loading = false;
    }
  }

  // Métodos adicionales para demostrar otras operaciones
  async createData() {
    this.loading = true;
    this.error = '';

    const newData = {
      name: 'Nuevo elemento',
      description: 'Creado desde Angular',
      timestamp: new Date().toISOString()
    };

    try {
      this.apiService.postData('/items', newData).subscribe({
        next: (response) => {
          this.data = response;
          this.loading = false;
          console.log('Datos creados:', response);
        },
        error: (err) => {
          this.error = err.message;
          this.loading = false;
          console.error('Error creando datos:', err);
        }
      });
    } catch (err: any) {
      this.error = err.message || 'Error desconocido';
      this.loading = false;
    }
  }

  // Nuevos métodos para manejo de token
  showFullToken() {
    this.showToken = !this.showToken;
  }

  async copyTokenToClipboard() {
    try {
      await navigator.clipboard.writeText(this.accessToken);
      console.log('Token copiado al portapapeles');
      // Podrías agregar una notificación visual aquí
    } catch (err) {
      console.error('Error copiando token:', err);
      // Fallback para navegadores que no soportan clipboard API
      this.fallbackCopyTextToClipboard(this.accessToken);
    }
  }

  private fallbackCopyTextToClipboard(text: string) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.left = '-999999px';
    textArea.style.top = '-999999px';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    
    try {
      document.execCommand('copy');
      console.log('Token copiado al portapapeles (fallback)');
    } catch (err) {
      console.error('Error copiando token (fallback):', err);
    }
    
    document.body.removeChild(textArea);
  }

  decodeToken() {
    if (!this.accessToken) {
      this.error = 'No hay token para decodificar';
      return;
    }

    try {
      // Remover "Bearer " si existe
      let token = this.accessToken.trim();
      if (token.startsWith('Bearer ')) {
        token = token.substring(7);
      }
      
      // Decodificar JWT (solo el payload, sin verificar firma)
      const parts = token.split('.');
      if (parts.length !== 3) {
        throw new Error('Token JWT inválido - debe tener 3 partes');
      }

      // Función para decodificar Base64URL (JWT usa Base64URL, no Base64 estándar)
      const base64UrlDecode = (str: string): string => {
        // Convertir Base64URL a Base64 estándar
        let base64 = str.replace(/-/g, '+').replace(/_/g, '/');
        // Agregar padding si es necesario
        const pad = base64.length % 4;
        if (pad) {
          if (pad === 1) {
            throw new Error('Base64URL inválido');
          }
          base64 += new Array(5 - pad).join('=');
        }
        return atob(base64);
      };

      // Decodificar header
      const header = JSON.parse(base64UrlDecode(parts[0]));
      
      // Decodificar payload
      const payload = JSON.parse(base64UrlDecode(parts[1]));

      this.decodedToken = {
        header: header,
        payload: payload,
        signature: parts[2],
        info: {
          issued: new Date(payload.iat * 1000).toLocaleString(),
          expires: new Date(payload.exp * 1000).toLocaleString(),
          issuer: payload.iss,
          audience: payload.aud,
          subject: payload.sub,
          scopes: payload.scp || payload.scope || payload.authorities || 'No disponible'
        }
      };

      console.log('✅ Token decodificado correctamente:', this.decodedToken);
    } catch (err: any) {
      console.error('❌ Error decodificando token:', err);
      this.error = `Error decodificando el token JWT: ${err.message}`;
    }
  }
}

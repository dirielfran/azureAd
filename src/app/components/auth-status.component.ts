import { Component, OnInit } from '@angular/core';
import { MsalService } from '@azure/msal-angular';
import { ApiService } from '../services/api.service';
import { AuthorizationService } from '../services/authorization.service';

@Component({
  selector: 'app-auth-status',
  template: `
    <div class="auth-status-container">
      <h3>🔍 Estado de Autenticación</h3>
      
      <div class="status-grid">
        <div class="status-item" [ngClass]="{'success': msalAuthenticated, 'error': !msalAuthenticated}">
          <h4>MSAL Authentication</h4>
          <p>{{ msalAuthenticated ? '✅ Autenticado' : '❌ No autenticado' }}</p>
          <small>Verifica tokens en localStorage</small>
        </div>
        
        <div class="status-item" [ngClass]="{'success': apiAuthenticated, 'error': !apiAuthenticated}">
          <h4>API Service</h4>
          <p>{{ apiAuthenticated ? '✅ Autenticado' : '❌ No autenticado' }}</p>
          <small>Método isAuthenticated()</small>
        </div>
        
        <div class="status-item" [ngClass]="{'success': isAuthorized, 'error': !isAuthorized}">
          <h4>Authorization Service</h4>
          <p>{{ isAuthorized ? '✅ Autorizado' : '❌ No autorizado' }}</p>
          <small>Verifica permisos cargados</small>
        </div>
        
        <div class="status-item" [ngClass]="{'success': fullyAuthenticated, 'error': !fullyAuthenticated}">
          <h4>Estado Completo</h4>
          <p>{{ fullyAuthenticated ? '✅ Completo' : '❌ Incompleto' }}</p>
          <small>Autenticado + Autorizado</small>
        </div>
      </div>
      
      <div class="user-info" *ngIf="msalAuthenticated">
        <h4>👤 Información del Usuario</h4>
        <p><strong>Nombre:</strong> {{ userName }}</p>
        <p><strong>Email:</strong> {{ userEmail }}</p>
        <p><strong>Cuentas MSAL:</strong> {{ accountsCount }}</p>
      </div>
      
      <div class="permissions-info" *ngIf="isAuthorized">
        <h4>🔑 Permisos del Usuario</h4>
        <p><strong>Perfiles:</strong> {{ profilesCount }}</p>
        <p><strong>Permisos:</strong> {{ permissionsCount }}</p>
        <div class="permissions-list">
          <span *ngFor="let permission of permissions" class="permission-tag">
            {{ permission }}
          </span>
        </div>
      </div>
      
      <div class="actions">
        <button (click)="refreshStatus()" class="btn btn-primary">
          🔄 Actualizar Estado
        </button>
        <button (click)="showDetailedLogs()" class="btn btn-secondary">
          📋 Ver Logs Detallados
        </button>
      </div>
    </div>
  `,
  styles: [`
    .auth-status-container {
      max-width: 800px;
      margin: 20px auto;
      padding: 20px;
      border: 1px solid #ddd;
      border-radius: 8px;
      background-color: #f9f9f9;
    }
    
    .status-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 15px;
      margin: 20px 0;
    }
    
    .status-item {
      padding: 15px;
      border-radius: 5px;
      text-align: center;
      border: 2px solid transparent;
    }
    
    .status-item.success {
      background-color: #d4edda;
      border-color: #c3e6cb;
      color: #155724;
    }
    
    .status-item.error {
      background-color: #f8d7da;
      border-color: #f5c6cb;
      color: #721c24;
    }
    
    .user-info, .permissions-info {
      margin: 20px 0;
      padding: 15px;
      background-color: #e9ecef;
      border-radius: 5px;
    }
    
    .permissions-list {
      display: flex;
      flex-wrap: wrap;
      gap: 5px;
      margin-top: 10px;
    }
    
    .permission-tag {
      background-color: #007bff;
      color: white;
      padding: 2px 8px;
      border-radius: 12px;
      font-size: 12px;
    }
    
    .actions {
      margin-top: 20px;
      text-align: center;
    }
    
    .btn {
      padding: 10px 20px;
      margin: 5px;
      border: none;
      border-radius: 5px;
      cursor: pointer;
    }
    
    .btn-primary {
      background-color: #007bff;
      color: white;
    }
    
    .btn-secondary {
      background-color: #6c757d;
      color: white;
    }
  `]
})
export class AuthStatusComponent implements OnInit {
  msalAuthenticated = false;
  apiAuthenticated = false;
  isAuthorized = false;
  fullyAuthenticated = false;
  
  userName = '';
  userEmail = '';
  accountsCount = 0;
  profilesCount = 0;
  permissionsCount = 0;
  permissions: string[] = [];

  constructor(
    private msalService: MsalService,
    private apiService: ApiService,
    private authorizationService: AuthorizationService
  ) {}

  ngOnInit() {
    this.refreshStatus();
  }

  refreshStatus() {
    console.log('🔄 [AuthStatusComponent] Actualizando estado de autenticación...');
    
    // 1. Verificación MSAL directa
    const accounts = this.msalService.instance.getAllAccounts();
    this.msalAuthenticated = accounts.length > 0;
    this.accountsCount = accounts.length;
    
    if (this.msalAuthenticated) {
      this.userName = accounts[0].name || 'Usuario';
      this.userEmail = accounts[0].username || '';
    }
    
    console.log('🔍 [AuthStatusComponent] MSAL - Cuentas:', accounts.length);
    console.log('🔍 [AuthStatusComponent] MSAL - Autenticado:', this.msalAuthenticated);
    
    // 2. Verificación API Service
    this.apiAuthenticated = this.apiService.isAuthenticated();
    console.log('🔍 [AuthStatusComponent] API Service - Autenticado:', this.apiAuthenticated);
    
    // 3. Verificación Authorization Service
    this.isAuthorized = this.authorizationService.isAuthorized();
    console.log('🔍 [AuthStatusComponent] Authorization - Autorizado:', this.isAuthorized);
    
    // 4. Estado completo
    this.fullyAuthenticated = this.apiAuthenticated && this.isAuthorized;
    console.log('🔍 [AuthStatusComponent] Estado completo:', this.fullyAuthenticated);
    
    // 5. Información adicional si está autorizado
    if (this.isAuthorized) {
      const userInfo = this.authorizationService.getCurrentUserInfo();
      if (userInfo) {
        this.profilesCount = userInfo.perfiles?.length || 0;
        this.permissionsCount = userInfo.permisos?.length || 0;
        this.permissions = userInfo.codigosPermisos || [];
        
        console.log('📊 [AuthStatusComponent] Perfiles:', this.profilesCount);
        console.log('🔑 [AuthStatusComponent] Permisos:', this.permissionsCount);
        console.log('📋 [AuthStatusComponent] Códigos de permisos:', this.permissions);
      }
    }
    
    console.log('✅ [AuthStatusComponent] Estado actualizado completamente');
  }

  showDetailedLogs() {
    console.log('📋 [AuthStatusComponent] === LOGS DETALLADOS DE AUTENTICACIÓN ===');
    
    // Logs MSAL
    const accounts = this.msalService.instance.getAllAccounts();
    console.log('🔍 [AuthStatusComponent] Cuentas MSAL:', accounts);
    
    // Logs localStorage
    const msalKeys = Object.keys(localStorage).filter(key => key.startsWith('msal.'));
    console.log('🔍 [AuthStatusComponent] Claves MSAL en localStorage:', msalKeys);
    
    // Logs sessionStorage
    const sessionKeys = Object.keys(sessionStorage);
    console.log('🔍 [AuthStatusComponent] Claves en sessionStorage:', sessionKeys);
    
    // Logs AuthorizationService
    const currentPermissions = this.authorizationService.getCurrentPermissions();
    const currentUserInfo = this.authorizationService.getCurrentUserInfo();
    console.log('🔍 [AuthStatusComponent] Permisos actuales:', currentPermissions);
    console.log('🔍 [AuthStatusComponent] Información del usuario:', currentUserInfo);
    
    console.log('📋 [AuthStatusComponent] === FIN DE LOGS DETALLADOS ===');
  }
}

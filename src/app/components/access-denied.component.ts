/**
 * =============================================================================
 * 🚫 ACCESS DENIED COMPONENT - PÁGINA DE ACCESO DENEGADO
 * =============================================================================
 * 
 * Componente que se muestra cuando un usuario intenta acceder a una página
 * para la cual no tiene los permisos necesarios.
 * 
 * @author Sistema de Autorización Angular-Entra
 * @version 1.0.0
 * =============================================================================
 */

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthorizationService } from '../services/authorization.service';

@Component({
  selector: 'app-access-denied',
  template: `
    <div class="access-denied-container">
      <div class="access-denied-card">
        <div class="icon-container">
          <i class="fas fa-shield-alt"></i>
        </div>
        
        <h1>Acceso Denegado</h1>
        
        <p class="message">
          No tienes los permisos necesarios para acceder a esta página.
        </p>
        
        <div class="details" *ngIf="attemptedUrl">
          <p><strong>Página solicitada:</strong> {{ attemptedUrl }}</p>
        </div>
        
        <div class="user-info" *ngIf="userInfo">
          <h3>Tu información actual:</h3>
          <p><strong>Usuario:</strong> {{ userInfo.nombre }} ({{ userInfo.email }})</p>
          
          <div class="profiles" *ngIf="userInfo.perfiles.length > 0">
            <p><strong>Perfiles asignados:</strong></p>
            <ul>
              <li *ngFor="let perfil of userInfo.perfiles">
                {{ perfil.nombre }} - {{ perfil.descripcion }}
              </li>
            </ul>
          </div>
          
          <div class="permissions" *ngIf="userInfo.codigosPermisos.length > 0">
            <p><strong>Permisos disponibles:</strong></p>
            <div class="permission-tags">
              <span 
                *ngFor="let permiso of userInfo.codigosPermisos" 
                class="permission-tag"
              >
                {{ permiso }}
              </span>
            </div>
          </div>
        </div>
        
        <div class="actions">
          <button 
            class="btn btn-primary" 
            (click)="goToDashboard()"
          >
            <i class="fas fa-home"></i>
            Ir al Dashboard
          </button>
          
          <button 
            class="btn btn-secondary" 
            (click)="goBack()"
          >
            <i class="fas fa-arrow-left"></i>
            Volver Atrás
          </button>
          
          <button 
            class="btn btn-info" 
            (click)="refreshPermissions()"
            [disabled]="isRefreshing"
          >
            <i class="fas fa-sync-alt" [class.fa-spin]="isRefreshing"></i>
            {{ isRefreshing ? 'Actualizando...' : 'Actualizar Permisos' }}
          </button>
        </div>
        
        <div class="help-section">
          <p class="help-text">
            Si crees que deberías tener acceso a esta página, contacta al administrador del sistema.
          </p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .access-denied-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 80vh;
      padding: 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    .access-denied-card {
      background: white;
      border-radius: 15px;
      box-shadow: 0 20px 40px rgba(0,0,0,0.1);
      padding: 40px;
      text-align: center;
      max-width: 600px;
      width: 100%;
    }

    .icon-container {
      margin-bottom: 30px;
    }

    .icon-container i {
      font-size: 4rem;
      color: #e74c3c;
    }

    h1 {
      color: #2c3e50;
      margin-bottom: 20px;
      font-size: 2.5rem;
    }

    .message {
      font-size: 1.2rem;
      color: #7f8c8d;
      margin-bottom: 30px;
      line-height: 1.6;
    }

    .details {
      background: #f8f9fa;
      padding: 15px;
      border-radius: 8px;
      margin-bottom: 30px;
      text-align: left;
    }

    .user-info {
      background: #f8f9fa;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 30px;
      text-align: left;
    }

    .user-info h3 {
      margin-top: 0;
      color: #2c3e50;
      border-bottom: 2px solid #3498db;
      padding-bottom: 10px;
    }

    .profiles ul {
      list-style-type: none;
      padding: 0;
    }

    .profiles li {
      background: #e8f4fd;
      padding: 8px 12px;
      margin: 5px 0;
      border-radius: 5px;
      border-left: 4px solid #3498db;
    }

    .permission-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-top: 10px;
    }

    .permission-tag {
      background: #27ae60;
      color: white;
      padding: 4px 8px;
      border-radius: 12px;
      font-size: 0.8rem;
      font-weight: 500;
    }

    .actions {
      display: flex;
      justify-content: center;
      gap: 15px;
      margin-bottom: 30px;
      flex-wrap: wrap;
    }

    .btn {
      padding: 12px 24px;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      font-size: 1rem;
      font-weight: 500;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      transition: all 0.3s ease;
    }

    .btn-primary {
      background: #3498db;
      color: white;
    }

    .btn-primary:hover {
      background: #2980b9;
      transform: translateY(-2px);
    }

    .btn-secondary {
      background: #95a5a6;
      color: white;
    }

    .btn-secondary:hover {
      background: #7f8c8d;
      transform: translateY(-2px);
    }

    .btn-info {
      background: #17a2b8;
      color: white;
    }

    .btn-info:hover:not(:disabled) {
      background: #138496;
      transform: translateY(-2px);
    }

    .btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .help-section {
      border-top: 1px solid #ecf0f1;
      padding-top: 20px;
    }

    .help-text {
      color: #7f8c8d;
      font-style: italic;
      margin: 0;
    }

    @media (max-width: 768px) {
      .access-denied-card {
        padding: 20px;
      }
      
      .actions {
        flex-direction: column;
        align-items: stretch;
      }
      
      .btn {
        justify-content: center;
      }
    }
  `]
})
export class AccessDeniedComponent implements OnInit {
  
  attemptedUrl: string | null = null;
  userInfo: any = null;
  isRefreshing = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authorizationService: AuthorizationService
  ) {}

  ngOnInit(): void {
    // Obtener la URL que se intentó acceder
    this.attemptedUrl = this.route.snapshot.queryParams['returnUrl'] || 
                       sessionStorage.getItem('attempted_url');
    
    // Obtener información del usuario actual
    this.userInfo = this.authorizationService.getCurrentUserInfo();
    
    console.log('🚫 AccessDeniedComponent inicializado:', {
      attemptedUrl: this.attemptedUrl,
      userInfo: this.userInfo
    });
  }

  /**
   * Navega al dashboard principal
   */
  goToDashboard(): void {
    this.router.navigate(['/']);
  }

  /**
   * Vuelve a la página anterior
   */
  goBack(): void {
    window.history.back();
  }

  /**
   * Refresca los permisos del usuario
   */
  refreshPermissions(): void {
    this.isRefreshing = true;
    
    this.authorizationService.refreshPermissions().subscribe({
      next: (userInfo) => {
        this.userInfo = userInfo;
        this.isRefreshing = false;
        
        // Intentar navegar a la URL original si ahora tiene permisos
        if (this.attemptedUrl) {
          setTimeout(() => {
            this.router.navigateByUrl(this.attemptedUrl!);
          }, 1000);
        }
      },
      error: (error) => {
        console.error('Error al refrescar permisos:', error);
        this.isRefreshing = false;
      }
    });
  }
}

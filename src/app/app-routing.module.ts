import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MsalGuard } from '@azure/msal-angular';
import { ProtectedDataComponent } from './components/protected-data.component';
import { AccessDeniedComponent } from './components/access-denied.component';
import { UserPermissionsComponent } from './components/user-permissions.component';
import { AuthStatusComponent } from './components/auth-status.component';
import { AuthSelectorComponent } from './components/auth-selector.component';
import { LocalLoginComponent } from './components/local-login.component';
import { PermissionGuard, AdminGuard, ManagerGuard } from './guards/permission.guard';
import { AuthGuard } from './guards/auth.guard';

const routes: Routes = [
  // Selector de autenticación
  {
    path: 'auth-selector',
    component: AuthSelectorComponent,
    data: {
      title: 'Seleccionar método de autenticación'
    }
  },

  // Login local
  {
    path: 'login',
    component: LocalLoginComponent,
    data: {
      title: 'Iniciar sesión'
    }
  },

  // Ruta principal - redirige al dashboard de permisos
  { 
    path: '', 
    redirectTo: '/mis-permisos', 
    pathMatch: 'full' 
  },
  
  // Dashboard de permisos del usuario
  { 
    path: 'mis-permisos', 
    component: UserPermissionsComponent, 
    canActivate: [AuthGuard],
    data: { 
      title: 'Mis Permisos'
    }
  },
  
  // Estado de autenticación (para debugging)
  { 
    path: 'estado-auth', 
    component: AuthStatusComponent,
    data: { 
      title: 'Estado de Autenticación'
    }
  },
  
  // Componente de datos protegidos (demo)
  { 
    path: 'datos-protegidos', 
    component: ProtectedDataComponent, 
    canActivate: [AuthGuard, PermissionGuard],
    data: { 
      permissions: ['DASHBOARD_LEER'],
      requireAll: false,
      title: 'Datos Protegidos'
    }
  },
  
  // Ejemplo de ruta solo para administradores
  { 
    path: 'admin', 
    component: UserPermissionsComponent, 
    canActivate: [AuthGuard, AdminGuard],
    data: { 
      title: 'Panel de Administración'
    }
  },
  
  // Ejemplo de ruta para gestores
  { 
    path: 'gestion', 
    component: UserPermissionsComponent, 
    canActivate: [AuthGuard, ManagerGuard],
    data: { 
      title: 'Panel de Gestión'
    }
  },
  
  // Ejemplo de rutas con permisos específicos
  { 
    path: 'usuarios', 
    component: UserPermissionsComponent, 
    canActivate: [AuthGuard, PermissionGuard],
    data: { 
      permissions: ['USUARIOS_LEER'],
      title: 'Gestión de Usuarios'
    }
  },
  
  { 
    path: 'reportes', 
    component: UserPermissionsComponent, 
    canActivate: [AuthGuard, PermissionGuard],
    data: { 
      module: 'REPORTES',
      title: 'Reportes del Sistema'
    }
  },
  
  { 
    path: 'configuracion', 
    component: UserPermissionsComponent, 
    canActivate: [AuthGuard, PermissionGuard],
    data: { 
      permissions: ['CONFIG_LEER', 'CONFIG_EDITAR'],
      requireAll: false,
      title: 'Configuración del Sistema'
    }
  },
  
  // Página de acceso denegado
  { 
    path: 'acceso-denegado', 
    component: AccessDeniedComponent,
    data: { 
      title: 'Acceso Denegado'
    }
  },
  
  // Ruta comodín - redirige a página principal
  { 
    path: '**', 
    redirectTo: '/mis-permisos' 
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    initialNavigation: 'enabledBlocking'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
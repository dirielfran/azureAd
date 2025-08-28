import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MsalGuard } from '@azure/msal-angular';
import { ProtectedDataComponent } from './components/protected-data.component';
import { AccessDeniedComponent } from './components/access-denied.component';
import { UserPermissionsComponent } from './components/user-permissions.component';
import { PermissionGuard, AdminGuard, ManagerGuard } from './guards/permission.guard';

const routes: Routes = [
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
    canActivate: [MsalGuard],
    data: { 
      title: 'Mis Permisos'
    }
  },
  
  // Componente de datos protegidos (demo)
  { 
    path: 'datos-protegidos', 
    component: ProtectedDataComponent, 
    canActivate: [MsalGuard, PermissionGuard],
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
    canActivate: [MsalGuard, AdminGuard],
    data: { 
      title: 'Panel de Administración'
    }
  },
  
  // Ejemplo de ruta para gestores
  { 
    path: 'gestion', 
    component: UserPermissionsComponent, 
    canActivate: [MsalGuard, ManagerGuard],
    data: { 
      title: 'Panel de Gestión'
    }
  },
  
  // Ejemplo de rutas con permisos específicos
  { 
    path: 'usuarios', 
    component: UserPermissionsComponent, 
    canActivate: [MsalGuard, PermissionGuard],
    data: { 
      permissions: ['USUARIOS_LEER'],
      title: 'Gestión de Usuarios'
    }
  },
  
  { 
    path: 'reportes', 
    component: UserPermissionsComponent, 
    canActivate: [MsalGuard, PermissionGuard],
    data: { 
      module: 'REPORTES',
      title: 'Reportes del Sistema'
    }
  },
  
  { 
    path: 'configuracion', 
    component: UserPermissionsComponent, 
    canActivate: [MsalGuard, PermissionGuard],
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
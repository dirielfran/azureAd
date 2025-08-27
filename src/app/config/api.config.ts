// Configuración para tu API protegida
export interface ApiConfig {
  baseUrl: string;
  scopes: string[];
  endpoints: {
    [key: string]: string;
  };
}

// Configuración para tu API Spring Boot - YA CONFIGURADA CON TUS CREDENCIALES
export const API_CONFIG: ApiConfig = {
  // URL de tu API Spring Boot local
  baseUrl: 'http://localhost:8080/api',
  
  // Scopes configurados en Azure AD
  scopes: [
    'api://4a12fbd8-bf63-4c12-be4c-9678b207fbe7/access_as_user'
  ],
  
  // Endpoints disponibles en tu API
  endpoints: {
    // Usuarios
    users: '/users',
    userProfile: '/users/mi-perfil',
    userStats: '/users/estadisticas',
    
    // Datos
    data: '/data',
    dashboard: '/data/dashboard',
    slowProcess: '/data/proceso-lento',
    config: '/data/config',
    reports: '/data/reportes',
    health: '/data/health',
    
    // Autenticación
    authInfo: '/auth/info',
    userInfo: '/auth/user-info',
    authTest: '/auth/test',
    tokenClaims: '/auth/token-claims'
  }
};

// Configuración para Microsoft Graph (ya incluida)
export const GRAPH_CONFIG = {
  baseUrl: 'https://graph.microsoft.com/v1.0',
  scopes: ['user.read', 'mail.read', 'calendars.read'],
  endpoints: {
    me: '/me',
    mail: '/me/messages',
    calendar: '/me/events'
  }
};

// Ejemplo de configuración para múltiples APIs
export const API_CONFIGURATIONS = {
  myApi: API_CONFIG,
  microsoftGraph: GRAPH_CONFIG,
  // Puedes agregar más APIs aquí
};

export const environment = {
  production: true,
  msalConfig: {
    auth: {
      clientId: '4a12fbd8-bf63-4c12-be4c-9678b207fbe7', // Tu Client ID real
      authority: 'https://login.microsoftonline.com/f128ae87-3797-42d7-8490-82c6b570f832', // Tu Tenant ID real
      redirectUri: 'https://tu-dominio-produccion.com', // <-- Reemplaza con tu URL de producción
    },
    cache: {
      cacheLocation: 'localStorage',
      storeAuthStateInCookie: false,
    }
  },
  apiConfig: {
    baseUrl: 'https://tu-api-produccion.com/api',
    scopes: ['api://4a12fbd8-bf63-4c12-be4c-9678b207fbe7/access_as_user']
  }
};

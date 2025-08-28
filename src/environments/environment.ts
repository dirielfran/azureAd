export const environment = {
  production: false,
  msalConfig: {
    auth: {
      clientId: '4a12fbd8-bf63-4c12-be4c-9678b207fbe7', // Tu Client ID real
      authority: 'https://login.microsoftonline.com/f128ae87-3797-42d7-8490-82c6b570f832', // Tu Tenant ID real
      redirectUri: 'http://localhost:4200',
    },
    cache: {
      cacheLocation: 'localStorage',
      storeAuthStateInCookie: false,
    }
  },
  apiConfig: {
    baseUrl: 'http://localhost:8080/api',
    scopes: ['api://4a12fbd8-bf63-4c12-be4c-9678b207fbe7/access_as_user']
  }
};
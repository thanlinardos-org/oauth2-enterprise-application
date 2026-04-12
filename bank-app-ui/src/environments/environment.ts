export const environment = {
  production: true,
  apiUrl : "/api",
  uiUrl : process.env["UI_URL"],
  keycloak: {
    config: {
      url: process.env["KEYCLOAK_URL"] ?? '',
      realm: process.env["KEYCLOAK_REALM"] ?? '',
      clientId: process.env["KEYCLOAK_CLIENT_ID"] ?? ''
    }
  }
};

// __APP_DOMAIN__ / __KEYCLOAK_DOMAIN__ are substituted at Docker build time — see
// frontend/Dockerfile. The Angular environment is resolved at build time, not read
// at runtime, so changing these means rebuilding the image.
const APP_DOMAIN = '__APP_DOMAIN__';
const KEYCLOAK_DOMAIN = '__KEYCLOAK_DOMAIN__';

export const environment = {
  production: true,
  baseurl: `https://${APP_DOMAIN}/api/`,
  keycloak: {
    url: `https://${KEYCLOAK_DOMAIN}`,
    realm: 'shop',
    clientId: 'frontend',
  },
};

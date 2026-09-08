import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { inject } from '@angular/core';
import Keycloak from 'keycloak-js';
import { KeycloakReady } from '../keycloak.config';

/**
 * Guards the routes that need an account, and some that need the shop-admin role.
 *
 * Adapted from fanvote-frontend's guard/auth.guard.ts. It awaits {@link KeycloakReady}
 * first: startup does not block on the silent SSO check, so for the first moment of
 * every page load `keycloak.authenticated` is false even for people who are signed
 * in, and a guard reading it at that moment would bounce them to a login form they
 * do not need.
 */
const isAccessAllowed = async (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
): Promise<boolean | UrlTree> => {
  const keycloak = inject(Keycloak);
  const router = inject(Router);
  const ready = inject(KeycloakReady);

  const authenticated = await ready.whenReady;

  if (!authenticated) {
    await keycloak.login({
      redirectUri: window.location.origin + state.url,
    });
    return false;
  }

  const requiredRole = route.data['role'];
  if (!requiredRole) {
    return true;
  }

  // Realm roles, read from the token here rather than the library's snapshot,
  // because that snapshot is taken before this function runs and would be the
  // stale "signed out" one.
  const claims = (keycloak.tokenParsed ?? {}) as any;
  const realmRoles: string[] = claims.realm_access?.roles ?? [];

  if (realmRoles.includes(requiredRole)) {
    return true;
  }

  return router.parseUrl('/forbidden');
};

export const canActivateAuthRole: CanActivateFn = isAccessAllowed;

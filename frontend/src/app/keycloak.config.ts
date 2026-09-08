import {
  provideKeycloak,
  createInterceptorCondition,
  IncludeBearerTokenCondition,
  INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  withAutoRefreshToken,
  AutoRefreshTokenService,
  UserActivityService
} from 'keycloak-angular';
import Keycloak from 'keycloak-js';
import {
  EnvironmentInjector,
  Injectable,
  inject,
  provideAppInitializer,
  runInInjectionContext
} from '@angular/core';
import { environment } from '../environment/environment';

const apiUrlRegex = new RegExp(`${environment.baseurl.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}(\\/.*)?`, 'i');

const apiInterceptorCondition = createInterceptorCondition<IncludeBearerTokenCondition>({
  urlPattern: apiUrlRegex
});

/**
 * Resolves once Keycloak has finished working out whether this browser is signed in.
 *
 * Adapted from fanvote-frontend's keycloak.config.ts: anything that needs a
 * definite answer — the route guards especially — awaits this rather than reading
 * `keycloak.authenticated`, which is simply `false` for the first moment of every
 * page load now that the check no longer blocks startup.
 */
@Injectable({ providedIn: 'root' })
export class KeycloakReady {
  private settle!: (authenticated: boolean) => void;
  private settled = false;

  /** Resolves to whether the visitor turned out to be signed in. Never rejects. */
  readonly whenReady = new Promise<boolean>((resolve) => (this.settle = resolve));

  resolve(authenticated: boolean): void {
    if (this.settled) return;
    this.settled = true;
    this.settle(authenticated);
  }
}

const initOptions = {
  onLoad: 'check-sso' as const,
  checkLoginIframe: false,
  silentCheckSsoRedirectUri: `${window.location.origin}/assets/silent-check-sso.html`,
  redirectUri: `${window.location.origin}/`,
  pkceMethod: 'S256' as const
};

const autoRefreshToken = withAutoRefreshToken({ onInactivityTimeout: 'none', sessionTimeout: 0 });

/**
 * Keycloak, started but not waited for.
 *
 * Not the library's blocking one-liner: `provideKeycloak({ config, initOptions })`
 * would register an app initializer that awaits `keycloak.init()`, delaying first
 * paint on every visit — mostly for visitors who are not signed in. `initOptions`
 * is deliberately absent from provideKeycloak below and `init()` is started here
 * instead, without being awaited. The page paints immediately; the answer arrives a
 * moment later through {@link KeycloakReady} and the KEYCLOAK_EVENT_SIGNAL.
 */
export const provideKeycloakAngular = () => [
  provideKeycloak({
    config: environment.keycloak,
    // No initOptions on purpose — see above. init() is called in the initializer below.
    providers: [
      AutoRefreshTokenService,
      UserActivityService,
      { provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG, useValue: [apiInterceptorCondition] }
    ]
  }),
  provideAppInitializer(() => {
    const injector = inject(EnvironmentInjector);
    const keycloak = inject(Keycloak);
    const ready = inject(KeycloakReady);

    runInInjectionContext(injector, () => autoRefreshToken.configure());

    // Started, and deliberately not returned. Returning the promise here would
    // restore exactly the blocking behaviour this exists to remove.
    keycloak
      .init(initOptions)
      .then((authenticated) => ready.resolve(authenticated))
      .catch((error) => {
        // A failed check means signed out, not broken. The site is usable either
        // way, and a guard that never resolves would be far worse than one that
        // says no.
        console.error('Keycloak initialisation failed — continuing as signed out', error);
        ready.resolve(false);
      });
  })
];

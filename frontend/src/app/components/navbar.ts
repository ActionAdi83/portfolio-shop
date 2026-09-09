import { Component, effect, inject, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import Keycloak from 'keycloak-js';
import { KEYCLOAK_EVENT_SIGNAL, KeycloakEventType, typeEventArgs, ReadyArgs } from 'keycloak-angular';
import { CartService } from '../services/cart.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="sticky top-0 z-50 bg-brand text-white shadow-sm">
      <nav class="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between gap-4">
        <a routerLink="/" class="flex items-center gap-2 font-bold text-lg shrink-0">
          <span>Portfolio Shop</span>
        </a>

        <div class="flex items-center gap-1 sm:gap-4 text-sm font-medium">
          <a routerLink="/products" routerLinkActive="underline" class="px-2 py-2 hover:opacity-80">Products</a>

          <a routerLink="/cart" routerLinkActive="underline" class="relative px-2 py-2 hover:opacity-80">
            Cart
            @if (cart.totalItems() > 0) {
              <span class="absolute -top-0.5 -right-1 min-w-[1.1rem] h-[1.1rem] px-1 grid place-items-center rounded-full bg-brand-accent text-white text-[10px] font-bold leading-none">
                {{ cart.totalItems() }}
              </span>
            }
          </a>

          @if (authenticated) {
            <a routerLink="/account" routerLinkActive="underline" class="px-2 py-2 hover:opacity-80">Account</a>
            <a routerLink="/admin" routerLinkActive="underline" class="px-2 py-2 hover:opacity-80">Admin</a>
            @if (!isAdmin) {
              <span class="px-2 py-1 rounded bg-white/15 text-[10px] font-bold uppercase tracking-wide">Read-only</span>
            }
            <button (click)="logout()" class="px-3 py-1.5 rounded-md bg-white/15 hover:bg-white/25 cursor-pointer">
              Sign out
            </button>
          } @else {
            <button (click)="login()" class="px-3 py-1.5 rounded-md bg-white/15 hover:bg-white/25 cursor-pointer">
              Sign in
            </button>
          }
        </div>
      </nav>
    </header>
  `,
})
export class NavbarComponent {
  authenticated = false;
  readonly cart = inject(CartService);

  private readonly keycloak = inject(Keycloak);
  private readonly keycloakSignal = inject(KEYCLOAK_EVENT_SIGNAL);

  /** Realm roles, straight off the token — the same claim the gateway enforces on. */
  private get realmRoles(): string[] {
    return (this.keycloak?.tokenParsed as any)?.realm_access?.roles ?? [];
  }

  get isAdmin(): boolean {
    return this.realmRoles.includes('shop-admin');
  }

  constructor() {
    effect(() => {
      const event = this.keycloakSignal();
      if (event.type === KeycloakEventType.Ready) {
        this.authenticated = typeEventArgs<ReadyArgs>(event.args);
      }
      if (event.type === KeycloakEventType.AuthLogout) {
        this.authenticated = false;
      }
    });
  }

  login() {
    this.keycloak.login({ redirectUri: window.location.href });
  }

  logout() {
    this.keycloak.logout({ redirectUri: window.location.origin });
  }
}

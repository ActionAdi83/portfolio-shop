import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-forbidden',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="max-w-2xl mx-auto px-4 py-20 text-center">
      <h1 class="text-3xl font-bold mb-4">Forbidden</h1>
      <p class="text-slate-500 mb-6">Your account does not have access to this page.</p>
      <a routerLink="/" class="text-brand hover:underline">Back to the shop</a>
    </div>
  `,
})
export class ForbiddenPage {}

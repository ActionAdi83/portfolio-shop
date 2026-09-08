import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-footer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <footer class="mt-16 border-t border-slate-200 bg-white">
      <div class="max-w-6xl mx-auto px-4 py-8 text-sm text-slate-500 flex flex-col sm:flex-row justify-between gap-2">
        <span>&copy; {{ year }} Portfolio Shop — a portfolio project, not a real store.</span>
        <span>Paid in Bitcoin via a self-hosted BTCPay Server.</span>
      </div>
    </footer>
  `,
})
export class FooterComponent {
  readonly year = new Date().getFullYear();
}

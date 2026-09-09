import { Component, OnInit, signal, ChangeDetectionStrategy, inject } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import Keycloak from 'keycloak-js';
import { KeycloakReady } from '../keycloak.config';
import { CartService } from '../services/cart.service';
import { AccountService } from '../services/account.service';
import { OrderService } from '../services/order.service';
import { PaymentService } from '../services/payment.service';
import { Address, AddressRequest } from '../entities/address';
import { imageUrl } from '../util/media';

@Component({
  selector: 'app-cart',
  imports: [RouterLink, DecimalPipe, FormsModule],
  templateUrl: './cart.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CartPage implements OnInit {
  readonly cart = inject(CartService);
  readonly imageUrl = imageUrl;

  readonly authenticated = signal(false);
  readonly addresses = signal<Address[]>([]);
  readonly selectedAddressId = signal<string>('');
  readonly useNewAddress = signal(false);
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  newAddress: AddressRequest = { label: '', line1: '', line2: '', city: '', postalCode: '', country: '' };

  private readonly keycloak = inject(Keycloak);
  private readonly keycloakReady = inject(KeycloakReady);

  constructor(
    private accountService: AccountService,
    private orderService: OrderService,
    private paymentService: PaymentService,
  ) {}

  async ngOnInit() {
    this.authenticated.set(await this.keycloakReady.whenReady);
    if (this.authenticated()) {
      this.accountService.listAddresses().subscribe(list => {
        this.addresses.set(list);
        if (list.length) this.selectedAddressId.set(list[0].id);
        else this.useNewAddress.set(true);
      });
    }
  }

  setQuantity(productId: string, value: string) {
    const n = Number(value);
    this.cart.setQuantity(productId, Number.isFinite(n) ? n : 1);
  }

  async checkout() {
    this.error.set(null);
    if (this.cart.items().length === 0) return;

    if (!this.authenticated()) {
      await this.keycloak.login({ redirectUri: window.location.href });
      return;
    }

    const items = this.cart.items().map(l => ({ productId: l.product.id, quantity: l.quantity }));
    const usingNew = this.useNewAddress() || !this.selectedAddressId();

    this.submitting.set(true);
    this.orderService
      .create({
        items,
        addressId: usingNew ? null : this.selectedAddressId(),
        shippingAddress: usingNew ? this.newAddress : null,
      })
      .subscribe({
        next: order => this.openInvoice(order.id),
        error: err => this.handleError(err),
      });
  }

  private openInvoice(orderId: string) {
    this.paymentService.openInvoice(orderId).subscribe({
      next: response => {
        this.cart.clear();
        window.location.href = response.checkoutLink;
      },
      error: (err: HttpErrorResponse) => {
        this.cart.clear();
        if (err.status === 503) {
          this.error.set(
            'Your order was placed (it will show up under Account → Orders), but online payment ' +
              'is not configured on this server yet. Contact the shop to arrange payment.',
          );
        } else {
          this.handleError(err);
        }
      },
    });
  }

  private handleError(err: HttpErrorResponse) {
    this.submitting.set(false);
    this.error.set(err.error?.error ?? 'Something went wrong placing the order. Please try again.');
  }
}

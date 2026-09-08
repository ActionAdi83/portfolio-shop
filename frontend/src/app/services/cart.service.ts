import { Injectable, computed, signal } from '@angular/core';
import { Product } from '../entities/product';

export interface CartLine {
  product: Product;
  quantity: number;
}

const STORAGE_KEY = 'portfolio-shop-cart';

/**
 * The shopping cart, kept in localStorage so it survives a page reload.
 *
 * There is deliberately no server-side cart: it only ever becomes real once
 * checkout turns it into an Order, at which point the backend re-validates stock
 * and snapshots prices — the client-held cart is just a convenience.
 */
@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly lines = signal<CartLine[]>(this.readFromStorage());

  readonly items = this.lines.asReadonly();
  readonly totalItems = computed(() => this.lines().reduce((sum, l) => sum + l.quantity, 0));
  readonly totalPrice = computed(() => this.lines().reduce((sum, l) => sum + l.quantity * l.product.price, 0));

  add(product: Product, quantity = 1) {
    const existing = this.lines().find(l => l.product.id === product.id);
    if (existing) {
      this.setQuantity(product.id, existing.quantity + quantity);
    } else {
      this.lines.set([...this.lines(), { product, quantity }]);
      this.persist();
    }
  }

  setQuantity(productId: string, quantity: number) {
    if (quantity <= 0) {
      this.remove(productId);
      return;
    }
    this.lines.set(this.lines().map(l => (l.product.id === productId ? { ...l, quantity } : l)));
    this.persist();
  }

  remove(productId: string) {
    this.lines.set(this.lines().filter(l => l.product.id !== productId));
    this.persist();
  }

  clear() {
    this.lines.set([]);
    this.persist();
  }

  private persist() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(this.lines()));
  }

  private readFromStorage(): CartLine[] {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  }
}

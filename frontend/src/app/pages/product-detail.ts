import { Component, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ProductService } from '../services/product.service';
import { CartService } from '../services/cart.service';
import { Product } from '../entities/product';

@Component({
  selector: 'app-product-detail',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './product-detail.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductDetailPage implements OnInit {
  readonly product = signal<Product | null>(null);
  readonly quantity = signal(1);
  readonly added = signal(false);

  constructor(
    private route: ActivatedRoute,
    private products: ProductService,
    private cart: CartService,
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.products.get(id).subscribe(p => this.product.set(p));
  }

  setQuantity(value: string) {
    const n = Number(value);
    this.quantity.set(Number.isFinite(n) && n > 0 ? n : 1);
  }

  addToCart() {
    const product = this.product();
    if (!product) return;
    this.cart.add(product, this.quantity());
    this.added.set(true);
    setTimeout(() => this.added.set(false), 2000);
  }
}

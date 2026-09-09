import { Component, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductService } from '../services/product.service';
import { CartService } from '../services/cart.service';
import { Product } from '../entities/product';
import { imageUrl } from '../util/media';

@Component({
  selector: 'app-home',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './home.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomePage implements OnInit {
  readonly featured = signal<Product[]>([]);
  readonly imageUrl = imageUrl;

  constructor(private products: ProductService, private cart: CartService) {}

  ngOnInit() {
    this.products.list().subscribe(list => this.featured.set(list.slice(0, 4)));
  }

  addToCart(product: Product) {
    this.cart.add(product);
  }
}

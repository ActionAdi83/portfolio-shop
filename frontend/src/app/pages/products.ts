import { Component, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductService } from '../services/product.service';
import { CategoryService } from '../services/category.service';
import { CartService } from '../services/cart.service';
import { Product } from '../entities/product';
import { Category } from '../entities/category';
import { imageUrl } from '../util/media';

@Component({
  selector: 'app-products',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './products.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductsPage implements OnInit {
  readonly products = signal<Product[]>([]);
  readonly categories = signal<Category[]>([]);
  readonly selectedSlug = signal<string>('');
  readonly imageUrl = imageUrl;

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private cart: CartService,
  ) {}

  ngOnInit() {
    this.categoryService.list().subscribe(list => this.categories.set(list));
    this.load();
  }

  selectCategory(slug: string) {
    this.selectedSlug.set(slug);
    this.load();
  }

  private load() {
    this.productService.list(this.selectedSlug() || undefined).subscribe(list => this.products.set(list));
  }

  addToCart(product: Product) {
    this.cart.add(product);
  }
}

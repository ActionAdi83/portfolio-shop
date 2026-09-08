import { Component, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../services/product.service';
import { CategoryService } from '../services/category.service';
import { OrderService } from '../services/order.service';
import { Product, ProductRequest } from '../entities/product';
import { Category, CategoryRequest } from '../entities/category';
import { Order, OrderStatus } from '../entities/order';

type Tab = 'products' | 'categories' | 'orders';

const ORDER_STATUSES: OrderStatus[] = ['NEW', 'AWAITING_PAYMENT', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

const BLANK_PRODUCT: ProductRequest = { name: '', description: '', price: 0, categorySlug: '', imageUrls: [], stock: 0 };
const BLANK_CATEGORY: CategoryRequest = { name: '', slug: '' };

@Component({
  selector: 'app-admin',
  imports: [FormsModule, DecimalPipe],
  templateUrl: './admin.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminPage implements OnInit {
  readonly tab = signal<Tab>('products');
  readonly statuses = ORDER_STATUSES;

  readonly products = signal<Product[]>([]);
  readonly editingProductId = signal<string | null>(null);
  productForm: ProductRequest = { ...BLANK_PRODUCT };
  /** Comma-separated in the form, split into imageUrls on save. */
  imageUrlsText = '';

  readonly categories = signal<Category[]>([]);
  readonly editingCategoryId = signal<string | null>(null);
  categoryForm: CategoryRequest = { ...BLANK_CATEGORY };

  readonly orders = signal<Order[]>([]);

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private orderService: OrderService,
  ) {}

  ngOnInit() {
    this.loadProducts();
    this.loadCategories();
    this.loadOrders();
  }

  selectTab(tab: Tab) {
    this.tab.set(tab);
  }

  private loadProducts() {
    this.productService.list().subscribe(list => this.products.set(list));
  }

  private loadCategories() {
    this.categoryService.list().subscribe(list => this.categories.set(list));
  }

  private loadOrders() {
    this.orderService.adminList().subscribe(list => this.orders.set(list));
  }

  // --- Products ---

  startCreateProduct() {
    this.editingProductId.set('new');
    this.productForm = { ...BLANK_PRODUCT };
    this.imageUrlsText = '';
  }

  startEditProduct(product: Product) {
    this.editingProductId.set(product.id);
    this.productForm = { ...product };
    this.imageUrlsText = product.imageUrls.join(', ');
  }

  cancelProduct() {
    this.editingProductId.set(null);
  }

  saveProduct() {
    this.productForm.imageUrls = this.imageUrlsText
      .split(',')
      .map(s => s.trim())
      .filter(Boolean);
    const id = this.editingProductId();
    const action = id && id !== 'new'
      ? this.productService.update(id, this.productForm)
      : this.productService.create(this.productForm);
    action.subscribe(() => {
      this.editingProductId.set(null);
      this.loadProducts();
    });
  }

  deleteProduct(id: string) {
    this.productService.delete(id).subscribe(() => this.loadProducts());
  }

  // --- Categories ---

  startCreateCategory() {
    this.editingCategoryId.set('new');
    this.categoryForm = { ...BLANK_CATEGORY };
  }

  startEditCategory(category: Category) {
    this.editingCategoryId.set(category.id);
    this.categoryForm = { ...category };
  }

  cancelCategory() {
    this.editingCategoryId.set(null);
  }

  saveCategory() {
    const id = this.editingCategoryId();
    const action = id && id !== 'new'
      ? this.categoryService.update(id, this.categoryForm)
      : this.categoryService.create(this.categoryForm);
    action.subscribe(() => {
      this.editingCategoryId.set(null);
      this.loadCategories();
    });
  }

  deleteCategory(id: string) {
    this.categoryService.delete(id).subscribe(() => this.loadCategories());
  }

  // --- Orders ---

  changeStatus(order: Order, status: string) {
    this.orderService.adminUpdateStatus(order.id, status as OrderStatus).subscribe(() => this.loadOrders());
  }

  total(order: Order): number {
    return order.items.reduce((sum, i) => sum + i.quantity * i.unitPrice, 0);
  }
}

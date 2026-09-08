import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../environment/environment';
import { Product, ProductRequest } from '../entities/product';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private base = `${environment.baseurl}products`;
  private adminBase = `${environment.baseurl}admin/products`;

  constructor(private http: HttpClient) {}

  list(categorySlug?: string) {
    let params = new HttpParams();
    if (categorySlug) params = params.set('categorySlug', categorySlug);
    return this.http.get<Product[]>(this.base, { params });
  }

  get(id: string) {
    return this.http.get<Product>(`${this.base}/${id}`);
  }

  create(request: ProductRequest) {
    return this.http.post<Product>(this.adminBase, request);
  }

  update(id: string, request: ProductRequest) {
    return this.http.put<Product>(`${this.adminBase}/${id}`, request);
  }

  delete(id: string) {
    return this.http.delete<void>(`${this.adminBase}/${id}`);
  }
}

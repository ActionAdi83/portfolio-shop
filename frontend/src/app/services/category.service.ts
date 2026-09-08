import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environment/environment';
import { Category, CategoryRequest } from '../entities/category';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private base = `${environment.baseurl}categories`;
  private adminBase = `${environment.baseurl}admin/categories`;

  constructor(private http: HttpClient) {}

  list() {
    return this.http.get<Category[]>(this.base);
  }

  create(request: CategoryRequest) {
    return this.http.post<Category>(this.adminBase, request);
  }

  update(id: string, request: CategoryRequest) {
    return this.http.put<Category>(`${this.adminBase}/${id}`, request);
  }

  delete(id: string) {
    return this.http.delete<void>(`${this.adminBase}/${id}`);
  }
}

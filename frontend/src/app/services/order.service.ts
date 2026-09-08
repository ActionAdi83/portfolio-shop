import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environment/environment';
import { Order, OrderStatus } from '../entities/order';
import { AddressRequest } from '../entities/address';

export interface OrderCreateRequest {
  items: { productId: string; quantity: number }[];
  addressId?: string | null;
  shippingAddress?: AddressRequest | null;
}

@Injectable({ providedIn: 'root' })
export class OrderService {
  private base = `${environment.baseurl}orders`;
  private accountBase = `${environment.baseurl}account/orders`;
  private adminBase = `${environment.baseurl}admin/orders`;

  constructor(private http: HttpClient) {}

  create(request: OrderCreateRequest) {
    return this.http.post<Order>(this.base, request);
  }

  get(id: string) {
    return this.http.get<Order>(`${this.base}/${id}`);
  }

  myOrders() {
    return this.http.get<Order[]>(this.accountBase);
  }

  adminList() {
    return this.http.get<Order[]>(this.adminBase);
  }

  adminUpdateStatus(id: string, status: OrderStatus) {
    return this.http.put<Order>(`${this.adminBase}/${id}/status`, { status });
  }
}

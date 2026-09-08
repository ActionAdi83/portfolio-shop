import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environment/environment';

export interface InvoiceResponse {
  invoiceId: string;
  checkoutLink: string;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private base = `${environment.baseurl}payments`;

  constructor(private http: HttpClient) {}

  openInvoice(orderId: string) {
    return this.http.post<InvoiceResponse>(`${this.base}/invoices`, { orderId });
  }
}

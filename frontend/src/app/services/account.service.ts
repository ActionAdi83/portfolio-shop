import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environment/environment';
import { UserProfile } from '../entities/profile';
import { Address, AddressRequest } from '../entities/address';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private base = `${environment.baseurl}account`;

  constructor(private http: HttpClient) {}

  getProfile() {
    return this.http.get<UserProfile>(this.base);
  }

  updateProfile(fullName: string, phone: string) {
    return this.http.put<UserProfile>(this.base, { fullName, phone });
  }

  listAddresses() {
    return this.http.get<Address[]>(`${this.base}/addresses`);
  }

  createAddress(request: AddressRequest) {
    return this.http.post<Address>(`${this.base}/addresses`, request);
  }

  updateAddress(id: string, request: AddressRequest) {
    return this.http.put<Address>(`${this.base}/addresses/${id}`, request);
  }

  deleteAddress(id: string) {
    return this.http.delete<void>(`${this.base}/addresses/${id}`);
  }
}

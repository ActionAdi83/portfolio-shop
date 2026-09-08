import { Component, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { DecimalPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AccountService } from '../services/account.service';
import { OrderService } from '../services/order.service';
import { Address, AddressRequest } from '../entities/address';
import { Order } from '../entities/order';

type Tab = 'profile' | 'addresses' | 'orders';

@Component({
  selector: 'app-account',
  imports: [FormsModule, DecimalPipe, DatePipe],
  templateUrl: './account.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountPage implements OnInit {
  readonly tab = signal<Tab>('profile');

  fullName = '';
  phone = '';
  readonly profileSaved = signal(false);

  readonly addresses = signal<Address[]>([]);
  readonly editingId = signal<string | null>(null);
  addressForm: AddressRequest = { label: '', line1: '', line2: '', city: '', postalCode: '', country: '' };

  readonly orders = signal<Order[]>([]);

  constructor(private account: AccountService, private orderService: OrderService) {}

  ngOnInit() {
    this.account.getProfile().subscribe(p => {
      this.fullName = p.fullName ?? '';
      this.phone = p.phone ?? '';
    });
    this.loadAddresses();
    this.orderService.myOrders().subscribe(list => this.orders.set(list));
  }

  selectTab(tab: Tab) {
    this.tab.set(tab);
  }

  saveProfile() {
    this.account.updateProfile(this.fullName, this.phone).subscribe(() => {
      this.profileSaved.set(true);
      setTimeout(() => this.profileSaved.set(false), 2000);
    });
  }

  private loadAddresses() {
    this.account.listAddresses().subscribe(list => this.addresses.set(list));
  }

  startCreate() {
    this.editingId.set('new');
    this.addressForm = { label: '', line1: '', line2: '', city: '', postalCode: '', country: '' };
  }

  startEdit(address: Address) {
    this.editingId.set(address.id);
    this.addressForm = { ...address };
  }

  cancelEdit() {
    this.editingId.set(null);
  }

  saveAddress() {
    const id = this.editingId();
    const action = id && id !== 'new'
      ? this.account.updateAddress(id, this.addressForm)
      : this.account.createAddress(this.addressForm);
    action.subscribe(() => {
      this.editingId.set(null);
      this.loadAddresses();
    });
  }

  deleteAddress(id: string) {
    this.account.deleteAddress(id).subscribe(() => this.loadAddresses());
  }
}

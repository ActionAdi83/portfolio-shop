export type OrderStatus = 'NEW' | 'AWAITING_PAYMENT' | 'PAID' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';

export interface OrderItem {
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
}

export interface ShippingAddress {
  label: string;
  line1: string;
  line2: string | null;
  city: string;
  postalCode: string;
  country: string;
}

export interface Order {
  id: string;
  userId: string;
  items: OrderItem[];
  shippingAddress: ShippingAddress;
  status: OrderStatus;
  btcpayInvoiceId: string | null;
  createdAt: string;
}

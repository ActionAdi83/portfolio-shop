export interface Address {
  id: string;
  userId: string;
  label: string;
  line1: string;
  line2: string | null;
  city: string;
  postalCode: string;
  country: string;
}

export interface AddressRequest {
  label: string;
  line1: string;
  line2: string | null;
  city: string;
  postalCode: string;
  country: string;
}

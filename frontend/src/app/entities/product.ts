export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  categorySlug: string;
  imageUrls: string[];
  stock: number;
}

/** Body sent to the admin create/update endpoints. */
export interface ProductRequest {
  name: string;
  description: string;
  price: number;
  categorySlug: string;
  imageUrls: string[];
  stock: number;
}

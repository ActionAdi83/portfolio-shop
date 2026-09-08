import { Routes } from '@angular/router';
import { canActivateAuthRole } from './guard/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home').then(m => m.HomePage),
  },
  {
    path: 'products',
    loadComponent: () => import('./pages/products').then(m => m.ProductsPage),
  },
  {
    path: 'products/:id',
    loadComponent: () => import('./pages/product-detail').then(m => m.ProductDetailPage),
  },
  {
    path: 'cart',
    loadComponent: () => import('./pages/cart').then(m => m.CartPage),
  },
  {
    path: 'account',
    loadComponent: () => import('./pages/account').then(m => m.AccountPage),
    canActivate: [canActivateAuthRole],
  },
  {
    path: 'admin',
    loadComponent: () => import('./pages/admin').then(m => m.AdminPage),
    canActivate: [canActivateAuthRole],
    data: { role: 'shop-admin' },
  },
  {
    path: 'forbidden',
    loadComponent: () => import('./pages/forbidden').then(m => m.ForbiddenPage),
  },
  { path: '**', redirectTo: '' },
];

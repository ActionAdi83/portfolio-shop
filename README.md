# Portfolio Shop

A small online shop built as a portfolio project: a Spring Boot / MongoDB
backend, an Angular frontend, Keycloak for authentication, and a self-hosted
BTCPay Server for checkout. It is a sibling project of `fanvote` on the same
machine and reuses the same stack and conventions, but is its own independent
repository — nothing here is required by, or required for, `fanvote`.

This is a **first iteration**: the core flows (browse, cart, checkout, order
history, admin CRUD) work end-to-end; polish and edge cases are called out as
`TODO` comments in the code rather than silently skipped.

## What's here

```
portfolio-shop/
├── backend/    Spring Boot (Java 21, Maven) — REST API + MongoDB
├── frontend/   Angular — the storefront, account area and admin panel
├── scripts/    Demo catalogue + seeder
└── docker-compose.yml
```

### Backend

- **Products / Categories** — public browsing (`GET /api/products`,
  `GET /api/products/{id}`, `GET /api/categories`); CRUD for both gated behind
  the `shop-admin` realm role (`/api/admin/products`, `/api/admin/categories`).
- **Orders** — `POST /api/orders` builds an order from cart contents + a
  shipping address (snapshotting price and product name per line, and taking
  stock at order time); `GET /api/account/orders` is a customer's own history;
  `/api/admin/orders` lists everything and moves an order through
  `NEW → AWAITING_PAYMENT → PAID → SHIPPED → DELIVERED` (or `CANCELLED`).
- **Account** — profile (`GET/PUT /api/account`) and an address book
  (`/api/account/addresses`), both keyed on the Keycloak `sub`, never on
  anything the client supplies.
- **Payments** — `POST /api/payments/invoices` opens a BTCPay invoice for an
  order (adapted from `fanvote`'s `BtcPayClient`/`PaymentService`) and
  `POST /api/payments/btcpay/webhook` settles it. The webhook is
  unauthenticated by necessity (BTCPay cannot present a token) and instead
  verified with an HMAC-SHA256 signature check, copied from `fanvote` because
  it is security-critical: constant-time comparison via `MessageDigest.isEqual`,
  never `.equals()`. Settling an invoice re-reads it from BTCPay before
  trusting anything about it, and is idempotent against redelivery via an
  atomic claim on `PaymentInvoice.orderMarkedPaid` — the same pattern
  `fanvote`'s `PaymentService.settleInvoice` uses.
- If BTCPay is not configured (any of `BTCPAY_URL` / `BTCPAY_API_KEY` /
  `BTCPAY_STORE_ID` blank), `POST /api/payments/invoices` answers `503`
  instead of the app crashing on startup — same as `fanvote`.
- **Auth** — Spring Security as an OAuth2 resource server validating Keycloak
  JWTs, with the same custom `jwtAuthenticationConverter()` `fanvote` uses to
  read `realm_access.roles` out of the raw claim map. (A plain
  `JwtGrantedAuthoritiesConverter` with a dotted claim name looks like it reads
  a nested claim and doesn't — see the comment in `SecurityConfig` for the
  full story; it was a real, silent bug in `fanvote` until this converter
  replaced it.)

### Frontend

Angular, standalone components, Tailwind for styling. Pages: `/` (hero +
featured products), `/products` (category filter), `/products/:id`,
`/cart` (checkout → redirects to the BTCPay checkout link), `/account`
(auth-guarded: profile, addresses, order history), `/admin` (auth-guarded,
role `shop-admin`: products/categories CRUD, order status changes). Keycloak
wiring (manual, non-blocking `init()`, PKCE S256, bearer-token interceptor,
silent-check-sso) is copied from `fanvote-frontend`'s `keycloak.config.ts` /
`guard/auth.guard.ts`, with the realm/client renamed to `shop` / `frontend`.

## Prerequisites

- Docker (for `docker compose up`), **or**
- Java 21 + Maven, and Node 20.19+ / 22.12+ / 24+ for running the two services
  natively (Angular 22's tooling does not run on Node 20.11 — see the note in
  `../fanvote`'s memory about needing a newer Node for frontend work).
- A Keycloak instance with a `shop` realm (client `frontend`, public, PKCE;
  realm role `shop-admin` for whoever should reach `/admin`). This project
  does not ship or start its own Keycloak — see `.env.example`.
- Optionally, a BTCPay Server instance, for the payment flow to actually work
  end to end. Without one, everything except checkout still works.

## Running locally

```bash
cp .env.example .env      # edit KEYCLOAK_ISSUER_URI / BTCPAY_* as needed
docker compose up --build
```

- Frontend: http://localhost:4300
- Backend: http://localhost:8082/api
- Mongo: `mongodb://localhost:27018/portfolio_shop`

Ports are deliberately not the obvious 8080/4200/27017 — this project sits
next to `fanvote`'s own compose stack on the same machine, which already uses
those.

### Running the two services natively instead

```bash
docker compose up -d mongo                 # just the database, on :27018
cd backend && mvn spring-boot:run           # backend on :8082 (application-development.properties)
cd frontend && npm install && npm start     # frontend on :4200 (ng serve default)
```

## Seeding the demo catalogue

Four demo sport shoes (`aero-runner`, `trail-blazer-x`, `urbanflex-knit`,
`sprintpro-elite`) live under `scripts/populate/`, one folder per product with
an `about.txt` (display name + marketing copy). With the Mongo container
running:

```bash
scripts/add-populated-products.sh scripts/populate
```

Idempotent (matches by product name, replaces rather than duplicates) and
reads `MONGO_DB`/`MONGO_USER`/`MONGO_PASSWORD` from `.env` rather than
hardcoding them, the same convention `fanvote`'s
`scripts/add-populated-profiles.sh` follows. See
`scripts/populate/README.md` for why the seeded products have no photos yet.

## Known follow-ups (first iteration)

- `OrderService.createOrder` decrements stock and saves the order as two
  non-transactional writes; a crash between them could in principle drop
  stock without an order to show for it. Fine for a demo; a production
  version would use a Mongo transaction or a reconciliation job.
- No pagination on `/api/products`, `/api/admin/orders`, etc. — fine at demo
  catalogue size, would need it before a real product count.
- `UserProfile` (full name, phone) is a small addition beyond the entities
  listed in the original spec, added because `GET/PUT /api/account` needs
  something to read and write — everything else about identity (email,
  login) stays in Keycloak.
- No automated tests were added in this iteration.
- The frontend's cart is client-side only (localStorage); it becomes durable
  the moment checkout turns it into an `Order`, which is the point at which
  stock and price are re-validated server-side.

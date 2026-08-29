# Forkful — Online Food / Restaurant Ordering System

A full-stack demo of an online food ordering platform, built with the requested stack:
**HTML + CSS + JavaScript + AJAX** (frontend) and **Java + Spring Boot + Hibernate + MySQL** (backend),
with **Maven** for dependency management.

```
food-ordering-system/
├── backend/                # Spring Boot REST API
│   ├── pom.xml
│   └── src/main/java/com/foodorder/app/
│       ├── entity/         # JPA/Hibernate entities (User, Restaurant, MenuItem, CartItem, Order, OrderItem)
│       ├── enums/          # Role, OrderStatus
│       ├── repository/     # Spring Data JPA repositories
│       ├── dto/            # Request/response payloads
│       ├── service/        # Business logic (Spring-managed beans, constructor injection)
│       ├── controller/     # REST controllers consumed by the frontend via AJAX
│       ├── config/         # AsyncConfig (multithreading), CORS, password hashing, token store
│       └── exception/      # Centralized error handling
├── frontend/                # Static HTML/CSS/JS client (AJAX via fetch)
│   ├── index.html           # Login / Registration
│   ├── restaurants.html     # Customer: browse restaurants
│   ├── menu.html             # Customer: view menu, add to cart (AJAX)
│   ├── cart.html              # Customer: edit cart, place order
│   ├── orders.html            # Customer: order history + live tracking (polling)
│   ├── admin.html              # Restaurant Admin: manage menu + accept/reject/update orders
│   ├── css/style.css
│   └── js/api.js               # Shared AJAX/fetch wrapper + session helpers
└── database/schema.sql      # Reference schema (Hibernate auto-generates this at runtime)
```

## How the modules map to the spec

### Customer module
| Requirement | Where it lives |
|---|---|
| Registration/Login | `AuthController` + `index.html` (BCrypt-hashed passwords, opaque session token) |
| Browse restaurants | `RestaurantController#browse` + `restaurants.html` |
| View menu | `MenuController#viewMenu` + `menu.html` |
| Add to cart | `CartController` + AJAX calls in `menu.html` / `cart.html` |
| Place order | `OrderController#placeOrder` (converts cart → order transactionally) |
| Order tracking | `OrderController#track` + polling in `orders.html` |
| Order history | `OrderController#history` + `orders.html` |

### Restaurant/Admin module
| Requirement | Where it lives |
|---|---|
| Add food items | `MenuController#addItem` + `admin.html` |
| Update price | `MenuController#updatePrice` |
| Manage availability | `MenuController#setAvailability` |
| Accept/reject orders | `OrderController#updateStatus` (`ACCEPTED` / `REJECTED`) |
| Update order status | `OrderController#updateStatus` (`PREPARING` → `OUT_FOR_DELIVERY` → `DELIVERED`) |

## Advanced concepts implemented

- **AJAX for cart updates** — every add/update/remove cart action (`frontend/js/api.js` → `menu.html`, `cart.html`)
  hits the backend via `fetch()` and updates the UI (cart badge, subtotal) without a full page reload.
  Order tracking also polls asynchronously (`orders.html`).
- **Hibernate ORM** — entities in `entity/` are mapped with JPA annotations; `spring.jpa.hibernate.ddl-auto=update`
  lets Hibernate create/evolve the MySQL schema automatically (see `database/schema.sql` for the reference DDL).
- **Spring dependency injection** — every service/controller uses constructor injection
  (`@RequiredArgsConstructor`), and `AsyncConfig`/`WebConfig`/`PasswordConfig` register beans Spring wires in.
- **Maven dependency management** — `backend/pom.xml` manages Spring Boot, Hibernate, the MySQL driver,
  validation, and Spring Security's password hashing utilities via `spring-boot-starter-parent`.
- **Multithreading for concurrent orders** — `AsyncConfig` defines a bounded `ThreadPoolTaskExecutor`
  (`orderTaskExecutor`); `OrderService#notifyRestaurantOfNewOrder` runs `@Async` on that pool so that many
  simultaneous checkouts are processed concurrently without blocking each other's HTTP request threads.
  `placeOrder` and `updateStatus` are `@Transactional`, so the cart→order conversion and status transitions
  stay atomic under concurrent access.

## Running it locally

### 1. Database
Have MySQL running locally (or update the connection URL in `application.properties`). Hibernate will
create the `food_ordering_db` schema and tables automatically on first run — `database/schema.sql` is
provided only for reference/manual setup.

### 2. Backend
```bash
cd backend
mvn spring-boot:run
```
The API starts on `http://localhost:8080`.

### 3. Frontend
The frontend is static HTML/CSS/JS — serve it with any static file server, e.g.:
```bash
cd frontend
python3 -m http.server 5500
```
Then open `http://localhost:5500`. CORS is already configured on the backend to accept requests from any
origin during development (`WebConfig`).

## Notes / production hardening

This is a learning/demo-scale implementation. Before shipping to production, you'd want to:
- Replace the in-memory `TokenStore` with real Spring Security + JWT (with expiry, refresh tokens).
- Add pagination to restaurant/menu/order listing endpoints.
- Add integration tests (e.g. `@SpringBootTest` + Testcontainers for MySQL).
- Replace polling-based order tracking with WebSockets/SSE for true real-time updates.
- Add request-level rate limiting and input sanitization beyond bean validation.

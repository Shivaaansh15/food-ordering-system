# 🍔 Forkful — Online Food / Restaurant Ordering System

A full-stack online food ordering platform where customers can browse restaurants, view menus, add items to a cart, place orders, and track them in real time — while restaurant admins manage their menu and incoming orders from a dedicated dashboard.

Built with **HTML, CSS, JavaScript (AJAX)** on the frontend and **Java, Spring Boot, Hibernate, MySQL** on the backend, using **Maven** for dependency management.

---

## ✨ Features

### Customer
- Register / log in
- Browse restaurants
- View restaurant menus
- Add items to cart (updates instantly via AJAX — no page reloads)
- Place an order with a delivery address
- Track order status live (Placed → Accepted → Preparing → Out for Delivery → Delivered)
- View past order history

### Restaurant Admin
- Create and manage a restaurant profile
- Add new food items
- Update item prices
- Mark items available / sold out
- Accept or reject incoming orders
- Update order status as it moves through the kitchen and delivery

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Frontend | HTML5, CSS3, Vanilla JavaScript, AJAX (`fetch`) |
| Backend | Java 17, Spring Boot, Spring MVC |
| ORM | Hibernate (via Spring Data JPA) |
| Database | MySQL |
| Build tool | Maven |
| Concurrency | Spring `@Async` + `ThreadPoolTaskExecutor` for concurrent order processing |

---

## 📁 Project Structure

```
food-ordering-system/
├── backend/                     # Spring Boot REST API
│   ├── pom.xml
│   └── src/main/java/com/foodorder/app/
│       ├── entity/              # User, Restaurant, MenuItem, CartItem, Order, OrderItem
│       ├── enums/                # Role, OrderStatus
│       ├── repository/           # Spring Data JPA repositories
│       ├── dto/                  # Request/response payloads
│       ├── service/               # Business logic
│       ├── controller/            # REST endpoints
│       ├── config/                 # Async, CORS, security, token store
│       └── exception/              # Centralized error handling
├── frontend/                    # Static HTML/CSS/JS client
│   ├── index.html               # Login / registration
│   ├── restaurants.html         # Browse restaurants
│   ├── menu.html                # View menu, add to cart
│   ├── cart.html                # Edit cart, place order
│   ├── orders.html              # Order history + live tracking
│   ├── admin.html               # Restaurant admin dashboard
│   ├── css/style.css
│   └── js/api.js                # Shared AJAX/fetch wrapper
├── database/schema.sql          # Reference SQL schema
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17 (JDK)
- Maven 3.6+
- MySQL 8+

### 1. Clone the repository
```bash
git clone https://github.com/<your-username>/food-ordering-system.git
cd food-ordering-system
```

### 2. Configure the database
Make sure MySQL is running locally, then update the credentials in `backend/src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=your_password
```
No manual schema setup is needed — Hibernate auto-creates the `food_ordering_db` database and all tables on first run (`ddl-auto=update`). See `database/schema.sql` for the equivalent DDL if you prefer to run it manually.

### 3. Run the backend
```bash
cd backend
mvn spring-boot:run
```
The API starts on `http://localhost:8080`.

### 4. Run the frontend
In a separate terminal:
```bash
cd frontend
python3 -m http.server 5500
```
Open `http://localhost:5500` in your browser.

### 5. Try it out
- Register one account as **Restaurant Admin** and one as **Customer** (use two tabs/windows).
- As the admin: create a restaurant and add a few menu items.
- As the customer: browse to that restaurant, add items to your cart, and place an order.
- Switch back to the admin tab to accept the order and move it through its status — watch the customer's order tracker update live.

---

## 🔌 API Overview

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create a customer or restaurant-admin account |
| POST | `/api/auth/login` | Log in and receive a session token |
| GET | `/api/restaurants` | List active restaurants |
| POST | `/api/restaurants` | Create a restaurant (admin) |
| GET | `/api/restaurants/{id}/menu` | View a restaurant's available menu |
| POST | `/api/restaurants/{id}/menu` | Add a menu item (admin) |
| PATCH | `/api/menu/{id}/price` | Update an item's price (admin) |
| PATCH | `/api/menu/{id}/availability` | Toggle item availability (admin) |
| GET / POST / DELETE | `/api/cart` | View, add to, or clear the cart |
| POST | `/api/orders` | Place an order from the current cart |
| GET | `/api/orders/history` | Customer's past orders |
| GET | `/api/orders/{id}/track` | Live status of a single order |
| GET | `/api/orders/restaurant/{id}` | Incoming orders for a restaurant (admin) |
| PATCH | `/api/orders/{id}/status` | Accept/reject/advance an order's status (admin) |

All authenticated endpoints expect an `Authorization` header containing the token returned from `/api/auth/login`.

---

## 🧠 Architecture Notes

- **AJAX everywhere it matters** — cart updates and order-status polling happen via `fetch()` calls, so the UI updates without full page reloads.
- **Hibernate ORM** — entities are mapped with JPA annotations; schema evolves automatically via `ddl-auto=update`.
- **Spring dependency injection** — every service and controller uses constructor injection.
- **Concurrency** — order-placement notifications run on a dedicated thread pool (`@Async`), and cart→order conversion plus status updates are wrapped in `@Transactional` blocks to stay safe under concurrent access.

---

## 📌 Roadmap / Production Hardening

This project is demo/learning-scale. Before shipping to production, consider:
- Swapping the in-memory token store for Spring Security + JWT with proper expiry
- Adding pagination to list endpoints
- Adding integration tests (Testcontainers for MySQL)
- Replacing polling-based tracking with WebSockets/SSE
- Rate limiting and stricter input validation

---

## 📄 License

This project is provided as-is for educational/demo purposes. Add a license of your choice (MIT is a common default) before using it publicly.

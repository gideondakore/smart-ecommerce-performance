# SmartShop: Advanced E-Commerce Backend

SmartShop is a robust, high-performance e-commerce backend built with **Java 25** and **Spring Boot 4.0.1**. It provides a comprehensive set of features for managing products, categories, orders, inventory, and users, supporting both RESTful and GraphQL interfaces.

The project emphasizes performance and scalability, featuring custom-built sorting algorithms, an AOP-based monitoring system, and a lightweight in-memory caching mechanism.

## Key Features

- **Hybrid API Architecture**: Full support for both **REST** and **GraphQL** endpoints.
- **Advanced Security**:
  - Custom token-based authentication.
  - Role-Based Access Control (RBAC) using custom annotations (`@RequiresRole`).
  - Password hashing using BCrypt.
- **Performance Optimized**:
  - **Custom Sorting**: Implementation of QuickSort and MergeSort for efficient data handling.
  - **In-Memory Caching**: Custom `CacheManager` with hit/miss tracking.
  - **Performance Monitoring**: AOP-based tracking of database query execution times and cache performance.
- **Data Management**:
  - Automated DTO mapping using **MapStruct**.
  - Database versioning/schema management with Hibernate (DDL-auto).
  - Seed data generation for development.
- **Documentation**: Integrated **Swagger/OpenAPI** for REST API exploration and **GraphiQL** for GraphQL queries.
- **Performance Report**: Comprehensive analytical comparison between REST and GraphQL endpoints (see `PERFORMANCE_REPORT.md`).

## Tech Stack

- **Core**: Java 25, Spring Boot 4.0.1
- **Data**: Spring Data JPA, PostgreSQL
- **API**: Spring Web (REST), Spring GraphQL
- **Utilities**: Lombok, MapStruct, BCrypt (jBCrypt)
- **Monitoring**: Spring AOP, Spring Boot Actuator
- **Documentation**: Springdoc OpenAPI, GraphiQL

## Project Architecture

### 1. Performance Monitoring (AOP)

The project uses Aspect-Oriented Programming to monitor performance without polluting business logic. The `PerformanceMonitoringAspect` intercepts repository calls to record execution times and tracks cache statistics.

- **Metrics Endpoint**: `/api/performance/db-metrics` & `/api/performance/cache-metrics` (Admin Only).

### 2. Custom Sorting Service

Instead of relying solely on database sorting, the project includes a `SortingService` that implements:

- **QuickSort**: Optimized for in-place sorting.
- **MergeSort**: Stable sorting algorithm.
- Supports sorting by Price, Name, Quantity, and Date.

### 3. Security Model

A custom security layer is implemented via `AuthInterceptor` and `RoleInterceptor`.

- **Authentication**: Uses a Bearer token format (`Bearer username-id`).
- **Authorization**: Custom `@RequiresRole` annotation allows fine-grained access control at the controller level.

## Project Structure

```text
src/main/java/com/amalitech/smartshop/
├── aspects/          # AOP for Logging and Performance Monitoring
├── cache/            # Custom In-memory Caching System
├── config/           # Security Interceptors and App Config
├── controllers/      # REST API Controllers
├── dtos/             # Data Transfer Objects
├── entities/         # Domain Entities (User, Product, Category, Order, Inventory)
├── enums/            # Shared Enumerations (Roles, Status)
├── exceptions/       # Global Exception Handling
├── graphql/          # GraphQL Resolvers and Controllers
├── interfaces/       # Repository and Service Interfaces
├── mappers/          # MapStruct Interface Definitions
├── repositories/     # JDBC Repository Implementations
├── services/         # Business Logic Layer (Service Implementations)
└── utils/            # Sorting Utilities (QuickSort, MergeSort)
```

## Getting Started

### Prerequisites

- JDK 25
- Maven 3.9+
- PostgreSQL

### Environment Configuration

Create a `.env` file or set environment variables:

```properties
DATABASE_URL=jdbc:postgresql://localhost:5432/commerce_db
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password
```

### Installation & Run

1. Clone the repository.
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## API Documentation

### REST API

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`

#### Standard REST Endpoints

All endpoints follow RESTful conventions:

- `POST /resource` - Create
- `GET /resource` - List all
- `GET /resource/{id}` - Get by ID
- `PUT /resource/{id}` - Update
- `DELETE /resource/{id}` - Delete

#### Categories

| Method | Endpoint               | Description         | Auth Required |
| ------ | ---------------------- | ------------------- | ------------- |
| POST   | `/api/categories`      | Create a category   | ADMIN         |
| GET    | `/api/categories`      | List all categories | No            |
| GET    | `/api/categories/{id}` | Get category by ID  | No            |
| PUT    | `/api/categories/{id}` | Update a category   | ADMIN         |
| DELETE | `/api/categories/{id}` | Delete a category   | ADMIN         |

#### Products

| Method | Endpoint             | Description          | Auth Required |
| ------ | -------------------- | -------------------- | ------------- |
| POST   | `/api/products`      | Create a product     | ADMIN, VENDOR |
| GET    | `/api/products`      | List/search products | No            |
| GET    | `/api/products/{id}` | Get product by ID    | No            |
| PUT    | `/api/products/{id}` | Update a product     | ADMIN, VENDOR |
| DELETE | `/api/products/{id}` | Delete a product     | ADMIN, VENDOR |

**Product Search Parameters:**

- `search` - Search by name or description
- `categoryId` - Filter by category
- `minPrice` - Minimum price filter
- `maxPrice` - Maximum price filter
- `inStock` - Filter products with available stock
- `sortBy` - Sort field (NAME, PRICE, QUANTITY)
- `ascending` - Sort direction (true/false)
- `page`, `size` - Pagination parameters

Example: `GET /api/products?search=book&minPrice=10&maxPrice=50&inStock=true&page=0&size=10`

#### Orders

| Method | Endpoint                  | Description         | Auth Required   |
| ------ | ------------------------- | ------------------- | --------------- |
| POST   | `/api/orders`             | Create an order     | CUSTOMER, ADMIN |
| GET    | `/api/orders`             | List all orders     | ADMIN, VENDOR   |
| GET    | `/api/orders/user`        | Get user's orders   | CUSTOMER, ADMIN |
| GET    | `/api/orders/{id}`        | Get order by ID     | CUSTOMER        |
| PUT    | `/api/orders/{id}/status` | Update order status | ADMIN           |
| DELETE | `/api/orders/{id}`        | Delete an order     | ADMIN           |

**Order Processing:**

- Inventory is automatically deducted when an order is placed
- Transactions ensure atomicity - if inventory update fails, the order is rolled back
- Insufficient stock returns an error with available quantity

#### Users

| Method | Endpoint              | Description                 | Auth Required |
| ------ | --------------------- | --------------------------- | ------------- |
| POST   | `/api/users/register` | Register a new user         | No            |
| POST   | `/api/users/login`    | Login and get token         | No            |
| GET    | `/api/users`          | List all users              | ADMIN         |
| GET    | `/api/users/profile`  | Get current user profile    | Yes           |
| PUT    | `/api/users/profile`  | Update current user profile | Yes           |
| GET    | `/api/users/{id}`     | Get user by ID              | Yes           |
| PUT    | `/api/users/{id}`     | Update user                 | ADMIN         |
| DELETE | `/api/users/{id}`     | Delete user                 | ADMIN         |

#### Cart

| Method | Endpoint             | Description      | Auth Required |
| ------ | -------------------- | ---------------- | ------------- |
| GET    | `/api/cart`          | Get user's cart  | CUSTOMER      |
| POST   | `/api/cart`          | Add item to cart | CUSTOMER      |
| PUT    | `/api/cart/{itemId}` | Update cart item | CUSTOMER      |
| DELETE | `/api/cart/{itemId}` | Remove cart item | CUSTOMER      |
| DELETE | `/api/cart/clear`    | Clear cart       | CUSTOMER      |
| POST   | `/api/cart/checkout` | Checkout cart    | CUSTOMER      |

#### Inventory

| Method | Endpoint                             | Description              | Auth Required |
| ------ | ------------------------------------ | ------------------------ | ------------- |
| POST   | `/api/inventory`                     | Create inventory record  | ADMIN         |
| GET    | `/api/inventory`                     | List all inventory       | ADMIN         |
| GET    | `/api/inventory/{id}`                | Get inventory by ID      | ADMIN         |
| GET    | `/api/inventory/product/{productId}` | Get inventory by product | ADMIN         |
| PUT    | `/api/inventory/{id}`                | Update inventory         | ADMIN         |
| PATCH  | `/api/inventory/{id}`                | Adjust quantity          | ADMIN         |
| DELETE | `/api/inventory/{id}`                | Delete inventory         | ADMIN         |

### GraphQL

- **GraphiQL**: `http://localhost:8080/graphiql`
- **Endpoint**: `http://localhost:8080/graphql`

**Example Queries:**

```graphql
# Get all products
query {
  allProducts {
    name
    price
    categoryName
  }
}

# Get product by ID
query {
  productById(id: 1) {
    name
    price
    quantity
  }
}

# Get all categories
query {
  allCategories {
    id
    name
    description
  }
}
```

**Example Mutations:**

```graphql
# Login
mutation {
  login(input: { email: "user@example.com", password: "password" }) {
    token
    email
    role
  }
}

# Create order
mutation {
  createOrder(input: { userId: 1, items: [{ productId: 1, quantity: 2 }] }) {
    id
    totalAmount
    status
  }
}
```

## Transaction Management

The application uses Spring's `@Transactional` annotation with proper configuration:

- **Propagation**: REQUIRED - joins existing transaction or creates new one
- **Rollback**: Automatic rollback on any Exception
- **Isolation**: Default READ_COMMITTED

Order creation is transactional:

1. Validates product availability
2. Checks and reserves inventory
3. Creates order record
4. If any step fails, entire transaction rolls back

## Security Roles

| Role         | Permissions                                               |
| :----------- | :-------------------------------------------------------- |
| **ADMIN**    | Full access to all resources and performance metrics.     |
| **VENDOR**   | Manage own products and inventory.                        |
| **STAFF**    | View all orders, update order status.                     |
| **CUSTOMER** | Browse products, manage cart, and manage personal orders. |

---

## Authentication

### JWT Authentication (Stateless)

All API endpoints (except public ones) require a valid JWT in the `Authorization` header:

```
Authorization: Bearer <your_jwt_token>
```

**Auth Endpoints:**

| Method | Endpoint             | Description              | Auth Required |
| ------ | -------------------- | ------------------------ | ------------- |
| POST   | `/api/auth/register` | Register a new user      | No            |
| POST   | `/api/auth/login`    | Login and get JWT tokens | No            |
| POST   | `/api/auth/refresh`  | Refresh access token     | Bearer Token  |
| POST   | `/api/auth/logout`   | Logout (revoke token)    | Bearer Token  |
| GET    | `/api/auth/validate` | Validate a JWT token     | Bearer Token  |

### Google OAuth2 Login

Users can authenticate via Google:

1. Redirect to: `GET /oauth2/authorization/google`
2. After Google consent, the user is redirected to the frontend with JWT tokens in the URL query parameters.

---

## CORS & CSRF Configuration

### CORS

CORS is configured globally to allow cross-origin requests from trusted frontend origins:

- **Allowed Origins:** `http://localhost:3000`, `http://localhost:3001`
- **Allowed Methods:** `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`
- **Allowed Headers:** `Authorization`, `Content-Type`, `Accept`, `X-Requested-With`
- **Credentials:** Enabled (cookies/auth headers are forwarded)

In production, replace `localhost` origins with actual deployed frontend domains.

### CSRF (Cross-Site Request Forgery)

#### When to Disable CSRF

CSRF protection should be **disabled** for **stateless REST APIs** that use JWT Bearer tokens in the `Authorization` header. Since JWTs are not automatically attached to requests by the browser (unlike cookies), the CSRF attack vector does not apply. An attacker's malicious page cannot forge a request with the victim's JWT because it has no access to the token stored in `localStorage` or memory.

This is the default for all `/api/**` endpoints in this application.

#### When to Enable CSRF

CSRF protection should be **enabled** for any endpoint that:

- Uses **session-based or cookie-based authentication**
- Serves **HTML forms** that the browser submits automatically
- Performs **state-changing operations** (POST, PUT, DELETE) via form submissions

When CSRF is enabled, the server generates a unique token per session. This token must be included in every state-changing request (as a hidden form field `_csrf` or in the `X-CSRF-TOKEN` header). The server rejects any request without a valid CSRF token.

#### CSRF Demo Endpoints

This application includes a demo endpoint with CSRF protection enabled to illustrate the concept:

| Method | Endpoint                  | Description                           | CSRF Required |
| ------ | ------------------------- | ------------------------------------- | ------------- |
| GET    | `/api/csrf-demo/token`    | Retrieve the CSRF token               | No            |
| POST   | `/api/csrf-demo/feedback` | Submit feedback (CSRF token required) | Yes           |

**Testing in Postman:**

1. `GET /api/csrf-demo/token` — copy the returned `token` value
2. `POST /api/csrf-demo/feedback` — add header `X-CSRF-TOKEN: <token>` and body param `message=Hello`
3. Without the CSRF token, the POST request returns **403 Forbidden**

---

## Running the Application

```bash
# Development mode
cd backend/SmartShop && mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Spring Graphql Playground

**url:** http://localhost:8080/graphiql

**example queries **

Query 1: Get All Products

{
allProducts {
id
name
price
quantity
categoryName
}
}

////////////////////////////////////

Query 2: Get Product by ID

{
productById(id: 1) {
id
name
price
quantity
categoryName
}
}

Query 3: Get All Categories

{
allCategories {
id
name
description
}
}

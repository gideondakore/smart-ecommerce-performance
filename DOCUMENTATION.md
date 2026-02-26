# SmartShop E-Commerce System - Complete Documentation

## Project Overview

SmartShop is a full-stack e-commerce application built with Spring Boot (Java 21) backend and Next.js frontend, featuring Spring Data JPA for data persistence, PostgreSQL database, and both REST and GraphQL APIs.

## Technology Stack

### Backend
- **Framework**: Spring Boot 4.0.1
- **Language**: Java 25
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **API**: REST + GraphQL
- **Security**: Custom token-based authentication with sessions
- **Caching**: Custom in-memory cache manager
- **Documentation**: Swagger/OpenAPI, GraphiQL

### Frontend
- **Framework**: Next.js 16.1.6 (React)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **State Management**: React Context API

## Architecture

### Layered Architecture
```
Controller Layer → Service Layer → Repository Layer → Database
```

### Key Components
1. **Controllers**: Handle HTTP requests and responses
2. **Services**: Business logic and transaction management
3. **Repositories**: Data access using Spring Data JPA
4. **Entities**: JPA entities mapped to database tables
5. **DTOs**: Data transfer objects for API requests/responses
6. **Mappers**: MapStruct for entity-DTO conversion

## Database Schema

### Tables
- **users**: User accounts with roles (ADMIN, VENDOR, CUSTOMER)
- **categories**: Product categories
- **products**: Product catalog with pricing and availability
- **inventory**: Stock levels and locations
- **orders**: Customer orders
- **order_items**: Line items in orders
- **reviews**: Product reviews and ratings
- **cart**: Shopping carts
- **cart_items**: Items in shopping carts
- **sessions**: User authentication sessions

## API Endpoints

### Authentication
- `POST /api/users/register` - Register new user
- `POST /api/users/login` - Login and get session token
- `POST /api/auth/logout` - Logout current session
- `POST /api/auth/logout-all` - Logout from all devices

### Users
- `GET /api/users` - Get all users (ADMIN)
- `GET /api/users/profile` - Get current user profile
- `PUT /api/users/profile` - Update current user profile
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user (ADMIN)
- `DELETE /api/users/{id}` - Delete user (ADMIN)

### Categories
- `POST /api/categories` - Create category (ADMIN)
- `GET /api/categories` - Get all categories (public)
- `GET /api/categories/{id}` - Get category by ID (public)
- `PUT /api/categories/{id}` - Update category (ADMIN)
- `DELETE /api/categories/{id}` - Delete category (ADMIN)

### Products
- `POST /api/products` - Create product (ADMIN, VENDOR)
- `POST /api/products/bulk` - Create multiple products (ADMIN, VENDOR)
- `GET /api/products` - Get all products with filters (public)
  - Query params: `page`, `size`, `categoryId`, `search`, `minPrice`, `maxPrice`, `inStock`, `sortBy`, `ascending`
- `GET /api/products/{id}` - Get product by ID (public)
- `PUT /api/products/{id}` - Update product (ADMIN, VENDOR)
- `DELETE /api/products/{id}` - Delete product (ADMIN, VENDOR)

### Orders
- `POST /api/orders` - Create order (CUSTOMER, ADMIN)
- `GET /api/orders` - Get all orders (ADMIN, VENDOR)
- `GET /api/orders/user` - Get current user's orders (CUSTOMER)
- `GET /api/orders/{id}` - Get order by ID (CUSTOMER)
- `PUT /api/orders/{id}/status` - Update order status (ADMIN)
- `DELETE /api/orders/{id}` - Delete order (ADMIN)

### Cart
- `GET /api/cart` - Get user's cart (CUSTOMER)
- `POST /api/cart/items` - Add item to cart (CUSTOMER)
- `PUT /api/cart/item/{itemId}` - Update cart item (CUSTOMER)
- `DELETE /api/cart/item/{itemId}` - Remove cart item (CUSTOMER)
- `DELETE /api/cart/clear` - Clear cart (CUSTOMER)
- `POST /api/cart/checkout` - Checkout cart (CUSTOMER)

### Inventory
- `POST /api/inventory` - Create inventory (ADMIN, VENDOR)
- `GET /api/inventory` - Get all inventory (ADMIN, VENDOR)
- `GET /api/inventory/{id}` - Get inventory by ID
- `GET /api/inventory/product/{productId}` - Get inventory by product
- `PUT /api/inventory/{id}` - Update inventory (ADMIN, VENDOR)
- `PATCH /api/inventory/{id}` - Adjust quantity (ADMIN, VENDOR)
- `DELETE /api/inventory/{id}` - Delete inventory (ADMIN)

### Reviews
- `POST /api/reviews` - Create review (CUSTOMER)
- `GET /api/reviews` - Get all reviews
- `GET /api/reviews/{id}` - Get review by ID
- `GET /api/reviews/product/{productId}` - Get reviews by product
- `GET /api/reviews/user` - Get current user's reviews (CUSTOMER)
- `PUT /api/reviews/{id}` - Update review (CUSTOMER)
- `DELETE /api/reviews/{id}` - Delete review (CUSTOMER)

## GraphQL API

### Endpoint
- `POST /graphql` - GraphQL endpoint
- `GET /graphiql` - GraphiQL interface (dev only)

### Sample Queries
```graphql
# Get all products
query {
  allProducts {
    id
    name
    price
    quantity
    categoryName
  }
}

# Get product by ID
query {
  productById(id: 1) {
    id
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

### Sample Mutations
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

## Key Features

### 1. Spring Data JPA Integration
- Repository interfaces extending `JpaRepository`
- Derived query methods (e.g., `findByCategoryId`)
- Custom JPQL queries with `@Query`
- JPA Specifications for dynamic queries
- Pagination and sorting support

### 2. Transaction Management
- `@Transactional` on service methods
- Automatic rollback on exceptions
- Inventory deduction in order creation is transactional
- Ensures data consistency

### 3. Inventory Management
- Automatic inventory deduction when orders are placed
- Stock validation before order creation
- Insufficient stock error handling
- Transaction rollback if inventory update fails

### 4. Search and Filtering
- Backend-based product search (name, description)
- Price range filtering (minPrice, maxPrice)
- Category filtering
- Stock availability filtering
- Combines with pagination and sorting

### 5. Authentication & Authorization
- Session-based authentication with tokens
- Role-based access control (RBAC)
- Custom interceptors for auth and role validation
- Session expiration and cleanup

### 6. Caching
- Custom in-memory cache manager
- Cache hit/miss tracking
- Performance monitoring with AOP
- Cache invalidation on updates

## Setup Instructions

### Prerequisites
- JDK 25
- Maven 3.9+
- PostgreSQL 12+
- Node.js 18+
- npm or yarn

### Backend Setup

1. **Configure Database**
```properties
# src/main/resources/application-dev.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce_db
spring.datasource.username=spycon
spring.datasource.password=myPassword
```

2. **Build and Run**
```bash
cd backend/SmartShop
mvn clean install
mvn spring-boot:run
```

3. **Access APIs**
- REST API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- GraphiQL: http://localhost:8080/graphiql

### Frontend Setup

1. **Install Dependencies**
```bash
cd frontend
npm install
```

2. **Configure API URL**
```bash
# .env.local
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

3. **Run Development Server**
```bash
npm run dev
```

4. **Access Application**
- Frontend: http://localhost:3000

## Testing

### Test User Accounts
```
Admin:
- Email: admin@smartshop.com
- Password: admin123

Vendor:
- Email: vendor@smartshop.com
- Password: vendor123

Customer:
- Email: jane.doe@example.com
- Password: customer123
```

### Testing Order with Inventory Deduction

1. Login as customer
2. Add products to cart
3. Checkout cart
4. Verify inventory quantity decreased in database:
```sql
SELECT quantity FROM inventory WHERE product_id = 1;
```

### Testing GraphQL

1. Open GraphiQL: http://localhost:8080/graphiql
2. Run sample queries from GraphQL API section
3. Test with authentication by adding header:
```
Authorization: Bearer <your-token>
```

## Performance Optimization

### Database Queries
- Indexed columns: product name, category_id, user email
- JPA Specifications for efficient dynamic queries
- Pagination to limit result sets
- Lazy loading for relationships

### Caching
- Product and category data cached
- Cache invalidation on updates
- Performance metrics tracked via AOP

### Custom Sorting
- QuickSort and MergeSort implementations
- In-memory sorting for small datasets
- Database sorting for large datasets

## Error Handling

### Standard Error Response
```json
{
  "timestamp": "2026-01-19T10:30:00.000+00:00",
  "status": 404,
  "message": "Product not found with id: 123",
  "path": "uri=/api/products/123"
}
```

### Validation Error Response
```json
{
  "timestamp": "2026-01-19T10:30:00.000+00:00",
  "status": 400,
  "errors": {
    "email": "must be a well-formed email address",
    "name": "must not be blank"
  },
  "path": "uri=/api/products"
}
```

## Development Guidelines

### Code Style
- Comments in first-person perspective
- No emojis in code or comments
- Minimal, focused implementations
- Follow REST conventions strictly

### Git Workflow
- Incremental commits after each feature
- Commit message format: `type: description`
- Types: feat, fix, refactor, docs, test

### REST Endpoint Standards
```java
// Correct
@PostMapping
@GetMapping
@GetMapping("/{id}")
@PutMapping("/{id}")
@DeleteMapping("/{id}")

// Incorrect
@PostMapping("/add")
@PutMapping("/update/{id}")
```

## Troubleshooting

### Products Not Displaying
- Verify backend is running on port 8080
- Check database has products: `SELECT COUNT(*) FROM products;`
- Clear browser localStorage if seeing auth errors
- Verify CORS configuration allows frontend origin

### GraphQL Not Working
- Ensure GraphiQL is enabled in application-dev.properties
- Check authentication token in request headers
- Verify GraphQL endpoint is not blocked by interceptors

### Order Creation Fails
- Check product availability
- Verify sufficient inventory
- Check user has CUSTOMER role
- Review transaction logs for rollback reasons

## Project Structure

```
smartshopjpa/
├── backend/SmartShop/
│   ├── src/main/java/com/amalitech/smartshop/
│   │   ├── aspects/          # AOP for logging and performance
│   │   ├── cache/            # Custom cache manager
│   │   ├── config/           # Security and app configuration
│   │   ├── controllers/      # REST controllers
│   │   ├── dtos/             # Request/Response DTOs
│   │   ├── entities/         # JPA entities
│   │   ├── enums/            # Enumerations
│   │   ├── exceptions/       # Custom exceptions
│   │   ├── graphql/          # GraphQL resolvers
│   │   ├── interfaces/       # Service interfaces
│   │   ├── mappers/          # MapStruct mappers
│   │   ├── repositories/     # JPA repositories
│   │   ├── services/         # Service implementations
│   │   └── utils/            # Utility classes
│   └── src/main/resources/
│       ├── application.properties
│       ├── application-dev.properties
│       └── graphql/schema.graphqls
└── frontend/
    ├── app/                  # Next.js pages
    ├── lib/                  # API clients and utilities
    └── public/               # Static assets
```

## License

This project is for educational purposes as part of the Spring Data JPA lab assignment.

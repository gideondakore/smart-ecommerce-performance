# SmartShop Frontend

Next.js frontend for the SmartShop e-commerce platform.

## Features

### Public Pages

- **Home** (`/`) - Product listing with category filtering and pagination
- **Product Detail** (`/products/[id]`) - View product details and place orders
- **Login** (`/login`) - User authentication
- **Register** (`/register`) - New user registration

### User Pages (Authenticated)

- **Profile** (`/profile`) - View and edit user profile
- **Orders** (`/orders`) - View order history

### Admin Pages (Admin Role Required)

- **Admin Dashboard** (`/admin`) - Manage products, categories, orders, users, and inventory
- **Add/Edit Product** - Full CRUD operations for products
- **Add/Edit Category** - Full CRUD operations for categories
- **Add/Edit Inventory** - Manage product inventory levels
- **Order Management** - View and update order status

## API Endpoints Covered

### User Management

- POST `/api/users/register` - Register new user
- POST `/api/users/login` - User login
- GET `/api/users/profile` - Get authenticated user profile
- PUT `/api/users/{id}` - Update user profile
- GET `/api/users/all` - Get all users (Admin)
- GET `/api/users/{id}` - Get user by ID
- PUT `/api/users/update/{id}` - Update user (Admin)
- DELETE `/api/users/{id}` - Delete user (Admin)

### Product Management

- POST `/api/products/add` - Add product (Admin)
- POST `/api/products/add/bulk` - Add multiple products (Admin)
- GET `/api/products/public/all` - Get all products with filtering and sorting
- GET `/api/products/{id}` - Get product by ID
- PUT `/api/products/update/{id}` - Update product (Admin)
- DELETE `/api/products/{id}` - Delete product (Admin)

### Category Management

- POST `/api/categories/add` - Add category (Admin)
- GET `/api/categories/public/all` - Get all categories
- GET `/api/categories/{id}` - Get category by ID
- PUT `/api/categories/update/{id}` - Update category (Admin)
- DELETE `/api/categories/{id}` - Delete category (Admin)

### Order Management

- POST `/api/orders/create` - Create order
- GET `/api/orders/all` - Get all orders (Admin)
- GET `/api/orders/user` - Get user orders
- GET `/api/orders/{id}` - Get order by ID
- PUT `/api/orders/update/{id}` - Update order status (Admin)
- DELETE `/api/orders/{id}` - Delete order (Admin)

### Inventory Management

- POST `/api/inventory/add` - Add inventory (Admin)
- GET `/api/inventory/all` - Get all inventories (Admin)
- GET `/api/inventory/{id}` - Get inventory by ID
- GET `/api/inventory/product/{productId}` - Get inventory by product ID
- PUT `/api/inventory/update/{id}` - Update inventory (Admin)
- PATCH `/api/inventory/adjust/{id}` - Adjust inventory quantity (Admin)
- DELETE `/api/inventory/{id}` - Delete inventory (Admin)

## Setup

1. Install dependencies:

```bash
npm install
```

2. Configure environment variables in `.env.local`:

```
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

3. Run the development server:

```bash
npm run dev
```

4. Open [http://localhost:3000](http://localhost:3000)

## Tech Stack

- Next.js 16
- React 19
- TypeScript
- Tailwind CSS
- Context API for state management

### API Usage Strategy:

**GraphQL (Simple Reads):**

- Homepage initial product listing
- Category dropdown
- Product detail by ID

**REST (Complex Operations):**

- Product search with filters
- Pagination
- All mutations (create, update, delete)
- Admin operations
- Cart, orders, reviews

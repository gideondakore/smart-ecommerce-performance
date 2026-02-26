# Smart E-Commerce System - Project Improvements

## Project Overview
Full-stack e-commerce application with Spring Boot backend (Java 21, Spring Data JPA) and Next.js frontend. PostgreSQL database with MCP server integration available.

## Critical Requirements

### 1. API Endpoint Standardization
- Fix non-RESTful endpoints (e.g., `/categories/update/{id}` should be `/categories/{id}` with PUT)
- Remove redundant path segments like `/add`, `/update` from URLs
- Follow REST conventions: POST `/resource`, GET `/resource`, GET `/resource/{id}`, PUT `/resource/{id}`, DELETE `/resource/{id}`
- Update frontend API calls in `/frontend/lib/api.ts` to match corrected endpoints
- Ensure all backend endpoints are actually used by the frontend

### 2. GraphQL Implementation Fix
- Fix broken GraphQL implementation in both backend and frontend
- Ensure GraphQL endpoint at `/graphql` is functional
- Fix queries in `/frontend/lib/graphql.ts`
- Test all GraphQL queries and mutations
- Verify authentication works with GraphQL requests

### 3. Inventory Management
- Implement product quantity deduction when order is placed
- Add transaction management to ensure atomicity
- Handle insufficient stock scenarios with proper error messages
- Update inventory in real-time during checkout process
- Rollback order if inventory update fails

### 4. Backend Search and Filtering
- Move all filtering and searching logic from frontend to backend
- Implement search parameters for products (name, description, price range)
- Add filtering by category, price range, availability
- Implement efficient database queries using Spring Data JPA specifications
- Support multiple filter combinations
- Return filtered results with pagination

### 5. Search Parameters Implementation
- Add `search` query parameter to product endpoints
- Implement fuzzy search on product name and description
- Add price range filters: `minPrice`, `maxPrice`
- Add availability filter: `inStock`
- Combine search with existing sorting and pagination
- Optimize queries with proper indexing

## Code Style Guidelines

### General Rules
- NO emojis anywhere in code, comments, or documentation
- Write comments in first-person perspective (e.g., "I am handling...", "I validate...")
- Write minimal, focused code - avoid verbose implementations
- Only write code that directly contributes to the solution

### Git Workflow
- Make incremental commits after each logical change
- Commit messages format: `feat: description` or `fix: description` or `refactor: description`
- Commit after fixing each endpoint, implementing each feature
- Do not bundle unrelated changes in single commit

### Documentation
- Maintain ONE comprehensive documentation file
- Update existing documentation instead of creating new files
- Document in `/backend/SmartShop/README.md`
- Include: API endpoints, setup instructions, testing guide, architecture overview

## Implementation Checklist

### Phase 1: Endpoint Standardization
- [ ] Audit all controllers for non-standard endpoints
- [ ] Refactor CategoryController endpoints
- [ ] Refactor ProductController endpoints
- [ ] Refactor OrderController endpoints
- [ ] Refactor UserController endpoints
- [ ] Update frontend api.ts to use corrected endpoints
- [ ] Test all endpoints with Postman/frontend
- [ ] Commit: `refactor: standardize REST endpoints`

### Phase 2: GraphQL Fix
- [ ] Review GraphQL schema and resolvers
- [ ] Fix authentication in GraphQL context
- [ ] Test all queries and mutations
- [ ] Update frontend GraphQL queries
- [ ] Verify GraphQL works with frontend
- [ ] Commit: `fix: repair GraphQL implementation`

### Phase 3: Inventory Deduction
- [ ] Add inventory check before order placement
- [ ] Implement quantity deduction in order service
- [ ] Add @Transactional with proper propagation
- [ ] Handle insufficient stock errors
- [ ] Add rollback mechanism
- [ ] Test order placement with inventory updates
- [ ] Commit: `feat: implement inventory deduction on order placement`

### Phase 4: Backend Search & Filtering
- [ ] Create ProductSpecification for dynamic queries
- [ ] Add search parameter to ProductRepository
- [ ] Implement filtering in ProductService
- [ ] Update ProductController with search/filter params
- [ ] Remove frontend filtering logic
- [ ] Update frontend to use backend filtering
- [ ] Test all filter combinations
- [ ] Commit: `feat: implement backend search and filtering`

### Phase 5: Testing & Documentation
- [ ] Test all endpoints with PostgreSQL MCP server
- [ ] Verify frontend integration for all features
- [ ] Test edge cases and error scenarios
- [ ] Update comprehensive documentation
- [ ] Commit: `docs: update comprehensive documentation`

## Technical Implementation Details

### REST Endpoint Standards
```java
// CORRECT
@PostMapping
@GetMapping
@GetMapping("/{id}")
@PutMapping("/{id}")
@DeleteMapping("/{id}")

// INCORRECT - DO NOT USE
@PostMapping("/add")
@PutMapping("/update/{id}")
@GetMapping("/getAll")
```

### Inventory Deduction Pattern
```java
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public OrderResponseDTO createOrder(CreateOrderDTO request) {
    // I validate product availability
    // I deduct inventory quantities
    // I create order
    // Transaction rolls back automatically on any exception
}
```

### Search & Filter Implementation
```java
// I use Specification pattern for dynamic queries
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {}

// I build specifications based on search criteria
Specification<Product> spec = Specification.where(null);
if (search != null) spec = spec.and(nameContains(search));
if (categoryId != null) spec = spec.and(categoryEquals(categoryId));
```

### Frontend API Update Pattern
```typescript
// BEFORE
update: (id: number, data: any) =>
  fetchApi<any>(`/categories/update/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  }),

// AFTER
update: (id: number, data: any) =>
  fetchApi<any>(`/categories/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  }),
```

## Testing Requirements
- Test each feature with actual database operations using PostgreSQL MCP
- Verify frontend successfully calls all backend endpoints
- Test error scenarios and edge cases
- Ensure transactions rollback properly on failures
- Validate search and filtering returns correct results
- Confirm inventory deduction works correctly

## Success Criteria
- All endpoints follow REST standards
- Frontend successfully uses all backend endpoints
- GraphQL queries and mutations work correctly
- Orders deduct product inventory automatically
- Search and filtering work from backend
- No emojis in codebase
- Comments use first-person perspective
- Incremental commits made throughout
- Single comprehensive documentation file maintained
- All features tested and verified working

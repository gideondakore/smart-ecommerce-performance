---
description: Smart E-Commerce System - Lab 6 Spring Data JPA Project Guidelines
applyTo: "**/*.java"
---

# Smart E-Commerce System - Lab 6: Spring Data JPA

## Project Context

This is an advanced Spring Boot e-commerce application integrating Spring Data JPA with focus on repository abstraction, query optimization, and transaction management. The project builds upon a layered architecture (Controller → Service → Repository) with pagination, sorting, and caching mechanisms.

## Technical Stack

- **Framework:** Spring Boot 3.x (Spring Data JPA, Spring Cache, Validation, AOP)
- **Language:** Java 21
- **Database:** PostgreSQL
- **Architecture:** Layered (Controller → Service → Repository)
- **Repositories:** Extend JpaRepository or CrudRepository
- **Transactions:** @Transactional with propagation and rollback rules
- **Queries:** Derived queries, JPQL, and native SQL
- **Caching:** Spring Cache (@EnableCaching, @Cacheable, @CacheEvict)

## Domain Entities

- User
- Product
- Category
- Order
- OrderItem
- Review

## Coding Guidelines

### Repository Layer

- All repositories must extend JpaRepository or CrudRepository
- Implement derived query methods (e.g., findByCategoryName, findByPriceBetween)
- Use @Query annotation for custom JPQL and native SQL queries
- Implement pagination using Pageable interface
- Support sorting in all list operations

### Service Layer

- Apply @Transactional on service methods with appropriate propagation and isolation levels
- Handle rollback scenarios explicitly
- Implement business logic for CRUD operations
- Integrate caching strategies using @Cacheable and @CacheEvict

### Controller Layer

- Return paginated responses for product listings and orders
- Support sorting and filtering query parameters
- Follow RESTful conventions

### Entity Annotations

- Use @Entity, @Id, and appropriate relationship mappings (@OneToMany, @ManyToOne, @ManyToMany)
- Apply proper cascade types and fetch strategies
- Include validation annotations where needed

### Query Development

- Prefer derived queries for simple operations
- Use JPQL for complex queries requiring joins
- Use native SQL only when JPQL is insufficient
- Optimize queries for performance

### Caching Strategy

- Enable caching with @EnableCaching
- Cache frequently accessed product and category data
- Implement cache eviction after create/update/delete operations
- Document cache configuration

### Performance Optimization

- Implement efficient pagination and sorting
- Use appropriate fetch strategies to avoid N+1 queries
- Apply indexing considerations in query design
- Measure and document performance improvements

## Key Features to Implement

1. CRUD operations through repositories
2. Pagination and sorting for product browsing
3. Custom queries for complex e-commerce operations
4. Transaction management with rollback handling
5. Caching for read-heavy operations
6. Performance optimization for searching and filtering

## Code Quality Standards

- Maintain clean, maintainable code structure
- Document repository methods and custom queries
- Explain transaction handling and rollback strategies
- Include comprehensive comments for complex logic
- Follow Java naming conventions
- Write minimal, focused implementations

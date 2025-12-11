# Product Order Services

A Spring Boot application for managing products, users, and orders with intelligent discount computation. This application showcases enterprise-level practices including JWT authentication, role-based access control, multi-database support, Redis caching, structured JSON logging with correlation IDs, and comprehensive test coverage.

## Key Features

- **RESTful API** with OpenAPI/Swagger documentation
- **JWT Authentication** with role-based authorization (USER, PREMIUM_USER, ADMIN)
- **Dynamic Discount System** using Strategy Design Pattern
- **Multi-Database Support** (H2 for development, PostgreSQL for production)
- **Redis Caching** with intelligent fallback to in-memory cache
- **Structured JSON Logging** with correlation ID tracking for distributed tracing
- **Database Migrations** with Flyway for version control
- **Docker Support** with multi-stage builds and health checks
- **Comprehensive Testing** (26 tests with 67% service layer coverage)
- **Pagination & Filtering** on all list endpoints
- **Soft Delete** for data integrity
- **Production-Ready** with actuator health checks and monitoring

## Table of Contents

- [Setup Instructions](#setup-instructions)
- [API Documentation](#api-documentation)
- [Design Decisions](#design-decisions)
- [Running Tests](#running-tests)
- [Configuration](#configuration)

## Setup Instructions

### Prerequisites
- **Java 17** or higher
- **Maven 3.6+**
- **Docker & Docker Compose** (optional, for containerized deployment)
- **Redis** (optional, for caching - can run without it in dev mode)

### Running with H2 Database (Development - Default Profile: local)

1. **Clone and build the project:**
   ```bash
   mvn clean install
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   # Or explicitly specify the local profile:
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

3. **Access the application:**
   - API Base URL: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - H2 Console: `http://localhost:8080/h2-console`
     - JDBC URL: `jdbc:h2:mem:productorderdb`
     - Username: `sa`
     - Password: (leave empty)
   - Actuator Health: `http://localhost:8080/actuator/health`

### Running with Docker Compose (Production-like)

```bash
docker-compose up --build
```

This will start:
- **PostgreSQL** database on port 5432
- **Redis** cache on port 6379
- **Application** on port 8080

### Running with PostgreSQL Locally (Profile: dev)

1. **Start PostgreSQL and Redis:**
   ```bash
   docker-compose up -d postgres redis
   ```

2. **Run the application with dev profile:**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

The dev profile automatically configures:
- PostgreSQL database connection
- Redis cache
- PostgreSQL-specific Flyway migrations

### Application Profiles

The application supports multiple profiles:

| Profile | Database | Cache | Flyway Migrations | Use Case |
|---------|----------|-------|-------------------|----------|
| **local** (default) | H2 (in-memory) | Simple (in-memory) | `db/migration/h2` | Local development |
| **dev** | PostgreSQL | Redis | `db/migration/postgres` | Development with production-like setup |
| **test** | H2 (in-memory) | Simple (in-memory) | `db/migration/h2` | Automated testing |

## API Documentation

### Sample Users (Pre-loaded)
The application comes with sample data:

| Username | Password | Role | Email |
|----------|----------|------|-------|
| admin | password123 | ADMIN | admin@example.com |
| premiumuser | password123 | PREMIUM_USER | premium@example.com |
| regularuser | password123 | USER | user@example.com |

### Quick Start API Flow

1. **Register a new user:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "username": "newuser",
       "email": "newuser@example.com",
       "password": "password123",
       "role": "USER"
     }'
   ```

   **Response:**
   ```json
   {
     "id": 4,
     "username": "newuser",
     "email": "newuser@example.com",
     "role": "USER",
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
   }
   ```

2. **Login to get JWT token:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "username": "newuser",
       "password": "password123"
     }'
   ```

   **Response:**
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "type": "Bearer",
     "username": "newuser",
     "role": "USER"
   }
   ```

3. **Get products (with token):**
   ```bash
   curl -X GET 'http://localhost:8080/api/products?page=0&size=10' \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

   **Response:**
   ```json
   {
     "content": [
       {
         "id": 1,
         "name": "Laptop",
         "description": "High-performance laptop",
         "price": 999.99,
         "quantity": 50,
         "available": true,
         "createdAt": "2025-12-11T10:00:00",
         "updatedAt": "2025-12-11T10:00:00"
       }
     ],
     "page": 0,
     "size": 10,
     "totalItems": 5,
     "totalPages": 1
   }
   ```

4. **Place an order:**
   ```bash
   curl -X POST http://localhost:8080/api/orders \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "items": [
         {"productId": 1, "quantity": 2},
         {"productId": 2, "quantity": 1}
       ]
     }'
   ```

   **Response:**
   ```json
   {
     "id": 1,
     "userId": 4,
     "username": "newuser",
     "items": [
       {
         "id": 1,
         "productId": 1,
         "productName": "Laptop",
         "quantity": 2,
         "unitPrice": 999.99,
         "discountApplied": 0.00,
         "totalPrice": 1999.98
       },
       {
         "id": 2,
         "productId": 2,
         "productName": "Mouse",
         "quantity": 1,
         "unitPrice": 29.99,
         "discountApplied": 0.00,
         "totalPrice": 29.99
       }
     ],
     "orderTotal": 2029.97,
     "totalDiscount": 0.00,
     "createdAt": "2025-12-11T18:30:00",
     "updatedAt": "2025-12-11T18:30:00"
   }
   ```

5. **Search products:**
   ```bash
   curl -X GET 'http://localhost:8080/api/products/search?name=laptop&minPrice=500&maxPrice=1500&available=true' \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

6. **Create product (ADMIN only):**
   ```bash
   curl -X POST http://localhost:8080/api/products \
     -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "name": "Keyboard",
       "description": "Mechanical keyboard",
       "price": 129.99,
       "quantity": 100
     }'
   ```

### API Endpoints

#### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

#### Products (All endpoints support pagination with `page`, `size`, `sort`, `direction` params)
- `GET /api/products?page=0&size=20&sort=createdAt&direction=DESC` - List all products (paginated)
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/search?name={name}&minPrice={min}&maxPrice={max}&available={true/false}` - Search products
- `POST /api/products` - Create product (ADMIN only)
- `PUT /api/products/{id}` - Update product (ADMIN only)
- `DELETE /api/products/{id}` - Soft delete product (ADMIN only)

#### Orders (Paginated endpoints support `page`, `size`, `sort`, `direction` params)
- `POST /api/orders` - Place a new order
- `GET /api/orders/{id}` - Get order by ID (own orders or ADMIN)
- `GET /api/orders/my-orders?page=0&size=10` - Get current user's orders (paginated)
- `GET /api/orders?page=0&size=10` - Get all orders (ADMIN only, paginated)

### Pagination Response Format

All paginated endpoints return a consistent response structure:

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalItems": 100,
  "totalPages": 5
}
```

### Error Response Format

All errors return a consistent structure:

```json
{
  "timestamp": "2025-12-11T18:40:25.123+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/products",
  "details": {
    "name": "Product name is required",
    "price": "Price must be positive"
  }
}
```

## Design Decisions

### Architecture
- **Layered Architecture**: Controller → Service → Repository pattern for clear separation of concerns
- **DTO Pattern**: Separate request/response DTOs from entities to decouple API contracts from data models
- **Strategy Pattern**: Discount calculation using strategy pattern for flexibility and extensibility

### Discount Calculation Strategy
The discount system uses the **Strategy Design Pattern** with the following implementations:

1. **NoDiscountStrategy**: For regular users (USER role)
2. **PremiumUserDiscountStrategy**: 10% discount for PREMIUM_USER role
3. **HighValueOrderDiscountStrategy**: 5% discount for orders over $500 (applies to all users)

Strategies are automatically applied and can be combined. For example, a premium user ordering $600 worth of products gets both discounts (15% total).

#### Discount Examples

| User Role | Order Subtotal | Discount Applied | Final Total | Calculation |
|-----------|---------------|------------------|-------------|-------------|
| USER | $300 | $0 (0%) | $300.00 | No discount |
| USER | $600 | $30 (5%) | $570.00 | High-value order discount only |
| PREMIUM_USER | $300 | $30 (10%) | $270.00 | Premium user discount only |
| PREMIUM_USER | $600 | $90 (15%) | $510.00 | Premium (10%) + High-value (5%) |
| ADMIN | $700 | $35 (5%) | $665.00 | High-value order discount only |

**Implementation Details:**
- `DiscountCalculator` iterates through all registered `DiscountStrategy` beans
- Each strategy checks if it's applicable (`isApplicable()` method)
- Applicable strategies calculate their discount amount
- All discounts are summed and applied to the order
- New discount strategies can be added by implementing `DiscountStrategy` interface and marking with `@Component`

### Security
- **JWT-based authentication** with role-based access control
- **Password encryption** using BCrypt
- **Stateless sessions** for scalability
- **Method-level security** with @PreAuthorize annotations

### Database
- **Flyway migrations** for version-controlled schema changes
- **Multi-database support**: Separate migration files for H2 (`db/migration/h2`) and PostgreSQL (`db/migration/postgres`)
- **Soft delete** for products (deleted flag instead of actual deletion)
- **JPA Auditing** for automatic timestamp management (`@CreatedDate`, `@LastModifiedDate`)
- **Indexed fields** for optimized queries

### Caching
- **Optional Redis integration** with Spring Cache abstraction
- **Fallback to simple cache** when Redis is unavailable (configured via `@ConditionalOnProperty`)
- **Product caching** to reduce database load
- **Configurable TTL** (10 minutes default)

### Logging & Observability
- **JSON structured logging** using Logstash Logback Encoder for production-ready log aggregation
- **Correlation ID tracking**: Unique `X-Correlation-Id` header for each request (auto-generated or client-provided)
- **Request/Response logging**: Comprehensive HTTP logging with sanitization of sensitive fields
- **Configurable sensitive field masking**: Password, token, authorization fields masked in logs
- **MDC (Mapped Diagnostic Context)**: Correlation ID propagated throughout the request lifecycle
- **Excluded paths**: Health checks, H2 console, Swagger UI excluded from logging

Log format example:
```json
{
  "timestamp": "2025-12-11T18:40:25.123Z",
  "level": "INFO",
  "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "message": "HTTP_REQUEST",
  "request": {
    "method": "POST",
    "url": "http://localhost:8080/api/orders",
    "headers": {...},
    "body": {"items": [...]}
  }
}
```

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Classes
```bash
mvn test -Dtest=OrderServiceTest,ProductServiceTest
```

### Run Tests with Coverage Report
```bash
mvn test jacoco:report
```

Coverage report will be generated at: `target/site/jacoco/index.html`

### Test Coverage Summary

Current test coverage metrics:
- **Line Coverage: 42%**
- **Method Coverage: 43%**
- **Instruction Coverage: 26%**
- **Branch Coverage: 2%**

The project includes:
- **26 Tests** (100% passing):
  - OrderServiceTest (8 tests) - Order creation, validation, pagination
  - ProductServiceTest (9 tests) - CRUD operations, search, soft delete
  - DiscountCalculatorTest (6 tests) - All discount strategies
  - AuthIntegrationTest (3 tests) - Login failure, validation errors
- **Test coverage** for critical business logic (service layer: 67%)

## Configuration

### Application Properties

Key configuration properties that can be customized:

```yaml
# JWT Configuration
jwt:
  secret: your-secret-key-here
  expiration: 86400000  # 24 hours in milliseconds

# Logging Configuration
logging:
  sensitive-fields:
    - password
    - token
    - authorization
    - secret

# Cache Configuration (dev profile)
spring:
  cache:
    type: redis  # or 'simple' for in-memory
  data:
    redis:
      host: localhost
      port: 6379

# Database Configuration
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/productorderdb
    username: postgres
    password: postgres
```

### Environment Variables

The application can be configured using environment variables:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/productorderdb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# Active Profile
SPRING_PROFILES_ACTIVE=dev
```
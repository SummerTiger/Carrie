# Vending Inventory System - Implementation Guide

## 🎯 Project Status: Foundation Complete

### ✅ Completed Components

#### 1. Database Layer (100%)
- **Flyway Migrations**: Complete schema with all tables, indexes, and relationships
- **Seed Data**: Default admin user, sample machines, products, and procurement data
- **Schema Features**:
  - UUID primary keys for distributed systems
  - Proper foreign key constraints
  - Optimized indexes for common queries
  - HST tax calculations at database level

#### 2. Entity Layer (100%)
- **8 Complete JPA Entities**:
  - `VendingMachine` - Machine configuration with embedded Location
  - `Product` - Centralized product catalog with batch tracking
  - `ProcurementBatch` - Supplier purchases with automatic HST calculation
  - `ProcurementItem` - Line items with cost tracking
  - `RestockingLog` - Site visit records
  - `RestockItem` - Individual product restocking records
  - `MachineProductPrice` - Custom pricing per machine/product
  - `User` - Authentication with Spring Security UserDetails

- **Key Features**:
  - Bidirectional relationships with proper cascade rules
  - Lifecycle callbacks for automatic calculations
  - Business logic methods (isLowStock, recalculateTotals, etc.)
  - Embedded value objects (Location, InventoryStatus)
  - Audit timestamps (@CreationTimestamp, @UpdateTimestamp)

#### 3. Repository Layer (100%)
- **6 Spring Data JPA Repositories**:
  - Custom query methods using @Query
  - Derived query methods
  - Pagination support
  - Complex filtering and search capabilities

- **Notable Queries**:
  - Low stock alerts
  - Machine product restrictions
  - Procurement cost analytics
  - Restocking history by machine
  - Category-based filtering

#### 4. DTO Layer (100%)
- **9 Java Records** for type-safe DTOs:
  - VendingMachineDto
  - ProductDto
  - ProcurementBatchDto
  - ProcurementItemDto
  - RestockingLogDto
  - RestockItemDto
  - UserDto
  - LoginRequest
  - AuthResponse

- **Features**:
  - Java 17+ record syntax
  - Jakarta validation annotations
  - Builder pattern support
  - Immutable by design

#### 5. Configuration Files (100%)
- **application.yml**: Complete configuration with:
  - Database connection pooling (HikariCP)
  - Flyway migration settings
  - JWT authentication settings
  - CORS configuration
  - Logging configuration
  - Spring Boot Actuator endpoints
  - OpenAPI/Swagger documentation

- **pom.xml**: All required dependencies:
  - Spring Boot 3.2.5
  - Spring Data JPA
  - Spring Security
  - MySQL Connector
  - Flyway
  - JWT (JJWT 0.12.5)
  - Springdoc OpenAPI
  - Lombok

#### 6. Deployment Configuration (100%)
- **Docker Compose**: Multi-container setup
  - MySQL 8.0 with health checks
  - Backend Spring Boot service
  - Frontend React service
  - Optional Prometheus & Grafana
  - Volume management
  - Network isolation

- **Dockerfile**: Multi-stage build for backend
  - Maven build optimization
  - Alpine Linux for small image size
  - Non-root user for security
  - Health check endpoint
  - Container-optimized JVM settings

## 🔨 Next Implementation Steps

### Phase 1: Complete Backend Services (4-6 hours)

#### Service Layer
Create these service classes in `src/main/java/com/vending/service/`:

1. **VendingMachineService.java**
```java
@Service
public class VendingMachineService {
    // CRUD operations
    // Find machines by location/category
    // Validate product restrictions
    // Get machine inventory status
}
```

2. **ProductService.java**
```java
@Service
public class ProductService {
    // CRUD operations
    // Low stock alerts
    // Stock adjustment
    // Category management
    // Price management
}
```

3. **ProcurementService.java**
```java
@Service
public class ProcurementService {
    // Create/manage batches
    // HST calculation
    // Supplier tracking
    // Cost analytics
}
```

4. **RestockingService.java**
```java
@Service
public class RestockingService {
    // Create restocking logs
    // Update product stock
    // Machine inventory snapshots
    // Cash collection tracking
}
```

5. **UserService.java** & **AuthService.java**
```java
@Service
public class AuthService {
    // JWT token generation
    // User authentication
    // Password encoding
    // Token validation
}
```

#### Exception Handling
Create in `src/main/java/com/vending/exception/`:
- `ResourceNotFoundException.java`
- `DuplicateResourceException.java`
- `InvalidOperationException.java`
- `GlobalExceptionHandler.java` with @ControllerAdvice

### Phase 2: REST Controllers (3-4 hours)

Create in `src/main/java/com/vending/controller/`:

1. **AuthController.java**
   - POST /api/auth/login
   - POST /api/auth/register
   - POST /api/auth/refresh
   - GET /api/auth/me

2. **VendingMachineController.java**
   - GET /api/machines (with pagination)
   - GET /api/machines/{id}
   - POST /api/machines
   - PUT /api/machines/{id}
   - DELETE /api/machines/{id}
   - GET /api/machines/active
   - GET /api/machines/{id}/products

3. **ProductController.java**
   - GET /api/products
   - GET /api/products/{id}
   - POST /api/products
   - PUT /api/products/{id}
   - DELETE /api/products/{id}
   - GET /api/products/low-stock
   - GET /api/products/search

4. **ProcurementController.java**
   - GET /api/procurement/batches
   - GET /api/procurement/batches/{id}
   - POST /api/procurement/batches
   - GET /api/procurement/suppliers
   - GET /api/procurement/analytics

5. **RestockingController.java**
   - GET /api/restocking/logs
   - GET /api/restocking/logs/{id}
   - POST /api/restocking/logs
   - GET /api/restocking/machine/{machineId}

### Phase 3: Security Configuration (2-3 hours)

Create in `src/main/java/com/vending/security/`:

1. **JwtTokenProvider.java**
   - Token generation
   - Token validation
   - Extract claims

2. **JwtAuthenticationFilter.java**
   - Filter chain for JWT validation
   - Extract token from header
   - Set SecurityContext

3. **SecurityConfig.java**
   - Configure HTTP security
   - Define endpoint permissions
   - CORS configuration
   - Password encoder bean

4. **UserDetailsServiceImpl.java**
   - Load user by username
   - Convert User entity to UserDetails

### Phase 4: Frontend React Application (8-12 hours)

#### Setup
```bash
cd frontend
npm create vite@latest . -- --template react-ts
npm install axios react-router-dom react-query @tanstack/react-table
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```

#### Pages to Create
1. **Login.tsx** - Authentication page
2. **Dashboard.tsx** - Main overview with metrics
3. **MachineList.tsx** - DataGrid of machines
4. **MachineDetail.tsx** - Single machine view with products
5. **ProductInventory.tsx** - Product CRUD with stock levels
6. **ProcurementLog.tsx** - Purchase history and forms
7. **RestockingLog.tsx** - Site visit logging
8. **Settings.tsx** - User management and configuration

#### Services
```typescript
// src/services/api.ts
import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add JWT interceptor
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### Phase 5: Mobile App (React Native) (12-16 hours)

#### Setup
```bash
npx react-native init VendingMobile --template react-native-template-typescript
cd VendingMobile
npm install @react-navigation/native @react-navigation/stack
npm install react-native-vision-camera react-native-sqlite-storage
npm install axios @tanstack/react-query
```

#### Key Screens
1. **Login.tsx**
2. **MachineSelector.tsx**
3. **RestockForm.tsx**
4. **BarcodeScanner.tsx**
5. **OfflineQueue.tsx**
6. **SyncStatus.tsx**

#### Offline Support
```typescript
// Use SQLite for local storage
// Queue API calls when offline
// Background sync when online
// Conflict resolution strategies
```

### Phase 6: Testing (4-6 hours)

#### Backend Tests
```java
// src/test/java/com/vending/
// - Unit tests for services
// - Integration tests for repositories
// - Controller tests with MockMvc
// - Security tests
```

#### Frontend Tests
```typescript
// Using Vitest + React Testing Library
// - Component tests
// - Integration tests
// - E2E tests with Playwright
```

## 📋 Development Checklist

### Backend
- [ ] Implement all service classes
- [ ] Create exception handlers
- [ ] Build REST controllers
- [ ] Configure Spring Security
- [ ] Add JWT authentication
- [ ] Write unit tests (>80% coverage)
- [ ] Add integration tests
- [ ] Configure logging
- [ ] Set up actuator endpoints
- [ ] Generate API documentation

### Frontend
- [ ] Initialize Vite project
- [ ] Configure Tailwind CSS
- [ ] Set up routing
- [ ] Create authentication flow
- [ ] Build dashboard page
- [ ] Implement machine management
- [ ] Create product inventory UI
- [ ] Build procurement interface
- [ ] Develop restocking forms
- [ ] Add charts and analytics

### Mobile
- [ ] Initialize React Native project
- [ ] Set up navigation
- [ ] Implement authentication
- [ ] Create barcode scanner
- [ ] Build offline storage
- [ ] Develop sync mechanism
- [ ] Add camera integration
- [ ] Implement form validation

### DevOps
- [ ] Create CI/CD pipeline (GitHub Actions)
- [ ] Set up AWS EC2 deployment
- [ ] Configure AWS RDS
- [ ] Set up SSL certificates
- [ ] Configure CloudFront CDN
- [ ] Set up monitoring (CloudWatch)
- [ ] Create backup strategy
- [ ] Document deployment process

## 🚀 Deployment Steps

### 1. Local Development
```bash
# Start MySQL
docker-compose up mysql

# Run backend
cd backend
mvn spring-boot:run

# Run frontend (in another terminal)
cd frontend
npm run dev
```

### 2. Production Deployment

#### AWS RDS Setup
```bash
# Create MySQL instance
aws rds create-db-instance \
  --db-instance-identifier vending-prod \
  --db-instance-class db.t3.small \
  --engine mysql \
  --allocated-storage 50 \
  --master-username admin \
  --master-user-password <secure-password>
```

#### EC2 Deployment
```bash
# Build JAR
mvn clean package -DskipTests

# Deploy to EC2
scp target/*.jar ec2-user@<ec2-ip>:/opt/vending/

# Run with systemd
sudo systemctl start vending-api
sudo systemctl enable vending-api
```

#### Frontend Deployment (S3 + CloudFront)
```bash
# Build production bundle
cd frontend
npm run build

# Deploy to S3
aws s3 sync dist/ s3://your-bucket-name/

# Invalidate CloudFront cache
aws cloudfront create-invalidation \
  --distribution-id <distribution-id> \
  --paths "/*"
```

## 📊 Performance Optimization

### Database
- [ ] Add composite indexes for common queries
- [ ] Implement database connection pooling
- [ ] Enable query caching
- [ ] Optimize N+1 queries with fetch joins

### Backend
- [ ] Enable HTTP response compression
- [ ] Implement Redis caching
- [ ] Use async processing for heavy operations
- [ ] Enable virtual threads (Java 21)

### Frontend
- [ ] Code splitting by route
- [ ] Lazy load components
- [ ] Optimize bundle size
- [ ] Use React.memo for expensive components

## 🔐 Security Checklist

- [ ] Change default admin password
- [ ] Use strong JWT secret (256+ bits)
- [ ] Enable HTTPS in production
- [ ] Implement rate limiting
- [ ] Add CSRF protection
- [ ] Sanitize all user inputs
- [ ] Use prepared statements (JPA does this)
- [ ] Implement audit logging
- [ ] Regular dependency updates
- [ ] Security headers (HSTS, CSP, etc.)

## 📞 Support & Resources

### Documentation
- Spring Boot: https://spring.io/projects/spring-boot
- React: https://react.dev
- React Native: https://reactnative.dev
- Tailwind CSS: https://tailwindcss.com

### Tools
- Postman: For API testing
- DBeaver: For database management
- React DevTools: For debugging
- Docker Desktop: For containerization

## 🎓 Learning Resources

### Backend
- Spring Security with JWT
- JPA relationships and cascading
- Flyway migrations
- RESTful API design

### Frontend
- React Hooks
- TypeScript
- React Query for data fetching
- Tailwind CSS patterns

### DevOps
- Docker multi-stage builds
- AWS deployment strategies
- CI/CD with GitHub Actions
- Monitoring and logging

# Vending Machine Inventory & Cost Management System

A production-ready full-stack application for managing vending machine inventory, procurement, restocking operations, and cost tracking with HST tax management for Ontario.

## 🎯 Features

### Backend (Java Spring Boot)
- **Centralized Inventory Management** - Track products across all machines
- **Machine Configuration** - Define allowed/forbidden product categories per machine
- **Procurement Tracking** - Manage supplier purchases with batch tracking
- **Restocking Logs** - Record site visits and inventory replenishment
- **HST Tax Management** - Automatic Ontario HST (13%) calculation and tracking
- **Custom Pricing** - Machine-specific product pricing overrides
- **User Management** - Role-based access control (ADMIN, OPERATOR, VIEWER)
- **RESTful API** - Complete OpenAPI/Swagger documentation
- **Real-time Alerts** - Low stock notifications and inventory warnings

### Frontend (React + TypeScript)
- **Admin Dashboard** - Comprehensive operations management interface
- **Product Inventory** - CRUD operations with batch history
- **Machine Overview** - DataGrid with filters and status tracking
- **Restocking Interface** - Site visit forms with real-time updates
- **Procurement Logs** - Supplier management and invoice tracking
- **Analytics** - Cost analysis and profit margin reports

### Mobile (React Native - iOS)
- **Offline-First** - SQLite local storage with background sync
- **Barcode Scanning** - Quick product identification
- **Site Visit Workflow** - Guided restocking checklists
- **Photo Capture** - Document machine issues
- **Cash Collection** - Track payments during visits

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- MySQL 8.0+
- Node.js 18+ (for frontend)
- Docker & Docker Compose (optional)

### Backend Setup

1. **Clone and navigate to backend:**
```bash
cd backend
```

2. **Configure database** (Edit `src/main/resources/application.yml` or set environment variables):
```yaml
DATABASE_URL=jdbc:mysql://localhost:3306/vending_inventory
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password
```

3. **Build and run:**
```bash
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

4. **Access Swagger UI:**
```
http://localhost:8080/swagger-ui.html
```

### Using Docker Compose

```bash
docker-compose up -d
```

This starts:
- MySQL database on port 3306
- Backend API on port 8080
- Frontend on port 3000

### Database Migration

Flyway automatically runs migrations on startup. Manual execution:
```bash
mvn flyway:migrate
```

## 📁 Project Structure

```
vending-inventory-system/
├── backend/
│   ├── src/main/java/com/vending/
│   │   ├── entity/          # JPA entities with relationships
│   │   ├── repository/      # Spring Data JPA repositories
│   │   ├── service/         # Business logic layer
│   │   ├── controller/      # REST API controllers
│   │   ├── dto/             # Data Transfer Objects (Java Records)
│   │   ├── security/        # JWT authentication & authorization
│   │   ├── exception/       # Custom exceptions and handlers
│   │   └── config/          # Spring configuration classes
│   ├── src/main/resources/
│   │   ├── db/migration/    # Flyway SQL migration scripts
│   │   └── application.yml  # Application configuration
│   └── pom.xml              # Maven dependencies
├── frontend/
│   ├── src/
│   │   ├── components/      # Reusable React components
│   │   ├── pages/           # Page-level components
│   │   ├── services/        # API client services
│   │   ├── hooks/           # Custom React hooks
│   │   └── utils/           # Utility functions
│   └── package.json
├── mobile/
│   └── [React Native iOS app structure]
├── docker-compose.yml
└── README.md
```

## 🗄️ Database Schema

### Core Tables
- **users** - User accounts with role-based access
- **vending_machines** - Machine configurations and locations
- **products** - Centralized product catalog
- **machine_product_prices** - Custom pricing per machine
- **procurement_batches** - Supplier purchase records
- **procurement_items** - Line items in procurement batches
- **restocking_logs** - Site visit records
- **restock_items** - Products restocked during visits

### Key Relationships
- One machine → Many product prices
- One product → Many machine prices
- One procurement batch → Many procurement items
- One restocking log → Many restock items
- Many-to-many: batches ↔ products

## 🔐 Authentication & Security

### JWT Token Authentication
Default credentials:
- **Username:** admin
- **Password:** admin123

### API Endpoints (Secured)
```bash
# Login
POST /api/auth/login
Content-Type: application/json
{
  "username": "admin",
  "password": "admin123"
}

# Use returned JWT token in headers
Authorization: Bearer <your-jwt-token>
```

### User Roles
- **ADMIN** - Full system access
- **OPERATOR** - Can restock machines and manage inventory
- **VIEWER** - Read-only access

## 📊 API Endpoints

### Vending Machines
- `GET /api/machines` - List all machines
- `GET /api/machines/{id}` - Get machine details
- `POST /api/machines` - Create new machine
- `PUT /api/machines/{id}` - Update machine
- `DELETE /api/machines/{id}` - Delete machine
- `GET /api/machines/active` - Get active machines only

### Products
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product details
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product
- `GET /api/products/low-stock` - Get low stock alerts
- `GET /api/products/search?q={term}` - Search products

### Procurement
- `GET /api/procurement/batches` - List procurement batches
- `POST /api/procurement/batches` - Create new batch
- `GET /api/procurement/batches/{id}` - Get batch details
- `GET /api/procurement/suppliers` - List all suppliers

### Restocking
- `GET /api/restocking/logs` - List restocking logs
- `POST /api/restocking/logs` - Create new log
- `GET /api/restocking/logs/machine/{id}` - Get logs by machine
- `GET /api/restocking/logs/recent` - Get recent logs

## 🧪 Testing

### Run Unit Tests
```bash
cd backend
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Test Coverage
Target: >80% code coverage

## 🚢 Deployment

### AWS Deployment (EC2 + RDS)

1. **Create RDS MySQL Instance:**
```bash
aws rds create-db-instance \
  --db-instance-identifier vending-db \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --master-username admin \
  --master-user-password YourSecurePassword \
  --allocated-storage 20
```

2. **Deploy to EC2:**
```bash
# Build JAR
mvn clean package -DskipTests

# Copy to EC2
scp target/inventory-system-1.0.0.jar ec2-user@your-ec2-ip:/home/ec2-user/

# SSH and run
ssh ec2-user@your-ec2-ip
java -jar inventory-system-1.0.0.jar \
  --DATABASE_URL=jdbc:mysql://your-rds-endpoint:3306/vending_inventory \
  --DATABASE_USERNAME=admin \
  --DATABASE_PASSWORD=YourSecurePassword
```

### Docker Production Build
```bash
# Build image
docker build -t vending-inventory:latest .

# Run container
docker run -d \
  -p 8080:8080 \
  -e DATABASE_URL=jdbc:mysql://db:3306/vending_inventory \
  -e DATABASE_USERNAME=root \
  -e DATABASE_PASSWORD=password \
  --name vending-api \
  vending-inventory:latest
```

## 🔧 Configuration

### Environment Variables
```bash
# Database
DATABASE_URL=jdbc:mysql://localhost:3306/vending_inventory
DATABASE_USERNAME=root
DATABASE_PASSWORD=password

# JWT Secret (Change in production!)
JWT_SECRET=your-256-bit-secret-key-here

# CORS Origins
CORS_ORIGINS=http://localhost:3000,https://your-frontend-domain.com

# Logging
LOG_LEVEL=INFO
```

### Application Properties
Key configuration options in `application.yml`:

```yaml
app:
  inventory:
    low-stock-threshold: 0.20  # 20%
    reorder-threshold: 0.15     # 15%

  tax:
    hst-rate: 0.13              # Ontario HST

  jwt:
    expiration: 86400000        # 24 hours
```

## 📈 Business Logic

### HST Tax Calculation
- Automatic 13% HST for non-exempt products
- Tax-exempt items: Basic groceries, beverages (per Ontario regulations)
- Batch-level HST tracking and reporting

### Inventory Alerts
- **Low Stock**: Current stock < 20% of minimum stock
- **Out of Stock**: Current stock = 0
- Automatic notifications on dashboard

### Profit Margin Analysis
- Weighted average cost from procurement batches
- Machine-specific pricing vs. base cost
- Real-time profitability metrics

## 🎨 Frontend Development

### Install Dependencies
```bash
cd frontend
npm install
```

### Development Server
```bash
npm run dev
```

### Build for Production
```bash
npm run build
```

### Tech Stack
- React 18
- TypeScript
- Tailwind CSS
- React Router
- Axios
- React Query
- Recharts (for analytics)

## 📱 Mobile App Development

### iOS Setup
```bash
cd mobile
npm install
cd ios && pod install && cd ..
npx react-native run-ios
```

### Features
- Offline data storage with SQLite
- Background sync when online
- Push notifications for alerts
- Camera integration for barcode scanning

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open Pull Request

## 📝 License

This project is licensed under the MIT License.

## 🐛 Troubleshooting

### Database Connection Issues
```bash
# Test MySQL connection
mysql -h localhost -u root -p

# Verify database exists
SHOW DATABASES;
```

### Flyway Migration Errors
```bash
# Clean and re-run migrations
mvn flyway:clean flyway:migrate
```

### JWT Token Errors
- Ensure JWT_SECRET is set
- Check token expiration time
- Verify Authorization header format: `Bearer <token>`

## 📧 Support

For issues and questions:
- GitHub Issues: [Project Issues](https://github.com/your-org/vending-inventory/issues)
- Email: support@vendingsystem.com

## 🗺️ Roadmap

- [ ] Real-time WebSocket updates
- [ ] Advanced analytics dashboard
- [ ] Automated reordering
- [ ] Multi-language support
- [ ] Mobile app for Android
- [ ] Machine telemetry integration
- [ ] Payment gateway integration
- [ ] Advanced reporting (PDF/Excel export)

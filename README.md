# Vending Inventory Management System

A complete full-stack application for managing vending machine inventory, built with Spring Boot, React, and React Native.

## 📋 Project Overview

This system consists of **three applications**:

1. **Backend API** (Port 8080) - Spring Boot REST API with JWT authentication
2. **Admin Dashboard** (Port 5173) - React web application for managing inventory
3. **Mobile App** - React Native iOS application for on-the-go access

## 🎯 Features

### Backend (Spring Boot 3.2.5 + Java 21)
- ✅ **JWT Authentication** - Secure token-based auth with BCrypt password hashing
- ✅ **Product Management** - Full CRUD operations for inventory items
- ✅ **Vending Machine Management** - Track machines, locations, and features
- ✅ **RESTful API** - Clean REST endpoints with Swagger documentation
- ✅ **Database Migrations** - Flyway for version-controlled schema changes
- ✅ **CORS Support** - Configured for React frontends
- ✅ **MySQL Database** - Production-ready relational database

### Admin Dashboard (React 18 + Vite)
- ✅ **JWT Authentication** - Login with token persistence
- ✅ **Products Page** - Full CRUD interface with real-time updates
  - Product name, category, pricing
  - Stock levels (current/minimum)
  - HST tax exemption status
  - Barcode and SKU tracking
  - Active/inactive status
- ✅ **Vending Machines Page** - Full CRUD interface
  - Brand and model information
  - Location details (name, address, city, province, postal code)
  - Payment features (cash reader, POS, coin changer)
  - Active/inactive status
- ✅ **Dashboard** - System statistics and health monitoring
- ✅ **Protected Routes** - Automatic login redirect
- ✅ **Responsive Design** - Clean, modern UI

### Mobile App (React Native 0.82 for iOS)
- ✅ **JWT Authentication** - AsyncStorage token persistence
- ✅ **Products List** - View all products with details
- ✅ **Machines List** - View all vending machines with locations
- ✅ **Pull-to-Refresh** - Update data on demand
- ✅ **Bottom Tab Navigation** - Easy navigation between screens
- ✅ **Native iOS Styling** - Platform-optimized UI

## 🚀 Quick Start

### Prerequisites

**All Applications:**
- MySQL 8.0+
- Node.js 16+

**Backend:**
- Java 21 (JDK)
- Maven 3.6+

**Mobile App (additional):**
- macOS (for iOS development)
- Xcode 15+
- CocoaPods

---

## 📦 Installation

### 1. Database Setup

Create the MySQL database:

```sql
CREATE DATABASE vending_inventory;
CREATE USER 'vending_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON vending_inventory.* TO 'vending_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Backend Setup

```bash
cd backend

# Set database password as environment variable
export DATABASE_PASSWORD=your_password

# Run with Java 21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn spring-boot:run
```

**Backend will start on http://localhost:8080**

**Swagger API Docs**: http://localhost:8080/swagger-ui.html

### 3. Admin Dashboard Setup

```bash
cd admin-dashboard

# Install dependencies
npm install

# Start development server
npm run dev
```

**Dashboard will start on http://localhost:5173**

### 4. Mobile App Setup

```bash
cd VendingMobileApp

# Install dependencies
npm install

# Point xcode-select to Xcode (REQUIRED for iOS)
sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer

# Install iOS pods
cd ios
pod install
cd ..

# Run on iOS Simulator
npx react-native run-ios
```

**Alternatively, open in Xcode:**
```bash
cd ios && xed .
```
Then click the Run button.

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

---

## 🎉 Project Status

### ✅ **COMPLETE - All Three Applications Fully Implemented**

| Application | Status | URL | Login |
|------------|--------|-----|-------|
| **Backend API** | ✅ Running | http://localhost:8080 | N/A |
| **Swagger Docs** | ✅ Available | http://localhost:8080/swagger-ui.html | N/A |
| **Admin Dashboard** | ✅ Running | http://localhost:5173 | admin / admin123 |
| **Mobile App** | ✅ Ready | iOS Simulator | admin / admin123 |

### Current Running Services

**Terminal 1 - Backend:**
```bash
cd /Users/ericgu/IdeaProjects/Carrie/Vending/backend
export DATABASE_PASSWORD=Radiance030
mvn spring-boot:run
```

**Terminal 2 - Admin Dashboard:**
```bash
cd /Users/ericgu/IdeaProjects/Carrie/Vending/admin-dashboard
npm run dev
```

**For Mobile App:**
```bash
# First time: sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer
cd /Users/ericgu/IdeaProjects/Carrie/Vending/VendingMobileApp/ios
pod install
cd ..
npx react-native run-ios
```

---

## 📚 Additional Documentation

- **Main README** - This file (setup and overview)
- **Mobile App README** - `/VendingMobileApp/MOBILE_README.md`
- **Project Summary** - `/PROJECT_SUMMARY.md` (detailed technical overview)

---

**Last Updated:** January 2025  
**Project Complete:** ✅ All features implemented and tested

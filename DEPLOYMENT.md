# Vending Inventory Management System - Deployment Guide

## Overview

This guide covers deploying the Vending Inventory Management System using Docker and Docker Compose.

## Architecture

The system consists of three main components:

- **Database (MySQL 8.0)**: Persistent data storage
- **Backend (Spring Boot)**: REST API with JWT authentication
- **Frontend (React + Nginx)**: Admin dashboard web application

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- 2GB RAM minimum
- 10GB disk space

## Quick Start

### 1. Clone the Repository

```bash
git clone <your-repository-url>
cd Vending
```

### 2. Configure Environment Variables

```bash
# Copy the example environment file
cp .env.example .env

# Edit .env with your production values
nano .env
```

**Required environment variables:**

```bash
# Database Configuration
DATABASE_NAME=vending_inventory
DATABASE_USER=vending_user
DATABASE_PASSWORD=<strong-password-here>
DATABASE_ROOT_PASSWORD=<strong-root-password-here>

# Spring Boot Configuration
SPRING_PROFILES_ACTIVE=prod

# JWT Configuration (generate a strong 32+ character secret)
JWT_SECRET=<generate-random-secret>
JWT_EXPIRATION=86400000              # 24 hours
JWT_REFRESH_EXPIRATION=604800000     # 7 days
```

**Generate a secure JWT secret:**

```bash
openssl rand -base64 32
```

### 3. Build and Start Services

```bash
# Build all images
docker-compose build

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f
```

### 4. Verify Deployment

Check service health:

```bash
# Check all services are running
docker-compose ps

# Check backend health
curl http://localhost:8080/api/health

# Check frontend health
curl http://localhost/health
```

### 5. Access the Application

- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui/index.html

**Default credentials:**
- Username: `admin`
- Password: `admin123`

**IMPORTANT**: Change the default admin password immediately after first login!

## Service Management

### Start Services

```bash
docker-compose up -d
```

### Stop Services

```bash
docker-compose down
```

### Restart a Service

```bash
docker-compose restart backend
docker-compose restart frontend
docker-compose restart db
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f db
```

### Rebuild After Code Changes

```bash
# Rebuild specific service
docker-compose build backend
docker-compose up -d backend

# Rebuild all services
docker-compose build
docker-compose up -d
```

## Database Management

### Database Migrations

Migrations run automatically on startup using Flyway. Migration scripts are located in:

```
backend/src/main/resources/db/migration/
```

### Backup Database

```bash
# Create backup
docker-compose exec db mysqldump \
  -u vending_user -p vending_inventory \
  > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore backup
docker-compose exec -T db mysql \
  -u vending_user -p vending_inventory \
  < backup_20250101_120000.sql
```

### Access Database

```bash
docker-compose exec db mysql -u vending_user -p
```

## Monitoring

### Check Service Health

```bash
# Backend health check
curl http://localhost:8080/api/health

# Frontend health check
curl http://localhost/health

# Database health
docker-compose exec db mysqladmin -u root -p ping
```

### Resource Usage

```bash
# View resource usage
docker stats

# View specific service
docker stats vending-backend
```

## Production Deployment

### Security Hardening

1. **Change Default Credentials**
   - Update admin password via `/api/auth/change-password`
   - Create separate user accounts for each admin

2. **Use Strong Secrets**
   - Generate strong JWT secret (32+ characters)
   - Use strong database passwords
   - Never commit `.env` file to version control

3. **Configure HTTPS**
   - Add SSL/TLS certificates to Nginx
   - Update `docker-compose.yml` to mount certificates
   - Update Nginx config for HTTPS

4. **Configure Firewall**
   ```bash
   # Allow only necessary ports
   ufw allow 80/tcp    # HTTP
   ufw allow 443/tcp   # HTTPS
   ufw enable
   ```

5. **Enable Docker Secrets** (for production)
   - Use Docker secrets instead of environment variables
   - Reference: https://docs.docker.com/engine/swarm/secrets/

### Performance Tuning

1. **Database**
   - Adjust `hikari.maximum-pool-size` in `application-prod.yml`
   - Configure MySQL buffer pool size
   - Enable query caching

2. **Backend**
   - Increase JVM heap size: `-Xmx1g -Xms512m`
   - Tune thread pool settings
   - Enable HTTP/2

3. **Frontend**
   - Enable Gzip compression (already configured)
   - Configure CDN for static assets
   - Implement caching strategy

### Monitoring Setup (Optional)

Start Prometheus and Grafana:

```bash
docker-compose --profile monitoring up -d
```

Access:
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3001 (admin/admin)

## Troubleshooting

### Backend Won't Start

```bash
# Check logs
docker-compose logs backend

# Common issues:
# 1. Database not ready
# 2. Invalid environment variables
# 3. Port 8080 already in use

# Solution: Check database is healthy
docker-compose exec db mysqladmin -u root -p ping
```

### Frontend Shows 502 Error

```bash
# Check backend is running
curl http://localhost:8080/api/health

# Check Nginx config
docker-compose exec frontend nginx -t

# Restart frontend
docker-compose restart frontend
```

### Database Connection Failed

```bash
# Check database is running
docker-compose ps db

# Verify credentials in .env
cat .env | grep DATABASE

# Check database logs
docker-compose logs db
```

### Reset Everything

```bash
# Stop and remove all containers, networks, volumes
docker-compose down -v

# Remove all images
docker-compose down --rmi all

# Start fresh
docker-compose up -d
```

## Maintenance

### Update Application

```bash
# Pull latest code
git pull

# Rebuild and restart
docker-compose build
docker-compose up -d
```

### Clean Up

```bash
# Remove unused images
docker image prune -a

# Remove unused volumes
docker volume prune

# Remove unused networks
docker network prune
```

## Support

For issues and questions:
- Check logs: `docker-compose logs -f`
- Review environment variables in `.env`
- Verify all services are healthy: `docker-compose ps`

## Architecture Diagram

```
┌─────────────────┐
│   Nginx (80)    │  ← Frontend (React SPA)
└────────┬────────┘
         │
┌────────▼────────┐
│  Backend (8080) │  ← Spring Boot API
└────────┬────────┘
         │
┌────────▼────────┐
│  MySQL (3306)   │  ← Database
└─────────────────┘
```

## File Structure

```
Vending/
├── docker-compose.yml          # Orchestration configuration
├── .env.example               # Environment variables template
├── .env                       # Your environment variables (DO NOT COMMIT)
├── backend/
│   ├── Dockerfile             # Backend container image
│   ├── .dockerignore          # Files to exclude from image
│   └── src/main/resources/
│       ├── application.yml     # Development config
│       └── application-prod.yml # Production config
├── admin-dashboard/
│   ├── Dockerfile             # Frontend container image
│   ├── nginx.conf             # Nginx web server config
│   └── .dockerignore          # Files to exclude from image
└── DEPLOYMENT.md              # This file
```

## Next Steps

1. Review and update security configurations
2. Set up automated backups
3. Configure monitoring and alerting
4. Set up CI/CD pipeline
5. Plan disaster recovery strategy

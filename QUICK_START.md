# Quick Start - Vending Inventory Management System

## 🚀 Get Everything Running in 5 Minutes

### Step 1: Start Backend (Terminal 1)

```bash
cd /Users/ericgu/IdeaProjects/Carrie/Vending/backend
export DATABASE_PASSWORD=Radiance030
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn spring-boot:run
```

✅ **Verify:** http://localhost:8080/swagger-ui.html

### Step 2: Start Admin Dashboard (Terminal 2)

```bash
cd /Users/ericgu/IdeaProjects/Carrie/Vending/admin-dashboard
npm run dev
```

✅ **Verify:** http://localhost:5173

**Login:** `admin` / `admin123`

### Step 3: (Optional) Start Mobile App (Terminal 3)

**First Time Setup:**
```bash
sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer
cd /Users/ericgu/IdeaProjects/Carrie/Vending/VendingMobileApp/ios
pod install
cd ..
```

**Run App:**
```bash
npx react-native run-ios
```

---

## 📱 What You Can Do

### Admin Dashboard (http://localhost:5173)

1. **Login** - Use `admin` / `admin123`
2. **Dashboard** - View system statistics
3. **Products** - Add, edit, delete products
   - Product info (name, category, price)
   - Stock levels
   - Barcode/SKU
   - HST exemption
4. **Vending Machines** - Manage machines
   - Machine details (brand, model)
   - Location info
   - Payment features
   - Active/inactive status

### Mobile App (iOS Simulator)

1. **Login** - Use `admin` / `admin123`
2. **Products Tab** - View all products
3. **Machines Tab** - View all vending machines
4. **Pull to Refresh** - Update data

### API (http://localhost:8080)

Test with curl:

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Get Products (replace TOKEN with JWT from login)
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer TOKEN"
```

---

## 🛠️ Troubleshooting

### Backend Won't Start

**Port 8080 in use:**
```bash
lsof -ti:8080 | xargs kill -9
```

**MySQL not running:**
```bash
mysql.server start
```

### Dashboard Won't Load

**Check backend is running:**
```bash
curl http://localhost:8080/api/health
```

**Browser cache:**
- Hard refresh: `Cmd+Shift+R`

### Mobile App Issues

**Pod install fails:**
```bash
sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer
cd ios && rm -rf Pods Podfile.lock && pod install
```

**Metro bundler stuck:**
```bash
npx react-native start --reset-cache
```

---

## 📊 Quick Reference

| Item | Value |
|------|-------|
| **Backend** | http://localhost:8080 |
| **Swagger** | http://localhost:8080/swagger-ui.html |
| **Dashboard** | http://localhost:5173 |
| **Username** | admin |
| **Password** | admin123 |
| **Database** | vending_inventory |
| **DB Password** | Radiance030 |

---

## 📚 Full Documentation

- **README.md** - Complete setup guide
- **MOBILE_README.md** - Mobile app specific docs
- **PROJECT_SUMMARY.md** - Technical architecture details

---

**Need Help?** Check the troubleshooting sections in README.md

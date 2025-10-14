# Vending Inventory Management - Mobile App

React Native mobile application for managing vending machine inventory.

## Prerequisites

- Node.js 16+ and npm
- Xcode 15+ (for iOS development)
- CocoaPods (for iOS dependencies)
- React Native development environment set up

## Features

- **Authentication**: JWT-based login with AsyncStorage for token persistence
- **Products Management**: View list of all products with details
- **Vending Machines Management**: View list of all vending machines with locations and features
- **Pull-to-Refresh**: Refresh data on both screens
- **Responsive UI**: Native iOS components with custom styling

## Installation

1. Install dependencies:
```bash
npm install
```

2. Install iOS pods (required for iOS):
```bash
cd ios
pod install
cd ..
```

Note: If you encounter Ruby/CocoaPods issues, you may need to:
- Install CocoaPods: `sudo gem install cocoapods`
- Or use Homebrew: `brew install cocoapods`

## Configuration

### API Base URL

The app is configured to connect to `http://localhost:8080/api` by default.

**For iOS Simulator**: This works as-is.

**For physical device**: You need to update the API URL in `src/services/api.js`:
```javascript
const API_BASE_URL = 'http://YOUR_COMPUTER_IP:8080/api';
```

Replace `YOUR_COMPUTER_IP` with your computer's local IP address (e.g., `http://192.168.1.100:8080/api`).

## Running the App

### Method 1: Using Xcode (Recommended for first run)

1. Open the workspace:
```bash
cd ios
open VendingMobileApp.xcworkspace
```

2. In Xcode:
   - Select the VendingMobileApp scheme at the top
   - Select a simulator (e.g., iPhone 16 Pro)
   - Click the Run button (▶️) or press Cmd+R

3. **IMPORTANT - Code Signing Setup (First Time Only)**:
   - When Xcode opens, you may see a signing error
   - Click on "VendingMobileApp" project in the left panel
   - Click on "VendingMobileApp" target
   - Go to "Signing & Capabilities" tab
   - Check "Automatically manage signing"
   - Select your Team from the dropdown (or add your Apple ID if needed)
   - Xcode will automatically generate a signing certificate

### Method 2: Using Command Line

After configuring code signing in Xcode (see above), you can run:

```bash
npx react-native run-ios
```

Or specify a simulator:
```bash
npx react-native run-ios --simulator="iPhone 16 Pro"
```

### iOS Simulator

```bash
npx react-native run-ios
```

Or open in Xcode:
```bash
cd ios
xed .
```
Then click the Run button in Xcode.

### Android Emulator (if needed)

```bash
npx react-native run-android
```

## Project Structure

```
VendingMobileApp/
├── src/
│   ├── contexts/
│   │   └── AuthContext.js          # Authentication state management
│   ├── navigation/
│   │   └── AppNavigator.js         # Navigation configuration
│   ├── screens/
│   │   ├── LoginScreen.js          # Login screen
│   │   ├── ProductsScreen.js       # Products list screen
│   │   └── MachinesScreen.js       # Vending machines list screen
│   └── services/
│       └── api.js                  # API service with Axios
├── App.tsx                         # Root component
└── package.json
```

## Screens

### Login Screen
- Username and password fields
- JWT token authentication
- Error handling with alerts
- Loading state during authentication

### Products Screen
- List of all products with cards
- Shows: name, category, price, stock, HST status, active/inactive badge
- Pull-to-refresh functionality
- Empty state message

### Vending Machines Screen
- List of all vending machines with cards
- Shows: brand/model, location details, features (Bill Reader, POS, Coin Changer)
- Active/inactive status badges
- Pull-to-refresh functionality
- Empty state message

## Authentication

Login credentials (from backend):
- Username: `admin`
- Password: `admin123`

The app stores JWT tokens in AsyncStorage and automatically includes them in API requests.

## Troubleshooting

### Build Error: xcodebuild error code 70

This typically means code signing isn't configured:

1. Open the project in Xcode:
   ```bash
   cd ios && open VendingMobileApp.xcworkspace
   ```

2. Select "VendingMobileApp" project in left panel
3. Select "VendingMobileApp" target
4. Go to "Signing & Capabilities" tab
5. Enable "Automatically manage signing"
6. Select your Team (add Apple ID if needed)

### Metro Bundler Issues
```bash
npx react-native start --reset-cache
```

### Build Issues
```bash
cd ios
rm -rf Pods Podfile.lock
pod install
cd ..
```

### Connection Issues
- Ensure backend is running on port 8080
- For physical devices, verify you're on the same WiFi network
- Update API_BASE_URL with your computer's local IP

### Xcode Not Configured
```bash
sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer
xcodebuild -runFirstLaunch
```

## Backend Dependency

This mobile app requires the Spring Boot backend to be running:
```bash
cd ../backend
DATABASE_PASSWORD=your_password mvn spring-boot:run
```

The backend should be accessible at `http://localhost:8080` (or your configured IP).

## Next Steps

Potential enhancements:
- Add CRUD operations (Create, Update, Delete) for products and machines
- Add product detail screen
- Add vending machine detail screen with product inventory
- Add offline support with local caching
- Add push notifications
- Add barcode scanning for products
- Add photo capture for products

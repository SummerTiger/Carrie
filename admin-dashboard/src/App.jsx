import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Products from './pages/Products';
import VendingMachines from './pages/VendingMachines';
import Procurement from './pages/Procurement';
import AuditLogs from './pages/AuditLogs';
import Users from './pages/Users';
import ChangePassword from './pages/ChangePassword';
import Analytics from './pages/Analytics';
import Vendors from './pages/Vendors';
import ProductCategories from './pages/ProductCategories';
import ProductBrands from './pages/ProductBrands';
import MachineBrands from './pages/MachineBrands';
import MachineModels from './pages/MachineModels';
import DashboardLayout from './components/DashboardLayout';

// Protected Route Component
function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />

          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Dashboard />} />
            <Route path="products" element={<Products />} />
            <Route path="machines" element={<VendingMachines />} />
            <Route path="procurement" element={<Procurement />} />
            <Route path="vendors" element={<Vendors />} />
            <Route path="categories" element={<ProductCategories />} />
            <Route path="product-brands" element={<ProductBrands />} />
            <Route path="machine-brands" element={<MachineBrands />} />
            <Route path="machine-models" element={<MachineModels />} />
            <Route path="analytics" element={<Analytics />} />
            <Route path="audit-logs" element={<AuditLogs />} />
            <Route path="users" element={<Users />} />
            <Route path="change-password" element={<ChangePassword />} />
          </Route>

          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;

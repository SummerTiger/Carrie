import axios from 'axios';

// Security: Use environment variable for API URL
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle token expiration
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Authentication
export const authAPI = {
  login: (username, password) =>
    api.post('/auth/login', { username, password }),
  logout: (refreshToken) =>
    api.post('/auth/logout', { refreshToken }),
  refresh: (refreshToken) =>
    api.post('/auth/refresh', { refreshToken }),
  changePassword: (currentPassword, newPassword) =>
    api.post('/auth/change-password', { currentPassword, newPassword }),
  validateToken: () =>
    api.get('/auth/validate'),
};

// Products
export const productsAPI = {
  getAll: () => api.get('/products'),
  getById: (id) => api.get(`/products/${id}`),
  getActive: () => api.get('/products/active'),
  getByCategory: (category) => api.get(`/products/category/${category}`),
  create: (product) => api.post('/products', product),
  update: (id, product) => api.put(`/products/${id}`, product),
  delete: (id) => api.delete(`/products/${id}`),
};

// Vending Machines
export const machinesAPI = {
  getAll: () => api.get('/vending-machines'),
  getById: (id) => api.get(`/vending-machines/${id}`),
  getActive: () => api.get('/vending-machines/active'),
  create: (machine) => api.post('/vending-machines', machine),
  update: (id, machine) => api.put(`/vending-machines/${id}`, machine),
  delete: (id) => api.delete(`/vending-machines/${id}`),
};

// Health
export const healthAPI = {
  check: () => api.get('/health'),
};

// Audit Logs
export const auditLogsAPI = {
  getAll: (params) => api.get('/audit-logs', { params }),
  getByUsername: (username, params) => api.get(`/audit-logs/user/${username}`, { params }),
  getByAction: (action, params) => api.get(`/audit-logs/action/${action}`, { params }),
  getByResource: (resourceType, params) => api.get(`/audit-logs/resource/${resourceType}`, { params }),
  getByDateRange: (startDate, endDate, params) => api.get('/audit-logs/date-range', {
    params: { startDate, endDate, ...params }
  }),
  getRecent: () => api.get('/audit-logs/recent'),
  cleanup: (daysToKeep) => api.delete('/audit-logs/cleanup', { params: { daysToKeep } }),
};

// Procurement Batches
export const procurementAPI = {
  getAll: () => api.get('/procurement-batches'),
  getById: (id) => api.get(`/procurement-batches/${id}`),
  getBySupplier: (supplier) => api.get(`/procurement-batches/supplier/${supplier}`),
  getAllSuppliers: () => api.get('/procurement-batches/suppliers'),
  create: (batch) => api.post('/procurement-batches', batch),
  update: (id, batch) => api.put(`/procurement-batches/${id}`, batch),
  delete: (id) => api.delete(`/procurement-batches/${id}`),
};

// Users
export const usersAPI = {
  getAll: () => api.get('/users'),
  getById: (id) => api.get(`/users/${id}`),
  create: (user) => api.post('/users', user),
  update: (id, user) => api.put(`/users/${id}`, user),
  delete: (id) => api.delete(`/users/${id}`),
};

// Analytics
export const analyticsAPI = {
  getSummary: (startDate, endDate) => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    return api.get(`/analytics/summary?${params}`);
  },
  getRevenueData: (startDate, endDate) => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    return api.get(`/analytics/revenue?${params}`);
  },
  getInventoryTrends: (startDate, endDate) => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    return api.get(`/analytics/inventory-trends?${params}`);
  },
  getMachinePerformance: (startDate, endDate) => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    return api.get(`/analytics/machine-performance?${params}`);
  },
  getProductAnalytics: (startDate, endDate) => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    return api.get(`/analytics/product-analytics?${params}`);
  },
  getCategoryBreakdown: (startDate, endDate) => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    return api.get(`/analytics/category-breakdown?${params}`);
  },
};

// Vendors
export const vendorsAPI = {
  getAll: () => api.get('/vendors'),
  getActive: () => api.get('/vendors/active'),
  getPreferred: () => api.get('/vendors/preferred'),
  getById: (id) => api.get(`/vendors/${id}`),
  search: (query) => api.get(`/vendors/search?query=${encodeURIComponent(query)}`),
  create: (vendor) => api.post('/vendors', vendor),
  update: (id, vendor) => api.put(`/vendors/${id}`, vendor),
  delete: (id) => api.delete(`/vendors/${id}`),
  toggleStatus: (id) => api.patch(`/vendors/${id}/toggle-status`),
};

// Product Categories
export const productCategoriesAPI = {
  getAll: () => api.get('/product-categories'),
  getById: (id) => api.get(`/product-categories/${id}`),
  getActive: () => api.get('/product-categories/active'),
  create: (category) => api.post('/product-categories', category),
  update: (id, category) => api.put(`/product-categories/${id}`, category),
  delete: (id) => api.delete(`/product-categories/${id}`),
};

// Product Brands
export const productBrandsAPI = {
  getAll: () => api.get('/product-brands'),
  getById: (id) => api.get(`/product-brands/${id}`),
  getActive: () => api.get('/product-brands/active'),
  create: (brand) => api.post('/product-brands', brand),
  update: (id, brand) => api.put(`/product-brands/${id}`, brand),
  delete: (id) => api.delete(`/product-brands/${id}`),
};

// Machine Brands
export const machineBrandsAPI = {
  getAll: () => api.get('/machine-brands'),
  getById: (id) => api.get(`/machine-brands/${id}`),
  getActive: () => api.get('/machine-brands/active'),
  create: (brand) => api.post('/machine-brands', brand),
  update: (id, brand) => api.put(`/machine-brands/${id}`, brand),
  delete: (id) => api.delete(`/machine-brands/${id}`),
};

// Machine Models
export const machineModelsAPI = {
  getAll: () => api.get('/machine-models'),
  getById: (id) => api.get(`/machine-models/${id}`),
  getActive: () => api.get('/machine-models/active'),
  getByBrand: (brandId) => api.get(`/machine-models/brand/${brandId}`),
  create: (model) => api.post('/machine-models', model),
  update: (id, model) => api.put(`/machine-models/${id}`, model),
  delete: (id) => api.delete(`/machine-models/${id}`),
};

// Sales Records
export const salesAPI = {
  getAll: () => api.get('/sales'),
  getById: (id) => api.get(`/sales/${id}`),
  getByDateRange: (startDate, endDate) => api.get('/sales/date-range', {
    params: { startDate, endDate }
  }),
  getBySource: (source) => api.get(`/sales/source/${source}`),
  importCSV: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/sales/import/csv', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },
  importExcel: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/sales/import/excel', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },
  delete: (id) => api.delete(`/sales/${id}`),
};

// Legacy alias for backward compatibility
export const categoriesAPI = {
  getAll: () => api.get('/products/categories'),
  getActive: () => api.get('/products/categories/active'),
};

export default api;

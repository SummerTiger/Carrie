import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

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

export default api;

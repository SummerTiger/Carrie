import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';

// Update this to your computer's local IP address when testing on a physical device
// For iOS Simulator, you can use localhost
const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token
api.interceptors.request.use(
  async config => {
    const token = await AsyncStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  },
);

// Response interceptor to handle token expiration
api.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      await AsyncStorage.removeItem('token');
      await AsyncStorage.removeItem('user');
      // Navigation will be handled by the auth context
    }
    return Promise.reject(error);
  },
);

export const authAPI = {
  login: (username, password) =>
    api.post('/auth/login', {username, password}),
};

export const productsAPI = {
  getAll: () => api.get('/products'),
  getById: id => api.get(`/products/${id}`),
  create: data => api.post('/products', data),
  update: (id, data) => api.put(`/products/${id}`, data),
  delete: id => api.delete(`/products/${id}`),
};

export const machinesAPI = {
  getAll: () => api.get('/vending-machines'),
  getById: id => api.get(`/vending-machines/${id}`),
  create: data => api.post('/vending-machines', data),
  update: (id, data) => api.put(`/vending-machines/${id}`, data),
  delete: id => api.delete(`/vending-machines/${id}`),
};

export const healthAPI = {
  check: () => api.get('/health'),
};

export default api;

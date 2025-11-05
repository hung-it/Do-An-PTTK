import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

// Tạo axios instance với base URL
const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor để tự động thêm token vào mọi request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ==================== AUTH ====================

export const authAPI = {
  // Đăng ký
  register: (data) => api.post('/auth/register', data),
  
  // Đăng nhập
  login: (data) => api.post('/auth/login', data),
  
  // Lấy thông tin profile
  getProfile: () => api.get('/auth/me'),
};

// ==================== PRODUCTS ====================

export const productAPI = {
  // Lấy tất cả sản phẩm
  getAll: () => api.get('/products'),
  
  // Lấy chi tiết sản phẩm
  getById: (id) => api.get(`/products/${id}`),
  
  // Lấy biến thể của sản phẩm
  getVariants: (id) => api.get(`/products/${id}/variants`),
  
  // Tìm kiếm sản phẩm
  search: (keyword) => api.get(`/products/search?keyword=${keyword}`),
};

// ==================== ORDERS ====================

export const orderAPI = {
  // Tạo đơn hàng mới
  create: (data) => api.post('/orders', data),
  
  // Lấy lịch sử đơn hàng
  getAll: () => api.get('/orders'),
  
  // Lấy chi tiết đơn hàng
  getById: (id) => api.get(`/orders/${id}`),
};

// ==================== HELPERS ====================

// Format giá tiền
export const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price);
};

// Format ngày tháng
export const formatDate = (date) => {
  return new Date(date).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

// Trạng thái đơn hàng
export const orderStatus = {
  pending: 'Chờ xử lý',
  processing: 'Đang xử lý',
  shipped: 'Đang giao',
  delivered: 'Đã giao',
  cancelled: 'Đã hủy',
};

export default api;

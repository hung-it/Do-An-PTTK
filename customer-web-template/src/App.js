import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import ProductList from './pages/ProductList';
import ProductDetail from './pages/ProductDetail';
import Login from './pages/Login';
import Register from './pages/Register';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import OrderHistory from './pages/OrderHistory';
import Profile from './pages/Profile';
import './App.css';

// Protected Route Component
function PrivateRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) {
    return <div className="loading">Đang tải...</div>;
  }
  
  return isAuthenticated ? children : <Navigate to="/login" />;
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <CartProvider>
          <div className="App">
            <Navbar />
            <main className="main-content">
              <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/products" element={<ProductList />} />
                <Route path="/products/:id" element={<ProductDetail />} />
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                
                {/* Protected Routes */}
                <Route path="/cart" element={
                  <PrivateRoute><Cart /></PrivateRoute>
                } />
                <Route path="/checkout" element={
                  <PrivateRoute><Checkout /></PrivateRoute>
                } />
                <Route path="/orders" element={
                  <PrivateRoute><OrderHistory /></PrivateRoute>
                } />
                <Route path="/profile" element={
                  <PrivateRoute><Profile /></PrivateRoute>
                } />
              </Routes>
            </main>
            <footer className="footer">
              <p>© 2024 Shoe Store. Đồ án PTTK - Hệ thống quản lý cửa hàng giày.</p>
            </footer>
          </div>
        </CartProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;

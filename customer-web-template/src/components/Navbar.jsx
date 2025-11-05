import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import './Navbar.css';

function Navbar() {
  const { user, logout, isAuthenticated } = useAuth();
  const { getItemCount } = useCart();

  return (
    <nav className="navbar">
      <div className="container">
        <Link to="/" className="navbar-brand">
          <span className="logo">👟</span>
          <span className="brand-name">Shoe Store</span>
        </Link>

        <div className="navbar-menu">
          <Link to="/products" className="nav-link">
            Sản phẩm
          </Link>

          {isAuthenticated ? (
            <>
              <Link to="/cart" className="nav-link cart-link">
                🛒 Giỏ hàng
                {getItemCount() > 0 && (
                  <span className="cart-badge">{getItemCount()}</span>
                )}
              </Link>
              <Link to="/orders" className="nav-link">
                📦 Đơn hàng
              </Link>
              <div className="dropdown">
                <button className="nav-link dropdown-toggle">
                  👤 {user?.name}
                </button>
                <div className="dropdown-menu">
                  <Link to="/profile" className="dropdown-item">
                    Thông tin cá nhân
                  </Link>
                  <button onClick={logout} className="dropdown-item">
                    Đăng xuất
                  </button>
                </div>
              </div>
            </>
          ) : (
            <>
              <Link to="/login" className="nav-link">
                Đăng nhập
              </Link>
              <Link to="/register" className="btn-primary">
                Đăng ký
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}

export default Navbar;

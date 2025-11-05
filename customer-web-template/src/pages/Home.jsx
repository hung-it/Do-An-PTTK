import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import ProductCard from '../components/ProductCard';
import { productAPI } from '../services/api';
import './Home.css';

function Home() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      const response = await productAPI.getAll();
      if (response.data.success) {
        // Chỉ lấy 4 sản phẩm đầu tiên cho trang chủ
        setProducts(response.data.data.slice(0, 4));
      }
    } catch (error) {
      console.error('Load products failed:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="home-page">
      {/* Hero Section */}
      <section className="hero">
        <div className="container">
          <div className="hero-content">
            <h1 className="hero-title">
              Khám phá bộ sưu tập giày<br />
              <span className="highlight">Phong cách & Chất lượng</span>
            </h1>
            <p className="hero-description">
              Tìm đôi giày hoàn hảo cho phong cách của bạn.<br />
              Giao hàng nhanh chóng - Đổi trả dễ dàng.
            </p>
            <Link to="/products" className="btn btn-primary btn-lg">
              Mua sắm ngay →
            </Link>
          </div>
          <div className="hero-image">
            <div className="hero-shoe">👟</div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="features">
        <div className="container">
          <div className="features-grid">
            <div className="feature-card">
              <div className="feature-icon">🚚</div>
              <h3>Giao hàng nhanh</h3>
              <p>Miễn phí vận chuyển cho đơn hàng trên 500k</p>
            </div>
            <div className="feature-card">
              <div className="feature-icon">💯</div>
              <h3>Chất lượng đảm bảo</h3>
              <p>Sản phẩm chính hãng 100%</p>
            </div>
            <div className="feature-card">
              <div className="feature-icon">🔄</div>
              <h3>Đổi trả dễ dàng</h3>
              <p>Đổi trả miễn phí trong 7 ngày</p>
            </div>
            <div className="feature-card">
              <div className="feature-icon">💳</div>
              <h3>Thanh toán linh hoạt</h3>
              <p>COD, Chuyển khoản, Ví điện tử</p>
            </div>
          </div>
        </div>
      </section>

      {/* Featured Products */}
      <section className="featured-products">
        <div className="container">
          <div className="section-header">
            <h2>Sản phẩm nổi bật</h2>
            <Link to="/products" className="view-all">
              Xem tất cả →
            </Link>
          </div>

          {loading ? (
            <div className="loading">Đang tải sản phẩm...</div>
          ) : (
            <div className="grid grid-cols-4">
              {products.map((product) => (
                <ProductCard key={product.product_id} product={product} />
              ))}
            </div>
          )}
        </div>
      </section>

      {/* CTA Section */}
      <section className="cta">
        <div className="container text-center">
          <h2>Tham gia cộng đồng của chúng tôi</h2>
          <p>Đăng ký nhận thông tin về sản phẩm mới và ưu đãi đặc biệt</p>
          <Link to="/register" className="btn btn-primary btn-lg">
            Đăng ký ngay
          </Link>
        </div>
      </section>
    </div>
  );
}

export default Home;

import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { formatPrice } from '../services/api';
import './Cart.css';

function Cart() {
  const navigate = useNavigate();
  const { items, removeFromCart, updateQuantity, getTotal, clearCart } = useCart();

  const handleQuantityChange = (variantId, newQuantity) => {
    if (newQuantity < 1) return;
    updateQuantity(variantId, newQuantity);
  };

  const handleCheckout = () => {
    if (items.length === 0) {
      alert('Giỏ hàng trống');
      return;
    }
    navigate('/checkout');
  };

  if (items.length === 0) {
    return (
      <div className="cart-page">
        <div className="container">
          <div className="empty-cart">
            <div className="empty-cart-icon">🛒</div>
            <h2>Giỏ hàng trống</h2>
            <p>Hãy thêm sản phẩm vào giỏ hàng để tiếp tục mua sắm</p>
            <Link to="/products" className="btn btn-primary btn-large">
              Tiếp tục mua sắm
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="cart-page">
      <div className="container">
        <h1 className="page-title">🛒 Giỏ hàng của bạn</h1>

        <div className="cart-layout">
          {/* Cart Items */}
          <div className="cart-items">
            {items.map((item) => (
              <div key={item.variant.variant_id} className="cart-item">
                <div className="item-image">
                  <div className="image-placeholder">
                    <span className="item-icon">👟</span>
                  </div>
                </div>

                <div className="item-info">
                  <h3 className="item-name">{item.product.name}</h3>
                  <div className="item-details">
                    <span className="detail-badge">
                      Size: {item.variant.size}
                    </span>
                    <span className="detail-badge">
                      Màu: {item.variant.color}
                    </span>
                    <span className="detail-badge">
                      SKU: {item.variant.sku_code}
                    </span>
                  </div>
                  <div className="item-price">
                    {formatPrice(item.variant.price)}
                  </div>
                </div>

                <div className="item-actions">
                  <div className="quantity-control">
                    <button
                      className="quantity-btn"
                      onClick={() =>
                        handleQuantityChange(
                          item.variant.variant_id,
                          item.quantity - 1
                        )
                      }
                    >
                      −
                    </button>
                    <input
                      type="number"
                      className="quantity-input"
                      value={item.quantity}
                      onChange={(e) =>
                        handleQuantityChange(
                          item.variant.variant_id,
                          parseInt(e.target.value) || 1
                        )
                      }
                      min="1"
                      max={item.variant.quantity_in_stock}
                    />
                    <button
                      className="quantity-btn"
                      onClick={() =>
                        handleQuantityChange(
                          item.variant.variant_id,
                          item.quantity + 1
                        )
                      }
                    >
                      +
                    </button>
                  </div>

                  <div className="item-total">
                    {formatPrice(item.variant.price * item.quantity)}
                  </div>

                  <button
                    className="btn-remove"
                    onClick={() => removeFromCart(item.variant.variant_id)}
                  >
                    🗑️ Xóa
                  </button>
                </div>
              </div>
            ))}

            <div className="cart-actions">
              <button className="btn btn-secondary" onClick={clearCart}>
                Xóa tất cả
              </button>
              <Link to="/products" className="btn btn-secondary">
                Tiếp tục mua sắm
              </Link>
            </div>
          </div>

          {/* Cart Summary */}
          <div className="cart-summary">
            <h2>Tổng quan đơn hàng</h2>

            <div className="summary-row">
              <span>Tạm tính:</span>
              <span className="summary-value">{formatPrice(getTotal())}</span>
            </div>

            <div className="summary-row">
              <span>Phí vận chuyển:</span>
              <span className="summary-value">
                {getTotal() >= 500000 ? 'Miễn phí' : formatPrice(30000)}
              </span>
            </div>

            <div className="summary-divider"></div>

            <div className="summary-row summary-total">
              <span>Tổng cộng:</span>
              <span className="summary-value total-value">
                {formatPrice(getTotal() >= 500000 ? getTotal() : getTotal() + 30000)}
              </span>
            </div>

            {getTotal() < 500000 && (
              <div className="free-shipping-notice">
                💡 Mua thêm {formatPrice(500000 - getTotal())} để được miễn phí vận chuyển
              </div>
            )}

            <button className="btn btn-success btn-block btn-large" onClick={handleCheckout}>
              Tiến hành thanh toán
            </button>

            <div className="secure-checkout">
              🔒 Thanh toán an toàn và bảo mật
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Cart;

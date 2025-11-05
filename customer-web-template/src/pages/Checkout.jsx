import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { orderAPI, formatPrice } from '../services/api';
import './Checkout.css';

function Checkout() {
  const navigate = useNavigate();
  const { items, getTotal, clearCart } = useCart();
  const { user } = useAuth();

  const [formData, setFormData] = useState({
    shipping_address: user?.address || '',
    payment_method: 'cod',
    notes: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.shipping_address.trim()) {
      setError('Vui lòng nhập địa chỉ giao hàng');
      return;
    }

    if (items.length === 0) {
      setError('Giỏ hàng trống');
      return;
    }

    setLoading(true);

    try {
      // Prepare order data
      const orderData = {
        items: items.map((item) => ({
          variant_id: item.variant.variant_id,
          quantity: item.quantity,
          price: item.variant.price,
        })),
        shipping_address: formData.shipping_address,
        payment_method: formData.payment_method,
        notes: formData.notes,
      };

      const response = await orderAPI.create(orderData);

      if (response.data.success) {
        clearCart();
        alert('Đặt hàng thành công! Cảm ơn bạn đã mua hàng.');
        navigate('/orders');
      } else {
        setError(response.data.message || 'Đặt hàng thất bại');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Lỗi kết nối server');
    } finally {
      setLoading(false);
    }
  };

  const shippingFee = getTotal() >= 500000 ? 0 : 30000;
  const totalAmount = getTotal() + shippingFee;

  if (items.length === 0) {
    return (
      <div className="checkout-page">
        <div className="container">
          <div className="empty-checkout">
            <h2>Giỏ hàng trống</h2>
            <p>Vui lòng thêm sản phẩm vào giỏ hàng trước khi thanh toán</p>
            <button className="btn btn-primary" onClick={() => navigate('/products')}>
              Tiếp tục mua sắm
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="checkout-page">
      <div className="container">
        <h1 className="page-title">💳 Thanh toán</h1>

        <div className="checkout-layout">
          {/* Checkout Form */}
          <div className="checkout-form">
            <form onSubmit={handleSubmit}>
              {/* Customer Info */}
              <div className="form-section">
                <h2>Thông tin khách hàng</h2>
                <div className="info-display">
                  <div className="info-row">
                    <span className="info-label">Họ tên:</span>
                    <span className="info-value">{user?.name}</span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">Số điện thoại:</span>
                    <span className="info-value">{user?.phone}</span>
                  </div>
                </div>
              </div>

              {/* Shipping Address */}
              <div className="form-section">
                <h2>Địa chỉ giao hàng</h2>
                <textarea
                  name="shipping_address"
                  className="form-control"
                  value={formData.shipping_address}
                  onChange={handleChange}
                  placeholder="Số nhà, đường, phường, quận, thành phố"
                  rows="3"
                  required
                />
              </div>

              {/* Payment Method */}
              <div className="form-section">
                <h2>Phương thức thanh toán</h2>
                <div className="payment-methods">
                  <label className="payment-option">
                    <input
                      type="radio"
                      name="payment_method"
                      value="cod"
                      checked={formData.payment_method === 'cod'}
                      onChange={handleChange}
                    />
                    <div className="payment-info">
                      <div className="payment-name">💵 Thanh toán khi nhận hàng (COD)</div>
                      <div className="payment-desc">Thanh toán bằng tiền mặt khi nhận hàng</div>
                    </div>
                  </label>

                  <label className="payment-option">
                    <input
                      type="radio"
                      name="payment_method"
                      value="bank_transfer"
                      checked={formData.payment_method === 'bank_transfer'}
                      onChange={handleChange}
                    />
                    <div className="payment-info">
                      <div className="payment-name">🏦 Chuyển khoản ngân hàng</div>
                      <div className="payment-desc">Chuyển khoản trước khi giao hàng</div>
                    </div>
                  </label>

                  <label className="payment-option">
                    <input
                      type="radio"
                      name="payment_method"
                      value="credit_card"
                      checked={formData.payment_method === 'credit_card'}
                      onChange={handleChange}
                    />
                    <div className="payment-info">
                      <div className="payment-name">💳 Thẻ tín dụng/ghi nợ</div>
                      <div className="payment-desc">Thanh toán trực tuyến an toàn</div>
                    </div>
                  </label>
                </div>
              </div>

              {/* Notes */}
              <div className="form-section">
                <h2>Ghi chú (tùy chọn)</h2>
                <textarea
                  name="notes"
                  className="form-control"
                  value={formData.notes}
                  onChange={handleChange}
                  placeholder="Ghi chú về đơn hàng, ví dụ: thời gian giao hàng..."
                  rows="3"
                />
              </div>

              {error && <div className="alert alert-error">{error}</div>}

              <button
                type="submit"
                className="btn btn-success btn-block btn-large"
                disabled={loading}
              >
                {loading ? 'Đang xử lý...' : `Đặt hàng - ${formatPrice(totalAmount)}`}
              </button>
            </form>
          </div>

          {/* Order Summary */}
          <div className="order-summary">
            <h2>Đơn hàng của bạn</h2>

            <div className="summary-items">
              {items.map((item) => (
                <div key={item.variant.variant_id} className="summary-item">
                  <div className="item-info">
                    <div className="item-name">{item.product.name}</div>
                    <div className="item-variant">
                      Size {item.variant.size} - {item.variant.color}
                    </div>
                    <div className="item-quantity">x{item.quantity}</div>
                  </div>
                  <div className="item-price">
                    {formatPrice(item.variant.price * item.quantity)}
                  </div>
                </div>
              ))}
            </div>

            <div className="summary-divider"></div>

            <div className="summary-row">
              <span>Tạm tính:</span>
              <span>{formatPrice(getTotal())}</span>
            </div>

            <div className="summary-row">
              <span>Phí vận chuyển:</span>
              <span>{shippingFee === 0 ? 'Miễn phí' : formatPrice(shippingFee)}</span>
            </div>

            <div className="summary-divider"></div>

            <div className="summary-row summary-total">
              <span>Tổng cộng:</span>
              <span className="total-value">{formatPrice(totalAmount)}</span>
            </div>

            {getTotal() >= 500000 && (
              <div className="free-shipping-badge">
                🎉 Bạn được miễn phí vận chuyển!
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Checkout;

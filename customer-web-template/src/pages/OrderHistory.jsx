import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { orderAPI, formatPrice, formatDate, orderStatus } from '../services/api';
import './OrderHistory.css';

function OrderHistory() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedOrder, setSelectedOrder] = useState(null);

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    try {
      setLoading(true);
      const response = await orderAPI.getAll();

      if (response.data.success) {
        setOrders(response.data.data);
      }
    } catch (error) {
      console.error('Load orders failed:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleViewDetail = async (orderId) => {
    try {
      const response = await orderAPI.getById(orderId);
      if (response.data.success) {
        setSelectedOrder(response.data.data);
      }
    } catch (error) {
      console.error('Load order detail failed:', error);
    }
  };

  const handleCloseDetail = () => {
    setSelectedOrder(null);
  };

  const getStatusClass = (status) => {
    const statusMap = {
      pending: 'status-pending',
      processing: 'status-processing',
      shipped: 'status-shipped',
      delivered: 'status-delivered',
      cancelled: 'status-cancelled',
    };
    return statusMap[status] || 'status-pending';
  };

  if (loading) {
    return <div className="loading">Đang tải lịch sử đơn hàng...</div>;
  }

  if (orders.length === 0) {
    return (
      <div className="order-history-page">
        <div className="container">
          <div className="empty-orders">
            <div className="empty-icon">📦</div>
            <h2>Chưa có đơn hàng nào</h2>
            <p>Hãy đặt hàng ngay để trải nghiệm dịch vụ của chúng tôi</p>
            <Link to="/products" className="btn btn-primary btn-large">
              Khám phá sản phẩm
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="order-history-page">
      <div className="container">
        <h1 className="page-title">📦 Lịch sử đơn hàng</h1>

        <div className="orders-list">
          {orders.map((order) => (
            <div key={order.order_id} className="order-card">
              <div className="order-header">
                <div className="order-info">
                  <div className="order-id">Đơn hàng #{order.order_id}</div>
                  <div className="order-date">{formatDate(order.order_date)}</div>
                </div>
                <div className={`order-status ${getStatusClass(order.status)}`}>
                  {orderStatus[order.status] || order.status}
                </div>
              </div>

              <div className="order-body">
                <div className="order-details">
                  <div className="detail-row">
                    <span className="detail-label">Tổng tiền:</span>
                    <span className="detail-value price">
                      {formatPrice(order.total_amount)}
                    </span>
                  </div>
                  {order.shipping_address && (
                    <div className="detail-row">
                      <span className="detail-label">Địa chỉ:</span>
                      <span className="detail-value">{order.shipping_address}</span>
                    </div>
                  )}
                  {order.payment_method && (
                    <div className="detail-row">
                      <span className="detail-label">Thanh toán:</span>
                      <span className="detail-value">
                        {order.payment_method === 'cod'
                          ? 'Thanh toán khi nhận hàng'
                          : order.payment_method === 'bank_transfer'
                          ? 'Chuyển khoản'
                          : 'Thẻ tín dụng'}
                      </span>
                    </div>
                  )}
                </div>

                <button
                  className="btn btn-primary"
                  onClick={() => handleViewDetail(order.order_id)}
                >
                  Xem chi tiết
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Order Detail Modal */}
      {selectedOrder && (
        <div className="modal-overlay" onClick={handleCloseDetail}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Chi tiết đơn hàng #{selectedOrder.order_id}</h2>
              <button className="modal-close" onClick={handleCloseDetail}>
                ✕
              </button>
            </div>

            <div className="modal-body">
              {/* Order Info */}
              <div className="modal-section">
                <h3>Thông tin đơn hàng</h3>
                <div className="info-grid">
                  <div className="info-item">
                    <span className="info-label">Ngày đặt:</span>
                    <span>{formatDate(selectedOrder.order_date)}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Trạng thái:</span>
                    <span className={`order-status ${getStatusClass(selectedOrder.status)}`}>
                      {orderStatus[selectedOrder.status] || selectedOrder.status}
                    </span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Địa chỉ giao hàng:</span>
                    <span>{selectedOrder.shipping_address}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Phương thức thanh toán:</span>
                    <span>
                      {selectedOrder.payment_method === 'cod'
                        ? 'Thanh toán khi nhận hàng'
                        : selectedOrder.payment_method === 'bank_transfer'
                        ? 'Chuyển khoản'
                        : 'Thẻ tín dụng'}
                    </span>
                  </div>
                </div>
              </div>

              {/* Order Items */}
              <div className="modal-section">
                <h3>Sản phẩm</h3>
                <div className="order-items">
                  {selectedOrder.items && selectedOrder.items.map((item, index) => (
                    <div key={index} className="order-item">
                      <div className="item-info">
                        <div className="item-name">{item.product_name}</div>
                        <div className="item-variant">
                          Size: {item.size} - Màu: {item.color}
                        </div>
                        <div className="item-quantity">Số lượng: x{item.quantity}</div>
                      </div>
                      <div className="item-price">
                        {formatPrice(item.price * item.quantity)}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Order Total */}
              <div className="modal-section">
                <div className="order-total">
                  <span>Tổng cộng:</span>
                  <span className="total-amount">
                    {formatPrice(selectedOrder.total_amount)}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default OrderHistory;

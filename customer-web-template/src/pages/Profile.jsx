import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import './Profile.css';

function Profile() {
  const { user, logout } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    name: user?.name || '',
    phone: user?.phone || '',
    address: user?.address || '',
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // TODO: Implement update profile API
    alert('Tính năng cập nhật thông tin đang được phát triển');
    setIsEditing(false);
  };

  const handleCancel = () => {
    setFormData({
      name: user?.name || '',
      phone: user?.phone || '',
      address: user?.address || '',
    });
    setIsEditing(false);
  };

  return (
    <div className="profile-page">
      <div className="container">
        <h1 className="page-title">👤 Thông tin tài khoản</h1>

        <div className="profile-layout">
          {/* Profile Card */}
          <div className="profile-card">
            <div className="profile-avatar">
              <div className="avatar-circle">
                <span className="avatar-icon">👤</span>
              </div>
              <h2 className="profile-name">{user?.name}</h2>
              <p className="profile-username">@{user?.username}</p>
            </div>

            <div className="profile-stats">
              <div className="stat-item">
                <div className="stat-value">0</div>
                <div className="stat-label">Đơn hàng</div>
              </div>
              <div className="stat-divider"></div>
              <div className="stat-item">
                <div className="stat-value">0đ</div>
                <div className="stat-label">Đã mua</div>
              </div>
            </div>
          </div>

          {/* Profile Info */}
          <div className="profile-info-card">
            <div className="card-header">
              <h2>Thông tin cá nhân</h2>
              {!isEditing && (
                <button className="btn btn-primary" onClick={() => setIsEditing(true)}>
                  ✏️ Chỉnh sửa
                </button>
              )}
            </div>

            {isEditing ? (
              <form onSubmit={handleSubmit} className="profile-form">
                <div className="form-group">
                  <label className="form-label">Họ và tên</label>
                  <input
                    type="text"
                    name="name"
                    className="form-control"
                    value={formData.name}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Số điện thoại</label>
                  <input
                    type="tel"
                    name="phone"
                    className="form-control"
                    value={formData.phone}
                    onChange={handleChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Địa chỉ</label>
                  <textarea
                    name="address"
                    className="form-control"
                    value={formData.address}
                    onChange={handleChange}
                    rows="3"
                  />
                </div>

                <div className="form-actions">
                  <button type="submit" className="btn btn-success">
                    💾 Lưu thay đổi
                  </button>
                  <button type="button" className="btn btn-secondary" onClick={handleCancel}>
                    ✖ Hủy
                  </button>
                </div>
              </form>
            ) : (
              <div className="profile-details">
                <div className="detail-row">
                  <span className="detail-label">Họ và tên:</span>
                  <span className="detail-value">{user?.name}</span>
                </div>

                <div className="detail-row">
                  <span className="detail-label">Tên đăng nhập:</span>
                  <span className="detail-value">{user?.username}</span>
                </div>

                <div className="detail-row">
                  <span className="detail-label">Số điện thoại:</span>
                  <span className="detail-value">{user?.phone}</span>
                </div>

                <div className="detail-row">
                  <span className="detail-label">Địa chỉ:</span>
                  <span className="detail-value">{user?.address || 'Chưa cập nhật'}</span>
                </div>

                <div className="detail-row">
                  <span className="detail-label">Vai trò:</span>
                  <span className="detail-value role-badge">
                    {user?.role === 'customer' ? 'Khách hàng' : user?.role}
                  </span>
                </div>
              </div>
            )}
          </div>

          {/* Account Actions */}
          <div className="account-actions-card">
            <h2>Cài đặt tài khoản</h2>

            <div className="action-list">
              <button className="action-item" onClick={() => alert('Tính năng đang phát triển')}>
                <span className="action-icon">🔑</span>
                <div className="action-info">
                  <div className="action-name">Đổi mật khẩu</div>
                  <div className="action-desc">Thay đổi mật khẩu của bạn</div>
                </div>
                <span className="action-arrow">›</span>
              </button>

              <button className="action-item" onClick={() => alert('Tính năng đang phát triển')}>
                <span className="action-icon">🔔</span>
                <div className="action-info">
                  <div className="action-name">Thông báo</div>
                  <div className="action-desc">Cài đặt thông báo đơn hàng</div>
                </div>
                <span className="action-arrow">›</span>
              </button>

              <button className="action-item" onClick={() => alert('Tính năng đang phát triển')}>
                <span className="action-icon">📍</span>
                <div className="action-info">
                  <div className="action-name">Địa chỉ giao hàng</div>
                  <div className="action-desc">Quản lý địa chỉ nhận hàng</div>
                </div>
                <span className="action-arrow">›</span>
              </button>

              <button className="action-item action-danger" onClick={logout}>
                <span className="action-icon">🚪</span>
                <div className="action-info">
                  <div className="action-name">Đăng xuất</div>
                  <div className="action-desc">Thoát khỏi tài khoản</div>
                </div>
                <span className="action-arrow">›</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Profile;

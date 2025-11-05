import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productAPI, formatPrice } from '../services/api';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import './ProductDetail.css';

function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const { isAuthenticated } = useAuth();

  const [product, setProduct] = useState(null);
  const [variants, setVariants] = useState([]);
  const [selectedVariant, setSelectedVariant] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadProductDetail();
  }, [id]);

  const loadProductDetail = async () => {
    try {
      setLoading(true);
      
      // Load product info
      const productRes = await productAPI.getById(id);
      if (productRes.data.success) {
        setProduct(productRes.data.data);
      }

      // Load variants
      const variantsRes = await productAPI.getVariants(id);
      if (variantsRes.data.success) {
        const variantList = variantsRes.data.data;
        setVariants(variantList);
        if (variantList.length > 0) {
          setSelectedVariant(variantList[0]);
        }
      }
    } catch (error) {
      console.error('Load product detail failed:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = () => {
    if (!isAuthenticated) {
      alert('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng');
      navigate('/login');
      return;
    }

    if (!selectedVariant) {
      alert('Vui lòng chọn size và màu');
      return;
    }

    if (quantity > selectedVariant.quantity_in_stock) {
      alert(`Chỉ còn ${selectedVariant.quantity_in_stock} sản phẩm trong kho`);
      return;
    }

    addToCart(product, selectedVariant, quantity);
    alert('Đã thêm vào giỏ hàng!');
  };

  const handleBuyNow = () => {
    handleAddToCart();
    navigate('/cart');
  };

  if (loading) {
    return <div className="loading">Đang tải thông tin sản phẩm...</div>;
  }

  if (!product) {
    return (
      <div className="container">
        <div className="no-product">
          <h2>Không tìm thấy sản phẩm</h2>
          <button className="btn btn-primary" onClick={() => navigate('/products')}>
            Quay lại danh sách
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="product-detail-page">
      <div className="container">
        <div className="product-detail">
          {/* Product Image */}
          <div className="product-image-section">
            <div className="product-main-image">
              <div className="image-placeholder">
                <span className="product-icon">👟</span>
              </div>
            </div>
          </div>

          {/* Product Info */}
          <div className="product-info-section">
            <h1 className="product-title">{product.name}</h1>
            <p className="product-description">{product.description}</p>

            <div className="product-price-section">
              <div className="price-label">Giá:</div>
              <div className="product-price">
                {selectedVariant
                  ? formatPrice(selectedVariant.price)
                  : formatPrice(product.base_price)}
              </div>
            </div>

            {/* Variant Selection */}
            {variants.length > 0 && (
              <div className="variant-selection">
                <h3>Chọn phiên bản:</h3>
                <div className="variant-grid">
                  {variants.map((variant) => (
                    <button
                      key={variant.variant_id}
                      className={`variant-option ${
                        selectedVariant?.variant_id === variant.variant_id
                          ? 'selected'
                          : ''
                      } ${variant.quantity_in_stock === 0 ? 'out-of-stock' : ''}`}
                      onClick={() => setSelectedVariant(variant)}
                      disabled={variant.quantity_in_stock === 0}
                    >
                      <div className="variant-label">
                        <span className="variant-size">Size {variant.size}</span>
                        <span className="variant-color">{variant.color}</span>
                      </div>
                      <div className="variant-price">
                        {formatPrice(variant.price)}
                      </div>
                      {variant.quantity_in_stock === 0 && (
                        <div className="out-of-stock-badge">Hết hàng</div>
                      )}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Quantity */}
            {selectedVariant && (
              <div className="quantity-section">
                <h3>Số lượng:</h3>
                <div className="quantity-control">
                  <button
                    className="quantity-btn"
                    onClick={() => setQuantity(Math.max(1, quantity - 1))}
                  >
                    −
                  </button>
                  <input
                    type="number"
                    className="quantity-input"
                    value={quantity}
                    onChange={(e) =>
                      setQuantity(Math.max(1, parseInt(e.target.value) || 1))
                    }
                    min="1"
                    max={selectedVariant.quantity_in_stock}
                  />
                  <button
                    className="quantity-btn"
                    onClick={() =>
                      setQuantity(
                        Math.min(selectedVariant.quantity_in_stock, quantity + 1)
                      )
                    }
                  >
                    +
                  </button>
                </div>
                <div className="stock-info">
                  Còn {selectedVariant.quantity_in_stock} sản phẩm
                </div>
              </div>
            )}

            {/* Action Buttons */}
            <div className="action-buttons">
              <button
                className="btn btn-primary btn-large"
                onClick={handleAddToCart}
                disabled={!selectedVariant || selectedVariant.quantity_in_stock === 0}
              >
                🛒 Thêm vào giỏ
              </button>
              <button
                className="btn btn-success btn-large"
                onClick={handleBuyNow}
                disabled={!selectedVariant || selectedVariant.quantity_in_stock === 0}
              >
                💳 Mua ngay
              </button>
            </div>

            {/* Additional Info */}
            <div className="additional-info">
              <div className="info-item">
                <span className="info-icon">✓</span>
                <span>Miễn phí vận chuyển cho đơn hàng trên 500k</span>
              </div>
              <div className="info-item">
                <span className="info-icon">✓</span>
                <span>Đổi trả miễn phí trong 7 ngày</span>
              </div>
              <div className="info-item">
                <span className="info-icon">✓</span>
                <span>Sản phẩm chính hãng 100%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductDetail;

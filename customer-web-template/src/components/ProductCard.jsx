import React from 'react';
import { Link } from 'react-router-dom';
import { formatPrice } from '../services/api';
import './ProductCard.css';

function ProductCard({ product }) {
  const { product_id, name, description, min_price, max_price, variant_count } = product;

  const displayPrice = min_price === max_price 
    ? formatPrice(min_price)
    : `${formatPrice(min_price)} - ${formatPrice(max_price)}`;

  return (
    <Link to={`/products/${product_id}`} className="product-card">
      <div className="product-image">
        <div className="product-placeholder">
          <span className="product-icon">👟</span>
        </div>
        {variant_count > 0 && (
          <div className="variant-badge">{variant_count} phiên bản</div>
        )}
      </div>

      <div className="product-info">
        <h3 className="product-name">{name}</h3>
        <p className="product-description">{description}</p>
        <div className="product-price">{displayPrice}</div>
        <button className="btn-view-detail">Xem chi tiết →</button>
      </div>
    </Link>
  );
}

export default ProductCard;

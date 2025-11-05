import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import ProductCard from '../components/ProductCard';
import { productAPI } from '../services/api';
import './ProductList.css';

function ProductList() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchKeyword, setSearchKeyword] = useState(searchParams.get('search') || '');

  useEffect(() => {
    loadProducts();
  }, [searchParams]);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const keyword = searchParams.get('search');
      
      let response;
      if (keyword) {
        response = await productAPI.search(keyword);
      } else {
        response = await productAPI.getAll();
      }

      if (response.data.success) {
        setProducts(response.data.data);
      }
    } catch (error) {
      console.error('Load products failed:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchKeyword.trim()) {
      setSearchParams({ search: searchKeyword });
    } else {
      setSearchParams({});
    }
  };

  const handleClearSearch = () => {
    setSearchKeyword('');
    setSearchParams({});
  };

  return (
    <div className="product-list-page">
      <div className="container">
        {/* Header */}
        <div className="page-header">
          <h1>🛍️ Tất cả sản phẩm</h1>
          <p>Khám phá bộ sưu tập giày đa dạng của chúng tôi</p>
        </div>

        {/* Search Bar */}
        <div className="search-section">
          <form onSubmit={handleSearch} className="search-form">
            <input
              type="text"
              className="search-input"
              placeholder="Tìm kiếm sản phẩm..."
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
            />
            <button type="submit" className="btn btn-primary">
              🔍 Tìm kiếm
            </button>
            {searchParams.get('search') && (
              <button
                type="button"
                className="btn btn-secondary"
                onClick={handleClearSearch}
              >
                ✖ Xóa
              </button>
            )}
          </form>
        </div>

        {/* Filter Section */}
        <div className="filter-section">
          <div className="filter-info">
            {searchParams.get('search') ? (
              <p>
                Kết quả tìm kiếm cho: <strong>"{searchParams.get('search')}"</strong>
                {' '}({products.length} sản phẩm)
              </p>
            ) : (
              <p>Hiển thị {products.length} sản phẩm</p>
            )}
          </div>
        </div>

        {/* Products Grid */}
        {loading ? (
          <div className="loading">Đang tải sản phẩm...</div>
        ) : products.length > 0 ? (
          <div className="grid grid-cols-4">
            {products.map((product) => (
              <ProductCard key={product.product_id} product={product} />
            ))}
          </div>
        ) : (
          <div className="no-products">
            <div className="no-products-icon">📦</div>
            <h3>Không tìm thấy sản phẩm</h3>
            <p>
              {searchParams.get('search')
                ? 'Thử tìm kiếm với từ khóa khác'
                : 'Chưa có sản phẩm nào'}
            </p>
            {searchParams.get('search') && (
              <button className="btn btn-primary" onClick={handleClearSearch}>
                Xem tất cả sản phẩm
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default ProductList;

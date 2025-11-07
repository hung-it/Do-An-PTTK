-- ============================================
-- HỆ THỐNG QUẢN LÝ CỬA HÀNG GIÀY
-- Database Setup Script (Tất cả trong 1 file)
-- Date: 07/11/2025
-- ============================================
-- HƯỚNG DẪN:
-- 1. Tạo database: createdb -U postgres shoe_store_management
-- 2. Chạy file này: psql -U postgres -d shoe_store_management -f sql/setup_database.sql
-- ============================================

-- ============================================
-- PHẦN 1: TẠO CÁC BẢNG
-- ============================================

-- Bảng 1: USER (Quản lý Admin và Staff)
CREATE TABLE IF NOT EXISTS "user" (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL -- 'Admin' hoặc 'Staff'
);

-- Bảng 2: PRODUCT (Thông tin chung về mẫu giày)
CREATE TABLE IF NOT EXISTS product (
    product_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    base_price NUMERIC(10, 2) NOT NULL, -- Giá cơ bản
    image_url VARCHAR(500) -- URL hoặc đường dẫn hình ảnh sản phẩm
);

-- Bảng 3: PRODUCT_VARIANT (Đơn vị Tồn kho - SKU)
CREATE TABLE IF NOT EXISTS product_variant (
    variant_id SERIAL PRIMARY KEY,
    product_id INT REFERENCES product(product_id) ON DELETE CASCADE,
    sku_code VARCHAR(100) UNIQUE NOT NULL,
    size VARCHAR(10) NOT NULL,
    color VARCHAR(50) NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    price NUMERIC(10, 2) NOT NULL
);

-- Bảng 4: CUSTOMER (Khách hàng - hỗ trợ cả offline và online)
CREATE TABLE IF NOT EXISTS customer (
    customer_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) UNIQUE,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(255),
    address TEXT, -- Địa chỉ giao hàng mặc định
    join_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- Ngày tham gia
);

-- Bảng 5: ORDER (Hóa đơn - hỗ trợ cả POS và Online)
CREATE TABLE IF NOT EXISTS "order" (
    order_id SERIAL PRIMARY KEY,
    order_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    staff_id INT REFERENCES "user"(user_id), -- NULL nếu đặt online
    customer_id INT REFERENCES customer(customer_id),
    total_amount NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending', -- pending, processing, shipped, delivered, cancelled
    shipping_address TEXT, -- Địa chỉ giao hàng cụ thể
    payment_method VARCHAR(50) -- COD, Banking, Momo, ZaloPay
);

-- Bảng 6: ORDER_DETAIL (Chi tiết hóa đơn)
CREATE TABLE IF NOT EXISTS order_detail (
    detail_id SERIAL PRIMARY KEY,
    order_id INT REFERENCES "order"(order_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variant(variant_id),
    quantity_sold INT NOT NULL,
    unit_price NUMERIC(10, 2)
);

-- ============================================
-- PHẦN 2: THÊM CONSTRAINTS VÀ INDEXES
-- ============================================

-- Constraint cho status
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'check_order_status'
    ) THEN
        ALTER TABLE "order" ADD CONSTRAINT check_order_status 
        CHECK (status IN ('pending', 'processing', 'shipped', 'delivered', 'cancelled'));
    END IF;
END $$;

-- Indexes để tăng hiệu suất
CREATE INDEX IF NOT EXISTS idx_order_customer_status ON "order"(customer_id, status);
CREATE INDEX IF NOT EXISTS idx_order_date ON "order"(order_date DESC);
CREATE INDEX IF NOT EXISTS idx_product_variant_product ON product_variant(product_id);

-- Comments
COMMENT ON TABLE "order" IS 'Bảng đơn hàng - hỗ trợ cả offline (POS) và online ordering';
COMMENT ON COLUMN "order".status IS 'Trạng thái: pending, processing, shipped, delivered, cancelled';
COMMENT ON COLUMN "order".shipping_address IS 'Địa chỉ giao hàng cụ thể cho đơn hàng';
COMMENT ON COLUMN "order".payment_method IS 'Phương thức: COD, Banking, Momo, ZaloPay';
COMMENT ON COLUMN customer.address IS 'Địa chỉ giao hàng mặc định';
COMMENT ON COLUMN customer.join_date IS 'Ngày khách hàng tham gia hệ thống';
COMMENT ON COLUMN product.image_url IS 'URL hoặc đường dẫn đến hình ảnh sản phẩm';

-- ============================================
-- PHẦN 3: DỮ LIỆU MẪU (DEMO DATA)
-- ============================================

-- Xóa dữ liệu cũ nếu có (chỉ khi cần reset hoàn toàn)
-- TRUNCATE TABLE order_detail, "order", product_variant, product, customer, "user" CASCADE;

-- Thêm tài khoản Admin và Staff (chỉ thêm nếu chưa có)
INSERT INTO "user" (username, password, role) 
SELECT 'admin', '123', 'Admin'
WHERE NOT EXISTS (SELECT 1 FROM "user" WHERE username = 'admin');

INSERT INTO "user" (username, password, role) 
SELECT 'staff1', '456', 'Staff'
WHERE NOT EXISTS (SELECT 1 FROM "user" WHERE username = 'staff1');

-- Thêm Khách hàng (chỉ thêm nếu chưa có)
INSERT INTO customer (name, phone_number, username, password, address) 
SELECT 'Nguyen Viet Hung', '0123456789', 'hung', '123', 'Số 1 Đại Cồ Việt, Hai Bà Trưng, Hà Nội'
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE phone_number = '0123456789');

INSERT INTO customer (name, phone_number, username, password, address) 
SELECT 'Nguyen Tuan An', '0987654321', 'tuanan', '456', 'Số 144 Xuân Thủy, Cầu Giấy, Hà Nội'
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE phone_number = '0987654321');

INSERT INTO customer (name, phone_number, username, password, address) 
SELECT 'Nguyen Gia Hung', '0111111111', 'giahung', '123', 'Số 54 Triều Khúc, Thanh Xuân, Hà Nội'
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE phone_number = '0111111111');

-- Thêm Sản phẩm mẫu (chỉ thêm nếu chưa có)
INSERT INTO product (product_id, name, description, base_price, image_url)
SELECT 1, 'Giày Runner X', 'Giày chạy bộ siêu nhẹ', 1500000.00, '/images/products/giay-runner-x.jpg'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id = 1);

INSERT INTO product (product_id, name, description, base_price, image_url)
SELECT 2, 'Giày Da Classic', 'Giày da công sở cao cấp', 2200000.00, '/images/products/giay-da-classic.jpg'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id = 2);

INSERT INTO product (product_id, name, description, base_price, image_url)
SELECT 3, 'Giày Thể Thao Flex', 'Giày thể thao đa năng', 1800000.00, '/images/products/giay-the-thao-flex.jpg'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_id = 3);

-- Cập nhật sequence để tránh xung đột ID
SELECT setval('product_product_id_seq', (SELECT COALESCE(MAX(product_id), 1) FROM product));

-- Thêm Biến thể (SKU) và Tồn kho (chỉ thêm nếu chưa có)
INSERT INTO product_variant (product_id, sku_code, size, color, quantity_in_stock, price)
SELECT 1, 'RX-40-DEN', '40', 'Đen', 50, 1500000.00
WHERE NOT EXISTS (SELECT 1 FROM product_variant WHERE sku_code = 'RX-40-DEN');

INSERT INTO product_variant (product_id, sku_code, size, color, quantity_in_stock, price)
SELECT 1, 'RX-41-DEN', '41', 'Đen', 30, 1500000.00
WHERE NOT EXISTS (SELECT 1 FROM product_variant WHERE sku_code = 'RX-41-DEN');

INSERT INTO product_variant (product_id, sku_code, size, color, quantity_in_stock, price)
SELECT 1, 'RX-40-TRG', '40', 'Trắng', 15, 1600000.00
WHERE NOT EXISTS (SELECT 1 FROM product_variant WHERE sku_code = 'RX-40-TRG');

INSERT INTO product_variant (product_id, sku_code, size, color, quantity_in_stock, price)
SELECT 2, 'DC-42-NAU', '42', 'Nâu', 25, 2200000.00
WHERE NOT EXISTS (SELECT 1 FROM product_variant WHERE sku_code = 'DC-42-NAU');

INSERT INTO product_variant (product_id, sku_code, size, color, quantity_in_stock, price)
SELECT 2, 'DC-43-NAU', '43', 'Nâu', 10, 2200000.00
WHERE NOT EXISTS (SELECT 1 FROM product_variant WHERE sku_code = 'DC-43-NAU');

INSERT INTO product_variant (product_id, sku_code, size, color, quantity_in_stock, price)
SELECT 3, 'TF-39-XANH', '39', 'Xanh', 40, 1800000.00
WHERE NOT EXISTS (SELECT 1 FROM product_variant WHERE sku_code = 'TF-39-XANH');

INSERT INTO product_variant (product_id, sku_code, size, color, quantity_in_stock, price)
SELECT 3, 'TF-40-XANH', '40', 'Xanh', 20, 1800000.00
WHERE NOT EXISTS (SELECT 1 FROM product_variant WHERE sku_code = 'TF-40-XANH');

-- ============================================
-- PHẦN 4: VERIFICATION (Kiểm tra)
-- ============================================

-- Hiển thị kết quả
SELECT '✓ DATABASE SETUP COMPLETED!' as status;

SELECT 
    '✓ Tables Created' as step,
    (SELECT COUNT(*) FROM information_schema.tables 
     WHERE table_schema = 'public' AND table_type = 'BASE TABLE') as table_count;

SELECT 
    '✓ Users' as type,
    COUNT(*) as count
FROM "user"
UNION ALL
SELECT '✓ Customers', COUNT(*) FROM customer
UNION ALL
SELECT '✓ Products', COUNT(*) FROM product
UNION ALL
SELECT '✓ Product Variants', COUNT(*) FROM product_variant
UNION ALL
SELECT '✓ Orders', COUNT(*) FROM "order"
UNION ALL
SELECT '✓ Order Details', COUNT(*) FROM order_detail;

-- Hiển thị thông tin chi tiết
\echo '============================================'
\echo 'THÔNG TIN TÀI KHOẢN DEMO:'
\echo '============================================'

SELECT 'ADMIN/STAFF ACCOUNTS:' as info;
SELECT user_id, username, role FROM "user" ORDER BY user_id;

\echo ''
SELECT 'CUSTOMER ACCOUNTS:' as info;
SELECT customer_id, name, phone_number, username FROM customer ORDER BY customer_id;

\echo ''
SELECT 'PRODUCTS:' as info;
SELECT product_id, name, base_price FROM product ORDER BY product_id;

\echo ''
SELECT 'PRODUCT VARIANTS (SKU):' as info;
SELECT 
    pv.sku_code,
    p.name as product_name,
    pv.size,
    pv.color,
    pv.quantity_in_stock,
    pv.price
FROM product_variant pv
JOIN product p ON pv.product_id = p.product_id
ORDER BY pv.variant_id;

-- Kịch bản SQL để tạo các bảng cho đồ án Quản lý Cửa hàng Giày
-- Sử dụng SERIAL cho PostgreSQL để tự động tăng (Auto Increment)

-- Bảng 1: USER (Quản lý Admin và Staff)
CREATE TABLE "user" (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL -- 'Admin' hoặc 'Staff'
);

-- Bảng 2: PRODUCT (Thông tin chung về mẫu giày)
CREATE TABLE product (
    product_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    base_price NUMERIC(10, 2) NOT NULL -- Sử dụng NUMERIC cho tiền tệ [1]
);

-- Bảng 3: PRODUCT_VARIANT (Đơn vị Tồn kho - SKU)
-- Đây là bảng cốt lõi chứa tồn kho và được dùng trong giao dịch [2]
CREATE TABLE product_variant (
    variant_id SERIAL PRIMARY KEY,
    product_id INT REFERENCES product(product_id) ON DELETE CASCADE,
    sku_code VARCHAR(100) UNIQUE NOT NULL,
    size VARCHAR(10) NOT NULL,
    color VARCHAR(50) NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    price NUMERIC(10, 2) NOT NULL
);

-- Bảng 4: CUSTOMER
CREATE TABLE customer (
    customer_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) UNIQUE,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(255)
);

-- Bảng 5: ORDER (Hóa đơn)
CREATE TABLE "order" (
    order_id SERIAL PRIMARY KEY,
    order_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    staff_id INT REFERENCES "user"(user_id),
    customer_id INT REFERENCES customer(customer_id),
    total_amount NUMERIC(10, 2) NOT NULL
);

-- Bảng 6: ORDER_DETAIL (Chi tiết hóa đơn)
CREATE TABLE order_detail (
    detail_id SERIAL PRIMARY KEY,
    order_id INT REFERENCES "order"(order_id) ON DELETE CASCADE,
    variant_id INT REFERENCES product_variant(variant_id), -- Liên kết với SKU [1]
    quantity_sold INT NOT NULL,
    unit_price NUMERIC(10, 2)
);

-- Dữ liệu Demo 

-- Thêm tài khoản Admin và Staff 
INSERT INTO "user" (username, password, role) VALUES ('admin', '123', 'Admin');
INSERT INTO "user" (username, password, role) VALUES ('staff1', '456', 'Staff');
-- Thêm Khách hàng
INSERT INTO customer (name, phone_number, username, password) VALUES
('Nguyen Viet Hung', '0123456789', 'hung', '123'),
('Nguyen Tuan An', '0987654321', 'tuanan', '456'),
('Nguyen Gia Hung', '0111111111', 'giahung', '123');

-- Thêm Sản phẩm mẫu
INSERT INTO product (product_id, name, description, base_price) VALUES 
(1, 'Giày Runner X', 'Giày chạy bộ siêu nhẹ', 1500000.00),
(2, 'Giày Da Classic', 'Giày da công sở cao cấp', 2200000.00),
(3, 'Giày Thể Thao Flex', 'Giày thể thao đa năng', 1800000.00);

-- Thêm Biến thể (SKU) và Tồn kho
INSERT INTO product_variant (product_id, sku_code, size, color, quantity_in_stock, price) VALUES
(1, 'RX-40-DEN', '40', 'Đen', 50, 1500000.00),
(1, 'RX-41-DEN', '41', 'Đen', 30, 1500000.00),
(1, 'RX-40-TRG', '40', 'Trắng', 15, 1600000.00),
(2, 'DC-42-NAU', '42', 'Nâu', 25, 2200000.00),
(2, 'DC-43-NAU', '43', 'Nâu', 10, 2200000.00),
(3, 'TF-39-XANH', '39', 'Xanh', 40, 1800000.00),
(3, 'TF-40-XANH', '40', 'Xanh', 20, 1800000.00);

-- Chạy file này trên PostgreSQL để tạo CSDL

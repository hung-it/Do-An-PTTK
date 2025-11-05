-- Migration: Thêm các trường cần thiết cho online ordering
-- Run this script after init_schema.sql

-- 1. Thêm cột status cho đơn hàng
ALTER TABLE "order" ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'pending';

COMMENT ON COLUMN "order".status IS 'Trạng thái đơn hàng: pending, processing, shipped, delivered, cancelled';

-- 2. Thêm địa chỉ giao hàng vào bảng customer
ALTER TABLE customer ADD COLUMN IF NOT EXISTS address TEXT;

COMMENT ON COLUMN customer.address IS 'Địa chỉ giao hàng mặc định của khách hàng';

-- 3. Thêm địa chỉ giao hàng và phương thức thanh toán vào đơn hàng
ALTER TABLE "order" ADD COLUMN IF NOT EXISTS shipping_address TEXT;
ALTER TABLE "order" ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50);

COMMENT ON COLUMN "order".shipping_address IS 'Địa chỉ giao hàng cụ thể cho đơn hàng này';
COMMENT ON COLUMN "order".payment_method IS 'Phương thức thanh toán: COD, Banking, Momo, ZaloPay';

-- 4. Cập nhật dữ liệu mẫu
UPDATE customer SET address = 'Số 1 Đại Cồ Việt, Hai Bà Trưng, Hà Nội' WHERE customer_id = 1;
UPDATE customer SET address = 'Số 144 Xuân Thủy, Cầu Giấy, Hà Nội' WHERE customer_id = 2;
UPDATE customer SET address = 'Số 54 Triều Khúc, Thanh Xuân, Hà Nội' WHERE customer_id = 3;

-- 5. Thêm index để tăng hiệu suất truy vấn
CREATE INDEX IF NOT EXISTS idx_order_customer_status ON "order"(customer_id, status);
CREATE INDEX IF NOT EXISTS idx_order_date ON "order"(order_date DESC);

-- 6. Thêm constraint cho status
ALTER TABLE "order" ADD CONSTRAINT check_order_status 
CHECK (status IN ('pending', 'processing', 'shipped', 'delivered', 'cancelled'));

-- 7. Verification
SELECT 
    'Migration completed successfully!' as status,
    (SELECT COUNT(*) FROM customer WHERE address IS NOT NULL) as customers_with_address,
    (SELECT COUNT(*) FROM "order") as total_orders;

COMMENT ON TABLE "order" IS 'Bảng đơn hàng - hỗ trợ cả offline (POS) và online ordering';

-- Migration SQL: Thêm các columns còn thiếu vào database

-- Thêm columns cho bảng customer
ALTER TABLE customer 
ADD COLUMN IF NOT EXISTS address TEXT,
ADD COLUMN IF NOT EXISTS join_date DATE DEFAULT CURRENT_DATE;

-- Thêm columns cho bảng order
ALTER TABLE "order" 
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'pending',
ADD COLUMN IF NOT EXISTS shipping_address TEXT,
ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50);

-- Cập nhật dữ liệu cũ (nếu có)
UPDATE customer SET join_date = CURRENT_DATE WHERE join_date IS NULL;
UPDATE "order" SET status = 'pending' WHERE status IS NULL;

-- Hiển thị kết quả
SELECT 'Migration completed successfully!' as message;

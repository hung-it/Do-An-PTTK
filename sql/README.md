# 📚 HƯỚNG DẪN SỬ DỤNG DATABASE SETUP

## 📋 Tổng quan

File `setup_database.sql` là **file SQL duy nhất** chứa tất cả những gì cần thiết để setup database cho đồ án:
- ✅ Tạo tất cả các bảng (6 bảng)
- ✅ Thêm constraints và indexes
- ✅ Insert dữ liệu mẫu (demo data)
- ✅ Kiểm tra kết quả

## 🎯 Các tình huống sử dụng

### 🆕 Tình huống 1: Lần đầu setup database

```powershell
# Bước 1: Tạo database mới
createdb -U postgres shoe_store_management

# Bước 2: Chạy setup script
psql -U postgres -d shoe_store_management -f setup_database.sql
```

**Kết quả:** Database mới hoàn chỉnh với 2 users, 3 customers, 3 products, 7 variants.

---

### 🔄 Tình huống 2: Database đã tồn tại, muốn thêm dữ liệu

File này **an toàn** khi chạy lại vì:
- Sử dụng `CREATE TABLE IF NOT EXISTS` (không tạo lại nếu đã có)
- Sử dụng `INSERT ... WHERE NOT EXISTS` (không duplicate data)

```powershell
# Chỉ cần chạy lại file
psql -U postgres -d shoe_store_management -f setup_database.sql
```

**Kết quả:** Chỉ thêm dữ liệu mới nếu chưa có, không làm mất dữ liệu cũ.

---

### 🗑️ Tình huống 3: Muốn reset hoàn toàn database

```powershell
# Bước 1: Xóa database cũ
dropdb -U postgres shoe_store_management

# Bước 2: Tạo database mới
createdb -U postgres shoe_store_management

# Bước 3: Setup lại từ đầu
psql -U postgres -d shoe_store_management -f setup_database.sql
```

**Kết quả:** Database mới hoàn toàn, xóa sạch dữ liệu cũ.

---

### 🔍 Tình huống 4: Chỉ muốn thêm 1 customer/product mới

Bạn có thể:

**Cách 1: Chỉnh sửa file `setup_database.sql`**
1. Mở file bằng editor
2. Tìm đến phần `-- Thêm Khách hàng`
3. Thêm dòng mới:
```sql
INSERT INTO customer (name, phone_number, username, password, address) 
SELECT 'Ten Khach Hang', '0988888888', 'username_moi', '123', 'Dia chi'
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE phone_number = '0988888888');
```
4. Lưu và chạy lại file

**Cách 2: Chạy trực tiếp SQL command**
```powershell
psql -U postgres -d shoe_store_management
```
```sql
-- Trong psql, chạy:
INSERT INTO customer (name, phone_number, username, password, address) 
VALUES ('Ten Khach Hang', '0988888888', 'username_moi', '123', 'Dia chi');
```

---

## 🖥️ Các cách chạy file SQL

### Cách 1: Command Line (Khuyên dùng - Nhanh nhất)

**Windows PowerShell:**
```powershell
# Nếu psql đã có trong PATH:
psql -U postgres -d shoe_store_management -f setup_database.sql

# Nếu chưa có trong PATH (đường dẫn đầy đủ):
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -d shoe_store_management -f setup_database.sql
```

**Mac/Linux Terminal:**
```bash
psql -U postgres -d shoe_store_management -f setup_database.sql
```

**Lưu ý:**
- Chạy command từ thư mục `sql/` hoặc dùng đường dẫn: `-f sql/setup_database.sql`
- Sẽ hỏi password PostgreSQL (thường là `postgres`)

---

### Cách 2: pgAdmin (GUI - Dễ cho người mới)

**Bước 1:** Mở pgAdmin 4

**Bước 2:** Kết nối database
- Mở `Servers` → `PostgreSQL 18`
- Nhập password nếu cần
- Mở `Databases` → `shoe_store_management`

**Bước 3:** Mở Query Tool
- Click chuột phải vào `shoe_store_management`
- Chọn **"Query Tool"**

**Bước 4:** Load file
- Menu: `File` → `Open File`
- Chọn `setup_database.sql`

**Bước 5:** Execute
- Click nút ▶️ (Execute/Refresh) hoặc nhấn `F5`
- Đợi 2-3 giây

**Bước 6:** Xem kết quả
- Tab `Data Output` sẽ hiển thị kết quả
- Tab `Messages` hiển thị logs

---

### Cách 3: Copy-Paste (Backup option)

1. Mở file `setup_database.sql` bằng Notepad++/VSCode
2. Copy toàn bộ nội dung (`Ctrl+A`, `Ctrl+C`)
3. Mở pgAdmin Query Tool
4. Paste vào (`Ctrl+V`)
5. Execute (`F5`)

---

## ✅ Kiểm tra sau khi setup

### Kiểm tra 1: Số lượng bảng

```sql
-- Trong psql hoặc pgAdmin Query Tool:
\dt

-- Hoặc:
SELECT COUNT(*) as table_count 
FROM information_schema.tables 
WHERE table_schema = 'public' AND table_type = 'BASE TABLE';
```

**Kết quả mong đợi:** 6 bảng

---

### Kiểm tra 2: Dữ liệu demo

```sql
-- Kiểm tra users
SELECT user_id, username, role FROM "user";
-- Kết quả: 2 rows (admin, staff1)

-- Kiểm tra customers
SELECT customer_id, name, phone_number FROM customer;
-- Kết quả: 3 rows

-- Kiểm tra products
SELECT product_id, name FROM product;
-- Kết quả: 3 rows

-- Kiểm tra product variants
SELECT COUNT(*) FROM product_variant;
-- Kết quả: 7
```

---

### Kiểm tra 3: Columns đầy đủ

```sql
-- Kiểm tra bảng customer có đủ columns
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'customer'
ORDER BY ordinal_position;
```

**Columns cần có:**
- customer_id
- name
- phone_number
- username
- password
- address (cho online ordering)
- join_date (ngày tham gia)

```sql
-- Kiểm tra bảng order có đủ columns
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'order'
ORDER BY ordinal_position;
```

**Columns cần có:**
- order_id
- order_date
- staff_id
- customer_id
- total_amount
- status (pending, processing, shipped, etc.)
- shipping_address (địa chỉ giao hàng)
- payment_method (COD, Banking, etc.)

---

## 🔧 Troubleshooting

### Lỗi: "database does not exist"

**Nguyên nhân:** Chưa tạo database

**Giải pháp:**
```powershell
createdb -U postgres shoe_store_management
```

---

### Lỗi: "permission denied"

**Nguyên nhân:** User không có quyền

**Giải pháp:**
```powershell
# Chạy với user postgres (superuser)
psql -U postgres -d shoe_store_management -f setup_database.sql
```

---

### Lỗi: "relation already exists"

**Nguyên nhân:** Bảng đã tồn tại (nhưng không ảnh hưởng gì)

**Giải pháp:** 
- File sử dụng `IF NOT EXISTS` nên lỗi này sẽ được bỏ qua
- Không cần làm gì cả, script sẽ tiếp tục chạy

---

### Lỗi: "duplicate key value"

**Nguyên nhân:** Dữ liệu đã tồn tại

**Giải pháp:**
- File sử dụng `WHERE NOT EXISTS` nên không tạo duplicate
- Nếu vẫn gặp lỗi, có thể do manual insert trước đó
- Bỏ qua hoặc reset database (Tình huống 3)

---

### Muốn xem toàn bộ log khi chạy

```powershell
# Redirect output ra file
psql -U postgres -d shoe_store_management -f setup_database.sql > output.log 2>&1

# Xem file log
cat output.log
```

---

## 📊 Dữ liệu demo có sẵn

### 👥 Users (Admin/Staff)

| user_id | username | password | role  |
|---------|----------|----------|-------|
| 1       | admin    | 123      | Admin |
| 2       | staff1   | 456      | Staff |

### 👤 Customers

| customer_id | name              | phone      | username  | password |
|-------------|-------------------|------------|-----------|----------|
| 1           | Nguyen Viet Hung  | 0123456789 | hung      | 123      |
| 2           | Nguyen Tuan An    | 0987654321 | tuanan    | 456      |
| 3           | Nguyen Gia Hung   | 0111111111 | giahung   | 123      |

### 👟 Products

| product_id | name                 | base_price  |
|------------|----------------------|-------------|
| 1          | Giày Runner X        | 1,500,000 đ |
| 2          | Giày Da Classic      | 2,200,000 đ |
| 3          | Giày Thể Thao Flex   | 1,800,000 đ |

### 📦 Product Variants (7 SKUs)

| SKU         | Product    | Size | Color  | Stock | Price       |
|-------------|------------|------|--------|-------|-------------|
| RX-40-DEN   | Runner X   | 40   | Đen    | 50    | 1,500,000 đ |
| RX-41-DEN   | Runner X   | 41   | Đen    | 30    | 1,500,000 đ |
| RX-40-TRG   | Runner X   | 40   | Trắng  | 15    | 1,600,000 đ |
| DC-42-NAU   | Da Classic | 42   | Nâu    | 25    | 2,200,000 đ |
| DC-43-NAU   | Da Classic | 43   | Nâu    | 10    | 2,200,000 đ |
| TF-39-XANH  | TT Flex    | 39   | Xanh   | 40    | 1,800,000 đ |
| TF-40-XANH  | TT Flex    | 40   | Xanh   | 20    | 1,800,000 đ |

---

## 🎓 Best Practices

### ✅ DO (Nên làm)

1. **Backup trước khi reset:**
```powershell
pg_dump -U postgres shoe_store_management > backup.sql
```

2. **Chạy trong transaction khi test:**
```sql
BEGIN;
-- Chạy các lệnh test
ROLLBACK; -- Hoặc COMMIT nếu OK
```

3. **Kiểm tra kết quả sau mỗi bước:**
```sql
SELECT COUNT(*) FROM "user";
SELECT COUNT(*) FROM customer;
```

### ❌ DON'T (Không nên làm)

1. ❌ Không xóa trực tiếp bảng bằng `DROP TABLE` (dùng dropdb rồi tạo lại)
2. ❌ Không chỉnh sửa trực tiếp trong production (test trước ở local)
3. ❌ Không commit password thật vào Git (dữ liệu demo OK)

---

## 📞 Hỗ trợ

Nếu gặp vấn đề:

1. Kiểm tra PostgreSQL đang chạy:
```powershell
Get-Service postgresql*
```

2. Kiểm tra có thể kết nối:
```powershell
psql -U postgres -c "SELECT version();"
```

3. Kiểm tra database tồn tại:
```powershell
psql -U postgres -l | Select-String "shoe_store"
```

4. Xem logs PostgreSQL:
```
C:\Program Files\PostgreSQL\18\data\log\
```

---

**📅 Last updated:** 07/11/2025  
**📚 Môn học:** Phân tích Thiết kế Hướng Đối tượng (PTTK)

# 🚀 Shoe Store REST API

REST API backend cho hệ thống đặt hàng online - Cửa hàng giày PTTK.

## 📋 Yêu cầu

- **JDK 11+**
- **Maven 3.6+**
- **PostgreSQL 12+**
- **Database**: `shoe_store_management` (đã setup từ Desktop app)

## ⚙️ Cài đặt

### 1. Chuẩn bị Database

```bash
# Chạy migration để thêm các trường cho online ordering
psql -U postgres -d shoe_store_management -f ../sql/migration_online_order.sql
```

### 2. Cấu hình kết nối

Sửa file `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/shoe_store_management
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD  # Đổi password của bạn
```

### 3. Build và chạy

**Cách 1: Maven**
```bash
cd api
mvn clean install
mvn spring-boot:run
```

**Cách 2: JAR file**
```bash
mvn clean package
java -jar target/shoe-store-api-1.0.0.jar
```

Server sẽ chạy tại: **http://localhost:8080/api**

## 📡 API Endpoints

### 🔐 Authentication

#### Đăng ký khách hàng
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Nguyễn Văn A",
  "phone": "0912345678",
  "username": "nguyenvana",
  "password": "123456",
  "address": "Số 1 Đại Cồ Việt, Hai Bà Trưng, Hà Nội"
}
```

#### Đăng nhập
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "nguyenvana",
  "password": "123456"
}

Response:
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "token": "fake-jwt-token-1",
    "customer": {
      "customer_id": 1,
      "username": "nguyenvana",
      "name": "Nguyễn Văn A",
      "phone": "0912345678",
      "address": "..."
    }
  }
}
```

#### Lấy thông tin profile
```http
GET /api/auth/me
Authorization: Bearer fake-jwt-token-1
```

---

### 👟 Products

#### Lấy danh sách sản phẩm
```http
GET /api/products

Response:
{
  "success": true,
  "data": [
    {
      "product_id": 1,
      "name": "Giày Runner X",
      "description": "Giày chạy bộ chuyên nghiệp",
      "base_price": 1500000,
      "variant_count": 3,
      "min_price": 1450000,
      "max_price": 1550000
    }
  ]
}
```

#### Lấy chi tiết sản phẩm
```http
GET /api/products/{id}
```

#### Lấy biến thể của sản phẩm
```http
GET /api/products/{id}/variants

Response:
{
  "success": true,
  "data": [
    {
      "variant_id": 1,
      "product_id": 1,
      "product_name": "Giày Runner X",
      "sku_code": "RX-40-BLK",
      "size": "40",
      "color": "Đen",
      "quantity_in_stock": 50,
      "price": 1500000
    }
  ]
}
```

#### Tìm kiếm sản phẩm
```http
GET /api/products/search?keyword=runner
```

---

### 🛒 Orders

#### Tạo đơn hàng mới
```http
POST /api/orders
Authorization: Bearer fake-jwt-token-1
Content-Type: application/json

{
  "items": [
    {
      "variantId": 1,
      "quantity": 2,
      "price": 1500000
    },
    {
      "variantId": 3,
      "quantity": 1,
      "price": 2200000
    }
  ],
  "shippingAddress": "Số 1 Đại Cồ Việt, Hai Bà Trưng, Hà Nội",
  "paymentMethod": "COD"
}

Response:
{
  "success": true,
  "message": "Đặt hàng thành công",
  "data": {
    "order_id": 1,
    "total_amount": 5200000,
    "status": "pending"
  }
}
```

#### Lấy lịch sử đơn hàng
```http
GET /api/orders
Authorization: Bearer fake-jwt-token-1

Response:
{
  "success": true,
  "data": [
    {
      "order_id": 1,
      "order_date": "2024-11-04T10:30:00",
      "total_amount": 5200000,
      "status": "pending",
      "item_count": 2
    }
  ]
}
```

#### Lấy chi tiết đơn hàng
```http
GET /api/orders/{orderId}
Authorization: Bearer fake-jwt-token-1

Response:
{
  "success": true,
  "data": {
    "order_id": 1,
    "order_date": "2024-11-04T10:30:00",
    "total_amount": 5200000,
    "status": "pending",
    "shipping_address": "Số 1 Đại Cồ Việt...",
    "payment_method": "COD",
    "items": [
      {
        "product_name": "Giày Runner X",
        "sku_code": "RX-40-BLK",
        "size": "40",
        "color": "Đen",
        "quantity": 2,
        "unit_price": 1500000
      }
    ]
  }
}
```

---

## 🧪 Test API với curl

```bash
# 1. Đăng ký
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","phone":"0912345678","username":"testuser","password":"123","address":"HN"}'

# 2. Đăng nhập
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123"}'

# 3. Lấy sản phẩm
curl http://localhost:8080/api/products

# 4. Đặt hàng (thay TOKEN)
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer fake-jwt-token-1" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"variantId":1,"quantity":1,"price":1500000}],"shippingAddress":"HN","paymentMethod":"COD"}'
```

---

## 📊 Database Schema Updates

Migration đã thêm:

```sql
-- Trạng thái đơn hàng
ALTER TABLE "order" ADD COLUMN status VARCHAR(20) DEFAULT 'pending';
-- Values: pending, processing, shipped, delivered, cancelled

-- Địa chỉ khách hàng
ALTER TABLE customer ADD COLUMN address TEXT;

-- Thông tin giao hàng
ALTER TABLE "order" ADD COLUMN shipping_address TEXT;
ALTER TABLE "order" ADD COLUMN payment_method VARCHAR(50);
-- Values: COD, Banking, Momo, ZaloPay
```

---

## 🔧 TODO / Improvements

- [ ] Implement real JWT authentication
- [ ] Hash passwords with BCrypt
- [ ] Add input validation
- [ ] Add rate limiting
- [ ] Implement cart functionality
- [ ] Add payment gateway integration
- [ ] Add email notifications
- [ ] Add Swagger documentation
- [ ] Add unit tests

---

## 📞 Support

Để chạy cùng với Desktop app:
1. Desktop app (Java Swing): Quản lý nội bộ
2. REST API (Spring Boot): Backend cho web/mobile
3. React Web App: Khách hàng đặt hàng online

**Next Step:** Tạo React frontend! 🚀

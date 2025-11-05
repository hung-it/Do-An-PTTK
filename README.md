# 🏪 HỆ THỐNG QUẢN LÝ CỬA HÀNG GIÀY - FULL STACK

## 📖 Tổng quan dự án

**Đồ án môn học:** Phân tích Thiết kế Hướng Đối tượng (PTTK)

**Mô tả:** Hệ thống quản lý cửa hàng giày toàn diện với 3 ứng dụng độc lập:

1. **Desktop App (Java Swing)** - Quản lý nội bộ cho Admin/Staff
2. **REST API (Spring Boot)** - Backend phục vụ đặt hàng online
3. **React Web App** - Giao diện khách hàng đặt hàng trực tuyến

---

## 🎯 Chức năng chính

### 🖥️ Desktop Application (Java Swing)
**Người dùng:** Admin, Staff (quản lý nội bộ)

#### Admin:
- ✅ Quản lý nhân viên (Thêm/Xóa Staff)
- ✅ Xem toàn bộ dữ liệu hệ thống

#### Staff:
- ✅ **Bán hàng tại quầy**
  - Tìm kiếm sản phẩm theo tên
  - Thêm vào giỏ hàng với size/màu/số lượng
  - Tìm kiếm khách hàng theo SĐT
  - Tạo khách hàng mới nếu chưa có
  - Thanh toán và in hóa đơn
  - Tự động cập nhật tồn kho

### 🌐 Web Application (React)
**Người dùng:** Khách hàng (đặt hàng online)

- ✅ Đăng ký/Đăng nhập tài khoản
- ✅ Xem danh sách sản phẩm
- ✅ Xem chi tiết sản phẩm + biến thể (size, màu)
- ✅ Thêm vào giỏ hàng
- ✅ Thanh toán online (COD, Banking, Credit Card)
- ✅ Xem lịch sử đơn hàng
- ✅ Quản lý thông tin cá nhân

### 🔌 REST API (Spring Boot)
**Mục đích:** Backend cho Web App + có thể mở rộng cho Mobile App

- ✅ Authentication (Register/Login)
- ✅ Product Management APIs
- ✅ Order Management APIs
- ✅ Customer Profile APIs
- ✅ CORS enabled cho React frontend
- ✅ Spring Security với public/protected endpoints

---

## ⚙️ Công nghệ sử dụng

### Backend Stack
| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| **Java** | JDK 25 | Ngôn ngữ lập trình |
| **Spring Boot** | 2.7.18 | REST API Framework |
| **PostgreSQL** | 18 | Database |
| **Maven** | 3.9.9 | Build tool |
| **JDBC** | 42.7.8 | Database connector |

### Frontend Stack
| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| **React** | 19.2.0 | UI Library |
| **React Router** | 7.9.5 | Routing |
| **Axios** | 1.13.2 | HTTP Client |
| **Node.js** | v24.11.0 | Runtime |
| **npm** | 11.6.1 | Package manager |

---

## 🏗️ Kiến trúc hệ thống

```
┌──────────────────────────────────────────────────────────────────┐
│                         USERS                                     │
├────────────────┬─────────────────────┬───────────────────────────┤
│   Admin/Staff  │     Customer        │      Customer             │
│   (Desktop)    │   (Web Browser)     │   (Mobile - Future)       │
└────────┬───────┴──────────┬──────────┴───────────┬───────────────┘
         │                  │                      │
         │                  │                      │
┌────────▼──────────┐  ┌───▼──────────────────┐  │
│  Desktop App      │  │  React Frontend      │  │
│  (Java Swing)     │  │  (customer-web/)     │  │
│  - LoginFrame     │  │  - Register/Login    │  │
│  - MainFrame      │  │  - Products          │  │
│  - SalePanel      │  │  - Cart/Checkout     │  │
│  - UserMgmt Panel │  │  - Order History     │  │
└────────┬──────────┘  └───┬──────────────────┘  │
         │                  │                      │
         │                  │                      │
         │             ┌────▼──────────────────────▼────┐
         │             │   REST API (Spring Boot)       │
         │             │   (api/)                       │
         │             │   - /auth/** (public)          │
         │             │   - /products/** (public)      │
         │             │   - /orders/** (authenticated) │
         │             └────┬───────────────────────────┘
         │                  │
         │                  │
┌────────▼──────────────────▼────────────────────────┐
│           PostgreSQL Database                      │
│           (shoe_store_management)                  │
│                                                    │
│  Tables:                                           │
│  - user (Admin, Staff)                            │
│  - customer (online customers)                    │
│  - product                                        │
│  - product_variant (SKU với size, color, stock)  │
│  - order                                          │
│  - order_detail                                   │
└───────────────────────────────────────────────────┘
```

---

## 📁 Cấu trúc thư mục

```
PTTK_Backup_Desktop/
│
├── 📂 src/                          # Desktop App (Java Swing)
│   ├── Application.java             # Main entry point
│   ├── model/                       # 6 Entity classes
│   ├── dataaccess/                  # DAO Pattern
│   │   ├── *DAO.java               # DAO interfaces
│   │   └── impl/
│   │       ├── PgConnection.java    # DB Connection Singleton
│   │       └── *DAOImpl.java       # DAO implementations
│   ├── service/                     # Business Logic
│   │   ├── UserService.java
│   │   ├── ProductService.java
│   │   ├── SaleService.java
│   │   └── CustomerService.java
│   ├── ui/                          # Swing UI
│   │   ├── LoginFrame.java
│   │   ├── MainFrame.java
│   │   └── panel/
│   │       ├── SalePanel.java       # ✅ Hoàn thiện
│   │       └── UserManagementPanel.java
│   └── util/
│
├── 📂 api/                          # REST API (Spring Boot)
│   ├── pom.xml                      # Maven dependencies
│   ├── src/main/
│   │   ├── java/com/pttk/api/
│   │   │   ├── ApiApplication.java  # Spring Boot main
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java  # CORS + Security
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java      # /auth/**
│   │   │   │   ├── ProductController.java   # /products/**
│   │   │   │   └── OrderController.java     # /orders/**
│   │   │   └── dto/
│   │   │       ├── ApiResponse.java
│   │   │       ├── LoginRequest.java
│   │   │       ├── RegisterRequest.java
│   │   │       └── CreateOrderRequest.java
│   │   └── resources/
│   │       └── application.properties   # DB config
│   └── target/
│       └── shoe-store-api-1.0.0.jar
│
├── 📂 customer-web/                 # React Frontend
│   ├── package.json
│   ├── public/
│   └── src/
│       ├── App.js                   # Main routing
│       ├── components/
│       │   └── Navbar.jsx           # Navigation bar
│       ├── context/
│       │   ├── AuthContext.js       # Auth state management
│       │   └── CartContext.js       # Cart state management
│       ├── pages/                   # 7 pages
│       │   ├── Register.jsx         # Đăng ký
│       │   ├── Login.jsx            # Đăng nhập
│       │   ├── ProductList.jsx      # Danh sách sản phẩm
│       │   ├── ProductDetail.jsx    # Chi tiết sản phẩm
│       │   ├── Cart.jsx             # Giỏ hàng
│       │   ├── Checkout.jsx         # Thanh toán
│       │   ├── OrderHistory.jsx     # Lịch sử đơn hàng
│       │   └── Profile.jsx          # Thông tin cá nhân
│       └── services/
│           └── api.js               # Axios HTTP client
│
├── 📂 sql/
│   ├── init_schema.sql              # Database schema
│   └── add_missing_columns.sql      # Migration
│
├── 📂 lib/                          # External JARs
│   └── postgresql-42.7.8.jar
│
├── 📂 build/                        # Compiled Java classes
├── PTTK.jar                         # Desktop app executable
├── build.bat                        # Build desktop app
├── run.bat                          # Run desktop app
├── README.md                        # Desktop app docs
└── PROJECT_README.md                # ← This file (Overview)
```

---

## 🚀 Hướng dẫn cài đặt nhanh

### Yêu cầu hệ thống

- **Java JDK 11+** (khuyên dùng JDK 25)
- **Maven 3.9+**
- **Node.js 18+** và npm
- **PostgreSQL 12+**

### Các bước cài đặt

#### 1️⃣ Cài đặt Database

```bash
# Tạo database
createdb -U postgres shoe_store_management

# Import schema
psql -U postgres -d shoe_store_management -f sql/init_schema.sql

# Thêm các columns cho online ordering
psql -U postgres -d shoe_store_management -f sql/add_missing_columns.sql
```

#### 2️⃣ Cấu hình Database Password

**Desktop App:**
Sửa `src/dataaccess/impl/PgConnection.java`:
```java
private static final String PASS = "your_password";  // ← Đổi password
```

**REST API:**
Sửa `api/src/main/resources/application.properties`:
```properties
spring.datasource.password=your_password  # ← Đổi password
```

#### 3️⃣ Chạy Desktop App

```bash
# Cách 1: Dùng script
.\build.bat
.\run.bat

# Cách 2: Chạy JAR
java -jar PTTK.jar
```

**Tài khoản đăng nhập:**
- Admin: `admin` / `123`
- Staff: `staff1` / `456`

#### 4️⃣ Chạy REST API

```bash
cd api
mvn spring-boot:run
```

API chạy tại: **http://localhost:8080/api**

#### 5️⃣ Chạy React Frontend

```bash
cd customer-web
npm install      # Lần đầu tiên
npm start
```

Web app chạy tại: **http://localhost:3000**

---

## 🧪 Test Cases

### Desktop App - Chức năng bán hàng

#### TC1: Tìm kiếm sản phẩm
1. Đăng nhập với `staff1` / `456`
2. Click tab "Bán Hàng"
3. Nhập "Runner" vào ô tìm kiếm
4. Click "Tìm kiếm"
5. **Kết quả:** Hiển thị "Giày Runner X" với các variant

#### TC2: Thêm vào giỏ hàng
1. Chọn 1 sản phẩm từ danh sách
2. Chọn số lượng (VD: 2)
3. Click "Thêm vào giỏ"
4. **Kết quả:** Sản phẩm xuất hiện trong giỏ hàng bên dưới

#### TC3: Tìm kiếm khách hàng
1. Nhập SĐT: `0123456789`
2. Click "Tìm khách hàng"
3. **Kết quả:** Hiển thị "Nguyen Viet Hung"

#### TC4: Tạo khách hàng mới
1. Nhập SĐT mới: `0999999999`
2. Click "Tìm khách hàng" → Không tìm thấy
3. Nhập tên: "Test Customer"
4. Click "Tạo khách hàng"
5. **Kết quả:** Khách hàng được tạo thành công

#### TC5: Thanh toán
1. Có sản phẩm trong giỏ và khách hàng
2. Click "Thanh toán"
3. **Kết quả:** 
   - Hiển thị thông báo thành công
   - Giỏ hàng được làm mới
   - Tồn kho giảm

### Web App - Đặt hàng online

#### TC6: Đăng ký tài khoản
1. Mở http://localhost:3000
2. Click "Đăng ký"
3. Nhập thông tin:
   - Tên: "Nguyen Test"
   - SĐT: "0988888888"
   - Username: "test123"
   - Password: "123456"
   - Địa chỉ: "123 Test Street"
4. Click "Đăng ký"
5. **Kết quả:** Đăng ký thành công, chuyển về trang chủ

#### TC7: Đăng nhập
1. Click "Đăng nhập"
2. Nhập:
   - Username: "hung"
   - Password: "123"
3. Click "Đăng nhập"
4. **Kết quả:** 
   - Hiển thị "Xin chào, Nguyen Viet Hung"
   - Menu có "Giỏ hàng", "Đơn hàng"

#### TC8: Xem sản phẩm
1. Click "Sản phẩm" trên menu
2. **Kết quả:** Hiển thị 3 sản phẩm (Runner X, Da Classic, Thể Thao Flex)

#### TC9: Xem chi tiết và thêm vào giỏ
1. Click vào "Giày Runner X"
2. Chọn size: 40
3. Chọn màu: Đen
4. Chọn số lượng: 2
5. Click "Thêm vào giỏ hàng"
6. **Kết quả:** Badge giỏ hàng hiện số 2

#### TC10: Thanh toán online
1. Click "Giỏ hàng"
2. Kiểm tra sản phẩm
3. Click "Thanh toán"
4. Nhập địa chỉ giao hàng
5. Chọn phương thức: "COD"
6. Click "Đặt hàng"
7. **Kết quả:** 
   - Thông báo "Đặt hàng thành công"
   - Chuyển đến trang "Đơn hàng"

#### TC11: Xem lịch sử đơn hàng
1. Click "Đơn hàng" trên menu
2. **Kết quả:** Hiển thị danh sách đơn hàng đã đặt

#### TC12: Xem chi tiết đơn hàng
1. Từ danh sách đơn hàng, click "Xem chi tiết"
2. **Kết quả:** Hiển thị đầy đủ thông tin:
   - Sản phẩm + size/màu/số lượng
   - Địa chỉ giao hàng
   - Phương thức thanh toán
   - Tổng tiền

### REST API - Endpoints

#### TC13: Test đăng ký API
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "API Test User",
    "phone": "0911111111",
    "username": "apitest",
    "password": "123",
    "address": "Test Address"
  }'
```
**Kết quả:** Response `{"success": true, "message": "Đăng ký thành công"}`

#### TC14: Test login API
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "hung", "password": "123"}'
```
**Kết quả:** Response với token và thông tin customer

#### TC15: Test lấy sản phẩm
```bash
curl http://localhost:8080/api/products
```
**Kết quả:** JSON array với 3 sản phẩm

#### TC16: Test đặt hàng API
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer fake-jwt-token-1" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [{"variantId": 1, "quantity": 1, "price": 1500000}],
    "shippingAddress": "Test Address",
    "paymentMethod": "cod"
  }'
```
**Kết quả:** Order được tạo thành công với order_id

---

## 📊 Database Schema

### Các bảng chính

```sql
-- Nhân viên quản lý (Desktop App)
user (user_id, username, password, role)

-- Khách hàng (Web/Mobile)
customer (customer_id, name, phone_number, username, password, address, join_date)

-- Sản phẩm
product (product_id, name, description, base_price)

-- Biến thể sản phẩm (SKU)
product_variant (variant_id, product_id, sku_code, size, color, quantity_in_stock, price)

-- Đơn hàng
"order" (order_id, order_date, staff_id, customer_id, total_amount, status, shipping_address, payment_method)

-- Chi tiết đơn hàng
order_detail (detail_id, order_id, variant_id, quantity_sold, unit_price)
```

### Dữ liệu mẫu

- **2 users:** admin, staff1
- **3 customers:** hung, tuanan, giahung
- **3 products:** Giày Runner X, Giày Da Classic, Giày Thể Thao Flex
- **7 product variants:** Các size/màu khác nhau
- **Orders:** Tạo khi bán hàng hoặc đặt online

---

## 🔐 Bảo mật & TODO

### ⚠️ Lưu ý bảo mật hiện tại
- Mật khẩu lưu plain text (TODO: BCrypt)
- JWT token đang dùng fake token (TODO: Real JWT)
- Database password hard-coded (TODO: Environment variables)

### 🚧 Các tính năng cần phát triển
- [ ] Implement real JWT authentication
- [ ] Hash passwords với BCrypt
- [ ] Add image upload cho products
- [ ] Add search/filter trong Web App
- [ ] Add admin panel trên Web
- [ ] Integrate payment gateway (Momo, ZaloPay)
- [ ] Email notifications
- [ ] Order tracking
- [ ] Reviews & ratings
- [ ] Mobile app (React Native)

---

## 📞 Hỗ trợ

**Giảng viên/Người chấm:** Vui lòng xem file [HUONG_DAN_GIANG_VIEN.md](HUONG_DAN_GIANG_VIEN.md) để có hướng dẫn chi tiết chạy và test đồ án.

---

## 👨‍💻 Thông tin đồ án

**Môn học:** Phân tích Thiết kế Hướng Đối tượng

**Học kỳ:** 2024-2025

**Ngày hoàn thành:** 05/11/2025

---

**🎉 Cảm ơn đã xem xét đồ án!**

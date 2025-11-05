package com.pttk.api.controller;

import com.pttk.api.dto.ApiResponse;
import com.pttk.api.dto.LoginRequest;
import com.pttk.api.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/shoe_store_management";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "asdf0123";

    /**
     * Đăng ký khách hàng mới
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            
            // Kiểm tra username đã tồn tại chưa
            String checkSql = "SELECT customer_id FROM customer WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, request.getUsername());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    return ResponseEntity.status(400)
                            .body(ApiResponse.error("Tên đăng nhập đã tồn tại"));
                }
            }
            
            // Kiểm tra số điện thoại đã tồn tại chưa
            checkSql = "SELECT customer_id FROM customer WHERE phone_number = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, request.getPhone());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    return ResponseEntity.status(400)
                            .body(ApiResponse.error("Số điện thoại đã được sử dụng"));
                }
            }
            
            // Tạo khách hàng mới
            String insertSql = "INSERT INTO customer (name, phone_number, username, password, address, join_date) " +
                              "VALUES (?, ?, ?, ?, ?, CURRENT_DATE) RETURNING customer_id";
            
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, request.getName());
                stmt.setString(2, request.getPhone());
                stmt.setString(3, request.getUsername());
                stmt.setString(4, request.getPassword()); // TODO: Hash password với BCrypt
                stmt.setString(5, request.getAddress());
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("customer_id", rs.getInt("customer_id"));
                    result.put("username", request.getUsername());
                    result.put("name", request.getName());
                    
                    return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công", result));
                }
            }
            
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Lỗi khi tạo tài khoản"));
            
        } catch (SQLException e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Lỗi khi đăng ký: " + e.getMessage()));
        }
    }

    /**
     * Đăng nhập khách hàng
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            
            String sql = "SELECT * FROM customer WHERE username = ? AND password = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, request.getUsername());
                stmt.setString(2, request.getPassword()); // TODO: Verify hashed password
                
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    Map<String, Object> customer = new HashMap<>();
                    customer.put("customer_id", rs.getInt("customer_id"));
                    customer.put("username", rs.getString("username"));
                    customer.put("name", rs.getString("name"));
                    customer.put("phone", rs.getString("phone_number"));
                    customer.put("address", rs.getString("address"));
                    
                    // TODO: Generate JWT token
                    String token = "fake-jwt-token-" + rs.getInt("customer_id");
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("token", token);
                    result.put("customer", customer);
                    
                    return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", result));
                } else {
                    return ResponseEntity.status(401)
                            .body(ApiResponse.error("Tên đăng nhập hoặc mật khẩu không đúng"));
                }
            }
            
        } catch (SQLException e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Lỗi khi đăng nhập: " + e.getMessage()));
        }
    }

    /**
     * Lấy thông tin profile khách hàng hiện tại
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentCustomer(@RequestHeader("Authorization") String token) {
        // TODO: Parse JWT token to get customer_id
        // For now, extract from fake token
        try {
            int customerId = Integer.parseInt(token.replace("Bearer fake-jwt-token-", ""));
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql = "SELECT customer_id, username, name, phone_number, address, join_date FROM customer WHERE customer_id = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, customerId);
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        Map<String, Object> customer = new HashMap<>();
                        customer.put("customer_id", rs.getInt("customer_id"));
                        customer.put("username", rs.getString("username"));
                        customer.put("name", rs.getString("name"));
                        customer.put("phone", rs.getString("phone_number"));
                        customer.put("address", rs.getString("address"));
                        customer.put("join_date", rs.getDate("join_date"));
                        
                        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin thành công", customer));
                    }
                }
            }
            
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Không tìm thấy khách hàng"));
            
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Token không hợp lệ"));
        }
    }
}

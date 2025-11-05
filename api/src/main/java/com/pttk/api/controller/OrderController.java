package com.pttk.api.controller;

import com.pttk.api.dto.ApiResponse;
import com.pttk.api.dto.CreateOrderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/shoe_store_management";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "asdf0123";

    /**
     * Tạo đơn hàng mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateOrderRequest request) {
        
        try {
            int customerId = extractCustomerId(token);
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                conn.setAutoCommit(false);
                
                try {
                    // Tính tổng tiền
                    BigDecimal total = BigDecimal.ZERO;
                    for (CreateOrderRequest.OrderItem item : request.getItems()) {
                        BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                        total = total.add(itemTotal);
                    }
                    
                    // Tạo đơn hàng (staff_id = 1 là staff mặc định cho online order)
                    String orderSql = "INSERT INTO \"order\" (staff_id, customer_id, order_date, total_amount, status, shipping_address, payment_method) " +
                                     "VALUES (1, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?) RETURNING order_id";
                    
                    int orderId;
                    try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
                        stmt.setInt(1, customerId);
                        stmt.setBigDecimal(2, total);
                        stmt.setString(3, "pending"); // status
                        stmt.setString(4, request.getShippingAddress());
                        stmt.setString(5, request.getPaymentMethod());
                        
                        ResultSet rs = stmt.executeQuery();
                        rs.next();
                        orderId = rs.getInt("order_id");
                    }
                    
                    // Thêm chi tiết đơn hàng và cập nhật tồn kho
                    String detailSql = "INSERT INTO order_detail (order_id, variant_id, quantity_sold, unit_price) VALUES (?, ?, ?, ?)";
                    String updateStockSql = "UPDATE product_variant SET quantity_in_stock = quantity_in_stock - ? WHERE variant_id = ?";
                    
                    try (PreparedStatement detailStmt = conn.prepareStatement(detailSql);
                         PreparedStatement stockStmt = conn.prepareStatement(updateStockSql)) {
                        
                        for (CreateOrderRequest.OrderItem item : request.getItems()) {
                            // Kiểm tra tồn kho
                            String checkStockSql = "SELECT quantity_in_stock FROM product_variant WHERE variant_id = ?";
                            try (PreparedStatement checkStmt = conn.prepareStatement(checkStockSql)) {
                                checkStmt.setInt(1, item.getVariantId());
                                ResultSet rs = checkStmt.executeQuery();
                                if (rs.next()) {
                                    int stock = rs.getInt("quantity_in_stock");
                                    if (stock < item.getQuantity()) {
                                        conn.rollback();
                                        return ResponseEntity.status(400)
                                                .body(ApiResponse.error("Sản phẩm ID " + item.getVariantId() + " không đủ hàng"));
                                    }
                                }
                            }
                            
                            // Thêm chi tiết đơn
                            detailStmt.setInt(1, orderId);
                            detailStmt.setInt(2, item.getVariantId());
                            detailStmt.setInt(3, item.getQuantity());
                            detailStmt.setBigDecimal(4, item.getPrice());
                            detailStmt.addBatch();
                            
                            // Cập nhật tồn kho
                            stockStmt.setInt(1, item.getQuantity());
                            stockStmt.setInt(2, item.getVariantId());
                            stockStmt.addBatch();
                        }
                        
                        detailStmt.executeBatch();
                        stockStmt.executeBatch();
                    }
                    
                    conn.commit();
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("order_id", orderId);
                    result.put("total_amount", total);
                    result.put("status", "pending");
                    
                    return ResponseEntity.ok(ApiResponse.success("Đặt hàng thành công", result));
                    
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Lỗi khi tạo đơn hàng: " + e.getMessage()));
        }
    }

    /**
     * Lấy lịch sử đơn hàng của khách hàng
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getCustomerOrders(@RequestHeader("Authorization") String token) {
        try {
            int customerId = extractCustomerId(token);
            List<Map<String, Object>> orders = new ArrayList<>();
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql = "SELECT o.order_id, o.order_date, o.total_amount, o.status, " +
                           "COUNT(od.detail_id) as item_count " +
                           "FROM \"order\" o " +
                           "LEFT JOIN order_detail od ON o.order_id = od.order_id " +
                           "WHERE o.customer_id = ? " +
                           "GROUP BY o.order_id " +
                           "ORDER BY o.order_date DESC";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, customerId);
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        Map<String, Object> order = new HashMap<>();
                        order.put("order_id", rs.getInt("order_id"));
                        order.put("order_date", rs.getTimestamp("order_date"));
                        order.put("total_amount", rs.getBigDecimal("total_amount"));
                        order.put("status", rs.getString("status"));
                        order.put("item_count", rs.getInt("item_count"));
                        orders.add(order);
                    }
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử đơn hàng thành công", orders));
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Lỗi khi lấy lịch sử đơn hàng: " + e.getMessage()));
        }
    }

    /**
     * Lấy chi tiết đơn hàng
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable int orderId) {
        
        try {
            int customerId = extractCustomerId(token);
            
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                // Kiểm tra quyền truy cập
                String checkSql = "SELECT customer_id FROM \"order\" WHERE order_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                    stmt.setInt(1, orderId);
                    ResultSet rs = stmt.executeQuery();
                    if (!rs.next() || rs.getInt("customer_id") != customerId) {
                        return ResponseEntity.status(403)
                                .body(ApiResponse.error("Không có quyền truy cập đơn hàng này"));
                    }
                }
                
                // Lấy thông tin đơn hàng
                String orderSql = "SELECT * FROM \"order\" WHERE order_id = ?";
                Map<String, Object> order = new HashMap<>();
                
                try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
                    stmt.setInt(1, orderId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        order.put("order_id", rs.getInt("order_id"));
                        order.put("order_date", rs.getTimestamp("order_date"));
                        order.put("total_amount", rs.getBigDecimal("total_amount"));
                        order.put("status", rs.getString("status"));
                        order.put("shipping_address", rs.getString("shipping_address"));
                        order.put("payment_method", rs.getString("payment_method"));
                    }
                }
                
                // Lấy chi tiết sản phẩm
                String detailSql = "SELECT od.*, pv.sku_code, pv.size, pv.color, p.name as product_name " +
                                  "FROM order_detail od " +
                                  "JOIN product_variant pv ON od.variant_id = pv.variant_id " +
                                  "JOIN product p ON pv.product_id = p.product_id " +
                                  "WHERE od.order_id = ?";
                
                List<Map<String, Object>> items = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement(detailSql)) {
                    stmt.setInt(1, orderId);
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("product_name", rs.getString("product_name"));
                        item.put("sku_code", rs.getString("sku_code"));
                        item.put("size", rs.getString("size"));
                        item.put("color", rs.getString("color"));
                        item.put("quantity", rs.getInt("quantity_sold"));
                        item.put("unit_price", rs.getBigDecimal("unit_price"));
                        items.add(item);
                    }
                }
                
                order.put("items", items);
                
                return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết đơn hàng thành công", order));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage()));
        }
    }

    private int extractCustomerId(String token) {
        // TODO: Parse JWT token
        return Integer.parseInt(token.replace("Bearer fake-jwt-token-", ""));
    }
}

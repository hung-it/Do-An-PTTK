package com.pttk.api.controller;

import com.pttk.api.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/shoe_store_management";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "asdf0123";

    /**
     * Lấy danh sách tất cả sản phẩm
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllProducts() {
        List<Map<String, Object>> products = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT p.product_id, p.name, p.description, p.base_price, p.image_url, " +
                        "COUNT(pv.variant_id) as variant_count, " +
                        "MIN(pv.price) as min_price, " +
                        "MAX(pv.price) as max_price " +
                        "FROM product p " +
                        "LEFT JOIN product_variant pv ON p.product_id = pv.product_id " +
                        "GROUP BY p.product_id, p.name, p.description, p.base_price, p.image_url " +
                        "ORDER BY p.product_id";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("product_id", rs.getInt("product_id"));
                    product.put("name", rs.getString("name"));
                    product.put("description", rs.getString("description"));
                    product.put("base_price", rs.getBigDecimal("base_price"));
                    product.put("image_url", rs.getString("image_url")); // Thêm image_url
                    product.put("variant_count", rs.getInt("variant_count"));
                    product.put("min_price", rs.getBigDecimal("min_price"));
                    product.put("max_price", rs.getBigDecimal("max_price"));
                    products.add(product);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm thành công", products));
            
        } catch (SQLException e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách sản phẩm: " + e.getMessage()));
        }
    }

    /**
     * Lấy chi tiết sản phẩm theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable int id) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT * FROM product WHERE product_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("product_id", rs.getInt("product_id"));
                    product.put("name", rs.getString("name"));
                    product.put("description", rs.getString("description"));
                    product.put("base_price", rs.getBigDecimal("base_price"));
                    product.put("image_url", rs.getString("image_url")); // Thêm image_url
                    
                    return ResponseEntity.ok(ApiResponse.success("Lấy thông tin sản phẩm thành công", product));
                } else {
                    return ResponseEntity.status(404)
                            .body(ApiResponse.error("Không tìm thấy sản phẩm"));
                }
            }
            
        } catch (SQLException e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Lỗi khi lấy thông tin sản phẩm: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách biến thể của sản phẩm
     */
    @GetMapping("/{id}/variants")
    public ResponseEntity<ApiResponse> getProductVariants(@PathVariable int id) {
        List<Map<String, Object>> variants = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT pv.*, p.name as product_name " +
                        "FROM product_variant pv " +
                        "JOIN product p ON pv.product_id = p.product_id " +
                        "WHERE pv.product_id = ? AND pv.quantity_in_stock > 0 " +
                        "ORDER BY pv.size, pv.color";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, Object> variant = new HashMap<>();
                    variant.put("variant_id", rs.getInt("variant_id"));
                    variant.put("product_id", rs.getInt("product_id"));
                    variant.put("product_name", rs.getString("product_name"));
                    variant.put("sku_code", rs.getString("sku_code"));
                    variant.put("size", rs.getString("size"));
                    variant.put("color", rs.getString("color"));
                    variant.put("quantity_in_stock", rs.getInt("quantity_in_stock"));
                    variant.put("price", rs.getBigDecimal("price"));
                    variants.add(variant);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách biến thể thành công", variants));
            
        } catch (SQLException e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách biến thể: " + e.getMessage()));
        }
    }

    /**
     * Tìm kiếm sản phẩm theo tên
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchProducts(@RequestParam String keyword) {
        List<Map<String, Object>> products = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT p.product_id, p.name, p.description, p.base_price, p.image_url, " +
                        "COUNT(pv.variant_id) as variant_count " +
                        "FROM product p " +
                        "LEFT JOIN product_variant pv ON p.product_id = pv.product_id " +
                        "WHERE LOWER(p.name) LIKE LOWER(?) " +
                        "GROUP BY p.product_id " +
                        "ORDER BY p.name";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + keyword + "%");
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("product_id", rs.getInt("product_id"));
                    product.put("name", rs.getString("name"));
                    product.put("description", rs.getString("description"));
                    product.put("base_price", rs.getBigDecimal("base_price"));
                    product.put("image_url", rs.getString("image_url")); // Thêm image_url
                    product.put("variant_count", rs.getInt("variant_count"));
                    products.add(product);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success("Tìm kiếm thành công", products));
            
        } catch (SQLException e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Lỗi khi tìm kiếm sản phẩm: " + e.getMessage()));
        }
    }
}

package dataaccess.impl;

import dataaccess.ProductVariantDAO;
import model.Product_Variant;
import java.sql.Connection;
import java.sql.PreparedStatement;  
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class ProductVariantDAOImpl implements ProductVariantDAO{
   // phương thức cốt lỗi: cập nhật tồn kho
    @Override
    public int updateInventory(int variantId, int quantityChange, Connection conn) throws SQLException {
        String sql = "UPDATE product_variant SET quantity_in_stock = quantity_in_stock + ? WHERE variant_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantityChange);
            ps.setInt(2, variantId);
            
            return ps.executeUpdate(); // Trả về số hàng bị ảnh hưởng
        }
    }

    // Tìm kiếm SKU theo mã
    @Override
    public Product_Variant findBySkuCode(String skuCode, Connection conn) throws SQLException {
        String sql = "SELECT * FROM product_variant WHERE sku_code = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, skuCode);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Product_Variant(
                        rs.getInt("variant_id"),
                        rs.getInt("product_id"),
                        rs.getString("sku_code"),
                        rs.getString("size"),
                        rs.getString("color"),
                        rs.getInt("quantity_in_stock"),
                        rs.getBigDecimal("price")
                    );
                }
            }
        }
        return null; // Trả về null nếu không tìm thấy
    }

    // Tìm kiếm theo id
    @Override
    public Product_Variant findById(Integer id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM product_variant WHERE variant_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Product_Variant(
                        rs.getInt("variant_id"),
                        rs.getInt("product_id"),
                        rs.getString("sku_code"),
                        rs.getString("size"),
                        rs.getString("color"),
                        rs.getInt("quantity_in_stock"),
                        rs.getBigDecimal("price")
                    );
                }
            }
        }
        return null; // Trả về null nếu không tìm thấy
    }

    // Lấy tất cả các đối tượng
    @Override
    public List<Product_Variant> findAll(Connection conn) throws SQLException {
        List<Product_Variant> variants = new ArrayList<>();
        String sql = "SELECT * FROM product_variant";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Product_Variant variant = new Product_Variant(
                    rs.getInt("variant_id"),
                    rs.getInt("product_id"),
                    rs.getString("sku_code"),
                    rs.getString("size"),
                    rs.getString("color"),
                    rs.getInt("quantity_in_stock"),
                    rs.getBigDecimal("price")
                );
                variants.add(variant);
            }
        }
        return variants;
    }
    
    // Tìm kiếm tất cả các biến thể của một Product ID cụ thể
    @Override
    public List<Product_Variant> findByProductId(int productId, Connection conn) throws SQLException {
        List<Product_Variant> variants = new ArrayList<>();
        String sql = "SELECT * FROM product_variant WHERE product_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product_Variant variant = new Product_Variant(
                        rs.getInt("variant_id"),
                        rs.getInt("product_id"),
                        rs.getString("sku_code"),
                        rs.getString("size"),
                        rs.getString("color"),
                        rs.getInt("quantity_in_stock"),
                        rs.getBigDecimal("price")
                    );
                    variants.add(variant);
                }
            }
        }
        return variants;
    }

    // Lưu trữ một đối tượng mới (Tạo)
    @Override
    public Integer save(Product_Variant entity, Connection conn) throws SQLException {
        String sql = "INSERT INTO product_variant (product_id, sku_code, size, color, price, quantity_in_stock) VALUES (?,?,?,?,?,?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, entity.getProductId());
            ps.setString(2, entity.getSkuCode());
            ps.setString(3, entity.getSize());
            ps.setString(4, entity.getColor());
            ps.setBigDecimal(5, entity.getPrice());
            ps.setInt(6, entity.getQuantityInStock());
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // Trả về khóa chính (variant_id)
                }
            }
        }
        return -1; // Lỗi khi không lấy được ID
    }

    // Cập nhật một đối tượng đã tồn tại
    @Override
    public boolean update(Product_Variant entity, Connection conn) throws SQLException {
        String sql = "UPDATE product_variant SET product_id = ?, sku_code = ?, size = ?, color = ?, price = ?, quantity_in_stock = ? WHERE variant_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, entity.getProductId());
            ps.setString(2, entity.getSkuCode());
            ps.setString(3, entity.getSize());
            ps.setString(4, entity.getColor());
            ps.setBigDecimal(5, entity.getPrice());
            ps.setInt(6, entity.getQuantityInStock());
            ps.setInt(7, entity.getVariantId());
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0; // Trả về true nếu cập nhật thành công
        }
    }

    // Xóa một đối tượng theo ID
    @Override
    public boolean delete(Integer id, Connection conn) throws SQLException {
        String sql = "DELETE FROM product_variant WHERE variant_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0; // Trả về true nếu xóa thành công
        }
    }
}

   
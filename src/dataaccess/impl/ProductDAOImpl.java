package dataaccess.impl;

import dataaccess.ProductDAO;
import model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {
    
    // --- Tìm sản phẩm theo tên ---
    @Override
    public List<Product> findByName(String name, Connection conn) throws SQLException {
        List<Product> products = new ArrayList<>();
        // Sử dụng ILIKE để tìm kiếm không phân biệt hoa thường trong PostgreSQL
        String sql = "SELECT * FROM product WHERE name ILIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }
    
    @Override
    public Product findById(Integer id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM product WHERE product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Product> findAll(Connection conn) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM product ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    @Override
    public Integer save(Product entity, Connection conn) throws SQLException {
        String sql = "INSERT INTO product (name, description, base_price) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getDescription());
            ps.setBigDecimal(3, entity.getBasePrice()); // Sử dụng setBigDecimal
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    @Override
    public boolean update(Product entity, Connection conn) throws SQLException {
        String sql = "UPDATE product SET name = ?, description = ?, base_price = ? WHERE product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getDescription());
            ps.setBigDecimal(3, entity.getBasePrice()); // Sử dụng setBigDecimal
            ps.setInt(4, entity.getProductId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id, Connection conn) throws SQLException {
        String sql = "DELETE FROM product WHERE product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Hàm tiện ích: Ánh xạ dữ liệu từ ResultSet sang đối tượng Product
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("product_id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getBigDecimal("base_price") // Sử dụng getBigDecimal
        );
    }
}
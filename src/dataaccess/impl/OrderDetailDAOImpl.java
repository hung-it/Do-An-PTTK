package dataaccess.impl;

import dataaccess.OrderDetailDAO;
import model.Order_Detail;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailDAOImpl implements OrderDetailDAO {
    
    // --- Phương thức cốt lõi: Save (Ghi nhận Chi tiết Hóa đơn) ---
    @Override
    public Integer save(Order_Detail detail, Connection conn) throws SQLException {
        // Ghi lại chi tiết đơn hàng (variant_id là ID của SKU)
        String sql = "INSERT INTO order_detail (order_id, variant_id, quantity_sold, unit_price) VALUES (?,?,?,?)";
        
        // Cần lấy ID tự tăng (detail_id) sau khi insert
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, detail.getOrderId());
            ps.setInt(2, detail.getVariantId()); 
            ps.setInt(3, detail.getQuantitySold());
            ps.setBigDecimal(4, detail.getUnitPrice()); // Sử dụng setBigDecimal
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // Trả về detail_id
                }
            }
        }
        return -1;
    }
    
    // --- Phương thức bắt buộc: Tìm theo ID ---
    @Override
    public Order_Detail findById(Integer detailId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM order_detail WHERE detail_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detailId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrderDetail(rs);
                }
            }
        }
        return null;
    }
    
    // --- Phương thức bắt buộc: Tìm tất cả ---
    @Override
    public List<Order_Detail> findAll(Connection conn) throws SQLException {
        List<Order_Detail> details = new ArrayList<>();
        String sql = "SELECT * FROM order_detail";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                details.add(mapResultSetToOrderDetail(rs));
            }
        }
        return details;
    }
    
    // --- Phương thức bắt buộc: Cập nhật ---
    @Override
    public boolean update(Order_Detail detail, Connection conn) throws SQLException {
        String sql = "UPDATE order_detail SET order_id = ?, variant_id = ?, quantity_sold = ?, unit_price = ? WHERE detail_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detail.getOrderId());
            ps.setInt(2, detail.getVariantId());
            ps.setInt(3, detail.getQuantitySold());
            ps.setBigDecimal(4, detail.getUnitPrice()); // Sử dụng setBigDecimal
            ps.setInt(5, detail.getDetailId());
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    // --- Phương thức bắt buộc: Xóa theo ID ---
    @Override
    public boolean delete(Integer detailId, Connection conn) throws SQLException {
        String sql = "DELETE FROM order_detail WHERE detail_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detailId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    // --- Phương thức bắt buộc: Tìm theo Order ID ---
    @Override
    public List<Order_Detail> findByOrderId(int orderId, Connection conn) throws SQLException {
        List<Order_Detail> details = new ArrayList<>();
        // Lưu ý: Cần JOIN với bảng ProductVariant nếu muốn lấy tên/size/color
        String sql = "SELECT * FROM order_detail WHERE order_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    details.add(mapResultSetToOrderDetail(rs));
                }
            }
        }
        return details;
    }
    
    // --- Phương thức bắt buộc: Tìm theo Variant ID ---
    @Override
    public List<Order_Detail> findByVarientId(int varientId, Connection conn) throws SQLException {
        List<Order_Detail> details = new ArrayList<>();
        String sql = "SELECT * FROM order_detail WHERE variant_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, varientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    details.add(mapResultSetToOrderDetail(rs));
                }
            }
        }
        return details;
    }

    // --- Hàm tiện ích: Mapping dữ liệu ---
    private Order_Detail mapResultSetToOrderDetail(ResultSet rs) throws SQLException {
        return new Order_Detail(
            rs.getInt("detail_id"),
            rs.getInt("order_id"),
            rs.getInt("variant_id"),
            rs.getInt("quantity_sold"),
            rs.getBigDecimal("unit_price") // Sử dụng getBigDecimal
        );
    }
}
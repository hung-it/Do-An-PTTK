package dataaccess.impl;

import dataaccess.OrderDAO;
import model.Order;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDAOImpl implements OrderDAO {
    
    // --- Phương thức cốt lõi: Save (Tạo Hóa đơn mới) ---
    @Override
    public Integer save(Order order, Connection conn) throws SQLException {
        String sql = "INSERT INTO \"order\" (staff_id, customer_id, total_amount) VALUES (?,?,?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            // Lệnh SQL không cần order_date vì nó có giá trị DEFAULT là CURRENT_TIMESTAMP
            ps.setInt(1, order.getUserId()); // Sử dụng getUserId() thay vì getStaffId()
            // Sử dụng setInt, nếu customer_id là null/0 (khách vãng lai), cần xử lý để không vi phạm FK
            if (order.getCustomerId() == 0) {
                 ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                 ps.setInt(2, order.getCustomerId());
            }
            ps.setBigDecimal(3, order.getTotalAmount()); // Sử dụng setBigDecimal
            
            ps.executeUpdate();
            
            // Lấy order_id vừa được PostgreSQL tạo ra
            try (ResultSet rs = ps.getGeneratedKeys()) { 
                if (rs.next()) {
                    return rs.getInt(1); // Trả về khóa chính (order_id)
                }
            }
        }
        return -1;
    }

    // --- Phương thức bắt buộc: Tìm Hóa đơn theo ID ---
    @Override
    public Order findById(Integer orderId, Connection conn) throws SQLException {
        String sql = "SELECT order_id, staff_id, customer_id, order_date, total_amount FROM \"order\" WHERE order_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setUserId(rs.getInt("staff_id")); // Sử dụng setUserId()
                    order.setCustomerId(rs.getInt("customer_id"));
                    // Chuyển Timestamp thành String
                    order.setOrderDate(rs.getTimestamp("order_date").toString());
                    order.setTotalAmount(rs.getBigDecimal("total_amount")); // Sử dụng getBigDecimal
                    return order;
                }
            }
        }
        return null; // Không tìm thấy
    }

    // --- Phương thức bắt buộc: Lấy tất cả Hóa đơn ---
    @Override
    public List<Order> findAll(Connection conn) throws SQLException {
        String sql = "SELECT order_id, staff_id, customer_id, order_date, total_amount FROM \"order\" ORDER BY order_date DESC";
        List<Order> orders = new ArrayList<>();
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setUserId(rs.getInt("staff_id"));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setOrderDate(rs.getTimestamp("order_date").toString());
                order.setTotalAmount(rs.getBigDecimal("total_amount")); // Sử dụng getBigDecimal
                orders.add(order);
            }
        }
        return orders;
    }

    // --- Phương thức bắt buộc: Cập nhật Hóa đơn ---
    @Override
    public boolean update(Order order, Connection conn) throws SQLException {
        String sql = "UPDATE \"order\" SET staff_id = ?, customer_id = ?, total_amount = ? WHERE order_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, order.getUserId());
            if (order.getCustomerId() == 0) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, order.getCustomerId());
            }
            ps.setBigDecimal(3, order.getTotalAmount()); // Sử dụng setBigDecimal
            ps.setInt(4, order.getOrderId());
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // --- Phương thức bắt buộc: Xóa Hóa đơn (theo Integer) ---
    @Override
    public boolean delete(Integer orderId, Connection conn) throws SQLException {
        String sql = "DELETE FROM \"order\" WHERE order_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0; // Trả về true nếu có bản ghi bị xóa
        }
    }

    // --- Phương thức bắt buộc: Tìm Hóa đơn theo Customer ID ---
    @Override
    public List<Order> findByCustomerId(int customerId, Connection conn) throws SQLException {
        String sql = "SELECT order_id, staff_id, customer_id, order_date, total_amount FROM \"order\" WHERE customer_id = ? ORDER BY order_date DESC";
        List<Order> orders = new ArrayList<>();
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setUserId(rs.getInt("staff_id"));
                    order.setCustomerId(rs.getInt("customer_id"));
                    order.setOrderDate(rs.getTimestamp("order_date").toString());
                    order.setTotalAmount(rs.getBigDecimal("total_amount")); // Sử dụng getBigDecimal
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    // --- Phương thức bắt buộc: Tìm Hóa đơn theo khoảng thời gian ---
    @Override
    public List<Order> findByDateRange(java.util.Date startDate, java.util.Date endDate, Connection conn) throws SQLException {
        String sql = "SELECT order_id, staff_id, customer_id, order_date, total_amount FROM \"order\" " +
                     "WHERE order_date BETWEEN ? AND ? ORDER BY order_date DESC";
        List<Order> orders = new ArrayList<>();
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Chuyển java.util.Date thành java.sql.Timestamp
            ps.setTimestamp(1, new java.sql.Timestamp(startDate.getTime()));
            ps.setTimestamp(2, new java.sql.Timestamp(endDate.getTime()));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setUserId(rs.getInt("staff_id"));
                    order.setCustomerId(rs.getInt("customer_id"));
                    order.setOrderDate(rs.getTimestamp("order_date").toString());
                    order.setTotalAmount(rs.getBigDecimal("total_amount")); // Sử dụng getBigDecimal
                    orders.add(order);
                }
            }
        }
        return orders;
    }
}
package dataaccess.impl;

import dataaccess.CustomerDAO;
import model.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAOImpl implements CustomerDAO {

    @Override
    public Customer findByPhoneNumber(String phoneNumber, Connection conn) throws SQLException {
        String sql = "SELECT * FROM customer WHERE phone_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phoneNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Customer findByUsernameAndPassword(String username, String password, Connection conn) throws SQLException {
         String sql = "SELECT * FROM customer WHERE username = ? AND password = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Customer findById(Integer id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM customer WHERE customer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Customer> findAll(Connection conn) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customer";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        }
        return customers;
    }

    @Override
    public Integer save(Customer entity, Connection conn) throws SQLException {
        String sql = "INSERT INTO customer (name, phone_number, username, password, join_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getPhoneNumber());
            ps.setString(3, entity.getUsername());
            ps.setString(4, entity.getPassword());
            // Nếu không có joinDate, dùng thời điểm hiện tại
            if (entity.getJoinDate() != null) {
                ps.setTimestamp(5, entity.getJoinDate());
            } else {
                ps.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
            }
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
    public boolean update(Customer entity, Connection conn) throws SQLException {
         String sql = "UPDATE customer SET name = ?, phone_number = ?, username = ?, password = ?, join_date = ? WHERE customer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getPhoneNumber());
            ps.setString(3, entity.getUsername());
            ps.setString(4, entity.getPassword());
            ps.setTimestamp(5, entity.getJoinDate());
            ps.setInt(6, entity.getCustomerId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id, Connection conn) throws SQLException {
        String sql = "DELETE FROM customer WHERE customer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
    
    @Override
    public List<Customer> findByName(String name, Connection conn) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customer WHERE name ILIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs));
                }
            }
        }
        return customers;
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
         Customer customer = new Customer(
            rs.getInt("customer_id"),
            rs.getString("name"),
            rs.getString("phone_number"),
            rs.getString("username"),
            rs.getString("password")
        );
        // Đọc join_date từ database (nếu có)
        try {
            customer.setJoinDate(rs.getTimestamp("join_date"));
        } catch (SQLException e) {
            // Cột join_date chưa tồn tại
            System.err.println("⚠️ WARNING: Column 'join_date' not found! Please run migration: sql/migration_add_join_date.sql");
            customer.setJoinDate(null);
        }
        return customer;
    }
}
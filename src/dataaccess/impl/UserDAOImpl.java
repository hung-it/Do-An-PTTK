package dataaccess.impl;

import dataaccess.UserDAO;
import model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class UserDAOImpl implements UserDAO {

    // --- Phương thức cốt lõi: Xác thực Đăng nhập (Admin/Staff) ---
    @Override
    public User findByUsernameAndPassword(String username, String password, Connection conn) throws SQLException {
        // Cần truy vấn cả username và password để xác thực
        String sql = "SELECT * FROM \"user\" WHERE username =? AND password =?"; 
        
        // Sử dụng try-with-resources [1]
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Mapping dữ liệu từ ResultSet sang POJO [1]
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                    );
                }
            }
        }
        return null; // Trả về null nếu đăng nhập thất bại
    }

    // --- Triển khai FindById (Dùng cho báo cáo hoặc tra cứu Staff) ---
    @Override
    public User findById(Integer id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM \"user\" WHERE user_id =?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                    );
                }
            }
        }
        return null;
    }
    
    // --- Triển khai Save (Dùng cho Admin tạo tài khoản Staff mới) ---
    @Override
    public Integer save(User user, Connection conn) throws SQLException {
        String sql = "INSERT INTO \"user\" (username, password, role) VALUES (?,?,?)";
        // Thêm Statement.RETURN_GENERATED_KEYS để lấy user_id tự tăng của PostgreSQL
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) { 
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // Trả về khóa chính (user_id)
                }
            }
        }
        return -1; // Lỗi khi không lấy được ID
    }
    
    // --- Triển khai FindAll (Lấy danh sách tất cả User) ---
    @Override
    public List<User> findAll(Connection conn) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM \"user\"";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                );
                users.add(user);
            }
        }
        return users;
    }
    
    // --- Triển khai Update (Cập nhật thông tin User) ---
    @Override
    public boolean update(User user, Connection conn) throws SQLException {
        String sql = "UPDATE \"user\" SET username =?, password =?, role =? WHERE user_id =?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.setInt(4, user.getUserId());
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    // --- Triển khai Delete (Xóa User theo ID) ---
    @Override
    public boolean delete(Integer id, Connection conn) throws SQLException {
        String sql = "DELETE FROM \"user\" WHERE user_id =?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }
}
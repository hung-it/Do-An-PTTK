 package service;

import dataaccess.UserDAO;
import dataaccess.impl.PgConnection;
import dataaccess.impl.UserDAOImpl;
import model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Xử lý các nghiệp vụ liên quan đến người dùng (Admin, Staff).
 * 
 * LƯU Ý: Service này chỉ xử lý đăng nhập cho Admin và Staff.
 * Khách hàng (Customer) KHÔNG được đăng nhập vào ứng dụng quản lý này.
 * Customer sử dụng bảng riêng (customer) và không có quyền truy cập hệ thống quản lý.
 */
public class UserService {

    private final UserDAO userDAO = new UserDAOImpl();

    /**
     * Xác thực đăng nhập cho Admin và Staff.
     * 
     * LƯU Ý: Phương thức này CHỈ kiểm tra trong bảng 'user' (Admin/Staff).
     * KHÔNG kiểm tra bảng 'customer'. Khách hàng không được đăng nhập vào app quản lý.
     * 
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return Đối tượng User nếu thành công, null nếu thất bại.
     */
    public User login(String username, String password) {
        try (Connection conn = PgConnection.getConnection()) {
            return userDAO.findByUsernameAndPassword(username, password, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi đăng nhập: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy danh sách tất cả các nhân viên (role 'Staff').
     * @return Danh sách các User là Staff.
     */
    public List<User> getAllStaff() {
        try (Connection conn = PgConnection.getConnection()) {
            // Lọc danh sách chỉ giữ lại những user có vai trò là "Staff"
            return userDAO.findAll(conn).stream()
                    .filter(user -> "Staff".equalsIgnoreCase(user.getRole()))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách nhân viên: " + e.getMessage());
            return List.of(); // Trả về danh sách rỗng
        }
    }
    
    /**
     * Lấy thông tin user theo ID.
     * @param userId ID của user.
     * @return Đối tượng User hoặc null.
     */
    public User getUserById(int userId) {
        try (Connection conn = PgConnection.getConnection()) {
            return userDAO.findById(userId, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy thông tin user: " + e.getMessage());
            return null;
        }
    }

    /**
     * Admin thêm một tài khoản nhân viên mới.
     * @param staff Tài khoản nhân viên mới (phải có role là 'Staff').
     * @return true nếu thêm thành công.
     */
    public boolean addStaff(User staff) {
        if (!"Staff".equalsIgnoreCase(staff.getRole())) {
            System.err.println("Chỉ có thể thêm tài khoản với vai trò 'Staff'.");
            return false;
        }
        try (Connection conn = PgConnection.getConnection()) {
            Integer id = userDAO.save(staff, conn);
            return id != -1;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm nhân viên: " + e.getMessage());
            return false;
        }
    }

    /**
     * Admin cập nhật thông tin một nhân viên.
     * @param staff Thông tin nhân viên cần cập nhật.
     * @return true nếu cập nhật thành công.
     */
    public boolean updateStaff(User staff) {
        try (Connection conn = PgConnection.getConnection()) {
            return userDAO.update(staff, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật nhân viên: " + e.getMessage());
            return false;
        }
    }

    /**
     * Admin xóa một tài khoản nhân viên.
     * @param userId ID của nhân viên cần xóa.
     * @return true nếu xóa thành công.
     */
    public boolean deleteStaff(int userId) {
        try (Connection conn = PgConnection.getConnection()) {
            return userDAO.delete(userId, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa nhân viên: " + e.getMessage());
            return false;
        }
    }
}
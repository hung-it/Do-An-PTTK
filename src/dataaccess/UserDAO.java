package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;
import model.User;

public interface UserDAO extends GenericDAO<User, Integer> { 
    // Khóa chính của User là Integer (user_id)
    
    /**
     * Phương thức đặc thù: Xác thực đăng nhập.
     * @param username Tên đăng nhập.
     * @param password Mật khẩu.
     * @param conn Connection được cung cấp.
     * @return Đối tượng User nếu thành công, null nếu thất bại.
     * @throws SQLException 
     */
    User findByUsernameAndPassword(String username, String password, Connection conn) throws SQLException;

    

}
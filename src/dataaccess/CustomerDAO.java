package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import model.Customer;

public interface CustomerDAO extends GenericDAO<Customer, Integer> { 
    
    // Phương thức đặc thù cho việc tìm kiếm khách hàng khi bán hàng (qua SĐT)
    Customer findByPhoneNumber(String phoneNumber, Connection conn) throws SQLException;
    
    // Phương thức đặc thù cho đăng nhập khách hàng (nếu có)
    Customer findByUsernameAndPassword(String username, String password, Connection conn) throws SQLException;
    
    // Tìm kiếm khách hàng theo tên (dùng LIKE)
    List<Customer> findByName(String name, Connection conn) throws SQLException;
}
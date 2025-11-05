package service;

import dataaccess.CustomerDAO;
import dataaccess.OrderDAO;
import dataaccess.impl.CustomerDAOImpl;
import dataaccess.impl.OrderDAOImpl;
import dataaccess.impl.PgConnection;
import model.Customer;
import model.Order;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Xử lý các nghiệp vụ liên quan đến Khách hàng.
 */
public class CustomerService {

    private final CustomerDAO customerDAO = new CustomerDAOImpl();
    private final OrderDAO orderDAO = new OrderDAOImpl();

    /**
     * Tìm kiếm khách hàng bằng số điện thoại. Dùng khi bán hàng.
     * @param phoneNumber Số điện thoại.
     * @return Đối tượng Customer hoặc null.
     */
    public Customer findCustomerByPhone(String phoneNumber) {
        try (Connection conn = PgConnection.getConnection()) {
            return customerDAO.findByPhoneNumber(phoneNumber, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khách hàng theo SĐT: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Lấy thông tin khách hàng theo ID.
     * @param customerId ID của khách hàng.
     * @return Đối tượng Customer hoặc null.
     */
    public Customer getCustomerById(int customerId) {
        try (Connection conn = PgConnection.getConnection()) {
            return customerDAO.findById(customerId, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy thông tin khách hàng: " + e.getMessage());
            return null;
        }
    }

    /**
     * Đăng ký một khách hàng mới.
     * @param customer Thông tin khách hàng.
     * @return true nếu đăng ký thành công.
     */
    public boolean registerCustomer(Customer customer) {
        try (Connection conn = PgConnection.getConnection()) {
            Integer id = customerDAO.save(customer, conn);
            return id != -1;
        } catch (SQLException e) {
            System.err.println("Lỗi khi đăng ký khách hàng: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy lịch sử mua hàng của một khách hàng.
     * @param customerId ID của khách hàng.
     * @return Danh sách các hóa đơn.
     */
    public List<Order> getOrderHistory(int customerId) {
        try (Connection conn = PgConnection.getConnection()) {
            return orderDAO.findByCustomerId(customerId, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử mua hàng: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Lấy tất cả khách hàng.
     * @return Danh sách tất cả khách hàng.
     */
    public List<Customer> getAllCustomers() {
        try (Connection conn = PgConnection.getConnection()) {
            return customerDAO.findAll(conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách khách hàng: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Tìm kiếm khách hàng theo tên.
     * @param name Tên khách hàng (có thể là một phần).
     * @return Danh sách khách hàng phù hợp.
     */
    public List<Customer> searchCustomersByName(String name) {
        try (Connection conn = PgConnection.getConnection()) {
            return customerDAO.findByName(name, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khách hàng theo tên: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Cập nhật thông tin khách hàng.
     * @param customer Đối tượng Customer với thông tin mới.
     * @return true nếu cập nhật thành công.
     */
    public boolean updateCustomer(Customer customer) {
        try (Connection conn = PgConnection.getConnection()) {
            return customerDAO.update(customer, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật khách hàng: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Xóa khách hàng theo ID.
     * @param customerId ID của khách hàng cần xóa.
     * @return true nếu xóa thành công.
     */
    public boolean deleteCustomer(int customerId) {
        try (Connection conn = PgConnection.getConnection()) {
            return customerDAO.delete(customerId, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa khách hàng: " + e.getMessage());
            return false;
        }
    }
}
package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import model.Order;

public interface OrderDAO extends GenericDAO<Order, Integer> { 
    
    /**
     * Tìm tất cả đơn hàng của một khách hàng cụ thể.
     * @param customerId ID của khách hàng.
     * @param conn Connection được cung cấp.
     * @return Danh sách đơn hàng.
     * @throws SQLException 
     */
    List<Order> findByCustomerId(int customerId, Connection conn) throws SQLException;
    
    /**
     * Tìm tất cả đơn hàng trong một khoảng thời gian cụ thể.
     * @param startDate Ngày bắt đầu (bao gồm).
     * @param endDate Ngày kết thúc (bao gồm).
     * @param conn Connection được cung cấp.
     * @return Danh sách đơn hàng.
     * @throws SQLException 
     */
    List<Order> findByDateRange(java.util.Date startDate, java.util.Date endDate, Connection conn) throws SQLException;
}

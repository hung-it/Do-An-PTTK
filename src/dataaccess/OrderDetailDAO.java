package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import model.Order_Detail;

public interface OrderDetailDAO extends GenericDAO<Order_Detail, Integer> { 
    // Khóa chính của Order_Detail là Integer (order_detail_id)
    
    /**
     * Tìm tất cả chi tiết đơn hàng của một đơn hàng cụ thể.
     * @param orderId ID của đơn hàng.
     * @param conn Connection được cung cấp.
     * @return Danh sách chi tiết đơn hàng.
     * @throws SQLException 
     */
    List<Order_Detail> findByOrderId(int orderId, Connection conn) throws SQLException;

    /**
     * Tìm tất cả chi tiết đơn hàng của một sản phẩm cụ thể.
     * @param varientId ID của sản phẩm.
     * @param conn Connection được cung cấp.
     * @return Danh sách chi tiết đơn hàng.
     * @throws SQLException 
     */
    List<Order_Detail> findByVarientId(int varientId, Connection conn) throws SQLException;
}
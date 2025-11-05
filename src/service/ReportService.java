package service;

import dataaccess.OrderDAO;
import dataaccess.impl.OrderDAOImpl;
import dataaccess.impl.PgConnection;
import model.Order;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Cung cấp các dịch vụ để tạo báo cáo, thống kê.
 */
public class ReportService {

    private final OrderDAO orderDAO = new OrderDAOImpl();

    /**
     * Tính tổng doanh thu trong một khoảng thời gian.
     * @param startDate Ngày bắt đầu.
     * @param endDate   Ngày kết thúc.
     * @return Tổng doanh thu, hoặc BigDecimal.ZERO nếu có lỗi.
     */
    public BigDecimal getRevenueByDateRange(Date startDate, Date endDate) {
        try (Connection conn = PgConnection.getConnection()) {
            List<Order> orders = orderDAO.findByDateRange(startDate, endDate, conn);
            
            // Sử dụng Stream API để tính tổng
            return orders.stream()
                         .map(Order::getTotalAmount)
                         .reduce(BigDecimal.ZERO, BigDecimal::add);
                         
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính doanh thu: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
     /**
     * Lấy danh sách các hóa đơn trong một khoảng thời gian.
     * @param startDate Ngày bắt đầu.
     * @param endDate   Ngày kết thúc.
     * @return Danh sách các Order.
     */
    public List<Order> getOrdersByDateRange(Date startDate, Date endDate) {
        try (Connection conn = PgConnection.getConnection()) {
            return orderDAO.findByDateRange(startDate, endDate, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách hóa đơn: " + e.getMessage());
            return List.of();
        }
    }
}
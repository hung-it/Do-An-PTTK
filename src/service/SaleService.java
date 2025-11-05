package service;

import dataaccess.OrderDAO;
import dataaccess.OrderDetailDAO;
import dataaccess.ProductVariantDAO;
import dataaccess.impl.OrderDAOImpl;
import dataaccess.impl.OrderDetailDAOImpl;
import dataaccess.impl.ProductVariantDAOImpl;
import dataaccess.impl.PgConnection;
import model.Order;
import model.Order_Detail;
import model.Product_Variant;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Xử lý nghiệp vụ bán hàng, quản lý Transaction.
 */
public class SaleService {

    private final OrderDAO orderDAO = new OrderDAOImpl();
    private final OrderDetailDAO orderDetailDAO = new OrderDetailDAOImpl();
    private final ProductVariantDAO productVariantDAO = new ProductVariantDAOImpl();

    /**
     * NGHIỆP VỤ CỐT LÕI: TẠO HÓA ĐƠN MỚI
     * <p>
     * Phương thức này quản lý một transaction bao gồm:
     * 1. Lưu thông tin hóa đơn (Order).
     * 2. Lưu các chi tiết hóa đơn (Order_Detail).
     * 3. Cập nhật số lượng tồn kho cho từng sản phẩm đã bán.
     * <p>
     * Nếu bất kỳ bước nào thất bại, toàn bộ transaction sẽ được rollback.
     *
     * @param order   Thông tin chung của hóa đơn (nhân viên, khách hàng, tổng tiền).
     * @param details Danh sách các sản phẩm trong hóa đơn.
     * @return true nếu tạo hóa đơn và cập nhật kho thành công.
     */
    public boolean createOrder(Order order, List<Order_Detail> details) {
        Connection conn = null;
        try {
            conn = PgConnection.getConnection();
            // 1. BẮT ĐẦU TRANSACTION: Tắt chế độ tự động commit
            conn.setAutoCommit(false);

            // Kiểm tra tồn kho trước khi thực hiện
            for (Order_Detail detail : details) {
                Product_Variant variant = productVariantDAO.findById(detail.getVariantId(), conn);
                if (variant == null || variant.getQuantityInStock() < detail.getQuantitySold()) {
                    throw new SQLException("Không đủ hàng tồn kho cho SKU: " + detail.getVariantId());
                }
            }

            // 2. LƯU HÓA ĐƠN CHÍNH (ORDER)
            // Việc này sẽ tạo ra một order_id mới
            Integer orderId = orderDAO.save(order, conn);
            if (orderId == -1) {
                throw new SQLException("Tạo hóa đơn thất bại, không lấy được ID.");
            }

            // 3. DUYỆT QUA TỪNG CHI TIẾT ĐỂ LƯU VÀ CẬP NHẬT TỒN KHO
            for (Order_Detail detail : details) {
                detail.setOrderId(orderId); // Gán order_id vừa tạo cho từng chi tiết

                // 3a. Lưu chi tiết hóa đơn vào CSDL
                orderDetailDAO.save(detail, conn);

                // 3b. Cập nhật tồn kho (số lượng thay đổi là số âm vì bán ra)
                int quantityChange = -detail.getQuantitySold();
                int rowsAffected = productVariantDAO.updateInventory(detail.getVariantId(), quantityChange, conn);
                if (rowsAffected == 0) {
                     throw new SQLException("Cập nhật tồn kho thất bại cho SKU: " + detail.getVariantId());
                }
            }

            // 4. COMMIT TRANSACTION: Nếu mọi thứ thành công
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo hóa đơn, đang rollback transaction: " + e.getMessage());
            // 5. ROLLBACK TRANSACTION: Nếu có bất kỳ lỗi nào xảy ra
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback thất bại: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            // 6. ĐÓNG CONNECTION VÀ TRẢ LẠI TRẠNG THÁI MẶC ĐỊNH
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Lỗi khi đóng connection: " + e.getMessage());
                }
            }
        }
    }
}
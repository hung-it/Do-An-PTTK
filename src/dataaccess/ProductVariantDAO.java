package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import model.Product_Variant;

public interface ProductVariantDAO extends GenericDAO<Product_Variant, Integer> { 
    
    /**
     * Phương thức đặc thù Cốt lõi: Cập nhật tồn kho.
     * Phương thức này sẽ được gọi trong Transaction Bán hàng.
     * 
     * @param variantId ID của SKU (variant_id) cần cập nhật.
     * @param quantityChange Số lượng thay đổi (Âm khi bán ra, Dương khi nhập kho).
     * @param conn Connection được kiểm soát bởi Service Layer.
     * @return Số hàng bị ảnh hưởng.
     * @throws SQLException 
     */
    int updateInventory(int variantId, int quantityChange, Connection conn) throws SQLException;
    
    /**
     * Tìm kiếm SKU theo mã.
     */
    Product_Variant findBySkuCode(String skuCode, Connection conn) throws SQLException;
    
    /**
     * Tìm kiếm tất cả các biến thể của một Product ID cụ thể.
     */
    List<Product_Variant> findByProductId(int productId, Connection conn) throws SQLException;
}
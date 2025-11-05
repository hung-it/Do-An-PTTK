package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import model.Product;

public interface ProductDAO extends GenericDAO<Product, Integer> {
      /**
     * Tìm tất cả sản phẩm theo tên.
     * @param name Tên sản phẩm.
     * @param conn Connection được cung cấp.
     * @return Danh sách sản phẩm.
     * @throws SQLException 
     */
    List<Product> findByName(String name, Connection conn) throws SQLException;
}

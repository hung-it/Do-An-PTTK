package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @param <T> Loại đối tượng Model (ví dụ: ProductVariant, User, Order)
 * @param <K> Loại khóa chính (ví dụ: Integer, String)
 */
public interface GenericDAO<T, K> {
    
    /**
     * Tìm kiếm một đối tượng theo ID.
     * @return Đối tượng T hoặc null nếu không tìm thấy.
     */
    T findById(K id, Connection conn) throws SQLException;
    
    /**
     * Lấy tất cả các đối tượng.
     * @return Danh sách các đối tượng T.
     */
    List<T> findAll(Connection conn) throws SQLException;
    
    /**
     * Lưu trữ một đối tượng mới (Tạo).
     * @return Khóa chính (K) của đối tượng vừa được tạo (ví dụ: ID tự tăng).
     */
    K save(T entity, Connection conn) throws SQLException;
    
    /**
     * Cập nhật một đối tượng đã tồn tại.
     * @param entity Đối tượng T chứa thông tin mới.
     * @return true nếu cập nhật thành công, false nếu không.
     */
    boolean update(T entity, Connection conn) throws SQLException;
    
    /**
     * Xóa một đối tượng theo ID.
     * @return true nếu xóa thành công, false nếu không.
     */
    boolean delete(K id, Connection conn) throws SQLException;
}
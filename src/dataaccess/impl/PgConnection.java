package dataaccess.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Lớp Singleton quản lý Connection (Connection Factory)
public class PgConnection {
    // URL cho PostgreSQL
    private static final String URL = "jdbc:postgresql://localhost:5432/shoe_store_management"; 
    private static final String USER = "postgres"; 
    private static final String PASS = "asdf0123"; 
    private static final String DRIVER = "org.postgresql.Driver";

    // Khối tĩnh: Được thực thi một lần khi lớp được nạp vào bộ nhớ
    static {
        try {
            Class.forName(DRIVER); 
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver không tìm thấy. Kiểm tra file JAR.", e); 
        }
    }
    
    /**
     * Cung cấp một đối tượng Connection mới.
     * Tầng Service sẽ gọi hàm này và quản lý Transaction trên đối tượng Connection này.
     * @return Connection đến CSDL
     * @throws SQLException 
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS); 
    }

    // Phương thức tiện ích để đóng Connection, Statement, ResultSet
    public static void closeQuietly(AutoCloseable resource) {
        if (resource!= null) {
            try {
                resource.close();
            } catch (Exception e) {
                System.err.println("Lỗi khi đóng tài nguyên: " + e.getMessage());
            }
        }
    }
}

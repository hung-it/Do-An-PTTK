import ui.LoginFrame;
import javax.swing.*;

/**
 * Entry point của ứng dụng
 * Class chính để khởi chạy hệ thống quản lý cửa hàng giày
 */
public class Application {
    
    public static void main(String[] args) {
        // Thiết lập Look and Feel cho ứng dụng
        try {
            // Sử dụng giao diện native của hệ điều hành
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // ===== FIX: Set màu chữ ĐEN CHỈ cho buttons trong JOptionPane =====
            // Không set Button.foreground để không ghi đè custom buttons
            UIManager.put("OptionPane.buttonForeground", java.awt.Color.BLACK);
            
        } catch (Exception e) {
            // Nếu không set được, sẽ dùng giao diện mặc định
            System.err.println("Không thể thiết lập Look and Feel: " + e.getMessage());
        }
        
        // Khởi chạy giao diện đăng nhập trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
        
        System.out.println("Ứng dụng Quản lý Cửa hàng Giày đã khởi động!");
    }
}

package ui;

import model.User;
import service.UserService;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final UserService userService;

    public LoginFrame() {
        userService = new UserService();

        setTitle("Đăng nhập - Quản lý cửa hàng giày");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Hiển thị cửa sổ ở giữa màn hình
        setLayout(new GridLayout(3, 2, 10, 10));

        // Các thành phần giao diện
        add(new JLabel("Tên đăng nhập:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Mật khẩu:"));
        passwordField = new JPasswordField();
        add(passwordField);

        add(new JLabel()); // Ô trống để căn chỉnh
        loginButton = new JButton("Đăng nhập");
        add(loginButton);

        // Xử lý sự kiện khi nhấn nút Đăng nhập
        loginButton.addActionListener(_ -> handleLogin());
        
        // Cho phép nhấn Enter để đăng nhập
        this.getRootPane().setDefaultButton(loginButton);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = userService.login(username, password);

        if (user != null) {
            // Đăng nhập thành công, mở cửa sổ chính và đóng cửa sổ đăng nhập
            JOptionPane.showMessageDialog(this, 
                "Đăng nhập thành công!\nChào mừng " + user.getRole() + ": " + user.getUsername(),
                "Thành công", 
                JOptionPane.INFORMATION_MESSAGE);
            new MainFrame(user).setVisible(true);
            this.dispose();
        } else {
            // Đăng nhập thất bại
            JOptionPane.showMessageDialog(this, 
                "Đăng nhập thất bại!\n\n" +
                "Tên đăng nhập hoặc mật khẩu không đúng.\n\n" +
                "Lưu ý: Hệ thống này chỉ dành cho Admin và Nhân viên.\n" +
                "Khách hàng không thể đăng nhập vào ứng dụng quản lý.", 
                "Đăng nhập thất bại", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
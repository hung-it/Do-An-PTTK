package ui.panel;

import model.User;
import service.UserService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel {

    private final UserService userService;
    private final JTable userTable;
    private final DefaultTableModel tableModel;

    public UserManagementPanel() {
        this.userService = new UserService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // === HEADER ===
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(33, 150, 243));
        JLabel titleLabel = new JLabel("QUẢN LÝ NHÂN VIÊN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // --- Bảng hiển thị danh sách nhân viên ---
        String[] columnNames = {"ID", "Tên đăng nhập", "Mật khẩu", "Vai trò"};
        tableModel = new DefaultTableModel(columnNames, 0);
        userTable = new JTable(tableModel);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userTable.setRowHeight(25);
        userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        loadStaffData(); // Tải dữ liệu vào bảng
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(33, 150, 243), 2),
            "Danh sách nhân viên",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(33, 150, 243)
        ));
        add(scrollPane, BorderLayout.CENTER);

        // --- Panel chứa các nút chức năng ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        JButton addButton = createStyledButton("Thêm nhân viên", new Color(76, 175, 80));
        JButton editButton = createStyledButton("Sửa", new Color(33, 150, 243));
        JButton deleteButton = createStyledButton("Xóa", new Color(244, 67, 54));
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Xử lý sự kiện cho các nút ---
        addButton.addActionListener(_ -> addStaff());
        editButton.addActionListener(_ -> editStaff());
        deleteButton.addActionListener(_ -> deleteStaff());
    }

    private void loadStaffData() {
        // Xóa dữ liệu cũ
        tableModel.setRowCount(0);
        // Lấy dữ liệu mới từ service
        List<User> staffList = userService.getAllStaff();
        for (User staff : staffList) {
            Object[] row = new Object[]{
                staff.getUserId(),
                staff.getUsername(),
                "******", // Không hiển thị mật khẩu
                staff.getRole()
            };
            tableModel.addRow(row);
        }
    }

    private void addStaff() {
        // Tạo một dialog để nhập thông tin nhân viên mới
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.add(new JLabel("Tên đăng nhập:*"));
        inputPanel.add(usernameField);
        inputPanel.add(new JLabel("Mật khẩu:*"));
        inputPanel.add(passwordField);

        int option = JOptionPane.showConfirmDialog(this, inputPanel, 
            "Thêm nhân viên mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            
            // Validation
            if (username.isEmpty() || password.isEmpty()) {
                 JOptionPane.showMessageDialog(this, 
                    "Thông tin không được để trống!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                 return;
            }
            
            if (username.length() < 3 || username.length() > 50) {
                JOptionPane.showMessageDialog(this, 
                    "Tên đăng nhập phải từ 3-50 ký tự!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (password.length() < 3) {
                JOptionPane.showMessageDialog(this, 
                    "Mật khẩu phải ít nhất 3 ký tự!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            User newStaff = new User(0, username, password, "Staff");
            boolean success = userService.addStaff(newStaff);

            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Thêm nhân viên thành công!", 
                    "Thành công", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadStaffData(); // Tải lại dữ liệu bảng
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Thêm nhân viên thất bại!\nTên đăng nhập có thể đã tồn tại.", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editStaff() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn nhân viên cần sửa!", 
                "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentUsername = (String) tableModel.getValueAt(selectedRow, 1);
        
        // Form nhập liệu
        JTextField usernameField = new JTextField(currentUsername, 20);
        usernameField.setEditable(false); // Không cho sửa username
        usernameField.setBackground(Color.LIGHT_GRAY);
        JPasswordField passwordField = new JPasswordField(20);
        
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("Tên đăng nhập:"));
        inputPanel.add(usernameField);
        inputPanel.add(new JLabel("Mật khẩu mới:*"));
        inputPanel.add(passwordField);
        inputPanel.add(new JLabel("(Để trống = không đổi)"));
        inputPanel.add(new JLabel(""));
        
        int result = JOptionPane.showConfirmDialog(this, inputPanel, 
            "Sửa thông tin nhân viên", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        
        String newPassword = new String(passwordField.getPassword()).trim();
        
        // Nếu không nhập password mới thì giữ nguyên
        if (newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Không có thay đổi nào!", 
                "Thông báo", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Validation password mới
        if (newPassword.length() < 3) {
            JOptionPane.showMessageDialog(this, 
                "Mật khẩu phải ít nhất 3 ký tự!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Cập nhật
        User updatedStaff = new User(userId, currentUsername, newPassword, "Staff");
        boolean success = userService.updateStaff(updatedStaff);
        
        if (success) {
            JOptionPane.showMessageDialog(this, 
                "Cập nhật nhân viên thành công!", 
                "Thành công", 
                JOptionPane.INFORMATION_MESSAGE);
            loadStaffData();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Cập nhật thất bại!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteStaff() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            int userId = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc chắn muốn xóa nhân viên này?", "Xác nhận xóa", 
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = userService.deleteStaff(userId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    loadStaffData();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên để xóa.");
        }
    }
    
    /**
     * Tạo button với style đẹp và hover effect
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(140, 34));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
}
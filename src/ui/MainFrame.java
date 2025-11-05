package ui;

import model.User;
import ui.panel.SalePanel;
import ui.panel.UserManagementPanel;
import ui.panel.ProductManagementPanel;
import ui.panel.ReportPanel;
import ui.panel.CustomerManagementPanel;
import javax.swing.*;

public class MainFrame extends JFrame {

    private final User currentUser;

    public MainFrame(User user) {
        this.currentUser = user;

        setTitle("Hệ thống Quản lý Cửa hàng Giày - User: " + currentUser.getUsername());
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Dựa vào vai trò của người dùng để thêm các tab chức năng
        if ("Admin".equalsIgnoreCase(currentUser.getRole())) {
            // Chức năng của Admin
            tabbedPane.addTab("Quản lý Nhân viên", new UserManagementPanel());
            tabbedPane.addTab("Quản lý Sản phẩm", new ProductManagementPanel());
            tabbedPane.addTab("Báo cáo & Thống kê", new ReportPanel());
            tabbedPane.addTab("Quản lý Khách hàng", new CustomerManagementPanel());

        } else if ("Staff".equalsIgnoreCase(currentUser.getRole())) {
            // Chức năng của Staff
            tabbedPane.addTab("Bán Hàng", new SalePanel(currentUser));
            tabbedPane.addTab("Quản lý Khách hàng", new CustomerManagementPanel());
        }
        
        // Thêm JTabbedPane vào frame
        add(tabbedPane);
    }
}
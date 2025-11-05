package ui.panel;

import model.Customer;
import model.Order;
import model.Order_Detail;
import model.Product_Variant;
import model.Product;
import service.CustomerService;
import service.ProductService;
import dataaccess.impl.OrderDetailDAOImpl;
import dataaccess.impl.PgConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Panel quản lý khách hàng - NÂNG CẤP 2.0
 * Dành cho Admin/Staff xem và quản lý thông tin khách hàng
 * Với UI đẹp mắt, chuyên nghiệp và dễ thao tác
 */
public class CustomerManagementPanel extends JPanel {
    
    private final CustomerService customerService;
    private final OrderDetailDAOImpl orderDetailDAO;
    private final ProductService productService;
    private DefaultTableModel customerTableModel;
    private JTable customerTable;
    private JTextField searchField;
    private JLabel statsLabel;
    
    // Colors cho UI đẹp
    private static final Color PRIMARY_COLOR = new Color(66, 133, 244);      // Blue
    private static final Color SUCCESS_COLOR = new Color(52, 168, 83);       // Green
    private static final Color WARNING_COLOR = new Color(251, 188, 5);       // Yellow
    private static final Color DANGER_COLOR = new Color(234, 67, 53);        // Red
    private static final Color INFO_COLOR = new Color(24, 90, 188);          // Dark Blue
    private static final Color LIGHT_BG = new Color(248, 249, 250);          // Light Gray
    private static final Color BORDER_COLOR = new Color(218, 220, 224);      // Border Gray
    
    public CustomerManagementPanel() {
        this.customerService = new CustomerService();
        this.orderDetailDAO = new OrderDetailDAOImpl();
        this.productService = new ProductService();
        initComponents();
        loadAllCustomers();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);
        
        // === HEADER với Stats ===
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // === MAIN CONTENT ===
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        
        // Panel tìm kiếm
        JPanel searchPanel = createSearchPanel();
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Bảng danh sách khách hàng
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Tạo header panel với tiêu đề và thống kê
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(33, 150, 243)); // Blue background giống SalePanel
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(25, 118, 210), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Tiêu đề
        JLabel titleLabel = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE); // Chữ trắng trên nền xanh
        
        // Stats label
        statsLabel = new JLabel("Tổng: 0 khách hàng");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsLabel.setForeground(Color.WHITE); // Chữ trắng
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(statsLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Tạo panel tìm kiếm với UI đẹp
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                "Tìm kiếm khách hàng",
                0,
                0,
                new Font("Segoe UI", Font.BOLD, 13),
                INFO_COLOR
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel label = new JLabel("Số điện thoại hoặc Tên:");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(label);
        
        searchField = new JTextField(30);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(300, 32));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panel.add(searchField);
        
        JButton searchBtn = createStyledButton("Tìm kiếm", PRIMARY_COLOR);
        JButton refreshBtn = createStyledButton("Làm mới", SUCCESS_COLOR);
        
        searchBtn.addActionListener(_ -> searchCustomer());
        refreshBtn.addActionListener(_ -> loadAllCustomers());
        searchField.addActionListener(_ -> searchCustomer());
        
        panel.add(searchBtn);
        panel.add(refreshBtn);
        
        return panel;
    }
    
    /**
     * Tạo button với style đẹp
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 32));
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
    
    /**
     * Tạo panel bảng khách hàng với UI đẹp
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        
        // Bảng danh sách
        String[] columns = {"ID", "Tên khách hàng", "Số điện thoại", "Username", "Ngày tham gia"};
        customerTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        customerTable = new JTable(customerTableModel);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.setRowHeight(35);
        customerTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        customerTable.setShowGrid(true);
        customerTable.setGridColor(BORDER_COLOR);
        customerTable.setSelectionBackground(new Color(232, 240, 254));
        customerTable.setSelectionForeground(Color.BLACK);
        
        // Header style
        customerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        customerTable.getTableHeader().setBackground(LIGHT_BG);
        customerTable.getTableHeader().setForeground(INFO_COLOR);
        customerTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        customerTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR));
        
        // Ẩn cột ID
        customerTable.getColumnModel().getColumn(0).setMinWidth(0);
        customerTable.getColumnModel().getColumn(0).setMaxWidth(0);
        
        // Set width cho các cột
        customerTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Tên
        customerTable.getColumnModel().getColumn(2).setPreferredWidth(120); // SĐT
        customerTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Username
        customerTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Ngày
        
        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel nút chức năng với icon đẹp
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(LIGHT_BG);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        
        JButton viewDetailsBtn = createStyledButton("Xem chi tiết", INFO_COLOR);
        JButton viewHistoryBtn = createStyledButton("Lịch sử mua hàng", new Color(156, 39, 176)); // Purple
        JButton editBtn = createStyledButton("Sửa thông tin", WARNING_COLOR);
        JButton deleteBtn = createStyledButton("Xóa", DANGER_COLOR);
        
        viewDetailsBtn.setToolTipText("Xem thông tin chi tiết khách hàng");
        viewHistoryBtn.setToolTipText("Xem lịch sử đơn hàng đã mua");
        editBtn.setToolTipText("Chỉnh sửa thông tin khách hàng");
        deleteBtn.setToolTipText("Xóa khách hàng (nếu không có đơn hàng)");
        
        viewDetailsBtn.addActionListener(_ -> viewCustomerDetails());
        viewHistoryBtn.addActionListener(_ -> viewPurchaseHistory());
        editBtn.addActionListener(_ -> editCustomer());
        deleteBtn.addActionListener(_ -> deleteCustomer());
        
        buttonPanel.add(viewDetailsBtn);
        buttonPanel.add(viewHistoryBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // ============= XỬ LÝ DỮ LIỆU =============
    
    /**
     * Load tất cả khách hàng với stats
     */
    private void loadAllCustomers() {
        customerTableModel.setRowCount(0);
        searchField.setText("");
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        SwingWorker<List<Customer>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Customer> doInBackground() {
                return customerService.getAllCustomers();
            }
            
            @Override
            protected void done() {
                try {
                    List<Customer> customers = get();
                    displayCustomers(customers);
                    
                    // Update stats
                    statsLabel.setText(String.format("Tổng: %d khách hàng", customers.size()));
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CustomerManagementPanel.this,
                        "Lỗi khi tải danh sách khách hàng:\n" + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Tìm kiếm khách hàng
     */
    private void searchCustomer() {
        String keyword = searchField.getText().trim();
        
        if (keyword.isEmpty()) {
            loadAllCustomers();
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Tìm theo SĐT trước
        Customer customer = customerService.findCustomerByPhone(keyword);
        
        if (customer != null) {
            customerTableModel.setRowCount(0);
            addCustomerToTable(customer);
            setCursor(Cursor.getDefaultCursor());
        } else {
            // Nếu không tìm thấy theo SĐT, tìm theo tên
            SwingWorker<List<Customer>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Customer> doInBackground() {
                    return customerService.searchCustomersByName(keyword);
                }
                
                @Override
                protected void done() {
                    try {
                        List<Customer> customers = get();
                        customerTableModel.setRowCount(0);
                        
                        if (customers.isEmpty()) {
                            JOptionPane.showMessageDialog(CustomerManagementPanel.this,
                                "Không tìm thấy khách hàng với từ khóa: " + keyword,
                                "Thông báo",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            displayCustomers(customers);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(CustomerManagementPanel.this,
                            "Lỗi khi tìm kiếm:\n" + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    } finally {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    /**
     * Hiển thị danh sách khách hàng
     */
    private void displayCustomers(List<Customer> customers) {
        customerTableModel.setRowCount(0);
        
        if (customers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Chưa có khách hàng nào trong hệ thống.",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        for (Customer customer : customers) {
            addCustomerToTable(customer);
        }
    }
    
    /**
     * Thêm khách hàng vào bảng
     */
    private void addCustomerToTable(Customer customer) {
        // Format ngày tham gia từ database
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String joinDate = "N/A";
        if (customer.getJoinDate() != null) {
            joinDate = dateFormat.format(customer.getJoinDate());
        }
        
        Object[] row = {
            customer.getCustomerId(),
            customer.getName(),
            customer.getPhoneNumber(),
            customer.getUsername() != null ? customer.getUsername() : "N/A",
            joinDate // Hiển thị ngày từ database
        };
        customerTableModel.addRow(row);
    }
    
    // ============= CHỨC NĂNG KHÁCH HÀNG =============
    
    /**
     * Xem chi tiết khách hàng
     */
    private void viewCustomerDetails() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn khách hàng!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int customerId = (int) customerTableModel.getValueAt(selectedRow, 0);
        String name = (String) customerTableModel.getValueAt(selectedRow, 1);
        String phone = (String) customerTableModel.getValueAt(selectedRow, 2);
        String username = (String) customerTableModel.getValueAt(selectedRow, 3);
        String joinDate = (String) customerTableModel.getValueAt(selectedRow, 4);
        
        String details = String.format(
            "THÔNG TIN KHÁCH HÀNG\n\n" +
            "Mã KH: #%d\n" +
            "Tên: %s\n" +
            "SĐT: %s\n" +
            "Username: %s\n" +
            "Ngày tham gia: %s\n",
            customerId, name, phone, username, joinDate
        );
        
        JTextArea textArea = new JTextArea(details);
        textArea.setEditable(false);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JOptionPane.showMessageDialog(this,
            new JScrollPane(textArea),
            "Chi tiết khách hàng",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Xem lịch sử mua hàng - HOÀN THIỆN với Dialog đẹp
     */
    private void viewPurchaseHistory() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn khách hàng!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int customerId = (int) customerTableModel.getValueAt(selectedRow, 0);
        String customerName = (String) customerTableModel.getValueAt(selectedRow, 1);
        
        // Hiển thị dialog lịch sử
        showPurchaseHistoryDialog(customerId, customerName);
    }
    
    /**
     * Hiển thị dialog lịch sử mua hàng với UI đẹp
     */
    private void showPurchaseHistoryDialog(int customerId, String customerName) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Lịch sử mua hàng - " + customerName, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(900, 550);
        dialog.setLocationRelativeTo(this);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(new Color(156, 39, 176)); // Purple
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("📋 LỊCH SỬ MUA HÀNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel customerLabel = new JLabel("Khách hàng: " + customerName);
        customerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        customerLabel.setForeground(Color.WHITE);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(customerLabel, BorderLayout.EAST);
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Main content - Loading
        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel loadingLabel = new JLabel("⏳ Đang tải lịch sử mua hàng...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
        
        dialog.add(loadingPanel, BorderLayout.CENTER);
        
        // Bottom button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(LIGHT_BG);
        JButton closeBtn = createStyledButton("✖️ Đóng", DANGER_COLOR);
        closeBtn.addActionListener(_ -> dialog.dispose());
        bottomPanel.add(closeBtn);
        
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        
        // Load data async
        SwingWorker<List<Order>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Order> doInBackground() {
                return customerService.getOrderHistory(customerId);
            }
            
            @Override
            protected void done() {
                try {
                    List<Order> orders = get();
                    
                    // Xóa loading panel
                    dialog.remove(loadingPanel);
                    
                    // Thêm content panel
                    JPanel contentPanel = createOrderHistoryPanel(orders);
                    dialog.add(contentPanel, BorderLayout.CENTER);
                    
                    dialog.revalidate();
                    dialog.repaint();
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(dialog,
                        "Lỗi khi tải lịch sử: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        dialog.setVisible(true);
    }
    
    /**
     * Tạo panel hiển thị lịch sử đơn hàng
     */
    private JPanel createOrderHistoryPanel(List<Order> orders) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);
        
        if (orders.isEmpty()) {
            JLabel emptyLabel = new JLabel("📭 Khách hàng chưa có đơn hàng nào", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            emptyLabel.setForeground(new Color(95, 99, 104));
            panel.add(emptyLabel, BorderLayout.CENTER);
            return panel;
        }
        
        // Stats panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        int totalOrders = orders.size();
        BigDecimal totalSpent = orders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgOrder = totalOrders > 0 ? totalSpent.divide(
            BigDecimal.valueOf(totalOrders), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        
        NumberFormat currencyFormat = NumberFormat.getInstance(Locale.of("vi", "VN"));
        
        statsPanel.add(createStatCard("Tổng đơn hàng", String.valueOf(totalOrders), PRIMARY_COLOR));
        statsPanel.add(createStatCard("Tổng chi tiêu", currencyFormat.format(totalSpent) + " ₫", SUCCESS_COLOR));
        statsPanel.add(createStatCard("TB/Đơn hàng", currencyFormat.format(avgOrder) + " ₫", WARNING_COLOR));
        
        panel.add(statsPanel, BorderLayout.NORTH);
        
        // Table đơn hàng
        String[] columns = {"Mã ĐH", "Ngày mua", "Tổng tiền", "Số mặt hàng", "Chi tiết"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable orderTable = new JTable(tableModel);
        orderTable.setRowHeight(35);
        orderTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        orderTable.setShowGrid(true);
        orderTable.setGridColor(BORDER_COLOR);
        orderTable.setSelectionBackground(new Color(232, 240, 254));
        
        // Header style
        orderTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        orderTable.getTableHeader().setBackground(LIGHT_BG);
        orderTable.getTableHeader().setForeground(INFO_COLOR);
        orderTable.getTableHeader().setPreferredSize(new Dimension(0, 35));
        
        // Populate table
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        for (Order order : orders) {
            try {
                // Count items in order
                List<Order_Detail> details;
                try (Connection conn = PgConnection.getConnection()) {
                    details = orderDetailDAO.findByOrderId(order.getOrderId(), conn);
                }
                
                int itemCount = details.size();
                String dateStr = order.getOrderDate();
                
                // Parse date if it's a timestamp string
                try {
                    java.sql.Timestamp ts = java.sql.Timestamp.valueOf(dateStr);
                    dateStr = dateFormat.format(ts);
                } catch (Exception ignored) {
                    // Keep original if parsing fails
                }
                
                tableModel.addRow(new Object[]{
                    "#" + order.getOrderId(),
                    dateStr,
                    currencyFormat.format(order.getTotalAmount()) + " ₫",
                    itemCount + " sản phẩm",
                    "Xem →"
                });
                
            } catch (Exception e) {
                System.err.println("Lỗi khi load order detail: " + e.getMessage());
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Double click to view details
        orderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = orderTable.getSelectedRow();
                    if (row >= 0) {
                        Order selectedOrder = orders.get(row);
                        showOrderDetailsDialog(selectedOrder);
                    }
                }
            }
        });
        
        return panel;
    }
    
    /**
     * Tạo stat card cho thống kê
     */
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(95, 99, 104));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Hiển thị chi tiết đơn hàng
     */
    private void showOrderDetailsDialog(Order order) {
        JDialog detailDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Chi tiết đơn hàng #" + order.getOrderId(), true);
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.setSize(700, 450);
        detailDialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(Color.WHITE);
        
        // Order info
        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        infoPanel.setBackground(LIGHT_BG);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dateStr = order.getOrderDate();
        try {
            java.sql.Timestamp ts = java.sql.Timestamp.valueOf(dateStr);
            dateStr = dateFormat.format(ts);
        } catch (Exception ignored) {}
        
        NumberFormat currencyFormat = NumberFormat.getInstance(Locale.of("vi", "VN"));
        
        infoPanel.add(new JLabel("Mã đơn hàng:"));
        infoPanel.add(new JLabel("#" + order.getOrderId()));
        infoPanel.add(new JLabel("Ngày đặt:"));
        infoPanel.add(new JLabel(dateStr));
        infoPanel.add(new JLabel("Tổng tiền:"));
        JLabel totalLabel = new JLabel(currencyFormat.format(order.getTotalAmount()) + " ₫");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(SUCCESS_COLOR);
        infoPanel.add(totalLabel);
        
        contentPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Table chi tiết sản phẩm
        String[] columns = {"STT", "Tên sản phẩm", "Size", "Màu", "SL", "Đơn giá", "Thành tiền"};
        DefaultTableModel detailTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable detailTable = new JTable(detailTableModel);
        detailTable.setRowHeight(30);
        detailTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        try (Connection conn = PgConnection.getConnection()) {
            List<Order_Detail> details = orderDetailDAO.findByOrderId(order.getOrderId(), conn);
            
            int stt = 1;
            for (Order_Detail detail : details) {
                // Lấy thông tin variant và product
                Product_Variant variant = productService.getVariantById(detail.getVariantId());
                String productName = "N/A";
                String size = "N/A";
                String color = "N/A";
                
                if (variant != null) {
                    Product product = productService.getProductById(variant.getProductId());
                    if (product != null) {
                        productName = product.getName();
                    }
                    size = String.valueOf(variant.getSize());
                    color = variant.getColor();
                }
                
                BigDecimal lineTotal = detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantitySold()));
                
                detailTableModel.addRow(new Object[]{
                    stt++,
                    productName,
                    size,
                    color,
                    detail.getQuantitySold(),
                    currencyFormat.format(detail.getUnitPrice()) + " ₫",
                    currencyFormat.format(lineTotal) + " ₫"
                });
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi load chi tiết đơn hàng: " + e.getMessage());
        }
        
        JScrollPane scrollPane = new JScrollPane(detailTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        detailDialog.add(contentPanel, BorderLayout.CENTER);
        
        // Close button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(LIGHT_BG);
        JButton closeBtn = createStyledButton("Đóng", DANGER_COLOR);
        closeBtn.addActionListener(_ -> detailDialog.dispose());
        bottomPanel.add(closeBtn);
        detailDialog.add(bottomPanel, BorderLayout.SOUTH);
        
        detailDialog.setVisible(true);
    }
    
    /**
     * Sửa thông tin khách hàng
     */
    private void editCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn khách hàng cần sửa!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int customerId = (int) customerTableModel.getValueAt(selectedRow, 0);
        String currentName = (String) customerTableModel.getValueAt(selectedRow, 1);
        String currentPhone = (String) customerTableModel.getValueAt(selectedRow, 2);
        String currentJoinDateStr = (String) customerTableModel.getValueAt(selectedRow, 4);
        
        // Lấy thông tin đầy đủ từ database để giữ lại username và password
        Customer existingCustomer = customerService.findCustomerByPhone(currentPhone);
        if (existingCustomer == null) {
            JOptionPane.showMessageDialog(this,
                "Không tìm thấy thông tin khách hàng!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Form nhập liệu
        JTextField nameField = new JTextField(currentName, 25);
        JTextField phoneField = new JTextField(currentPhone, 15);
        phoneField.setEditable(false); // Không cho sửa SĐT (PK)
        phoneField.setBackground(Color.LIGHT_GRAY);
        
        // Thêm trường ngày tham gia
        JSpinner joinDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(joinDateSpinner, "dd/MM/yyyy");
        joinDateSpinner.setEditor(dateEditor);
        
        // Set giá trị hiện tại cho date spinner
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            java.util.Date currentJoinDate = sdf.parse(currentJoinDateStr);
            joinDateSpinner.setValue(currentJoinDate);
        } catch (Exception e) {
            joinDateSpinner.setValue(new java.util.Date()); // Default to today
        }
        
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("Tên khách hàng:*"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Số điện thoại:"));
        inputPanel.add(phoneField);
        inputPanel.add(new JLabel("Ngày tham gia:*"));
        inputPanel.add(joinDateSpinner);
        
        int result = JOptionPane.showConfirmDialog(this, inputPanel,
            "Sửa thông tin khách hàng", JOptionPane.OK_CANCEL_OPTION);
        
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        
        String newName = nameField.getText().trim();
        java.util.Date newJoinDate = (java.util.Date) joinDateSpinner.getValue();
        
        // Validation
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Tên khách hàng không được để trống!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (newName.length() < 2 || newName.length() > 100) {
            JOptionPane.showMessageDialog(this,
                "Tên khách hàng phải từ 2-100 ký tự!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Cập nhật - GIỮ NGUYÊN username và password
        Customer updatedCustomer = new Customer();
        updatedCustomer.setCustomerId(customerId);
        updatedCustomer.setName(newName);
        updatedCustomer.setPhoneNumber(currentPhone);
        updatedCustomer.setUsername(existingCustomer.getUsername()); // ✅ Giữ username cũ
        updatedCustomer.setPassword(existingCustomer.getPassword()); // ✅ Giữ password cũ
        updatedCustomer.setJoinDate(new java.sql.Timestamp(newJoinDate.getTime()));
        
        boolean success = customerService.updateCustomer(updatedCustomer);
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Cập nhật thông tin khách hàng thành công!",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
            loadAllCustomers();
        } else {
            JOptionPane.showMessageDialog(this,
                "Cập nhật thất bại!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Xóa khách hàng
     */
    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn khách hàng cần xóa!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int customerId = (int) customerTableModel.getValueAt(selectedRow, 0);
        String name = (String) customerTableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa khách hàng này?\n\n" +
            "Tên: " + name + "\n\n" +
            "⚠️ Cảnh báo: Không thể xóa nếu có đơn hàng liên quan!",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        boolean success = customerService.deleteCustomer(customerId);
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Xóa khách hàng thành công!",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
            loadAllCustomers();
        } else {
            JOptionPane.showMessageDialog(this,
                "Xóa thất bại!\n\n" +
                "Có thể do:\n" +
                "- Khách hàng có đơn hàng liên quan\n" +
                "- Lỗi database",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

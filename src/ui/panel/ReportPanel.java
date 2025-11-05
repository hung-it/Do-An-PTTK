package ui.panel;

import model.Order;
import model.Customer;
import model.User;
import service.ReportService;
import service.CustomerService;
import service.UserService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Panel báo cáo và thống kê doanh thu
 * Dành cho Admin xem báo cáo kinh doanh
 */
public class ReportPanel extends JPanel {
    
    private final ReportService reportService;
    private final CustomerService customerService;
    private final UserService userService;
    private DefaultTableModel orderTableModel;
    private JTable orderTable;
    private JLabel totalRevenueLabel;
    private JLabel orderCountLabel;
    private JLabel avgOrderLabel;
    
    // Date selection
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    
    public ReportPanel() {
        this.reportService = new ReportService();
        this.customerService = new CustomerService();
        this.userService = new UserService();
        initComponents();
        loadTodayReport(); // Load báo cáo ngày hôm nay khi khởi tạo
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // === HEADER ===
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(33, 150, 243));
        JLabel titleLabel = new JLabel("BÁO CÁO DOANH THU");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // === MAIN CONTENT ===
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        
        // Panel lọc theo thời gian
        JPanel filterPanel = createFilterPanel();
        mainPanel.add(filterPanel, BorderLayout.NORTH);
        
        // Panel thống kê tổng quan
        JPanel statsPanel = createStatsPanel();
        mainPanel.add(statsPanel, BorderLayout.CENTER);
        
        // Panel bảng chi tiết đơn hàng
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Tạo panel lọc theo thời gian
     */
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(33, 150, 243), 2),
            "Chọn khoảng thời gian",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(33, 150, 243)
        ));
        
        // Ngày bắt đầu
        panel.add(new JLabel("Từ ngày:"));
        startDateSpinner = createDateSpinner();
        panel.add(startDateSpinner);
        
        // Ngày kết thúc
        panel.add(new JLabel("Đến ngày:"));
        endDateSpinner = createDateSpinner();
        panel.add(endDateSpinner);
        
        // Các nút quick select
        JButton todayBtn = createStyledButton("Hôm nay", new Color(33, 150, 243));
        JButton thisWeekBtn = createStyledButton("Tuần này", new Color(33, 150, 243));
        JButton thisMonthBtn = createStyledButton("Tháng này", new Color(33, 150, 243));
        JButton customBtn = createStyledButton("Tùy chỉnh", new Color(158, 158, 158));
        
        todayBtn.addActionListener(_ -> {
            setDateRangeToToday();
            loadReport();
        });
        
        thisWeekBtn.addActionListener(_ -> {
            setDateRangeToThisWeek();
            loadReport();
        });
        
        thisMonthBtn.addActionListener(_ -> {
            setDateRangeToThisMonth();
            loadReport();
        });
        
        customBtn.addActionListener(_ -> loadReport());
        
        panel.add(todayBtn);
        panel.add(thisWeekBtn);
        panel.add(thisMonthBtn);
        panel.add(customBtn);
        
        return panel;
    }
    
    /**
     * Tạo DateSpinner
     */
    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy");
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(120, 30));
        return spinner;
    }
    
    /**
     * Tạo panel thống kê tổng quan
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Thống kê tổng quan"));
        panel.setPreferredSize(new Dimension(0, 150));
        
        // Card 1: Tổng doanh thu
        JPanel revenueCard = createStatCard("TỔNG DOANH THU", "0 đ", new Color(76, 175, 80));
        totalRevenueLabel = (JLabel) ((JPanel) revenueCard.getComponent(1)).getComponent(0);
        panel.add(revenueCard);
        
        // Card 2: Số đơn hàng
        JPanel orderCountCard = createStatCard("SỐ ĐƠN HÀNG", "0", new Color(33, 150, 243));
        orderCountLabel = (JLabel) ((JPanel) orderCountCard.getComponent(1)).getComponent(0);
        panel.add(orderCountCard);
        
        // Card 3: Giá trị TB/đơn
        JPanel avgOrderCard = createStatCard("GIÁ TRỊ TB/ĐƠN", "0 đ", new Color(255, 152, 0));
        avgOrderLabel = (JLabel) ((JPanel) avgOrderCard.getComponent(1)).getComponent(0);
        panel.add(avgOrderCard);
        
        return panel;
    }
    
    /**
     * Tạo card thống kê
     */
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(Color.GRAY);
        card.add(titleLabel, BorderLayout.NORTH);
        
        // Value
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        valuePanel.setBackground(Color.WHITE);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valuePanel.add(valueLabel);
        card.add(valuePanel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Tạo panel bảng chi tiết
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(33, 150, 243), 2),
            "Chi tiết đơn hàng",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(33, 150, 243)
        ));
        panel.setPreferredSize(new Dimension(0, 300));
        
        // Bảng danh sách đơn hàng
        String[] columns = {"Mã ĐH", "Ngày", "Khách hàng", "Nhân viên", "Tổng tiền"};
        orderTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        orderTable = new JTable(orderTableModel);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.setRowHeight(25);
        orderTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(orderTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel nút chức năng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        
        JButton exportBtn = createStyledButton("Xuất Excel", new Color(76, 175, 80));
        JButton refreshBtn = createStyledButton("Làm mới", new Color(158, 158, 158));
        
        exportBtn.addActionListener(_ -> exportToExcel());
        refreshBtn.addActionListener(_ -> loadReport());
        
        // Enable export button
        buttonPanel.add(exportBtn);
        buttonPanel.add(refreshBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // ============= XỬ LÝ DỮ LIỆU =============
    
    /**
     * Load báo cáo ngày hôm nay
     */
    private void loadTodayReport() {
        setDateRangeToToday();
        loadReport();
    }
    
    /**
     * Load báo cáo theo khoảng thời gian
     */
    private void loadReport() {
        Date startDate = (Date) startDateSpinner.getValue();
        Date endDate = (Date) endDateSpinner.getValue();
        
        // Validation
        if (startDate.after(endDate)) {
            JOptionPane.showMessageDialog(this,
                "Ngày bắt đầu phải trước ngày kết thúc!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Set time to start/end of day
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        
        // Load data trong background thread
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        SwingWorker<ReportData, Void> worker = new SwingWorker<>() {
            @Override
            protected ReportData doInBackground() {
                List<Order> orders = reportService.getOrdersByDateRange(startCal.getTime(), endCal.getTime());
                BigDecimal totalRevenue = reportService.getRevenueByDateRange(startCal.getTime(), endCal.getTime());
                return new ReportData(orders, totalRevenue);
            }
            
            @Override
            protected void done() {
                try {
                    ReportData data = get();
                    displayReport(data);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ReportPanel.this,
                        "Lỗi khi tải báo cáo:\n" + e.getMessage(),
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
     * Hiển thị dữ liệu báo cáo
     */
    private void displayReport(ReportData data) {
        List<Order> orders = data.orders;
        BigDecimal totalRevenue = data.totalRevenue;
        
        // Clear table
        orderTableModel.setRowCount(0);
        
        // Update stats
        int orderCount = orders.size();
        BigDecimal avgOrder = orderCount > 0 
            ? totalRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        totalRevenueLabel.setText(String.format("%,.0f đ", totalRevenue));
        orderCountLabel.setText(String.valueOf(orderCount));
        avgOrderLabel.setText(String.format("%,.0f đ", avgOrder));
        
        // Populate table
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Order order : orders) {
            // Parse orderDate string to display format
            String dateStr = order.getOrderDate();
            try {
                // orderDate là String dạng timestamp, parse nó
                java.sql.Timestamp ts = java.sql.Timestamp.valueOf(dateStr);
                dateStr = sdf.format(ts);
            } catch (Exception e) {
                // Nếu lỗi thì giữ nguyên format gốc
                dateStr = order.getOrderDate();
            }
            
            // Lấy tên khách hàng và nhân viên
            String customerName = "Khách #" + order.getCustomerId();
            String userName = "NV #" + order.getUserId();
            
            try {
                Customer customer = customerService.getCustomerById(order.getCustomerId());
                if (customer != null && customer.getName() != null) {
                    customerName = customer.getName();
                }
            } catch (Exception e) {
                // Giữ nguyên tên mặc định
            }
            
            try {
                User user = userService.getUserById(order.getUserId());
                if (user != null && user.getUsername() != null) {
                    userName = user.getUsername();
                }
            } catch (Exception e) {
                // Giữ nguyên tên mặc định
            }
            
            Object[] row = {
                "#" + order.getOrderId(),
                dateStr,
                customerName,
                userName,
                String.format("%,.0f đ", order.getTotalAmount())
            };
            orderTableModel.addRow(row);
        }
        
        // Show message if no data
        if (orderCount == 0) {
            JOptionPane.showMessageDialog(this,
                "Không có đơn hàng nào trong khoảng thời gian này.",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // ============= HELPER METHODS =============
    
    private void setDateRangeToToday() {
        Date today = new Date();
        startDateSpinner.setValue(today);
        endDateSpinner.setValue(today);
    }
    
    private void setDateRangeToThisWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        startDateSpinner.setValue(cal.getTime());
        
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        endDateSpinner.setValue(cal.getTime());
    }
    
    private void setDateRangeToThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        startDateSpinner.setValue(cal.getTime());
        
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        endDateSpinner.setValue(cal.getTime());
    }
    
    private void exportToExcel() {
        // Kiểm tra có dữ liệu không
        if (orderTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "Không có dữ liệu để xuất!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Chọn nơi lưu file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu báo cáo");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String defaultFileName = "BaoCaoDoanhThu_" + sdf.format(new Date()) + ".csv";
        fileChooser.setSelectedFile(new File(defaultFileName));
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            // Đảm bảo file có đuôi .csv
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }
            
            // Xuất dữ liệu ra CSV
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave, java.nio.charset.StandardCharsets.UTF_8))) {
                // Write BOM for UTF-8
                writer.write('\ufeff');
                
                // Header thống kê
                writer.println("BÁO CÁO DOANH THU");
                writer.println("Từ ngày:," + new SimpleDateFormat("dd/MM/yyyy").format((Date) startDateSpinner.getValue()));
                writer.println("Đến ngày:," + new SimpleDateFormat("dd/MM/yyyy").format((Date) endDateSpinner.getValue()));
                writer.println("Tổng doanh thu:," + totalRevenueLabel.getText());
                writer.println("Số đơn hàng:," + orderCountLabel.getText());
                writer.println("Giá trị TB/đơn:," + avgOrderLabel.getText());
                writer.println(); // Dòng trống
                
                // Header bảng
                writer.println("Mã ĐH,Ngày,Khách hàng,Nhân viên,Tổng tiền");
                
                // Dữ liệu
                for (int row = 0; row < orderTableModel.getRowCount(); row++) {
                    StringBuilder line = new StringBuilder();
                    for (int col = 0; col < orderTableModel.getColumnCount(); col++) {
                        Object value = orderTableModel.getValueAt(row, col);
                        String cellValue = value != null ? value.toString() : "";
                        
                        // Escape dấu phẩy và quotes
                        if (cellValue.contains(",") || cellValue.contains("\"")) {
                            cellValue = "\"" + cellValue.replace("\"", "\"\"") + "\"";
                        }
                        
                        line.append(cellValue);
                        if (col < orderTableModel.getColumnCount() - 1) {
                            line.append(",");
                        }
                    }
                    writer.println(line);
                }
                
                JOptionPane.showMessageDialog(this,
                    "Xuất báo cáo thành công!\n\nFile: " + fileToSave.getAbsolutePath(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
                    
                // Hỏi có muốn mở file không
                int openFile = JOptionPane.showConfirmDialog(this,
                    "Bạn có muốn mở file ngay bây giờ?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
                    
                if (openFile == JOptionPane.YES_OPTION) {
                    try {
                        Desktop.getDesktop().open(fileToSave);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this,
                            "Không thể mở file tự động. Vui lòng mở thủ công.",
                            "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi xuất file:\n" + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
                System.err.println("Lỗi xuất file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Tạo button với style đẹp và hover effect
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
    
    // ============= INNER CLASS =============
    
    /**
     * Class để truyền dữ liệu từ background thread
     */
    private static class ReportData {
        final List<Order> orders;
        final BigDecimal totalRevenue;
        
        ReportData(List<Order> orders, BigDecimal totalRevenue) {
            this.orders = orders;
            this.totalRevenue = totalRevenue;
        }
    }
}

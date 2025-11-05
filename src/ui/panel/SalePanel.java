package ui.panel;

import model.User;
import model.Customer;
import model.Product_Variant;
import model.Order;
import model.Order_Detail;
import service.ProductService;
import service.CustomerService;
import service.SaleService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * Panel chức năng Bán Hàng
 * Dành cho nhân viên (Staff) để thực hiện các giao dịch bán hàng
 */
public class SalePanel extends JPanel {
    
    private final User currentUser;
    private final ProductService productService;
    private final CustomerService customerService;
    private final SaleService saleService;
    
    private DefaultTableModel productTableModel;
    private DefaultTableModel cartTableModel;
    private JTable productTable;
    private JTable cartTable;
    
    private JTextField phoneField;
    private JLabel customerNameLabel;
    private JLabel totalLabel;
    private JTextField searchField;
    
    private Customer currentCustomer;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    // Cache để tránh query DB nhiều lần
    private final java.util.Map<Integer, Product_Variant> variantCache = new java.util.HashMap<>();
    
    // Loading state
    private boolean isLoading = false;
    
    // Flag để tránh infinite loop khi update table
    private boolean isUpdatingTotal = false;
    
    public SalePanel(User currentUser) {
        this.currentUser = currentUser;
        this.productService = new ProductService();
        this.customerService = new CustomerService();
        this.saleService = new SaleService();
        initComponents();
        loadProducts(); // Load dữ liệu sản phẩm khi khởi tạo
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);
        
        // === HEADER đẹp ===
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(new Color(33, 150, 243)); // Blue
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(25, 118, 210), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel("BÁN HÀNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel staffLabel = new JLabel("Nhân viên: " + currentUser.getUsername());
        staffLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        staffLabel.setForeground(Color.WHITE);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(staffLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
        
        // === MAIN CONTENT ===
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        mainPanel.setBackground(Color.WHITE);
        
        // Panel tìm kiếm sản phẩm
        JPanel searchPanel = createSearchPanel();
        mainPanel.add(searchPanel);
        
        // Panel giỏ hàng
        JPanel cartPanel = createCartPanel();
        mainPanel.add(cartPanel);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // === FOOTER - Thanh toán ===
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    // Tạo panel tìm kiếm sản phẩm với UI đẹp
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(218, 220, 224), 1),
                "Tìm kiếm Sản phẩm",
                0,
                0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(24, 90, 188)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Thanh tìm kiếm
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchBar.setBackground(Color.WHITE);
        
        JLabel searchLabel = new JLabel("Tên sản phẩm:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchBar.add(searchLabel);
        
        searchField = new JTextField(25);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(250, 32));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 220, 224), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchBar.add(searchField);
        
        JButton searchButton = new JButton("Tìm kiếm");
        styleButton(searchButton, new Color(66, 133, 244)); // Blue
        searchBar.add(searchButton);
        
        JButton refreshButton = new JButton("Làm mới");
        styleButton(refreshButton, new Color(52, 168, 83)); // Green
        searchBar.add(refreshButton);
        
        panel.add(searchBar, BorderLayout.NORTH);
        
        // Bảng danh sách sản phẩm
        String[] columns = {"Variant ID", "SKU", "Tên sản phẩm", "Size", "Màu", "Giá", "Tồn kho"};
        productTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép edit
            }
        };
        
        productTable = new JTable(productTableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setRowHeight(30);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        productTable.setShowGrid(true);
        productTable.setGridColor(new Color(218, 220, 224));
        productTable.setSelectionBackground(new Color(232, 240, 254));
        productTable.setSelectionForeground(Color.BLACK);
        
        // Header style
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        productTable.getTableHeader().setBackground(new Color(248, 249, 250));
        productTable.getTableHeader().setForeground(new Color(24, 90, 188));
        productTable.getTableHeader().setPreferredSize(new Dimension(0, 35));
        
        // Ẩn cột Variant ID
        productTable.getColumnModel().getColumn(0).setMinWidth(0);
        productTable.getColumnModel().getColumn(0).setMaxWidth(0);
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(218, 220, 224), 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Nút thêm vào giỏ hàng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addToCartButton = new JButton("Thêm vào giỏ hàng");
        addToCartButton.setBackground(new Color(33, 150, 243));
        addToCartButton.setForeground(Color.WHITE);
        addToCartButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addToCartButton.setPreferredSize(new Dimension(180, 35));
        addToCartButton.setFocusPainted(false);
        addToCartButton.setBorderPainted(false);
        addToCartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        addToCartButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addToCartButton.setBackground(new Color(25, 118, 210));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addToCartButton.setBackground(new Color(33, 150, 243));
            }
        });
        
        buttonPanel.add(addToCartButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Event handlers
        searchButton.addActionListener(_ -> searchProducts(searchField.getText()));
        refreshButton.addActionListener(_ -> loadProducts());
        addToCartButton.addActionListener(_ -> addToCart());
        
        // Enter để tìm kiếm
        searchField.addActionListener(_ -> searchProducts(searchField.getText()));
        
        return panel;
    }
    
    // Helper: Style button
    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(110, 32));
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
    }
    
    // Load tất cả sản phẩm từ database
    private void loadProducts() {
        if (isLoading) return; // Tránh load nhiều lần
        
        isLoading = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Chạy trong background thread để không block UI
        SwingWorker<List<Product_Variant>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Product_Variant> doInBackground() {
                return productService.getAllProductVariants();
            }
            
            @Override
            protected void done() {
                try {
                    List<Product_Variant> variants = get();
                    
                    productTableModel.setRowCount(0);
                    variantCache.clear();
                    
                    if (variants.isEmpty()) {
                        JOptionPane.showMessageDialog(SalePanel.this, 
                            "Không có sản phẩm nào trong hệ thống.\n" +
                            "Vui lòng kiểm tra kết nối database hoặc thêm sản phẩm.", 
                            "Thông báo", 
                            JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    
                    for (Product_Variant variant : variants) {
                        variantCache.put(variant.getVariantId(), variant);
                        
                        Object[] row = {
                            variant.getVariantId(),
                            variant.getSkuCode(),
                            productService.getProductNameById(variant.getProductId()),
                            variant.getSize(),
                            variant.getColor(),
                            String.format("%,.0f đ", variant.getPrice()),
                            variant.getQuantityInStock()
                        };
                        productTableModel.addRow(row);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SalePanel.this, 
                        "Lỗi khi tải danh sách sản phẩm:\n" + e.getMessage(), 
                        "Lỗi", 
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    isLoading = false;
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        worker.execute();
    }
    
    // Tìm kiếm sản phẩm theo tên
    private void searchProducts(String keyword) {
        if (keyword.trim().isEmpty()) {
            loadProducts();
            return;
        }
        
        if (isLoading) return;
        
        isLoading = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        final String searchKeyword = keyword.trim();
        
        SwingWorker<List<Product_Variant>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Product_Variant> doInBackground() {
                return productService.searchProductVariants(searchKeyword);
            }
            
            @Override
            protected void done() {
                try {
                    List<Product_Variant> variants = get();
                    
                    productTableModel.setRowCount(0);
                    variantCache.clear();
                    
                    if (variants.isEmpty()) {
                        JOptionPane.showMessageDialog(SalePanel.this, 
                            "Không tìm thấy sản phẩm nào với từ khóa: " + searchKeyword, 
                            "Thông báo", 
                            JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    
                    for (Product_Variant variant : variants) {
                        variantCache.put(variant.getVariantId(), variant);
                        
                        Object[] row = {
                            variant.getVariantId(),
                            variant.getSkuCode(),
                            productService.getProductNameById(variant.getProductId()),
                            variant.getSize(),
                            variant.getColor(),
                            String.format("%,.0f đ", variant.getPrice()),
                            variant.getQuantityInStock()
                        };
                        productTableModel.addRow(row);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SalePanel.this, 
                        "Lỗi khi tìm kiếm:\n" + e.getMessage(), 
                        "Lỗi", 
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    isLoading = false;
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        worker.execute();
    }
    
    // Tạo panel giỏ hàng
    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(218, 220, 224), 1),
                "Giỏ hàng",
                0,
                0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(24, 90, 188)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Thông tin khách hàng với UI đẹp
        JPanel customerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        customerPanel.setBackground(Color.WHITE);
        
        JLabel phoneLabel = new JLabel("SĐT Khách hàng:");
        phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        customerPanel.add(phoneLabel);
        
        phoneField = new JTextField(15);
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phoneField.setPreferredSize(new Dimension(150, 32));
        phoneField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 220, 224), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        customerPanel.add(phoneField);
        
        JButton searchCustomerButton = new JButton("Tìm KH");
        styleButton(searchCustomerButton, new Color(66, 133, 244));
        customerPanel.add(searchCustomerButton);
        
        customerNameLabel = new JLabel("(Chưa có khách hàng)");
        customerNameLabel.setForeground(Color.GRAY);
        customerNameLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        customerPanel.add(customerNameLabel);
        
        panel.add(customerPanel, BorderLayout.NORTH);
        
        // Bảng giỏ hàng với UI đẹp
        String[] columns = {"Variant ID", "Sản phẩm", "Size", "Màu", "Số lượng", "Đơn giá", "Thành tiền"};
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Chỉ cho phép sửa cột "Số lượng"
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 4) return Integer.class;
                return String.class;
            }
        };
        
        cartTable = new JTable(cartTableModel);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.setRowHeight(30);
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cartTable.setShowGrid(true);
        cartTable.setGridColor(new Color(218, 220, 224));
        cartTable.setSelectionBackground(new Color(232, 240, 254));
        cartTable.setSelectionForeground(Color.BLACK);
        
        // Header style
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartTable.getTableHeader().setBackground(new Color(248, 249, 250));
        cartTable.getTableHeader().setForeground(new Color(24, 90, 188));
        cartTable.getTableHeader().setPreferredSize(new Dimension(0, 35));
        
        // Ẩn cột Variant ID
        cartTable.getColumnModel().getColumn(0).setMinWidth(0);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(0);
        cartTable.getColumnModel().getColumn(0).setWidth(0);
        
        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(218, 220, 224), 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Nút xóa khỏi giỏ hàng với style đẹp
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton removeButton = new JButton("Xóa");
        removeButton.setBackground(new Color(244, 67, 54));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        removeButton.setPreferredSize(new Dimension(100, 32));
        removeButton.setFocusPainted(false);
        removeButton.setBorderPainted(false);
        removeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        removeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                removeButton.setBackground(new Color(211, 47, 47));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                removeButton.setBackground(new Color(244, 67, 54));
            }
        });
        
        buttonPanel.add(removeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Event handlers
        searchCustomerButton.addActionListener(_ -> searchCustomer());
        removeButton.addActionListener(_ -> removeFromCart());
        
        // Listener cho việc sửa số lượng trong bảng
        cartTableModel.addTableModelListener(e -> {
            // Chỉ tính lại khi user edit số lượng (column 4)
            if (e.getColumn() == 4 && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                if (!isUpdatingTotal) {
                    calculateTotal();
                }
            }
        });
        
        return panel;
    }
    
    // Tạo panel footer - Thanh toán
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Tổng tiền
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel totalTitleLabel = new JLabel("TỔNG TIỀN:");
        totalTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalPanel.add(totalTitleLabel);
        totalLabel = new JLabel("0 đ");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalLabel.setForeground(new Color(244, 67, 54));
        totalPanel.add(totalLabel);
        
        panel.add(totalPanel, BorderLayout.CENTER);
        
        // Các nút chức năng
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        
        JButton clearButton = new JButton("Hủy đơn");
        clearButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clearButton.setBackground(new Color(158, 158, 158));
        clearButton.setForeground(Color.WHITE);
        clearButton.setPreferredSize(new Dimension(100, 35));
        clearButton.setFocusPainted(false);
        clearButton.setBorderPainted(false);
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton paymentButton = new JButton("Thanh toán");
        paymentButton.setBackground(new Color(76, 175, 80)); // Green
        paymentButton.setForeground(Color.WHITE); // Chữ trắng rõ ràng
        paymentButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        paymentButton.setPreferredSize(new Dimension(130, 35));
        paymentButton.setFocusPainted(false);
        paymentButton.setBorderPainted(false);
        paymentButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effects
        clearButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                clearButton.setBackground(new Color(117, 117, 117));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                clearButton.setBackground(new Color(158, 158, 158));
            }
        });
        
        paymentButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                paymentButton.setBackground(new Color(56, 142, 60));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                paymentButton.setBackground(new Color(76, 175, 80));
            }
        });
        
        actionPanel.add(clearButton);
        actionPanel.add(paymentButton);
        
        panel.add(actionPanel, BorderLayout.EAST);
        
        // Event handlers
        clearButton.addActionListener(_ -> clearCart());
        paymentButton.addActionListener(_ -> processPayment());
        
        return panel;
    }
    
    // ============= CÁC PHƯƠNG THỨC XỬ LÝ NGHIỆP VỤ =============
    
    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    private void addToCart() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn sản phẩm muốn thêm vào giỏ!", 
                "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Lấy thông tin sản phẩm từ bảng
        int variantId = (int) productTableModel.getValueAt(selectedRow, 0);
        String skuCode = (String) productTableModel.getValueAt(selectedRow, 1);
        String productName = (String) productTableModel.getValueAt(selectedRow, 2);
        String size = (String) productTableModel.getValueAt(selectedRow, 3);
        String color = (String) productTableModel.getValueAt(selectedRow, 4);
        int stock = (int) productTableModel.getValueAt(selectedRow, 6);
        
        // Kiểm tra tồn kho
        if (stock <= 0) {
            JOptionPane.showMessageDialog(this, 
                "Sản phẩm này đã hết hàng!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Lấy variant từ cache hoặc DB
        Product_Variant variant = variantCache.get(variantId);
        if (variant == null) {
            variant = productService.findVariantBySkuCode(skuCode);
            if (variant != null) {
                variantCache.put(variantId, variant);
            }
        }
        
        if (variant == null) {
            JOptionPane.showMessageDialog(this, 
                "Không tìm thấy thông tin sản phẩm!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Hỏi số lượng với validation
        String quantityStr = (String) JOptionPane.showInputDialog(
            this,
            "Nhập số lượng (Tồn kho: " + stock + "):",
            "Số lượng",
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            "1"
        );
        
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            return; // Người dùng hủy
        }
        
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr.trim());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "Số lượng phải lớn hơn 0!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (quantity > stock) {
                JOptionPane.showMessageDialog(this, 
                    "Số lượng vượt quá tồn kho!\n\n" +
                    "Tồn kho hiện tại: " + stock + "\n" +
                    "Bạn nhập: " + quantity, 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (quantity > 1000) {
                JOptionPane.showMessageDialog(this, 
                    "Số lượng quá lớn!\nTối đa: 1000", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Số lượng không hợp lệ!\nVui lòng nhập số nguyên dương.", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Kiểm tra sản phẩm đã có trong giỏ chưa
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            int existingVariantId = (int) cartTableModel.getValueAt(i, 0);
            if (existingVariantId == variantId) {
                int existingQuantity = (int) cartTableModel.getValueAt(i, 4);
                int newQuantity = existingQuantity + quantity;
                
                if (newQuantity > stock) {
                    JOptionPane.showMessageDialog(this, 
                        "Tổng số lượng vượt quá tồn kho!\n\n" +
                        "Tồn kho: " + stock + "\n" +
                        "Trong giỏ: " + existingQuantity + "\n" +
                        "Muốn thêm: " + quantity + "\n" +
                        "Tổng: " + newQuantity + " (vượt quá!)", 
                        "Lỗi", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Cập nhật số lượng
                cartTableModel.setValueAt(newQuantity, i, 4);
                calculateTotal();
                return; // Đã cập nhật, không thêm dòng mới
            }
        }
        
        // Thêm mới vào giỏ
        BigDecimal unitPrice = variant.getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        
        Object[] row = {
            variantId,
            productName,
            size,
            color,
            quantity,
            String.format("%,.0f đ", unitPrice),
            String.format("%,.0f đ", subtotal)
        };
        
        cartTableModel.addRow(row);
        
        // Tắt tạm thời listener để tránh gọi calculateTotal nhiều lần
        calculateTotal();
        
        // Visual feedback - highlight dòng vừa thêm
        int newRow = cartTableModel.getRowCount() - 1;
        cartTable.setRowSelectionInterval(newRow, newRow);
    }
    
    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    private void removeFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn sản phẩm muốn xóa!", 
                "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn xóa sản phẩm này khỏi giỏ?", 
            "Xác nhận", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            cartTableModel.removeRow(selectedRow);
            calculateTotal();
        }
    }
    
    /**
     * Tìm kiếm khách hàng theo số điện thoại
     */
    private void searchCustomer() {
        String phone = phoneField.getText().trim();
        
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng nhập số điện thoại!", 
                "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return;
        }
        
        // Validation: Kiểm tra định dạng SĐT
        if (!phone.matches("^0\\d{9}$")) {
            JOptionPane.showMessageDialog(this, 
                "Số điện thoại không hợp lệ!\n\n" +
                "Định dạng: 0XXXXXXXXX (10 số, bắt đầu bằng 0)", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            phoneField.requestFocus();
            phoneField.selectAll();
            return;
        }
        
        // Tìm khách hàng
        Customer customer = customerService.findCustomerByPhone(phone);
        
        if (customer != null) {
            currentCustomer = customer;
            customerNameLabel.setText("✓ " + customer.getName());
            customerNameLabel.setForeground(new Color(76, 175, 80));
            JOptionPane.showMessageDialog(this, 
                "Tìm thấy khách hàng: " + customer.getName(), 
                "Thành công", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Khách hàng chưa có, hỏi có muốn tạo mới không
            int option = JOptionPane.showConfirmDialog(this, 
                "Không tìm thấy khách hàng.\nBạn có muốn tạo khách hàng mới?", 
                "Khách hàng mới", 
                JOptionPane.YES_NO_OPTION);
            
            if (option == JOptionPane.YES_OPTION) {
                createNewCustomer(phone);
            }
        }
    }
    
    /**
     * Tạo khách hàng mới
     */
    private void createNewCustomer(String phone) {
        JTextField nameField = new JTextField(20);
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.add(new JLabel("Số điện thoại:"));
        JTextField phoneDisplayField = new JTextField(phone);
        phoneDisplayField.setEditable(false);
        phoneDisplayField.setBackground(Color.LIGHT_GRAY);
        inputPanel.add(phoneDisplayField);
        inputPanel.add(new JLabel("Tên khách hàng:*"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("(* bắt buộc)"));
        inputPanel.add(new JLabel(""));
        
        int result = JOptionPane.showConfirmDialog(this, inputPanel, 
            "Tạo khách hàng mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        
        String name = nameField.getText().trim();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Tên khách hàng không được để trống!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validation: Kiểm tra tên hợp lệ
        if (name.length() < 2 || name.length() > 100) {
            JOptionPane.showMessageDialog(this, 
                "Tên khách hàng phải từ 2-100 ký tự!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Customer newCustomer = new Customer();
        newCustomer.setName(name);
        newCustomer.setPhoneNumber(phone);
        
        boolean success = customerService.registerCustomer(newCustomer);
        
        if (success) {
            // Lấy lại khách hàng vừa tạo để có ID
            currentCustomer = customerService.findCustomerByPhone(phone);
            customerNameLabel.setText("✓ " + name + " (Mới)");
            customerNameLabel.setForeground(new Color(76, 175, 80));
        } else {
            JOptionPane.showMessageDialog(this, 
                "Không thể tạo khách hàng mới!\nSố điện thoại có thể đã tồn tại.", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Tính tổng tiền - OPTIMIZED VERSION
     */
    private void calculateTotal() {
        if (isUpdatingTotal) return; // Tránh infinite loop
        
        isUpdatingTotal = true;
        totalAmount = BigDecimal.ZERO;
        
        try {
            for (int i = 0; i < cartTableModel.getRowCount(); i++) {
                try {
                    int variantId = (int) cartTableModel.getValueAt(i, 0);
                    int quantity = (int) cartTableModel.getValueAt(i, 4);
                    
                    // Validate số lượng
                    if (quantity <= 0) {
                        quantity = 1;
                        cartTableModel.setValueAt(1, i, 4);
                    }
                    
                    // Lấy giá từ cache trước, nếu không có mới query DB
                    Product_Variant variant = variantCache.get(variantId);
                    if (variant == null) {
                        // Fallback: Query từ DB và lưu vào cache
                        String skuCode = getSkuCodeByVariantId(variantId);
                        if (skuCode != null) {
                            variant = productService.findVariantBySkuCode(skuCode);
                            if (variant != null) {
                                variantCache.put(variantId, variant);
                            }
                        }
                    }
                    
                    if (variant != null) {
                        BigDecimal subtotal = variant.getPrice().multiply(BigDecimal.valueOf(quantity));
                        totalAmount = totalAmount.add(subtotal);
                        
                        // Cập nhật cột thành tiền
                        cartTableModel.setValueAt(String.format("%,.0f đ", subtotal), i, 6);
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi tính tổng dòng " + i + ": " + e.getMessage());
                }
            }
            
            totalLabel.setText(String.format("%,.0f đ", totalAmount));
        } finally {
            isUpdatingTotal = false;
        }
    }
    
    /**
     * Lấy SKU code từ variant ID
     */
    private String getSkuCodeByVariantId(int variantId) {
        for (int i = 0; i < productTableModel.getRowCount(); i++) {
            if ((int) productTableModel.getValueAt(i, 0) == variantId) {
                return (String) productTableModel.getValueAt(i, 1);
            }
        }
        return null;
    }
    
    /**
     * Xóa giỏ hàng
     */
    private void clearCart() {
        if (cartTableModel.getRowCount() == 0) {
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn hủy đơn hàng này?", 
            "Xác nhận", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            resetCart();
            JOptionPane.showMessageDialog(this, 
                "Đã hủy đơn hàng!", 
                "Thông báo", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Reset giỏ hàng và form (không có dialog xác nhận)
     */
    private void resetCart() {
        cartTableModel.setRowCount(0);
        currentCustomer = null;
        phoneField.setText("");
        customerNameLabel.setText("(Chưa có khách hàng)");
        customerNameLabel.setForeground(Color.GRAY);
        calculateTotal();
    }
    
    /**
     * Xử lý thanh toán
     */
    private void processPayment() {
        // Kiểm tra giỏ hàng
        if (cartTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Giỏ hàng trống! Vui lòng thêm sản phẩm.", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Kiểm tra khách hàng
        if (currentCustomer == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng nhập thông tin khách hàng!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            phoneField.requestFocus();
            return;
        }
        
        // Xác nhận thanh toán
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Xác nhận thanh toán?\n\n" +
            "Khách hàng: " + currentCustomer.getName() + "\n" +
            "Tổng tiền: " + String.format("%,.0f đ", totalAmount), 
            "Xác nhận thanh toán", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Tạo Order
        Order order = new Order();
        order.setOrderDate(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        order.setUserId(currentUser.getUserId());
        order.setCustomerId(currentCustomer.getCustomerId());
        order.setTotalAmount(totalAmount);
        
        // Tạo danh sách Order_Detail
        List<Order_Detail> orderDetails = new ArrayList<>();
        
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            int variantId = (int) cartTableModel.getValueAt(i, 0);
            int quantity = (int) cartTableModel.getValueAt(i, 4);
            
            // Lấy giá từ cache trước
            Product_Variant variant = variantCache.get(variantId);
            if (variant == null) {
                String skuCode = getSkuCodeByVariantId(variantId);
                if (skuCode != null) {
                    variant = productService.findVariantBySkuCode(skuCode);
                    if (variant != null) {
                        variantCache.put(variantId, variant);
                    }
                }
            }
            
            if (variant == null) {
                JOptionPane.showMessageDialog(this, 
                    "Lỗi: Không tìm thấy thông tin sản phẩm!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Order_Detail detail = new Order_Detail();
            detail.setVariantId(variantId);
            detail.setQuantitySold(quantity);
            detail.setUnitPrice(variant.getPrice());
            
            orderDetails.add(detail);
        }
        
        // Lưu vào database
        boolean success = saleService.createOrder(order, orderDetails);
        
        if (success) {
            JOptionPane.showMessageDialog(this, 
                "Thanh toán thành công!\n\n" +
                "Mã đơn hàng: #" + order.getOrderId() + "\n" +
                "Tổng tiền: " + String.format("%,.0f đ", totalAmount) + "\n\n" +
                "Cảm ơn quý khách!", 
                "Thành công", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Reset form (không cần hỏi xác nhận)
            resetCart();
            variantCache.clear(); // Xóa cache sau khi thanh toán
            loadProducts(); // Reload để cập nhật tồn kho
        } else {
            JOptionPane.showMessageDialog(this, 
                "Thanh toán thất bại!\n\nVui lòng kiểm tra lại thông tin.", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

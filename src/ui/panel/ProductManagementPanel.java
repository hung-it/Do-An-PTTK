package ui.panel;

import model.Product;
import model.Product_Variant;
import service.ProductService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Panel quản lý sản phẩm và biến thể (Product & Product_Variant)
 * Dành cho Admin để quản lý kho hàng
 */
public class ProductManagementPanel extends JPanel {
    
    private final ProductService productService;
    
    private DefaultTableModel productTableModel;
    private DefaultTableModel variantTableModel;
    private JTable productTable;
    private JTable variantTable;
    
    private Product selectedProduct;
    
    public ProductManagementPanel() {
        this.productService = new ProductService();
        initComponents();
        loadProducts();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // === HEADER ===
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(33, 150, 243));
        JLabel titleLabel = new JLabel("QUẢN LÝ SẢN PHẨM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // === MAIN CONTENT - Split Panel ===
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.4);
        
        // Panel sản phẩm (trên)
        JPanel productPanel = createProductPanel();
        splitPane.setTopComponent(productPanel);
        
        // Panel biến thể (dưới)
        JPanel variantPanel = createVariantPanel();
        splitPane.setBottomComponent(variantPanel);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    // ============= PRODUCT PANEL =============
    private JPanel createProductPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(33, 150, 243), 2),
            "Danh sách sản phẩm",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(33, 150, 243)
        ));
        
        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        JButton addProductButton = createStyledButton("Thêm sản phẩm", new Color(76, 175, 80));
        JButton editProductButton = createStyledButton("Sửa", new Color(33, 150, 243));
        JButton deleteProductButton = createStyledButton("Xóa", new Color(244, 67, 54));
        JButton refreshButton = createStyledButton("Làm mới", new Color(158, 158, 158));
        
        toolbar.add(addProductButton);
        toolbar.add(editProductButton);
        toolbar.add(deleteProductButton);
        toolbar.add(refreshButton);
        
        panel.add(toolbar, BorderLayout.NORTH);
        
        // Bảng sản phẩm
        String[] columns = {"ID", "Tên sản phẩm", "Mô tả", "Giá cơ bản"};
        productTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(productTableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setRowHeight(25);
        
        // Listener khi chọn sản phẩm
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadVariantsForSelectedProduct();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Event handlers
        addProductButton.addActionListener(_ -> addProduct());
        editProductButton.addActionListener(_ -> editProduct());
        deleteProductButton.addActionListener(_ -> deleteProduct());
        refreshButton.addActionListener(_ -> loadProducts());
        
        return panel;
    }
    
    // ============= VARIANT PANEL =============
    private JPanel createVariantPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(33, 150, 243), 2),
            "Biến thể (SKU) - Chọn sản phẩm ở trên",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(33, 150, 243)
        ));
        
        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        JButton addVariantButton = createStyledButton("Thêm biến thể", new Color(76, 175, 80));
        JButton editVariantButton = createStyledButton("Sửa", new Color(33, 150, 243));
        JButton deleteVariantButton = createStyledButton("Xóa", new Color(244, 67, 54));
        JButton updateStockButton = createStyledButton("Cập nhật tồn kho", new Color(255, 152, 0));
        
        toolbar.add(addVariantButton);
        toolbar.add(editVariantButton);
        toolbar.add(deleteVariantButton);
        toolbar.add(updateStockButton);
        
        panel.add(toolbar, BorderLayout.NORTH);
        
        // Bảng biến thể
        String[] columns = {"Variant ID", "SKU", "Size", "Màu", "Giá", "Tồn kho"};
        variantTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        variantTable = new JTable(variantTableModel);
        variantTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        variantTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(variantTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Event handlers
        addVariantButton.addActionListener(_ -> addVariant());
        editVariantButton.addActionListener(_ -> editVariant());
        deleteVariantButton.addActionListener(_ -> deleteVariant());
        updateStockButton.addActionListener(_ -> updateStock());
        
        return panel;
    }
    
    // ============= LOAD DATA =============
    
    private void loadProducts() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        SwingWorker<List<Product>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Product> doInBackground() {
                return productService.getAllProducts();
            }
            
            @Override
            protected void done() {
                try {
                    List<Product> products = get();
                    
                    productTableModel.setRowCount(0);
                    
                    for (Product product : products) {
                        Object[] row = {
                            product.getProductId(),
                            product.getName(),
                            product.getDescription(),
                            String.format("%,.0f đ", product.getBasePrice())
                        };
                        productTableModel.addRow(row);
                    }
                    
                    if (products.isEmpty()) {
                        variantTableModel.setRowCount(0);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ProductManagementPanel.this,
                        "Lỗi khi tải danh sách sản phẩm:\n" + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        worker.execute();
    }
    
    private void loadVariantsForSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            variantTableModel.setRowCount(0);
            selectedProduct = null;
            return;
        }
        
        int productId = (int) productTableModel.getValueAt(selectedRow, 0);
        String productName = (String) productTableModel.getValueAt(selectedRow, 1);
        
        // Lưu thông tin sản phẩm đã chọn
        selectedProduct = new Product();
        selectedProduct.setProductId(productId);
        selectedProduct.setName(productName);
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        SwingWorker<List<Product_Variant>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Product_Variant> doInBackground() {
                return productService.getAllVariantsOfProduct(productId);
            }
            
            @Override
            protected void done() {
                try {
                    List<Product_Variant> variants = get();
                    
                    variantTableModel.setRowCount(0);
                    
                    for (Product_Variant variant : variants) {
                        Object[] row = {
                            variant.getVariantId(),
                            variant.getSkuCode(),
                            variant.getSize(),
                            variant.getColor(),
                            String.format("%,.0f đ", variant.getPrice()),
                            variant.getQuantityInStock()
                        };
                        variantTableModel.addRow(row);
                    }
                    
                    // Cập nhật title
                    ((javax.swing.border.TitledBorder) ((JPanel) variantTable.getParent().getParent().getParent())
                        .getBorder()).setTitle("Biến thể của: " + productName);
                    repaint();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ProductManagementPanel.this,
                        "Lỗi khi tải biến thể:\n" + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        worker.execute();
    }
    
    // ============= PRODUCT CRUD =============
    
    private void addProduct() {
        JTextField nameField = new JTextField(30);
        JTextArea descField = new JTextArea(3, 30);
        descField.setLineWrap(true);
        JScrollPane descScroll = new JScrollPane(descField);
        JTextField priceField = new JTextField(15);
        
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Tên sản phẩm:*"), gbc);
        gbc.gridx = 1;
        inputPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        inputPanel.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        inputPanel.add(descScroll, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        inputPanel.add(new JLabel("Giá cơ bản (đ):*"), gbc);
        gbc.gridx = 1;
        inputPanel.add(priceField, gbc);
        
        int result = JOptionPane.showConfirmDialog(this, inputPanel,
            "Thêm Sản phẩm mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        
        String name = nameField.getText().trim();
        String desc = descField.getText().trim();
        String priceStr = priceField.getText().trim();
        
        // Validation
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên sản phẩm không được để trống!",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giá cơ bản không được để trống!",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        BigDecimal basePrice;
        try {
            basePrice = new BigDecimal(priceStr);
            if (basePrice.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Giá phải lớn hơn 0!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá không hợp lệ!\nVui lòng nhập số.",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Tạo Product object
        Product newProduct = new Product(0, name, desc, basePrice);
        
        // Lưu vào database
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        int newId = productService.createProduct(newProduct);
        setCursor(Cursor.getDefaultCursor());
        
        if (newId > 0) {
            JOptionPane.showMessageDialog(this,
                "Thêm sản phẩm thành công!\n\nID: " + newId,
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
            loadProducts(); // Reload table
        } else {
            JOptionPane.showMessageDialog(this,
                "Thêm sản phẩm thất bại!\nVui lòng thử lại.",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần sửa!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Lấy thông tin hiện tại
        int productId = (int) productTableModel.getValueAt(selectedRow, 0);
        String currentName = (String) productTableModel.getValueAt(selectedRow, 1);
        String currentDesc = (String) productTableModel.getValueAt(selectedRow, 2);
        String currentPriceStr = (String) productTableModel.getValueAt(selectedRow, 3);
        
        // Parse giá hiện tại
        BigDecimal currentPrice = new BigDecimal(currentPriceStr.replace(",", "").replace(" đ", ""));
        
        // Form nhập liệu
        JTextField nameField = new JTextField(currentName, 30);
        JTextArea descField = new JTextArea(currentDesc, 3, 30);
        descField.setLineWrap(true);
        JScrollPane descScroll = new JScrollPane(descField);
        JTextField priceField = new JTextField(currentPrice.toString(), 15);
        
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Tên sản phẩm:*"), gbc);
        gbc.gridx = 1;
        inputPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        inputPanel.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        inputPanel.add(descScroll, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        inputPanel.add(new JLabel("Giá cơ bản (đ):*"), gbc);
        gbc.gridx = 1;
        inputPanel.add(priceField, gbc);
        
        int result = JOptionPane.showConfirmDialog(this, inputPanel,
            "Sửa Sản phẩm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        
        String newName = nameField.getText().trim();
        String newDesc = descField.getText().trim();
        String priceStr = priceField.getText().trim();
        
        // Validation
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên sản phẩm không được để trống!",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giá cơ bản không được để trống!",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        BigDecimal newPrice;
        try {
            newPrice = new BigDecimal(priceStr);
            if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Giá phải lớn hơn 0!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá không hợp lệ!\nVui lòng nhập số.",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Tạo Product object với dữ liệu mới
        Product updatedProduct = new Product(productId, newName, newDesc, newPrice);
        
        // Cập nhật vào database
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        boolean success = productService.updateProduct(updatedProduct);
        setCursor(Cursor.getDefaultCursor());
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Cập nhật sản phẩm thành công!",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
            loadProducts(); // Reload table
        } else {
            JOptionPane.showMessageDialog(this,
                "Cập nhật sản phẩm thất bại!\nVui lòng thử lại.",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần xóa!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int productId = (int) productTableModel.getValueAt(selectedRow, 0);
        String productName = (String) productTableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa sản phẩm này?\n\n" +
            "Tên: " + productName + "\n\n" +
            "⚠️ Cảnh báo: Phải xóa tất cả biến thể trước!",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Xóa khỏi database
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        boolean success = productService.deleteProduct(productId);
        setCursor(Cursor.getDefaultCursor());
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Xóa sản phẩm thành công!",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
            loadProducts(); // Reload table
            variantTableModel.setRowCount(0); // Clear variant table
        } else {
            JOptionPane.showMessageDialog(this,
                "Xóa sản phẩm thất bại!\n\n" +
                "Có thể do:\n" +
                "- Sản phẩm còn biến thể chưa xóa\n" +
                "- Có đơn hàng liên quan",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ============= VARIANT CRUD =============
    
    private void addVariant() {
        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn sản phẩm trước!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JTextField skuField = new JTextField(20);
        JTextField sizeField = new JTextField(10);
        JTextField colorField = new JTextField(15);
        JTextField priceField = new JTextField(selectedProduct.getBasePrice().toString(), 15);
        JTextField stockField = new JTextField("0", 10);
        
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        inputPanel.add(new JLabel("Sản phẩm:"));
        JLabel productLabel = new JLabel(selectedProduct.getName());
        productLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        inputPanel.add(productLabel);
        inputPanel.add(new JLabel("Mã SKU:*"));
        inputPanel.add(skuField);
        inputPanel.add(new JLabel("Size:*"));
        inputPanel.add(sizeField);
        inputPanel.add(new JLabel("Màu:*"));
        inputPanel.add(colorField);
        inputPanel.add(new JLabel("Giá (đ):*"));
        inputPanel.add(priceField);
        inputPanel.add(new JLabel("Tồn kho:*"));
        inputPanel.add(stockField);
        
        int result = JOptionPane.showConfirmDialog(this, inputPanel,
            "Thêm Biến thể mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        
        String sku = skuField.getText().trim();
        String size = sizeField.getText().trim();
        String color = colorField.getText().trim();
        String priceStr = priceField.getText().trim();
        String stockStr = stockField.getText().trim();
        
        // Validation
        if (sku.isEmpty() || size.isEmpty() || color.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        BigDecimal price;
        int stock;
        try {
            price = new BigDecimal(priceStr);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Giá phải lớn hơn 0!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá không hợp lệ!",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "Tồn kho không được âm!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Tồn kho không hợp lệ!",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Tạo Product_Variant object
        Product_Variant newVariant = new Product_Variant(0, selectedProduct.getProductId(),
            sku, size, color, stock, price);
        
        // Lưu vào database
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        int newId = productService.createVariant(newVariant);
        setCursor(Cursor.getDefaultCursor());
        
        if (newId > 0) {
            JOptionPane.showMessageDialog(this,
                "Thêm biến thể thành công!",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
            loadVariantsForSelectedProduct(); // Reload variant table
        } else {
            JOptionPane.showMessageDialog(this,
                "Thêm biến thể thất bại!\nKiểm tra mã SKU có bị trùng không.",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editVariant() {
        int selectedRow = variantTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn biến thể cần sửa!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Lấy thông tin hiện tại
        int variantId = (int) variantTableModel.getValueAt(selectedRow, 0);
        String currentSku = (String) variantTableModel.getValueAt(selectedRow, 1);
        String currentSize = (String) variantTableModel.getValueAt(selectedRow, 2);
        String currentColor = (String) variantTableModel.getValueAt(selectedRow, 3);
        String currentPriceStr = (String) variantTableModel.getValueAt(selectedRow, 4);
        int currentStock = (int) variantTableModel.getValueAt(selectedRow, 5);
        
        BigDecimal currentPrice = new BigDecimal(currentPriceStr.replace(",", "").replace(" đ", ""));
        
        // Form nhập liệu
        JTextField skuField = new JTextField(currentSku, 20);
        JTextField sizeField = new JTextField(currentSize, 10);
        JTextField colorField = new JTextField(currentColor, 15);
        JTextField priceField = new JTextField(currentPrice.toString(), 15);
        JTextField stockField = new JTextField(String.valueOf(currentStock), 10);
        
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        inputPanel.add(new JLabel("Sản phẩm:"));
        JLabel productLabel = new JLabel(selectedProduct.getName());
        productLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        inputPanel.add(productLabel);
        inputPanel.add(new JLabel("Mã SKU:*"));
        inputPanel.add(skuField);
        inputPanel.add(new JLabel("Size:*"));
        inputPanel.add(sizeField);
        inputPanel.add(new JLabel("Màu:*"));
        inputPanel.add(colorField);
        inputPanel.add(new JLabel("Giá (đ):*"));
        inputPanel.add(priceField);
        inputPanel.add(new JLabel("Tồn kho:*"));
        inputPanel.add(stockField);
        
        int result = JOptionPane.showConfirmDialog(this, inputPanel,
            "Sửa Biến thể", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        
        String newSku = skuField.getText().trim();
        String newSize = sizeField.getText().trim();
        String newColor = colorField.getText().trim();
        String priceStr = priceField.getText().trim();
        String stockStr = stockField.getText().trim();
        
        // Validation
        if (newSku.isEmpty() || newSize.isEmpty() || newColor.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        BigDecimal newPrice;
        int newStock;
        try {
            newPrice = new BigDecimal(priceStr);
            if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Giá phải lớn hơn 0!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá không hợp lệ!",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            newStock = Integer.parseInt(stockStr);
            if (newStock < 0) {
                JOptionPane.showMessageDialog(this, "Tồn kho không được âm!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Tồn kho không hợp lệ!",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Tạo Product_Variant object với dữ liệu mới
        Product_Variant updatedVariant = new Product_Variant(variantId, selectedProduct.getProductId(),
            newSku, newSize, newColor, newStock, newPrice);
        
        // Cập nhật vào database
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        boolean success = productService.updateVariant(updatedVariant);
        setCursor(Cursor.getDefaultCursor());
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Cập nhật biến thể thành công!",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
            loadVariantsForSelectedProduct(); // Reload variant table
        } else {
            JOptionPane.showMessageDialog(this,
                "Cập nhật biến thể thất bại!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteVariant() {
        int selectedRow = variantTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn biến thể cần xóa!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int variantId = (int) variantTableModel.getValueAt(selectedRow, 0);
        String sku = (String) variantTableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa biến thể này?\n\n" +
            "SKU: " + sku,
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Xóa khỏi database
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        boolean success = productService.deleteVariant(variantId);
        setCursor(Cursor.getDefaultCursor());
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Xóa biến thể thành công!",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
            loadVariantsForSelectedProduct(); // Reload variant table
        } else {
            JOptionPane.showMessageDialog(this,
                "Xóa biến thể thất bại!\n\nCó thể do có đơn hàng liên quan.",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateStock() {
        int selectedRow = variantTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn biến thể cần cập nhật tồn kho!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int variantId = (int) variantTableModel.getValueAt(selectedRow, 0);
        String sku = (String) variantTableModel.getValueAt(selectedRow, 1);
        String size = (String) variantTableModel.getValueAt(selectedRow, 2);
        String color = (String) variantTableModel.getValueAt(selectedRow, 3);
        String priceStr = (String) variantTableModel.getValueAt(selectedRow, 4);
        int currentStock = (int) variantTableModel.getValueAt(selectedRow, 5);
        
        String input = JOptionPane.showInputDialog(this,
            "SKU: " + sku + " (" + size + " - " + color + ")\n" +
            "Tồn kho hiện tại: " + currentStock + "\n\n" +
            "Nhập tồn kho mới:",
            String.valueOf(currentStock));
        
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        
        try {
            int newStock = Integer.parseInt(input.trim());
            if (newStock < 0) {
                JOptionPane.showMessageDialog(this,
                    "Tồn kho không được âm!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Lấy variant hiện tại và cập nhật tồn kho
            BigDecimal price = new BigDecimal(priceStr.replace(",", "").replace(" đ", ""));
            Product_Variant updatedVariant = new Product_Variant(variantId, selectedProduct.getProductId(),
                sku, size, color, newStock, price);
            
            // Cập nhật vào database
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            boolean success = productService.updateVariant(updatedVariant);
            setCursor(Cursor.getDefaultCursor());
            
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Cập nhật tồn kho thành công!\n\n" +
                    "Tồn kho mới: " + newStock,
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
                loadVariantsForSelectedProduct(); // Reload variant table
            } else {
                JOptionPane.showMessageDialog(this,
                    "Cập nhật tồn kho thất bại!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Số lượng không hợp lệ!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
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
        button.setPreferredSize(new Dimension(130, 34));
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

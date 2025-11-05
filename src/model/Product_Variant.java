package model;

import java.math.BigDecimal; // Sử dụng BigDecimal cho giá tiền để tránh lỗi làm tròn

// Đối tượng ánh xạ cho bảng Product_Variant ( Đơn vị tồn kho - SKU)
public class Product_Variant {
    private int variantId;
    private int productId; // Khóa ngoại tham chiếu đến bảng Product
    private String skuCode; // Mã SKU duy nhất
    private String size;    
    private String color;   
    private int quantityInStock; 
    private BigDecimal price; 

    public Product_Variant() {}

    public Product_Variant(int variantId, int productId, String skuCode, String size, String color, int quantityInStock, BigDecimal price) {
        this.variantId = variantId;
        this.productId = productId;
        this.skuCode = skuCode;
        this.size = size;
        this.color = color;
        this.quantityInStock = quantityInStock;
        this.price = price;
    }

    // --- Getters ---
    public int getVariantId() {
        return variantId;
    }
    public int getProductId() {
        return productId;
    }
    public String getSkuCode() {
        return skuCode;
    }
    public String getSize() {
        return size;
    }
    public String getColor() {
        return color;
    }
    public int getQuantityInStock() {
        return quantityInStock;
    }
    public BigDecimal getPrice() {
        return price;
    }

    // --- Setters ---
    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }
    public void setProductId(int productId) {
        this.productId = productId;
    }
    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }
    public void setSize(String size) {
        this.size = size;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}

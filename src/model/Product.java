package model;

import java.math.BigDecimal;

public class Product {
    private int productId;
    private String name;
    private String description;
    private BigDecimal basePrice; // Sử dụng BigDecimal cho giá tiền
    private String imageUrl; // URL hoặc đường dẫn hình ảnh
    
    public Product() {}

    public Product(int productId, String name, String description, BigDecimal basePrice) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
    }
    
    public Product(int productId, String name, String description, BigDecimal basePrice, String imageUrl) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.imageUrl = imageUrl;
    }

    // --- Getters ---
    public int getProductId() {
        return productId;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    // --- Setters ---
    public void setProductId(int productId) {
        this.productId = productId;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice; 
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

package model;

import java.math.BigDecimal;

public class Product {
    private int productId;
    private String name;
    private String description;
    private BigDecimal basePrice; // Sử dụng BigDecimal cho giá tiền
    
    public Product() {}

    public Product(int productId, String name, String description, BigDecimal basePrice) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
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
}

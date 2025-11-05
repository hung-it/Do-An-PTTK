package model;

import java.math.BigDecimal;

public class Order_Detail {
    private int detailId;
    private int orderId;
    private int variantId;
    private int quantitySold;
    private BigDecimal unitPrice; // Sử dụng BigDecimal cho giá tiền

    public Order_Detail() {}

    public Order_Detail(int detailId, int orderId, int variantId, int quantitySold, BigDecimal unitPrice) {
        this.detailId = detailId;
        this.orderId = orderId;
        this.variantId = variantId;
        this.quantitySold = quantitySold;
        this.unitPrice = unitPrice;
    }

    // --- Getters ---
    public int getDetailId() {
        return detailId;
    }
    public int getOrderId() {
        return orderId;
    }
    public int getVariantId() {
        return variantId;
    }
    public int getQuantitySold() {
        return quantitySold;
    }
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    // --- Setters ---
    public void setDetailId(int detailId) {
        this.detailId = detailId;
    }
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }
    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    } 
}

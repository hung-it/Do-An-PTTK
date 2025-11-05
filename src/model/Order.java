package model;

import java.math.BigDecimal;

public class Order {
    private int orderId;
    private String orderDate;
    private int userId;  // Nhân viên xử lý đơn hàng
    private int customerId;
    private BigDecimal totalAmount; // Sử dụng BigDecimal cho giá tiền

    public Order() {}

    public Order(int orderId, String orderDate, int userId, int customerId, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.userId = userId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
    }

    // --- Getters ---
    public int getOrderId() {
        return orderId;
    }
    public String getOrderDate() {
        return orderDate;
    }
    public int getUserId() {
        return userId;
    }
    public int getCustomerId() {
        return customerId;
    }
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    // --- Setters ---
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}

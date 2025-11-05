package model;

import java.sql.Timestamp;

public class Customer {
    private int customerId;
    private String name;
    private String phoneNumber;
    private String username;
    private String password;
    private Timestamp joinDate; // Ngày tham gia

    public Customer() {}

    public Customer(int customerId, String name, String phoneNumber, String username, String password) {
        this.customerId = customerId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.password = password;
    }
    
    public Customer(int customerId, String name, String phoneNumber, String username, String password, Timestamp joinDate) {
        this.customerId = customerId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.password = password;
        this.joinDate = joinDate;
    }

    // --- Getters ---
    public int getCustomerId() {
        return customerId;
    }
    public String getName() {
        return name;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public Timestamp getJoinDate() {
        return joinDate;
    }
    
    // --- Setters ---
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setJoinDate(Timestamp joinDate) {
        this.joinDate = joinDate;
    }
}

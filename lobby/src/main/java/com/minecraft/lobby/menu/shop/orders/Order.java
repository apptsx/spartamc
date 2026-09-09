package com.minecraft.lobby.menu.shop.orders;

public class Order {

    private final String id;
    private final String productName;
    private final double price;
    private String status; // PENDING, PAID, CANCELLED
    private final long timestamp;

    public Order(String productName, double price) {
        this.id = java.util.UUID.randomUUID().toString().substring(0, 8);
        this.productName = productName;
        this.price = price;
        this.status = "PENDING";
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public double getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getStatusColor() {
        switch (status) {
            case "PAID": return "§a";
            case "CANCELLED": return "§c";
            default: return "§e";
        }
    }
}

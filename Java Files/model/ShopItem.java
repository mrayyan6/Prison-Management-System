package com.prison.model;

public class ShopItem {
    private String itemName;
    private double price;
    private String category;
    private String description;
    private boolean phoneCallService;

    public ShopItem(String itemName, double price, String category, String description) {
        this(itemName, price, category, description, false);
    }

    public ShopItem(String itemName, double price, String category, String description, boolean phoneCallService) {
        this.itemName = itemName;
        this.price = price;
        this.category = category;
        this.description = description;
        this.phoneCallService = phoneCallService;
    }

    public String getItemName() {
        return itemName;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPhoneCallService() {
        return phoneCallService;
    }
}
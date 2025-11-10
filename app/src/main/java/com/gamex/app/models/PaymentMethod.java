package com.gamex.app.models;

public class PaymentMethod {
    private String id;
    private String name;
    private String description;
    private int logoResource;
    private boolean available;

    public PaymentMethod(String id, String name, String description, int logoResource, boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.logoResource = logoResource;
        this.available = available;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getLogoResource() {
        return logoResource;
    }

    public boolean isAvailable() {
        return available;
    }
}

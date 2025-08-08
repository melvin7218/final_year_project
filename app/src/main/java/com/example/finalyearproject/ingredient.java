package com.example.finalyearproject;

public class ingredient {
    private String name;
    private String amount;

    // Constructor
    public ingredient(String name, String amount) {
        this.name = name;
        this.amount = amount;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getAmount() {
        return amount;
    }
}

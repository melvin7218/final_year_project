package com.example.finalyearproject;

import java.util.List;

public class RecipeResponse {
    private int id;
    private String title;
    private String description;
    private String time;
    private String image;
    private String cuisine;
    private String dietary;
    private List<String> ingredients;
    private List<String> instructions;

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTime() { return time; }
    public String getImage() { return image; }
    public String getCuisine() { return cuisine; }
    public String getDietary() { return dietary; }
    public List<String> getIngredients() { return ingredients; }
    public List<String> getInstructions() { return instructions; }
}

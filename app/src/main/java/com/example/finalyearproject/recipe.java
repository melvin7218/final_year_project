package com.example.finalyearproject;

import java.util.ArrayList;
import java.util.List;

public class recipe {
    private String id;
    private String name;
    private String imageUrl;
    private String category;
    private int readyInMinutes;
    private int servings;
    private String instructions;
    private boolean isUserRecipe;
    private String visibility;
    private String mealPlanRecipeId;
    private List<String> hadByNames = new ArrayList<>();
    private double calories, protein, fat, carbohydrates;

    // Basic constructor
    public recipe(String id, String name, String imageUrl, String category) {
        this.id = id != null ? id : "";
        this.name = name;
        this.imageUrl = imageUrl;
        this.category = category;
        this.isUserRecipe = false;
        this.visibility = "public";
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }
    public int getReadyInMinutes() { return readyInMinutes; }
    public int getServings() { return servings; }
    public String getInstructions() { return instructions; }
    public boolean isUserRecipe() { return isUserRecipe; }
    public String getMealPlanRecipeId() { return mealPlanRecipeId; }
    public List<String> getHadByNames() { return hadByNames; }
    public String getVisibility() { return visibility; }
    public double getCalories() { return calories; }
    public double getProtein() { return protein; }
    public double getFat() { return fat; }
    public double getCarbohydrates() { return carbohydrates; }

    // Setters
    public void setReadyInMinutes(int readyInMinutes) { this.readyInMinutes = readyInMinutes; }
    public void setServings(int servings) { this.servings = servings; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public void setUserRecipe(boolean isUserRecipe) { this.isUserRecipe = isUserRecipe; }
    public void setMealPlanRecipeId(String mealPlanRecipeId) { this.mealPlanRecipeId = mealPlanRecipeId; }
    public void setHadByNames(List<String> hadByNames) { this.hadByNames = hadByNames; }
    public void setCalories(double calories) { this.calories = calories; }
    public void setProtein(double protein) { this.protein = protein; }
    public void setFat(double fat) { this.fat = fat; }
    public void setCarbohydrates(double carbohydrates) { this.carbohydrates = carbohydrates; }
} 
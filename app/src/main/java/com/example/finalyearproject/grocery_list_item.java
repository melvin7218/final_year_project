package com.example.finalyearproject;

public class grocery_list_item {
    private String ingredientName;
    private String amount;
    private String recipeName;
    private String recipeImage;
    private boolean purchased;

    public grocery_list_item(String ingredientName, String amount, String recipeName, String recipeImage,boolean purchased) {
        this.ingredientName = ingredientName;
        this.amount = amount;
        this.recipeName = recipeName;
        this.recipeImage = recipeImage;
        this.purchased = purchased;
    }

    // Getters
    public String getIngredientName() { return ingredientName; }
    public String getAmount() { return amount; }
    public String getRecipeName() { return recipeName; }
    public String getRecipeImage() { return recipeImage; }
    public boolean isPurchased() { return purchased; }
    public void setPurchased(boolean purchased) { this.purchased = purchased; }
}
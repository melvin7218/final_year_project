package com.example.finalyearproject;

import org.json.JSONException;
import org.json.JSONObject;

public class userPreference {
    private String  allergyIngredients, gender, cuisineType;
    private int age;
    private double height, weight, activityFactor, bmr, tdee;

    public userPreference(String allergyIngredients, String cuisineType, int age, double height,
                          double weight, String gender, double activityFactor){
        this.allergyIngredients = allergyIngredients;
        this.cuisineType = cuisineType;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
        this.activityFactor = activityFactor;
        calculateBMR();
        calculateTDEE();
    }

    private void calculateBMR(){
        if(gender.equalsIgnoreCase("male")){
            bmr = (10*weight) + (6.25*height) -(5*age)+5;
        }else{
            bmr = (10*weight) + (6.25*height) -(5*age)-161;
        }
    }

    private void calculateTDEE(){
        tdee = bmr * activityFactor;
    }


    public String getAllergyIngredients() { return allergyIngredients; }
    public int getAge() { return age; }
    public double getHeight() { return height; }
    public double getWeight() { return weight; }
    public String getGender() { return gender; }
    public double getActivityFactor() { return activityFactor; }
    public double getBMR() { return bmr; }
    public double getTDEE() { return tdee; }
    public String getCuisineType() { return cuisineType;}

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("allergy_ingredients", allergyIngredients);
        json.put("cuisine_type", cuisineType);
        json.put("age", age);
        json.put("height", height);
        json.put("weight", weight);
        json.put("gender", gender);
        json.put("activity_factor", activityFactor);
        json.put("bmr", bmr);
        json.put("tdee", tdee);
        return json;
    }
}

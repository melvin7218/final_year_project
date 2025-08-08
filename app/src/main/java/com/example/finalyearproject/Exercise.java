package com.example.finalyearproject;

import java.util.List;

public class Exercise {
    private String id;
    private String name;
    private double caloriesBurned;
    private double met;
    private String duration;
    private String startTime;
    private String endTime;
    private List<String> hadByNames;
    private boolean isUserExercise;

    public Exercise(String id, String name, double caloriesBurned, double met, String duration, String startTime, String endTime) {
        this.id = id;
        this.name = name;
        this.caloriesBurned = caloriesBurned;
        this.met = met;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
        this.hadByNames = new java.util.ArrayList<>();
        this.isUserExercise = false;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getCaloriesBurned() { return caloriesBurned; }
    public double getMet() { return met; }
    public String getDuration() { return duration; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public List<String> getHadByNames() { return hadByNames; }
    public boolean isUserExercise() { return isUserExercise; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCaloriesBurned(double caloriesBurned) { this.caloriesBurned = caloriesBurned; }
    public void setMet(double met) { this.met = met; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setHadByNames(List<String> hadByNames) { this.hadByNames = hadByNames; }
    public void setUserExercise(boolean userExercise) { isUserExercise = userExercise; }

    // Helper method to get time range
    public String getTimeRange() {
        if (startTime != null && endTime != null) {
            return startTime + " - " + endTime;
        }
        return "";
    }
} 
package com.example.finalyearproject;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class meal_plan_add_recipe extends AppCompatActivity implements MealPlanAddRecipeAdapter.OnRecipeClickListener {

    private RecyclerView recipesRecyclerView;
    private MealPlanAddRecipeAdapter adapter;
    private List<recipe> recipeList;
    private List<recipe> allRecipes; // Store all recipes for filtering
    private RequestQueue requestQueue;
    private String currentCategory = "all";
    private String searchKeyword = "";

    // UI Elements
    private EditText searchButton;
    private Button allSelection;
    private Button myRecipeSelection;
    private Button lowFatSelection;
    private Button favoriteSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan_add_recipe);

        // Initialize Volley
        requestQueue = Volley.newRequestQueue(this);

        // Initialize UI elements
        initializeViews();
        setupClickListeners();

        // Initialize RecyclerView
        recipeList = new ArrayList<>();
        allRecipes = new ArrayList<>(); // Initialize allRecipes
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView);
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MealPlanAddRecipeAdapter(this, recipeList, this);
        recipesRecyclerView.setAdapter(adapter);

        // Load recipes
        loadRecipes();
    }

    private void initializeViews() {
        searchButton = findViewById(R.id.searchButton);
        allSelection = findViewById(R.id.allSelection);
        myRecipeSelection = findViewById(R.id.myRecipeSelection);
        lowFatSelection = findViewById(R.id.lowFatSelection);
        favoriteSelection = findViewById(R.id.favoriteSelection);
    }

    private void setupClickListeners() {
        // Category selection buttons
        allSelection.setOnClickListener(v -> {
            currentCategory = "all";
            updateButtonStates();
            loadRecipes();
        });

        myRecipeSelection.setOnClickListener(v -> {
            currentCategory = "my_recipe";
            updateButtonStates();
            loadRecipes();
        });

        lowFatSelection.setOnClickListener(v -> {
            currentCategory = "low_fat";
            updateButtonStates();
            loadRecipes();
        });

        favoriteSelection.setOnClickListener(v -> {
            currentCategory = "favorite";
            updateButtonStates();
            loadFavoriteRecipes();
        });

        // Real-time search functionality
        searchButton.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Real-time filtering as user types
                searchKeyword = s.toString().trim();
                filterRecipes();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // Not needed
            }
        });

        // Search functionality - trigger search when user presses enter (keep for backward compatibility)
        searchButton.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    searchKeyword = searchButton.getText().toString().trim();
                    filterRecipes();
                    return true;
                }
                return false;
            }
        });
    }

    private void updateButtonStates() {
        // Reset all buttons
        allSelection.setBackgroundResource(R.drawable.button_passthrough_state);
        myRecipeSelection.setBackgroundResource(R.drawable.button_passthrough_state);
        lowFatSelection.setBackgroundResource(R.drawable.button_passthrough_state);
        favoriteSelection.setBackgroundResource(R.drawable.button_passthrough_state);

        // Highlight selected button
        switch (currentCategory) {
            case "all":
                allSelection.setBackgroundResource(R.drawable.black_round);
                break;
            case "my_recipe":
                myRecipeSelection.setBackgroundResource(R.drawable.black_round);
                break;
            case "low_fat":
                lowFatSelection.setBackgroundResource(R.drawable.black_round);
                break;
            case "favorite":
                favoriteSelection.setBackgroundResource(R.drawable.black_round);
                break;
        }
    }

    private void loadRecipes() {
        int userId = sharedPref.getInstance(this).getUserId();
        String url = "http://192.168.0.16/final%20year%20project/retrieve_recipe.php";
        url += "?user_id=" + userId;
        if (!currentCategory.equals("all")) {
            url += "&category=" + currentCategory;
        }
        if (!searchKeyword.isEmpty()) {
            url += "&search=" + searchKeyword;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            allRecipes.clear(); // Clear previous recipes
                            switch (currentCategory) {
                                case "all":
                                    parseRecipeArray(response, "other_users_recipes");
                                    parseRecipeArray(response, "own_recipes");
                                    parseRecipeArray(response, "spoonacular_recipes");
                                    parseRecipeArray(response, "customized_recipes");
                                    break;
                                case "my_recipe":
                                    parseRecipeArray(response, "own_recipes");
                                    break;
                                case "low_fat":
                                    parseRecipeArray(response, "other_users_recipes");
                                    parseRecipeArray(response, "own_recipes");
                                    parseRecipeArray(response, "spoonacular_recipes");
                                    parseRecipeArray(response, "customized_recipes");
                                    // Filter for dietary = 'low fat'
                                    List<recipe> filtered = new ArrayList<>();
                                    for (recipe r : allRecipes) {
                                        if ("low fat".equalsIgnoreCase(getDietaryFromResponse(r.getId(), response))) {
                                            filtered.add(r);
                                        }
                                    }
                                    allRecipes.clear();
                                    allRecipes.addAll(filtered);
                                    break;
                                default:
                                    parseRecipeArray(response, "other_users_recipes");
                                    parseRecipeArray(response, "own_recipes");
                                    parseRecipeArray(response, "spoonacular_recipes");
                                    parseRecipeArray(response, "customized_recipes");
                                    break;
                            }
                            filterRecipes(); // Apply initial filter
                            adapter.notifyDataSetChanged();
                            if (recipeList.isEmpty()) {
                                Toast.makeText(meal_plan_add_recipe.this, 
                                    "No recipes found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("MealPlanAddRecipe", "JSON parsing error: " + e.getMessage());
                            Toast.makeText(meal_plan_add_recipe.this, 
                                "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("MealPlanAddRecipe", "Volley error: " + error.getMessage());
                        Toast.makeText(meal_plan_add_recipe.this, 
                            "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    private void parseRecipeArray(JSONObject response, String arrayKey) throws JSONException {
        if (response.has(arrayKey)) {
            JSONArray recipesArray = response.getJSONArray(arrayKey);
            
            for (int i = 0; i < recipesArray.length(); i++) {
                JSONObject recipeObj = recipesArray.getJSONObject(i);
                
                // Create recipe object with new fields
                recipe recipe = new recipe(
                        String.valueOf(recipeObj.getInt("id")),
                        recipeObj.getString("title"),
                        recipeObj.getString("image_url"),
                        recipeObj.getString("cuisine_type")
                );
                
                // Set additional fields from the new backend
                recipe.setServings(recipeObj.optInt("servings", 1));
                recipe.setReadyInMinutes(parseTimeToMinutes(recipeObj.optString("time_recipe", "0")));
                
                // Set nutrition information
                recipe.setCalories(recipeObj.optDouble("calories", 0));
                recipe.setProtein(recipeObj.optDouble("protein", 0));
                recipe.setFat(recipeObj.optDouble("fat", 0));
                recipe.setCarbohydrates(recipeObj.optDouble("carbohydrates", 0));
                
                // Add to allRecipes list
                allRecipes.add(recipe);
            }
        }
    }

    private int parseTimeToMinutes(String timeRecipe) {
        if (timeRecipe == null || timeRecipe.isEmpty()) {
            return 0;
        }
        
        try {
            // Handle different time formats like "25 mins", "1 Hours", etc.
            String lowerTime = timeRecipe.toLowerCase();
            if (lowerTime.contains("hour") || lowerTime.contains("hr")) {
                // Extract hours
                String[] parts = lowerTime.split("\\s+");
                for (String part : parts) {
                    if (part.matches("\\d+")) {
                        return Integer.parseInt(part) * 60; // Convert to minutes
                    }
                }
            } else if (lowerTime.contains("min")) {
                // Extract minutes
                String[] parts = lowerTime.split("\\s+");
                for (String part : parts) {
                    if (part.matches("\\d+")) {
                        return Integer.parseInt(part);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("MealPlanAddRecipe", "Error parsing time: " + timeRecipe);
        }
        
        return 0;
    }

    // Helper to get dietary from response for a given recipe id
    private String getDietaryFromResponse(String recipeId, JSONObject response) {
        try {
            String[] arrays = {"other_users_recipes", "own_recipes", "spoonacular_recipes", "customized_recipes"};
            for (String arrayKey : arrays) {
                if (response.has(arrayKey)) {
                    JSONArray arr = response.getJSONArray(arrayKey);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        if (String.valueOf(obj.getInt("id")).equals(recipeId)) {
                            return obj.optString("dietary", "");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    // Load favorite recipes from a different endpoint
    private void loadFavoriteRecipes() {
        int userId = sharedPref.getInstance(this).getUserId();
        String url = "http://192.168.0.16/Final%20Year%20Project/retrieve_favorite.php?user_id=" + userId;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            allRecipes.clear(); // Clear previous recipes
                            
                            if (response.getString("status").equals("success")) {
                                JSONArray recipesArray = response.getJSONArray("recipes");
                                for (int i = 0; i < recipesArray.length(); i++) {
                                    JSONObject recipeJson = recipesArray.getJSONObject(i);
                                    
                                    // Create recipe object with basic fields
                                    recipe recipe = new recipe(
                                            recipeJson.getString("id"),
                                            recipeJson.getString("title"),
                                            recipeJson.optString("image_url", ""),
                                            recipeJson.optString("cuisine_type", "Unknown")
                                    );
                                    
                                    // Set additional fields if available
                                    recipe.setServings(recipeJson.optInt("servings", 1));
                                    recipe.setReadyInMinutes(parseTimeToMinutes(recipeJson.optString("time_recipe", "0")));
                                    
                                    // Set nutrition information if available
                                    recipe.setCalories(recipeJson.optDouble("calories", 0));
                                    recipe.setProtein(recipeJson.optDouble("protein", 0));
                                    recipe.setFat(recipeJson.optDouble("fat", 0));
                                    recipe.setCarbohydrates(recipeJson.optDouble("carbohydrates", 0));
                                    
                                    allRecipes.add(recipe);
                                }
                            } else {
                                Toast.makeText(meal_plan_add_recipe.this, 
                                    "Failed to load favorite recipes", Toast.LENGTH_SHORT).show();
                            }
                            
                            filterRecipes(); // Apply initial filter
                            adapter.notifyDataSetChanged();
                            if (recipeList.isEmpty()) {
                                Toast.makeText(meal_plan_add_recipe.this, 
                                    "No favorite recipes found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("MealPlanAddRecipe", "JSON parsing error: " + e.getMessage());
                            Toast.makeText(meal_plan_add_recipe.this, 
                                "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("MealPlanAddRecipe", "Volley error: " + error.getMessage());
                        Toast.makeText(meal_plan_add_recipe.this, 
                            "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    private void filterRecipes() {
        recipeList.clear();
        if (allRecipes.isEmpty()) {
            adapter.notifyDataSetChanged();
            return;
        }

        for (recipe r : allRecipes) {
            boolean matchesSearch = true;

            // Keyword filtering - check if recipe name contains the search keyword
            if (!searchKeyword.isEmpty()) {
                String recipeTitle = r.getName().toLowerCase();
                String keyword = searchKeyword.toLowerCase();
                if (!recipeTitle.contains(keyword)) {
                    matchesSearch = false;
                }
            }

            if (matchesSearch) {
                recipeList.add(r);
            }
        }
        
        adapter.notifyDataSetChanged();
        
        // Show message if no recipes found after filtering
        if (recipeList.isEmpty() && !searchKeyword.isEmpty()) {
            Toast.makeText(this, "No recipes found matching '" + searchKeyword + "'", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRecipeAddClick(recipe recipe) {
        showServingAdjustmentDialog(recipe);
    }

    private void showServingAdjustmentDialog(recipe recipe) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Adjust Recipe Servings");

        // Create custom layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        android.view.View dialogView = inflater.inflate(R.layout.dialog_serving_adjustment, null);
        
        // Initialize views
        TextView recipeNameText = dialogView.findViewById(R.id.recipeName);
        TextView originalServingsText = dialogView.findViewById(R.id.originalServings);
        Spinner servingSpinner = dialogView.findViewById(R.id.servingSpinner);
        TextView nutritionText = dialogView.findViewById(R.id.nutritionText);
        
        // Set recipe name
        recipeNameText.setText(recipe.getName());
        originalServingsText.setText("Original Servings: " + recipe.getServings());
        
        // Setup spinner for serving selection
        Integer[] servings = new Integer[20];
        for (int i = 0; i < 20; i++) {
            servings[i] = i + 1;
        }
        android.widget.ArrayAdapter<Integer> adapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, servings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        servingSpinner.setAdapter(adapter);
        servingSpinner.setSelection(recipe.getServings() - 1);
        
        // Calculate and display nutrition based on selected servings
        android.widget.AdapterView.OnItemSelectedListener spinnerListener = new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                int selectedServings = servings[position];
                double multiplier = (double) selectedServings / recipe.getServings();
                
                double adjustedCalories = recipe.getCalories() * multiplier;
                double adjustedProtein = recipe.getProtein() * multiplier;
                double adjustedFat = recipe.getFat() * multiplier;
                double adjustedCarbs = recipe.getCarbohydrates() * multiplier;
                
                String nutritionInfo = String.format(
                    "Nutrition (for %d servings):\n" +
                    "Calories: %.0f cal\n" +
                    "Protein: %.1fg\n" +
                    "Fat: %.1fg\n" +
                    "Carbs: %.1fg",
                    selectedServings, adjustedCalories, adjustedProtein, adjustedFat, adjustedCarbs
                );
                nutritionText.setText(nutritionInfo);
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        };
        servingSpinner.setOnItemSelectedListener(spinnerListener);
        
        // Trigger initial calculation
        spinnerListener.onItemSelected(null, null, recipe.getServings() - 1, 0);
        
        builder.setView(dialogView);
        builder.setPositiveButton("Save to Meal Plan", (dialog, which) -> {
            int selectedServings = servings[servingSpinner.getSelectedItemPosition()];
            saveRecipeToMealPlan(recipe, selectedServings);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveRecipeToMealPlan(recipe recipe, int servings) {
        int userId = sharedPref.getInstance(this).getUserId();
        String url = "http://192.168.0.16/Final%20Year%20Project/save_meal_plan_recipe.php";
        
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("user_id", userId);
            requestData.put("recipe_id", recipe.getId());
            requestData.put("servings", servings);
            requestData.put("calories", recipe.getCalories() * servings / recipe.getServings());
            requestData.put("protein", recipe.getProtein() * servings / recipe.getServings());
            requestData.put("fat", recipe.getFat() * servings / recipe.getServings());
            requestData.put("carbohydrates", recipe.getCarbohydrates() * servings / recipe.getServings());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, requestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("success")) {
                                Toast.makeText(meal_plan_add_recipe.this, 
                                    "Recipe added to meal plan successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = response.optString("message", "Failed to add recipe");
                                Toast.makeText(meal_plan_add_recipe.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(meal_plan_add_recipe.this, 
                                "Error saving recipe", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(meal_plan_add_recipe.this, 
                            "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        
        requestQueue.add(jsonObjectRequest);
    }
}
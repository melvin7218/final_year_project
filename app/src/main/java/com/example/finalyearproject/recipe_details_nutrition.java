package com.example.finalyearproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class recipe_details_nutrition extends AppCompatActivity {

    private TextView recipeTotalNutrition;
    private LinearLayout overview, ingredient, steps, nutrition;
    private String recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details_nutrition);
        recipeId = getIntent().getStringExtra(RecipeAdapter.EXTRA_RECIPE_ID);
        recipeTotalNutrition = findViewById(R.id.detailRecipeNutritionList);
        fetchRecipeNutrition(recipeId);

        // Get the recipe name from the intent or header
        TextView recipeNameHeader = findViewById(R.id.recipe_name);
        final String recipeName;
        String incomingName = getIntent().getStringExtra("recipe_name");
        if (incomingName != null && !incomingName.isEmpty()) {
            recipeNameHeader.setText(incomingName);
            recipeName = incomingName;
        } else {
            recipeName = recipeNameHeader.getText().toString();
        }

        overview = findViewById(R.id.overview_page);
        overview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(recipe_details_nutrition.this, detail_recipe.class);
                intent.putExtra(RecipeAdapter.EXTRA_RECIPE_ID, recipeId);
                intent.putExtra("recipe_name", recipeName);
                startActivity(intent);
            }
        });

        ingredient = findViewById(R.id.ingredient_page);
        ingredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(recipe_details_nutrition.this, recipe_details_ingredients.class);
                intent.putExtra(RecipeAdapter.EXTRA_RECIPE_ID, recipeId);
                intent.putExtra("recipe_name", recipeName);
                startActivity(intent);
            }
        });

        steps = findViewById(R.id.steps_page);
        steps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(recipe_details_nutrition.this, recipe_details_instruction.class);
                intent.putExtra(RecipeAdapter.EXTRA_RECIPE_ID, recipeId);
                intent.putExtra("recipe_name", recipeName);
                startActivity(intent);
            }
        });

        nutrition = findViewById(R.id.nutrition_page);
        nutrition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(recipe_details_nutrition.this, recipe_details_nutrition.class);
                intent.putExtra(RecipeAdapter.EXTRA_RECIPE_ID, recipeId);
                intent.putExtra("recipe_name", recipeName);
                startActivity(intent);
            }
        });
    }

    private void fetchRecipeNutrition(String recipeId) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);
        String url = "http://192.168.0.130/Final%20Year%20Project/details_recipe.php?id=" + recipeId;
        if (userId != -1) {
            url += "&user_id=" + userId;
        }
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONObject recipe = response.getJSONObject("data");
                            if (recipe.has("nutrition")) {
                                JSONArray nutritionArray = recipe.getJSONArray("nutrition");
                                if (nutritionArray.length() > 0) {
                                    JSONObject nutrition = nutritionArray.getJSONObject(0);
                                    StringBuilder nutritionText = new StringBuilder();
                                    if (nutrition.has("calories")) {
                                        nutritionText.append("Calories: ").append(nutrition.getDouble("calories")).append(" kcal\n");
                                    }
                                    if (nutrition.has("fat")) {
                                        nutritionText.append("Fat: ").append(nutrition.getDouble("fat")).append(" g\n");
                                    }
                                    if (nutrition.has("fiber")) {
                                        nutritionText.append("Fiber: ").append(nutrition.getDouble("fiber")).append(" g\n");
                                    }
                                    if (nutrition.has("carbohydrates")) {
                                        nutritionText.append("Carbohydrates: ").append(nutrition.getDouble("carbohydrates")).append(" g\n");
                                    }
                                    recipeTotalNutrition.setText(nutritionText.toString());
                                } else {
                                    recipeTotalNutrition.setText("No nutrition data available.");
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        recipeTotalNutrition.setText("Error loading nutrition data.");
                    }
                },
                error -> {
                    Log.e("RecipeNutrition", "Error fetching nutrition data", error);
                    recipeTotalNutrition.setText("Error fetching nutrition data.");
                }
        );
        queue.add(jsonObjectRequest);
    }
}
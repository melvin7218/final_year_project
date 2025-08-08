package com.example.finalyearproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.SharedPreferences;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class recipe_details_instruction extends AppCompatActivity {

    private TextView instructionListTextView;
    private String recipeId;
    private LinearLayout overview, ingredient, steps, nutrition;
    private static final String RECIPE_DETAIL_URL = "http://192.168.0.130/Final%20Year%20Project/details_recipe.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details_instruction);

        instructionListTextView = findViewById(R.id.instructionListTextView);
        recipeId = getIntent().getStringExtra(RecipeAdapter.EXTRA_RECIPE_ID);
        if (recipeId != null && !recipeId.isEmpty()) {
            fetchAndDisplayInstructions(recipeId);
        } else {
            instructionListTextView.setText("No recipe ID provided.");
        }

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

        //navigate to other page
        overview = findViewById(R.id.overview_page);
        overview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(recipe_details_instruction.this, detail_recipe.class);
                intent.putExtra(RecipeAdapter.EXTRA_RECIPE_ID, recipeId);
                intent.putExtra("recipe_name", recipeName);
                startActivity(intent);
            }
        });

        ingredient = findViewById(R.id.ingredient_page);
        ingredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(recipe_details_instruction.this, recipe_details_ingredients.class);
                intent.putExtra(RecipeAdapter.EXTRA_RECIPE_ID, recipeId);
                intent.putExtra("recipe_name", recipeName);
                startActivity(intent);
            }
        });

        steps = findViewById(R.id.steps_page);
        steps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(recipe_details_instruction.this, recipe_details_instruction.class);
                intent.putExtra(RecipeAdapter.EXTRA_RECIPE_ID, recipeId);
                intent.putExtra("recipe_name", recipeName);
                startActivity(intent);
            }
        });

        nutrition = findViewById(R.id.nutrition_page);
        nutrition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(recipe_details_instruction.this, recipe_details_nutrition.class);
                intent.putExtra(RecipeAdapter.EXTRA_RECIPE_ID, recipeId);
                intent.putExtra("recipe_name", recipeName);
                startActivity(intent);
            }
        });
    }

    private void fetchAndDisplayInstructions(String recipeId) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);
        String url = RECIPE_DETAIL_URL + "?id=" + recipeId;
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
                            if (recipe.has("instructions") && recipe.get("instructions") instanceof JSONArray) {
                                JSONArray instructions = recipe.getJSONArray("instructions");
                                StringBuilder instructionBuilder = new StringBuilder();
                                for (int i = 0; i < instructions.length(); i++) {
                                    instructionBuilder.append(i + 1)
                                            .append(". ")
                                            .append(instructions.getString(i))
                                            .append("\n\n");
                                }
                                instructionListTextView.setText(instructionBuilder.toString());
                            } else {
                                instructionListTextView.setText("No instructions available.");
                            }
                        } else {
                            instructionListTextView.setText("Failed to load recipe details.");
                        }
                    } catch (JSONException e) {
                        instructionListTextView.setText("Error loading instructions.");
                    }
                },
                error -> instructionListTextView.setText("Error fetching instructions.")
        );
        queue.add(jsonObjectRequest);
    }
}
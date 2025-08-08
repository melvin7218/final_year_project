package com.example.finalyearproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

public class detail_recipe extends AppCompatActivity {

    private static final String User_Recipe_URL = "http://192.168.0.130/Final%20Year%20Project/details_recipe.php";
    private static final String Update_Ingredient_URL = "http://192.168.0.130/Final%20Year%20Project/update_ingredients.php";
    private static final String Nutrition_URL = "http://192.168.0.130/Final%20Year%20Project/nutrition.php";


    private ImageView recipeImage;
    private TextView recipeName, recipeCategory, recipeTime, recipeServings, recipeNameHeader;
    private String recipeId;
    private LinearLayout overview, ingredient, steps, nutrition;
    private boolean isMealDBRecipe;
    private ImageView favoriteImageView;
    private boolean isFavorite = false;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_recipe);

        recipeId = getIntent().getStringExtra(RecipeAdapter.EXTRA_RECIPE_ID);
        if (recipeId == null || recipeId.isEmpty()) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeView();
        // Get userId from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);
        // Setup favorite button
        favoriteImageView = findViewById(R.id.favorite);
        favoriteImageView.setOnClickListener(v -> {
            if (userId == -1) {
                Toast.makeText(this, "Please log in to favorite recipes", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isFavorite) {
                removeFavoriteRecipe(userId, recipeId);
            } else {
                addRecipeToFavorites(userId, recipeId);
            }
        });
        checkIfFavorite(userId, recipeId);
        fetchLocalRecipeDetails(recipeId);

        overview = findViewById(R.id.overview_page);
        overview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(detail_recipe.this, detail_recipe.class);
                intent.putExtra(RecipeAdapter.EXTRA_RECIPE_ID, recipeId);
                startActivity(intent);
            }
        });
        ingredient = findViewById(R.id.ingredient_page);
        ingredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(detail_recipe.this, recipe_details_ingredients.class);
                intent.putExtra(RecipeAdapter.EXTRA_RECIPE_ID, recipeId);
                intent.putExtra("recipe_name", recipeNameHeader.getText().toString());
                startActivity(intent);
            }
        });
        steps = findViewById(R.id.steps_page);
        steps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(detail_recipe.this, recipe_details_instruction.class);
                intent.putExtra(RecipeAdapter.EXTRA_RECIPE_ID, recipeId);
                intent.putExtra("recipe_name", recipeNameHeader.getText().toString());
                startActivity(intent);
            }
        });
        nutrition = findViewById(R.id.nutrition_page);
        nutrition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(detail_recipe.this, recipe_details_nutrition.class);
                intent.putExtra(RecipeAdapter.EXTRA_RECIPE_ID, recipeId);
                intent.putExtra("recipe_name", recipeNameHeader.getText().toString());
                startActivity(intent);
            }
        });
    }

    private void initializeView() {
        recipeImage = findViewById(R.id.detailRecipeImage);

        recipeCategory = findViewById(R.id.detailRecipeCategory);
        recipeTime = findViewById(R.id.detailRecipeTime);
        recipeServings = findViewById(R.id.detailRecipeServings);
        recipeNameHeader = findViewById(R.id.recipe_name);
    }

    private void fetchLocalRecipeDetails(String recipeId) {
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

                            recipeNameHeader.setText(recipe.getString("title"));
                            recipeCategory.setText(recipe.getString("cuisine"));
                            recipeTime.setText(recipe.getString("time"));
                            String servingsText = "N/A";
                            if (recipe.has("servings")) {
                                String servings = recipe.optString("servings", "");
                                if (!servings.isEmpty()) {
                                    servingsText = servings + " servings";
                                }
                            }
                            recipeServings.setText(servingsText);
                            if (!recipe.isNull("image") && !recipe.getString("image").isEmpty()) {
                                Glide.with(this)
                                        .load(recipe.getString("image"))
                                        .placeholder(R.drawable.placeholder_image)
                                        .error(R.drawable.error_image)
                                        .into(recipeImage);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("DetailRecipe", "Error parsing recipe details", e);
                        Toast.makeText(this, "Error loading recipe details", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("DetailRecipe", "Error fetching recipe details", error);
                    Toast.makeText(this, "Error fetching recipe details", Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(jsonObjectRequest);
    }

    private void addRecipeToFavorites(int userId, String recipeId) {
        String url = "http://192.168.0.130/Final%20Year%20Project/add_favorite.php";
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", userId);
            data.put("recipe_id", Integer.parseInt(recipeId));
        } catch (JSONException e) {
            Toast.makeText(this, "Error preparing favorite request", Toast.LENGTH_SHORT).show();
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
            response -> {
                if (response.optString("status").equals("success")) {
                    isFavorite = true;
                    favoriteImageView.setImageResource(R.drawable.selected_favorite_logo);
                    Toast.makeText(this, "Added to favorites!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to add favorite", Toast.LENGTH_SHORT).show();
                }
            },
            error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void removeFavoriteRecipe(int userId, String recipeId) {
        String url = "http://192.168.0.130/Final%20Year%20Project/remove_favorite.php";
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", userId);
            data.put("recipe_id", Integer.parseInt(recipeId));
        } catch (JSONException e) {
            Toast.makeText(this, "Error preparing unfavorite request", Toast.LENGTH_SHORT).show();
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
            response -> {
                if (response.optString("status").equals("success")) {
                    isFavorite = false;
                    favoriteImageView.setImageResource(R.drawable.favorite_logo);
                    Toast.makeText(this, "Removed from favorites!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to remove favorite", Toast.LENGTH_SHORT).show();
                }
            },
            error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void checkIfFavorite(int userId, String recipeId) {
        String url = "http://192.168.0.130/Final%20Year%20Project/check_favorite.php";
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", userId);
            data.put("recipe_id", Integer.parseInt(recipeId));
        } catch (JSONException e) {
            favoriteImageView.setImageResource(R.drawable.favorite_logo);
            isFavorite = false;
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
            response -> {
                if (response.optString("status").equals("success") && response.optBoolean("is_favorite", false)) {
                    isFavorite = true;
                    favoriteImageView.setImageResource(R.drawable.selected_favorite_logo);
                } else {
                    isFavorite = false;
                    favoriteImageView.setImageResource(R.drawable.favorite_logo);
                }
            },
            error -> {
                isFavorite = false;
                favoriteImageView.setImageResource(R.drawable.favorite_logo);
            }
        );
        Volley.newRequestQueue(this).add(request);
    }
}

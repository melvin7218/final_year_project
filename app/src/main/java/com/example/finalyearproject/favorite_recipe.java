package com.example.finalyearproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class favorite_recipe extends AppCompatActivity {

    private RecyclerView recyclerView;
    private recipe_adapter_mealPlan adapter;
    private List<recipe> favoriteRecipeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_recipe);

        recyclerView = findViewById(R.id.favorite_recipes_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoriteRecipeList = new ArrayList<>();
        adapter = new recipe_adapter_mealPlan(this, favoriteRecipeList);
        recyclerView.setAdapter(adapter);

        fetchFavoriteRecipes();
    }

    private void fetchFavoriteRecipes() {
        int userId = sharedPref.getInstance(this).getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Please log in to see your favorite recipes.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://172.16.62.183/Final%20Year%20Project/retrieve_favorite.php?user_id=" + userId;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray recipesArray = response.getJSONArray("recipes");
                            favoriteRecipeList.clear();
                            for (int i = 0; i < recipesArray.length(); i++) {
                                JSONObject recipeJson = recipesArray.getJSONObject(i);
                                String id = recipeJson.getString("id");
                                String title = recipeJson.getString("title");
                                String imageUrl = recipeJson.optString("image_url", "");
                                String category = recipeJson.optString("cuisine_type", "Unknown");

                                recipe recipe = new recipe(id, title, imageUrl, category);
                                favoriteRecipeList.add(recipe);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "Failed to load favorite recipes.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("FavoriteRecipe", "JSON parsing error", e);
                        Toast.makeText(this, "An error occurred while loading recipes.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("FavoriteRecipe", "Volley error", error);
                    Toast.makeText(this, "Failed to connect to the server.", Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonObjectRequest);
    }
}
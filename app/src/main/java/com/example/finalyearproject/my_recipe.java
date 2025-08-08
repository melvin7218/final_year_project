package com.example.finalyearproject;

import android.os.Bundle;

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

public class my_recipe extends AppCompatActivity {

    private RecyclerView recyclerView;
    private recipe_adapter_mealPlan adapter;
    private List<recipe> myRecipeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipe);

        recyclerView = findViewById(R.id.my_recipes_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myRecipeList = new ArrayList<>();
        adapter = new recipe_adapter_mealPlan(this, myRecipeList);
        recyclerView.setAdapter(adapter);

        fetchMyRecipes();
    }

    private void fetchMyRecipes() {
        int userId = sharedPref.getInstance(this).getUserId();
        if (userId == -1) {
            // Not logged in
            return;
        }
        String url = "http://192.168.0.130/Final%20Year%20Project/retrieve_recipe.php?user_id=" + userId;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        myRecipeList.clear();
                        if (response.has("own_recipes")) {
                            JSONArray ownRecipes = response.getJSONArray("own_recipes");
                            for (int i = 0; i < ownRecipes.length(); i++) {
                                JSONObject recipeJson = ownRecipes.getJSONObject(i);
                                recipe recipe = createRecipeFromJson(recipeJson, true);
                                myRecipeList.add(recipe);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    error.printStackTrace();
                }
        );
        queue.add(request);
    }

    private recipe createRecipeFromJson(JSONObject recipeJson, boolean isUserRecipe) throws JSONException {
        String id = recipeJson.getString("id");
        String title = recipeJson.getString("title");
        String imageUrl = recipeJson.optString("image_url", "");
        String category = recipeJson.optString("cuisine_type", "Unknown");
        String time = recipeJson.optString("time_recipe", "N/A");
        String visibility = recipeJson.optString("visibility", "public");

        recipe recipe = new recipe(id, title, imageUrl, category);
        recipe.setReadyInMinutes(parseTimeToMinutes(time));
        recipe.setUserRecipe(isUserRecipe);
        recipe.setVisibility(visibility);
        return recipe;
    }

    private int parseTimeToMinutes(String time) {
        if (time == null || time.isEmpty()) return 0;
        try {
            if (time.contains("hour")) {
                return Integer.parseInt(time.replaceAll("[^0-9]", "")) * 60;
            } else if (time.contains("mins") || time.contains("min")) {
                return Integer.parseInt(time.replaceAll("[^0-9]", ""));
            }
            return 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
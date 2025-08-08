package com.example.finalyearproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class pantry_list extends AppCompatActivity {
    private RecyclerView recyclerView;
    private GroceryListAdapter adapter;
    private ArrayList<grocery_list_item> groceryItems;
    private int userId;
    private ArrayList<grocery_list_item> allItems;
    private ArrayList<grocery_list_item> purchasedItems;
    private ArrayList<grocery_list_item> nonPurchasedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.groceryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        groceryItems = new ArrayList<>();
        allItems = new ArrayList<>();
        purchasedItems = new ArrayList<>();
        nonPurchasedItems = new ArrayList<>();

        adapter = new GroceryListAdapter(this, groceryItems);
        recyclerView.setAdapter(adapter);

        Button groceryList = findViewById(R.id.groceryList);
        groceryList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(pantry_list.this, groceryList.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });
        fetchGroceryList();

        Button recommend_recipe = findViewById(R.id.recommend_recipe);
        recommend_recipe.setOnClickListener(v -> {findRecipe();});



        TextView mainPage = findViewById(R.id.text_menu);
        mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(pantry_list.this, MainScreen.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView mealPlan = findViewById(R.id.meal_plan);
        mealPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(pantry_list.this, meal_plan.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView grocery_list = findViewById(R.id.grocery_logo);
        grocery_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(pantry_list.this, groceryList.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView accountPage = findViewById(R.id.account_setting);
        accountPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(pantry_list.this, user_setting.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });
    }

    private void fetchGroceryList() {
        String url = "http://192.168.0.130/Final%20Year%20Project/get_pantry_list.php?user_id=" + userId;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray data = response.getJSONArray("data");
                            groceryItems.clear();
                            allItems.clear();
                            purchasedItems.clear();
                            nonPurchasedItems.clear();

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject item = data.getJSONObject(i);

                                boolean purchased = item.optInt("purchased", 0) == 1;

                                grocery_list_item groceryItem = new grocery_list_item(
                                        item.getString("ingredient_name"),
                                        item.getString("amount"),
                                        item.optString("recipe_name", ""),
                                        item.optString("recipe_image", ""),
                                        purchased
                                );

                                allItems.add(groceryItem);
                                if (purchased) {
                                    purchasedItems.add(groceryItem);
                                } else {
                                    nonPurchasedItems.add(groceryItem);
                                }
                            }
                            // Show only purchased items in the main list
                            groceryItems.addAll(purchasedItems);
                            adapter.notifyDataSetChanged();

                        } else {
                            Toast.makeText(this, "Failed to load grocery list", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Failed to fetch grocery list", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }

    private String getPurchasedIngredientCSV() {
        StringBuilder builder = new StringBuilder();
        for (grocery_list_item item : purchasedItems) {
            builder.append(item.getIngredientName()).append(",");
        }
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }

    private void findRecipe() {
        if (purchasedItems.isEmpty()) {
            Toast.makeText(this, "No purchased ingredients found", Toast.LENGTH_SHORT).show();
            return;
        }

        String ingredientCsv = getPurchasedIngredientCSV();

        // First try to find recipes that use EXACTLY these ingredients (no more, no less)
        String exactMatchUrl = "https://api.spoonacular.com/recipes/findByIngredients" +
                "?ingredients=" + ingredientCsv +
                "&number=5" +
                "&ranking=2" + // Ranking=2 returns recipes that use ALL the given ingredients
                "&ignorePantry=true" +
                "&apiKey=d4574df105914e0fa1cb3c47bd80ea00";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest exactMatchRequest = new JsonArrayRequest(Request.Method.GET, exactMatchUrl, null,
                response -> {
                    if (response.length() > 0) {
                        // Found exact matches - show these
                        showRecipeDialog(response, "Recipes using exactly your ingredients");
                    } else {
                        // If no exact matches, try to find recipes that use SOME of the ingredients
                        String partialMatchUrl = "https://api.spoonacular.com/recipes/findByIngredients" +
                                "?ingredients=" + ingredientCsv +
                                "&number=5" +
                                "&ranking=1" + // Ranking=1 returns recipes that use SOME ingredients
                                "&ignorePantry=true" +
                                "&apiKey=d4574df105914e0fa1cb3c47bd80ea00";

                        JsonArrayRequest partialMatchRequest = new JsonArrayRequest(Request.Method.GET, partialMatchUrl, null,
                                partialResponse -> {
                                    if (partialResponse.length() > 0) {
                                        showRecipeDialog(partialResponse, "Recipes using some of your ingredients");
                                    } else {
                                        showNoRecipeDialog();
                                    }
                                },
                                error -> {
                                    error.printStackTrace();
                                    Toast.makeText(this, "Failed to find recipes", Toast.LENGTH_SHORT).show();
                                });

                        queue.add(partialMatchRequest);
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Failed to find recipes", Toast.LENGTH_SHORT).show();
                });

        queue.add(exactMatchRequest);
    }

    private void showRecipeDialog(JSONArray recipesJson, String title) {
        List<recipe> recipeList = new ArrayList<>();

        try {
            for (int i = 0; i < recipesJson.length(); i++) {
                JSONObject recipeObj = recipesJson.getJSONObject(i);
                String id = String.valueOf(recipeObj.getInt("id"));
                String name = recipeObj.getString("title");
                String image = recipeObj.getString("image");
                int readyTime = recipeObj.has("readyInMinutes") ? recipeObj.getInt("readyInMinutes") : 0;

                recipe r = new recipe(id, name, image, "Recommended");
                r.setReadyInMinutes(readyTime);
                r.setUserRecipe(false);
                r.setImageUrl(image);
                recipeList.add(r);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.recommend_recipe_based_on_ingredient, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.dialogRecipeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        RecipeAdapter adapter = new RecipeAdapter(this, recipeList);
        recyclerView.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void showNoRecipeDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Recipe Found")
                .setMessage("No recipe can be made with exactly your purchased ingredients.")
                .setPositiveButton("OK", null)
                .show();
    }
}
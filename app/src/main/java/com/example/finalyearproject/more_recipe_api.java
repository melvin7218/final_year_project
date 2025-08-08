package com.example.finalyearproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class more_recipe_api extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private static final String SPOONACULAR_API_KEY = "d4574df105914e0fa1cb3c47bd80ea00";
    private static final String SPOONACULAR_BASE_URL = "https://api.spoonacular.com/recipes/";

    private HashMap<String, List<recipe>> mealDBRecipes = new HashMap<>();
    private LinearLayout cuisineContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_recipe_api);

        initializeViews();
        checkLoginStatus();

        cuisineContainer = findViewById(R.id.cuisineContainer);
        fetchUserPreferencesAndRecipes();

        ImageView groceryIcon = findViewById(R.id.grocery_logo);
        groceryIcon.setOnClickListener(view -> {
            Intent intent = new Intent(more_recipe_api.this, groceryList.class);
            startActivity(intent);
        });
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        ImageView accountImage = findViewById(R.id.account_setting);
        accountImage.setOnClickListener(view -> {
            if (sharedPref.getInstance(this).isLoggedIn()) {
                startActivity(new Intent(more_recipe_api.this, account_page.class));
            } else {
                startActivity(new Intent(more_recipe_api.this, LoginPage.class));
                Toast.makeText(this, "Please login to access account settings", Toast.LENGTH_SHORT).show();
            }
        });

        TextView appTitle = findViewById(R.id.text_menu);
        appTitle.setOnClickListener(v -> startActivity(new Intent(more_recipe_api.this, MainScreen.class)));

        ImageView menuIcon = findViewById(R.id.menu);
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        ImageView addRecipe = findViewById(R.id.add_recipe_logo);
        addRecipe.setOnClickListener(view -> {
            if (sharedPref.getInstance(this).isLoggedIn()) {
                startActivity(new Intent(more_recipe_api.this, add_recipe_page.class));
            } else {
                Toast.makeText(more_recipe_api.this, "Please login to add recipes", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(more_recipe_api.this, LoginPage.class));
            }
        });
    }

    private void checkLoginStatus() {
        sharedPref.getInstance(this).isLoggedIn();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(this, MainScreen.class));
        } else if (id == R.id.nav_logout) {
            sharedPref.getInstance(this).clear();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginPage.class));
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // ✅ Fetch cuisine_type and allergy_ingredients
    private void fetchUserPreferencesAndRecipes() {
        int userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://192.168.0.130/Final%20Year%20Project/get_user_preference.php?user_id=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject data = response.getJSONObject("data");
                            String cuisineType = data.getString("cuisine_type");
                            String allergyIngredients = data.getString("allergy_ingredients");

                            fetchSpoonacularRecipesByCuisine(cuisineType, allergyIngredients);
                        } else {
                            Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing preferences", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Network error loading preferences", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(request);
    }

    private void fetchSpoonacularRecipesByCuisine(String cuisineType, String allergies) {
        cuisineContainer.removeAllViews();

        for (String cuisine : cuisineType.split(",")) {
            fetchSpoonacularRecipes(cuisine.trim(), allergies);
        }
    }

    private void fetchSpoonacularRecipes(String cuisine, String allergies) {
        String url = SPOONACULAR_BASE_URL + "complexSearch?apiKey=" + SPOONACULAR_API_KEY
                + "&number=10&addRecipeInformation=true"
                + "&cuisine=" + cuisine
                + "&intolerances=" + allergies;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        List<recipe> recipes = new ArrayList<>();

                        for (int i = 0; i < Math.min(results.length(), 3); i++) {
                            JSONObject obj = results.getJSONObject(i);
                            recipe rec = new recipe(
                                String.valueOf(obj.getInt("id")),
                                obj.getString("title"),
                                obj.getString("image"),
                                cuisine
                            );
                            recipes.add(rec);
                        }

                        mealDBRecipes.put(cuisine, recipes);
                        displayRecipes();
                    } catch (Exception e) {
                        Log.e("Spoonacular", "Parse error", e);
                    }
                },
                error -> Toast.makeText(this, "API error for cuisine: " + cuisine, Toast.LENGTH_SHORT).show());

        queue.add(request);
    }

    private void displayRecipes() {
        runOnUiThread(() -> {
            cuisineContainer.removeAllViews();

            for (String keyword : mealDBRecipes.keySet()) {
                View categoryView = getLayoutInflater().inflate(R.layout.category_section, cuisineContainer, false);
                TextView categoryTitle = categoryView.findViewById(R.id.categoryTitle);
                RecyclerView recipeList = categoryView.findViewById(R.id.recipeList);

                categoryTitle.setText(keyword.substring(0, 1).toUpperCase() + keyword.substring(1));
                recipeList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

                RecipeAdapter adapter = new RecipeAdapter(this, mealDBRecipes.get(keyword));
                recipeList.setAdapter(adapter);

                cuisineContainer.addView(categoryView);
            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}

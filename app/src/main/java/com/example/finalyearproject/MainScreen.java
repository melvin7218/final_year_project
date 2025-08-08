package com.example.finalyearproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainScreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private static final String SPOONACULAR_API_KEY = "d4574df105914e0fa1cb3c47bd80ea00";
    private static final String SPOONACULAR_BASE_URL = "https://api.spoonacular.com/recipes/";

    private HashMap<String, List<recipe>> mealDBRecipes = new HashMap<>();
    private HashMap<String, List<recipe>> userRecipes = new HashMap<>();

    private LinearLayout cuisineContainer;
    private LinearLayout recipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        initializeViews();
        checkLoginStatus();

        recipeContainer = findViewById(R.id.recipeContainer);
        fetchUserRecipes();

        ImageView groceryIcon = findViewById(R.id.grocery_logo);
        groceryIcon.setOnClickListener(view -> {
            Intent intent = new Intent(MainScreen.this, groceryList.class);
            startActivity(intent);
        });

        Button more_recipe = findViewById(R.id.more_recipe);
        more_recipe .setOnClickListener(view -> {
            Intent intent = new Intent(MainScreen.this, more_recipe_api.class);
            startActivity(intent);
        });

        ImageView mealPlan = findViewById(R.id.meal_plan);
        mealPlan.setOnClickListener(view ->{
            if(sharedPref.getInstance(this).isLoggedIn()){
                Intent intent = new Intent(MainScreen.this, meal_plan.class);
                startActivity(intent);
            }else{
                Toast.makeText(this, "User not login", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainScreen.this, LoginPage.class);
                startActivity(intent);
            }
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
                startActivity(new Intent(MainScreen.this, user_setting.class));
            } else {
                startActivity(new Intent(MainScreen.this, LoginPage.class));
                Toast.makeText(this, "Please login to access account settings", Toast.LENGTH_SHORT).show();
            }
        });

        TextView appTitle = findViewById(R.id.text_menu);
        appTitle.setOnClickListener(v -> startActivity(new Intent(MainScreen.this, MainScreen.class)));

        ImageView menuIcon = findViewById(R.id.menu);
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        ImageView addRecipe = findViewById(R.id.add_recipe_logo);
        addRecipe.setOnClickListener(view -> {
            if (sharedPref.getInstance(this).isLoggedIn()) {
                startActivity(new Intent(MainScreen.this, add_recipe_page.class));
            } else {
                Toast.makeText(MainScreen.this, "Please login to add recipes", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainScreen.this, LoginPage.class));
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
        } else if (id == R.id.nav_favorite) {
            startActivity(new Intent(this, favorite_recipe.class));
        } else if (id == R.id.nav_my_recipe) {
            startActivity(new Intent(this, my_recipe.class));
        } else if (id == R.id.nav_grocery_list) {
            startActivity(new Intent(this, grocery_list_item.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void fetchUserRecipes() {
        int userId = sharedPref.getInstance(this).isLoggedIn() ?
                sharedPref.getInstance(this).getUserId() : -1;

        String url = "http://192.168.0.16/final%20year%20project/retrieve_recipe.php";
        if (userId != -1) {
            url += "?user_id=" + userId;
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        userRecipes.clear();

                        if (response.has("other_users_recipes")) {
                            JSONArray otherUserRecipes = response.getJSONArray("other_users_recipes");
                            List<recipe> publicRecipes = new ArrayList<>();
                            for (int i = 0; i < otherUserRecipes.length(); i++) {
                                JSONObject recipeJson = otherUserRecipes.getJSONObject(i);
                                publicRecipes.add(createRecipeFromJson(recipeJson, false));
                            }
                            if (!publicRecipes.isEmpty()) {
                                userRecipes.put("Community Recipes", publicRecipes);
                            }
                        }

                        if (userId != -1 && response.has("own_recipes")) {
                            JSONArray ownRecipes = response.getJSONArray("own_recipes");
                            List<recipe> myRecipes = new ArrayList<>();
                            for (int i = 0; i < ownRecipes.length(); i++) {
                                JSONObject recipeJson = ownRecipes.getJSONObject(i);
                                myRecipes.add(createRecipeFromJson(recipeJson, true));
                            }
                            if (!myRecipes.isEmpty()) {
                                userRecipes.put("My Recipes", myRecipes);
                            }
                        }

                        if (response.has("customized_recipes")) {
                            JSONArray customized = response.getJSONArray("customized_recipes");
                            List<recipe> customizedList = new ArrayList<>();

                            for (int i = 0; i < customized.length(); i++) {
                                JSONObject recipeJson = customized.getJSONObject(i);
                                String id = recipeJson.getString("id");
                                String title = recipeJson.getString("title");
                                String imageUrl = recipeJson.optString("image_url", "");
                                String category = recipeJson.optString("cuisine_type", "Customized");
                                String time = recipeJson.optString("time_recipe", "N/A");

                                recipe recipe = new recipe(id, title, imageUrl, category);
                                recipe.setReadyInMinutes(parseTimeToMinutes(time));
                                recipe.setUserRecipe(true);
                                customizedList.add(recipe);
                            }

                            if (!customizedList.isEmpty()) {
                                userRecipes.put("Customized Recipes", customizedList);
                            }
                        }

                        if (response.has("spoonacular_recipes")) {
                            JSONArray api = response.getJSONArray("spoonacular_recipes");
                            List<recipe> apiList = new ArrayList<>();

                            for (int i = 0; i < api.length(); i++) {
                                JSONObject recipeJson = api.getJSONObject(i);
                                String id = recipeJson.getString("id");
                                String title = recipeJson.getString("title");
                                String imageUrl = recipeJson.optString("image_url", "");
                                String category = recipeJson.optString("cuisine_type", "Customized");
                                String time = recipeJson.optString("time_recipe", "N/A");

                                recipe recipe = new recipe(id, title, imageUrl, category);
                                recipe.setReadyInMinutes(parseTimeToMinutes(time));
                                recipe.setUserRecipe(true);
                                apiList.add(recipe);
                            }

                            if (!apiList.isEmpty()) {
                                userRecipes.put("Spoonacular Recipes", apiList);
                            }
                        }

                        displayUserRecipes();

                    } catch (JSONException e) {
                        Log.e("RecipeError", "Error parsing recipe data", e);
                        Toast.makeText(this, "Error loading recipes", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("RecipeError", "Error fetching recipes", error);
                    Toast.makeText(this, "Error fetching recipes", Toast.LENGTH_SHORT).show();
                });

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

    private void displayUserRecipes() {
        runOnUiThread(() -> {
            recipeContainer.removeAllViews();

            for (String category : userRecipes.keySet()) {
                View categoryView = getLayoutInflater().inflate(R.layout.category_section, recipeContainer, false);
                TextView categoryTitle = categoryView.findViewById(R.id.categoryTitle);
                RecyclerView recipeRecycler = categoryView.findViewById(R.id.recipeList);

                categoryTitle.setText(category);
                recipeRecycler.setLayoutManager(new LinearLayoutManager(
                        this, LinearLayoutManager.HORIZONTAL, false));

                RecipeAdapter adapter = new RecipeAdapter(this, userRecipes.get(category));
                recipeRecycler.setAdapter(adapter);

                recipeContainer.addView(categoryView);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLoginStatus();
    }
}

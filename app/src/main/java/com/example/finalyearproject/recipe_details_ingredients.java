package com.example.finalyearproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



public class recipe_details_ingredients extends AppCompatActivity {

    private List<ingredient> recipeIngredientsList;
    private LinearLayout overview, ingredient, steps, nutrition;
    private String recipeId;
    private int currentServings = 1;
    private List<String> originalAmounts = new ArrayList<>();
    private LinearLayout ingredientImageListContainer;
    private static final String Update_Ingredient_URL = "http://192.168.0.130/Final%20Year%20Project/update_ingredients.php";
    private boolean ingredientsLoaded = false;
    private static final String INGREDIENT_SUGGEST_URL = "http://192.168.0.130/Final%20Year%20Project/autocomplete_ingredient.php";
    private Spinner servingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details_ingredients);

        servingSpinner = findViewById(R.id.serving_size);
        Integer[] servings = new Integer[10];
        for (int i = 0; i < 10; i++) {
            servings[i] = i + 1;
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                servings
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        servingSpinner.setAdapter(adapter);
        servingSpinner.setSelection(0);
        servingSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                currentServings = servings[position];
                updateIngredientDisplayForServings();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });


        recipeId = getIntent().getStringExtra(RecipeAdapter.EXTRA_RECIPE_ID);
        if (recipeId != null && !recipeId.isEmpty()) {
            retrieveAndDisplayIngredients(recipeId);
        }
        if (recipeId == null || recipeId.isEmpty()) {
            Toast.makeText(this, "did not have recipe id", Toast.LENGTH_SHORT).show();
        }

        ingredientImageListContainer = findViewById(R.id.ingredientImageListContainer);

        Button updateRecipeButton = findViewById(R.id.updateRecipe);
        updateRecipeButton.setOnClickListener(v -> showEditIngredientsDialog());

        Button addIngredientButton = findViewById(R.id.add_ingredient);
        addIngredientButton.setOnClickListener(v -> showAddIngredientDialog());

        Button saveIngredientButton = findViewById(R.id.save_ingredient);
        saveIngredientButton.setOnClickListener(v -> saveIngredientsToDatabase());

        // Set recipe name in header if available
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
                Intent intent = new Intent(recipe_details_ingredients.this, detail_recipe.class);
                intent.putExtra(RecipeAdapter.EXTRA_RECIPE_ID, recipeId);
                intent.putExtra("recipe_name", recipeName);
                startActivity(intent);
            }
        });

        ingredient = findViewById(R.id.ingredient_page);
        ingredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(recipe_details_ingredients.this, recipe_details_ingredients.class);
                intent.putExtra(RecipeAdapter.EXTRA_RECIPE_ID, recipeId);
                intent.putExtra("recipe_name", recipeName);
                startActivity(intent);
            }
        });

        steps = findViewById(R.id.steps_page);
        steps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(recipe_details_ingredients.this, recipe_details_instruction.class);
                intent.putExtra(RecipeAdapter.EXTRA_RECIPE_ID, recipeId);
                intent.putExtra("recipe_name", recipeName);
                startActivity(intent);
            }
        });

        nutrition = findViewById(R.id.nutrition_page);
        nutrition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(recipe_details_ingredients.this, recipe_details_nutrition.class);
                intent.putExtra(RecipeAdapter.EXTRA_RECIPE_ID, recipeId);
                intent.putExtra("recipe_name", recipeName);
                startActivity(intent);
            }
        });
    }


    // Refactored public function to retrieve and display ingredients, following detail_recipe.java logic
    public void retrieveAndDisplayIngredients(String recipeId) {
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
                            // Set spinner to recipe's servings
                            int servingsValue = 1;
                            if (recipe.has("servings")) {
                                String servingsStr = recipe.optString("servings", "1");
                                try {
                                    servingsValue = Integer.parseInt(servingsStr);
                                } catch (NumberFormatException ignored) {}
                            }
                            if (servingsValue >= 1 && servingsValue <= 10) {
                                servingSpinner.setSelection(servingsValue - 1);
                            } else {
                                servingSpinner.setSelection(0);
                            }
                            boolean showUpdatedIngredients = recipe.has("updated_ingredients") &&
                                    recipe.getJSONArray("updated_ingredients").length() > 0;
                            JSONArray ingredients = showUpdatedIngredients ?
                                    recipe.getJSONArray("updated_ingredients") :
                                    recipe.getJSONArray("ingredients");
                            recipeIngredientsList = new ArrayList<>();
                            originalAmounts.clear();
                            for (int i = 0; i < ingredients.length(); i++) {
                                JSONObject ingredient = ingredients.getJSONObject(i);
                                String name = ingredient.getString("ingredient_name");
                                String amount = ingredient.getString("amount");
                                recipeIngredientsList.add(new ingredient(name, amount));
                                originalAmounts.add(amount);
                            }
                            updateIngredientDisplayForServings();
                        } else {
                            Toast.makeText(this, "No ingredients found.", Toast.LENGTH_SHORT).show();
                            ingredientImageListContainer.removeAllViews();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error loading ingredients", Toast.LENGTH_SHORT).show();
                        ingredientImageListContainer.removeAllViews();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error fetching ingredients", Toast.LENGTH_SHORT).show();
                    ingredientImageListContainer.removeAllViews();
                }
        );
        queue.add(jsonObjectRequest);
    }

    private void updateIngredientDisplayForServings() {
        ingredientImageListContainer.removeAllViews();
        if (recipeIngredientsList == null || recipeIngredientsList.isEmpty()) {
            if (ingredientsLoaded) {
                Toast.makeText(this, "No ingredients found.", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        ingredientsLoaded = true;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < recipeIngredientsList.size(); i++) {
            ingredient ing = recipeIngredientsList.get(i);
            String name = ing.getName();
            String originalAmount = originalAmounts.get(i);
            String displayAmount = originalAmount;
            try {
                double baseAmount = Double.parseDouble(originalAmount.replaceAll("[^\\d.]", ""));
                double newAmount = baseAmount * currentServings;
                String unit = originalAmount.replaceAll("[\\d.]+", "").trim();
                displayAmount = String.format("%.2f %s", newAmount, unit).trim();
            } catch (NumberFormatException e) {
                displayAmount = originalAmount + " (not numeric)";
            }
            // Inflate a custom view for ingredient with image
            View ingredientView = inflater.inflate(R.layout.ingredient_item_with_image, ingredientImageListContainer, false);
            TextView nameView = ingredientView.findViewById(R.id.ingredientName);
            TextView amountView = ingredientView.findViewById(R.id.ingredientAmount);
            ImageView imageView = ingredientView.findViewById(R.id.ingredientImage);
            nameView.setText(name);
            amountView.setText(displayAmount);
            String imageUrl = "https://www.themealdb.com/images/ingredients/" + name + ".png";
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(imageView);

            ingredientImageListContainer.addView(ingredientView);
        }
    }

    private void showEditIngredientsDialog() {
        if (recipeIngredientsList == null || recipeIngredientsList.isEmpty()) {
            Toast.makeText(this, "No ingredients to edit.", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Ingredients");
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_edit_ingredients, null);
        LinearLayout container = dialogView.findViewById(R.id.editIngredientsContainer);
        List<EditText> amountEditTexts = new ArrayList<>();
        for (int i = 0; i < recipeIngredientsList.size(); i++) {
            ingredient ing = recipeIngredientsList.get(i);
            View row = inflater.inflate(R.layout.row_edit_ingredient, container, false);
            TextView nameView = row.findViewById(R.id.ingredientName);
            EditText amountEdit = row.findViewById(R.id.ingredientAmountEdit);
            nameView.setText(ing.getName());
            amountEdit.setText(originalAmounts.get(i));
            amountEditTexts.add(amountEdit);
            container.addView(row);
        }
        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            for (int i = 0; i < amountEditTexts.size(); i++) {
                String newAmount = amountEditTexts.get(i).getText().toString();
                originalAmounts.set(i, newAmount);
                recipeIngredientsList.set(i, new ingredient(recipeIngredientsList.get(i).getName(), newAmount));
            }
            updateIngredientDisplayForServings();
            updateIngredientsToDatabase();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateIngredientsToDatabase() {
        JSONArray updateIngredients = new JSONArray();
        for (int i = 0; i < recipeIngredientsList.size(); i++) {
            ingredient ing = recipeIngredientsList.get(i);
            String name = ing.getName();
            String amount = originalAmounts.get(i);
            if (!name.isEmpty()) {
                JSONObject ingObj = new JSONObject();
                try {
                    ingObj.put("ingredient_name", name);

                    // Parse amount into value and unit
                    String amountValueStr = "";
                    String unit = "";
                    if (amount != null && !amount.isEmpty()) {
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^([\\d.]+)\\s*(.*)$").matcher(amount.trim());
                        if (m.find()) {
                            amountValueStr = m.group(1);
                            unit = m.group(2).trim();
                        }
                    }
                    Double amountValue = amountValueStr.isEmpty() ? null : Double.valueOf(amountValueStr);
                    ingObj.put("amount_value", amountValue);
                    ingObj.put("unit", unit);

                    updateIngredients.put(ingObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        JSONObject requestData = new JSONObject();
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", 0);
        int userId = sharedPreferences.getInt("user_id", -1);
        try {
            requestData.put("recipe_id", recipeId);
            requestData.put("user_id", userId);
            requestData.put("ingredients", updateIngredients);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, Update_Ingredient_URL, requestData,
                response -> {
                    Toast.makeText(this, "Ingredients updated!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(this, "Failed to update ingredients", Toast.LENGTH_SHORT).show();
                });
        Volley.newRequestQueue(this).add(request);
    }

    private void showAddIngredientDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Ingredient");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        // Use AutoCompleteTextView for ingredient name
        final AutoCompleteTextView nameInput = new AutoCompleteTextView(this);
        nameInput.setHint("Ingredient Name");
        nameInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        ArrayAdapter<String> suggestionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        nameInput.setAdapter(suggestionAdapter);
        nameInput.setThreshold(1);
        layout.addView(nameInput);

        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fetchIngredientSuggestions(s.toString(), suggestionAdapter);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        final EditText amountInput = new EditText(this);
        amountInput.setHint("Amount (e.g., 100g, 2 tbsp)");
        layout.addView(amountInput);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String amount = amountInput.getText().toString().trim();

            if (name.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Please enter both name and amount.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check for duplicate (case-insensitive)
            for (ingredient ing : recipeIngredientsList) {
                if (ing.getName().equalsIgnoreCase(name)) {
                    Toast.makeText(this, "The ingredient already had", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Add to lists
            recipeIngredientsList.add(new ingredient(name, amount));
            originalAmounts.add(amount);

            updateIngredientDisplayForServings();
            Toast.makeText(this, "Ingredient added.", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void fetchIngredientSuggestions(String query, ArrayAdapter<String> adapter) {
        if (query.length() < 1) return;
        String url = INGREDIENT_SUGGEST_URL + "?q=" + java.net.URLEncoder.encode(query);
        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.GET, url,
                response -> {
                    try {
                        org.json.JSONArray arr = new org.json.JSONArray(response);
                        List<String> suggestions = new ArrayList<>();
                        for (int i = 0; i < arr.length() && i < 3; i++) {
                            suggestions.add(arr.getString(i));
                        }
                        adapter.clear();
                        adapter.addAll(suggestions);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        // Ignore parse errors
                    }
                },
                error -> {
                    // Ignore network errors
                }
        );
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private void saveIngredientsToDatabase() {
    JSONArray ingredientsArray = new JSONArray();
    for (int i = 0; i < recipeIngredientsList.size(); i++) {
        ingredient ing = recipeIngredientsList.get(i);
        String name = ing.getName();
        String amount = originalAmounts.get(i);
        JSONObject ingObj = new JSONObject();
        try {
            ingObj.put("ingredient_name", name);

            // Parse amount into value and unit
            String amountValueStr = "";
            String unit = "";
            if (amount != null && !amount.isEmpty()) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("^([\\d.]+)\\s*(.*)$").matcher(amount.trim());
                if (m.find()) {
                    amountValueStr = m.group(1);
                    unit = m.group(2).trim();
                }
            }
            Double amountValue = amountValueStr.isEmpty() ? null : Double.valueOf(amountValueStr);
            ingObj.put("amount_value", amountValue);
            ingObj.put("unit", unit);

            ingredientsArray.put(ingObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    JSONObject requestData = new JSONObject();
    SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", 0);
    int userId = sharedPreferences.getInt("user_id", -1);
    try {
        requestData.put("recipe_id", recipeId);
        requestData.put("user_id", userId);
        requestData.put("ingredients", ingredientsArray);
    } catch (JSONException e) {
        e.printStackTrace();
    }
    String url = "http://192.168.0.130/Final%20Year%20Project/save_ingredient_changes.php";
    JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.POST, url, requestData,
            response -> {
                try {
                    String status = response.getString("status");
                    String message = response.getString("message");
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            },
            error -> Toast.makeText(this, "Failed to save ingredients", Toast.LENGTH_SHORT).show()
    );
    Volley.newRequestQueue(this).add(request);
}
}
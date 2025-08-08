package com.example.finalyearproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class meal_plan extends AppCompatActivity {

    private RecyclerView mealPlanRecyclerView;
    private LinearLayout weekMealPlanContainer;
    private Spinner weekSelectorSpinner;
    private List<Calendar> weekStartDates = new ArrayList<>();
    private ArrayAdapter<String> weekAdapter;
    private List<JSONObject> userMembers = new ArrayList<>();
    private int selectedMemberId = -1;

    private BottomSheetDialog bottomSheetDialog;
    private List<recipe> recipeList = new ArrayList<>();
    private recipe_adapter_mealPlan_dialog recipe_adapter_mealPlan_dialog;
    private recipe selectedRecipe;
    private int userId;
    private Calendar currentWeekStart;
    private int selectedDayIndex = -1;
    private Button[] dayButtons = new Button[7];
    private FrameLayout caloriesTodayContainer;

    // Add this interface inside the meal_plan class
    public interface RecipesCallback {
        void onRecipesFetched(List<recipe> recipes);
    }

    private class IngredientDisplayItem {
        String name, displayName, imageUrl, amount;
        boolean checked = false;
        IngredientDisplayItem(String name, String displayName, String imageUrl, String amount) {
            this.name = name;
            this.displayName = displayName;
            this.imageUrl = imageUrl;
            this.amount = amount;
        }
    }

    // Adapter for RecyclerView
    private class IngredientCheckboxAdapter extends RecyclerView.Adapter<IngredientCheckboxAdapter.ViewHolder> {
        private final List<IngredientDisplayItem> items;
        IngredientCheckboxAdapter(List<IngredientDisplayItem> items) { this.items = items; }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ingredient_checkbox_item, parent, false);
            return new ViewHolder(v);
        }
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            IngredientDisplayItem item = items.get(position);
            holder.checkBox.setChecked(item.checked);
            holder.nameView.setText(item.displayName + " (" + item.amount + ")");
            Glide.with(holder.imageView.getContext())
                    .load(item.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(holder.imageView);
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> item.checked = isChecked);
            holder.itemView.setOnClickListener(v -> {
                item.checked = !item.checked;
                holder.checkBox.setChecked(item.checked);
            });
        }
        @Override
        public int getItemCount() { return items.size(); }
        public List<IngredientDisplayItem> getSelectedItems() {
            List<IngredientDisplayItem> selected = new ArrayList<>();
            for (IngredientDisplayItem item : items) if (item.checked) selected.add(item);
            return selected;
        }
        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;
            ImageView imageView;
            TextView nameView;
            ViewHolder(View v) {
                super(v);
                checkBox = v.findViewById(R.id.ingredient_checkbox);
                imageView = v.findViewById(R.id.ingredient_image);
                nameView = v.findViewById(R.id.ingredient_name);
            }
        }
    }

    // Adapter for RecyclerView
    // Duplicate IngredientCheckboxAdapter removed here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan);

        // Initialize all UI elements
        weekMealPlanContainer = findViewById(R.id.weekMealPlanContainer);
        weekSelectorSpinner = findViewById(R.id.weekSelectorSpinner);
        mealPlanRecyclerView = findViewById(R.id.mealPlanRecyclerView);

        // Day buttons
        dayButtons[0] = findViewById(R.id.buttonMon);
        dayButtons[1] = findViewById(R.id.buttonTue);
        dayButtons[2] = findViewById(R.id.buttonWed);
        dayButtons[3] = findViewById(R.id.buttonThu);
        dayButtons[4] = findViewById(R.id.buttonFri);
        dayButtons[5] = findViewById(R.id.buttonSat);
        dayButtons[6] = findViewById(R.id.buttonSun);

        // Set default selected day to today (robust for all days, including Sunday)
        Calendar today = Calendar.getInstance();
        // Always set weekStart to the Monday of the current week
        Calendar weekStart = (Calendar) today.clone();
        int dayOfWeek = weekStart.get(Calendar.DAY_OF_WEEK);
        int diffToMonday = (dayOfWeek == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dayOfWeek);
        weekStart.add(Calendar.DATE, diffToMonday);
        int daysDiff = (int) ((today.getTimeInMillis() - weekStart.getTimeInMillis()) / (24 * 60 * 60 * 1000));
        selectedDayIndex = daysDiff;

        // Debug logging
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd EEEE", Locale.getDefault());
        Log.d("MealPlan", "Today: " + sdf.format(today.getTime()) + ", Week Start: " + sdf.format(weekStart.getTime()) + ", Selected Day Index: " + selectedDayIndex);

        // Initialize currentWeekStart before calling updateDayButtonStyles
        currentWeekStart = (Calendar) weekStart.clone();
        updateDayButtonStyles();

        for (int i = 0; i < 7; i++) {
            final int idx = i;
            dayButtons[i].setOnClickListener(v -> {
                selectedDayIndex = idx;
                updateDayButtonStyles();
                displaySelectedDayMealPlan();
            });
        }

        // Set up spinner and currentWeekStart
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        setToStartOfWeek(weekStart); // Use the correct weekStart (Monday)
        weekStartDates.clear();
        List<String> weekLabels = new ArrayList<>();
        Calendar cal = (Calendar) weekStart.clone();
        cal.add(Calendar.WEEK_OF_YEAR, -26); // 26 weeks before
        for (int i = 0; i < 52; i++) {
            Calendar thisWeekStart = (Calendar) cal.clone();
            Calendar weekEnd = (Calendar) thisWeekStart.clone();
            weekEnd.add(Calendar.DATE, 6);
            weekStartDates.add((Calendar) thisWeekStart.clone());
            weekLabels.add("Week of " + displayFormat.format(thisWeekStart.getTime()) + " - " + displayFormat.format(weekEnd.getTime()));
            cal.add(Calendar.WEEK_OF_YEAR, 1);
        }
        weekAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, weekLabels);
        weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekSelectorSpinner.setAdapter(weekAdapter);

        // Find the index of the week containing today
        int defaultIndex = 0;
        for (int i = 0; i < weekStartDates.size(); i++) {
            Calendar ws = weekStartDates.get(i);
            Calendar weekEnd = (Calendar) ws.clone();
            weekEnd.add(Calendar.DATE, 6);
            if (!today.before(ws) && !today.after(weekEnd)) {
                defaultIndex = i;
                break;
            }
        }
        weekSelectorSpinner.setSelection(defaultIndex);
        currentWeekStart = (Calendar) weekStartDates.get(defaultIndex).clone();

        // Set userId
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);

        // Set up spinner listener
        weekSelectorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentWeekStart = (Calendar) weekStartDates.get(position).clone();
                displaySelectedDayMealPlan();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        mealPlanRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        caloriesTodayContainer = findViewById(R.id.caloriesTodayContainer);

        // Fetch members first, then display meal plan
        fetchMembers(() -> {
            displaySelectedDayMealPlan();
        });

        TextView mainPage = findViewById(R.id.text_menu);
        mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(meal_plan.this, MainScreen.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView mealPlan = findViewById(R.id.meal_plan);
        mealPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(meal_plan.this, meal_plan.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView main_screen = findViewById(R.id.home_logo_footer);
        main_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(meal_plan.this, MainScreen.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView grocery_list = findViewById(R.id.grocery_logo);
        grocery_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(meal_plan.this, groceryList.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView accountPage = findViewById(R.id.account_setting);
        accountPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(meal_plan.this, user_setting.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        Button checkGroceryListBtn = findViewById(R.id.check_grocery_list);
        checkGroceryListBtn.setOnClickListener(v -> fetchAndShowGroceryListDialog());

        Button nutrition_analysis = findViewById(R.id.nutrition_analysis);
        nutrition_analysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(meal_plan.this, mealPlan_nutrition_analysis.class);
                intent.putExtra("user_id", userId);
                if (currentWeekStart != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                    String startDate = sdf.format(currentWeekStart.getTime());
                    java.util.Calendar weekEnd = (java.util.Calendar) currentWeekStart.clone();
                    weekEnd.add(java.util.Calendar.DATE, 6);
                    String endDate = sdf.format(weekEnd.getTime());
                    intent.putExtra("start_date", startDate);
                    intent.putExtra("end_date", endDate);
                }
                startActivity(intent);
            }
        });

        Button exercise_page = findViewById(R.id.exercise_page);
        exercise_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(meal_plan.this, com.example.finalyearproject.exercise_page.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });
    }

    private void setToStartOfWeek(Calendar calendar) {
        // Set to Monday as the first day of the week (since buttons are Mon-Sun)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    }

    private void updateDayButtonStyles() {
        // Add null check to prevent NullPointerException
        if (currentWeekStart == null) {
            Log.e("MealPlan", "currentWeekStart is null in updateDayButtonStyles");
            return;
        }
        
        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        for (int i = 0; i < 7; i++) {
            // Calculate the date for this day button
            Calendar dayDate = (Calendar) currentWeekStart.clone();
            dayDate.add(Calendar.DATE, i);

            // Get day number
            String dayNumber = dayFormat.format(dayDate.getTime());
            String dayName = dayNames[i];

            // Create text with day number on top and day name below
            String buttonText = dayNumber + "\n" + dayName;

            if (i == selectedDayIndex) {
                dayButtons[i].setBackgroundColor(getResources().getColor(R.color.primary_blue));
                dayButtons[i].setTextColor(getResources().getColor(R.color.white));
            } else {
                dayButtons[i].setBackgroundColor(getResources().getColor(android.R.color.transparent));
                dayButtons[i].setTextColor(getResources().getColor(R.color.black));
            }

            // Update button text
            dayButtons[i].setText(buttonText);
        }
    }

    private void displaySelectedDayMealPlan() {
        // Add null check to prevent NullPointerException
        if (currentWeekStart == null) {
            Log.e("MealPlan", "currentWeekStart is null in displaySelectedDayMealPlan");
            return;
        }
        
        weekMealPlanContainer.removeAllViews();
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, dd/MM", Locale.getDefault());
        Calendar day = (Calendar) currentWeekStart.clone();
        day.add(Calendar.DATE, selectedDayIndex);
        String dbDate = dbFormat.format(day.getTime());
        String displayDate = displayFormat.format(day.getTime());

        // Debug logging
        Log.d("MealPlan", "Displaying meal plan for: " + dbDate + " (" + displayDate + "), Selected Day Index: " + selectedDayIndex);

        // Create main container
        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(16, 16, 16, 16);

        // Add day title
        TextView dayTitle = new TextView(this);
        dayTitle.setText(displayDate);
        dayTitle.setTextSize(20);
        dayTitle.setTextColor(getResources().getColor(R.color.black));
        dayTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        dayTitle.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        dayTitle.setPadding(0, 0, 0, 16);
        mainContainer.addView(dayTitle);

        // Create three meal categories: Breakfast, Lunch, Dinner
        String[] mealCategories = {"Breakfast", "Lunch", "Dinner"};

        
        for (String category : mealCategories) {
            // Inflate the meal category layout
            View categoryView = LayoutInflater.from(this).inflate(R.layout.mealplan_category_section, mainContainer, false);
            
            // Set category title
            TextView categoryTitle = categoryView.findViewById(R.id.mealCategoryTitle);
            categoryTitle.setText(category);
            
            // Get the content container for this category
            LinearLayout contentContainer = categoryView.findViewById(R.id.mealContentContainer);
            
            // Set up the "ADD SOMETHING TO EAT" button
            TextView addMealButton = categoryView.findViewById(R.id.addMealButton);
            addMealButton.setOnClickListener(v -> {
                Intent intent = new Intent(meal_plan.this, meal_plan_add_recipe.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            });
            
            // Store reference to content container for later population
            categoryView.setTag(contentContainer);
            
            mainContainer.addView(categoryView);
        }

        weekMealPlanContainer.addView(mainContainer);

        // Fetch and display meal plan data for the three categories
        fetchAndDisplayDayMealPlan(dbDate, mainContainer);
        updateCaloriesTodayDisplay(dbDate);
    }

    private void fetchAndDisplayDayMealPlan(String date, LinearLayout mainContainer) {
        String url = "http://192.168.0.16/Final%20Year%20Project/retrieve_meal_plan.php";
        int userId = sharedPref.getInstance(this).getUserId();
        String requestUrl = url + "?user_id=" + userId + "&meal_date=" + date;
        Log.d("MealPlanRequest", "Requesting: " + requestUrl);
        
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                requestUrl,
                null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray mealPlans = response.getJSONArray("meal_plans");
                            List<recipe> breakfastList = new ArrayList<>();
                            List<recipe> lunchList = new ArrayList<>();
                            List<recipe> dinnerList = new ArrayList<>();
                            
                            for (int i = 0; i < mealPlans.length(); i++) {
                                JSONObject mealPlan = mealPlans.getJSONObject(i);
                                JSONArray recipes = mealPlan.getJSONArray("recipes");
                                for (int j = 0; j < recipes.length(); j++) {
                                    JSONObject recipeObj = recipes.getJSONObject(j);
                                    recipe r = new recipe(
                                            recipeObj.getString("recipe_id"),
                                            recipeObj.getString("title"),
                                            recipeObj.optString("image_url", ""),
                                            recipeObj.getString("category")
                                    );
                                    r.setUserRecipe(recipeObj.getInt("is_user_recipe") == 1);
                                    r.setMealPlanRecipeId(recipeObj.optString("meal_plan_recipe_id", null));
                                    
                                    // Parse had_by_names
                                    List<String> hadByNames = new ArrayList<>();
                                    if (recipeObj.has("had_by_names")) {
                                        JSONArray hadByArr = recipeObj.getJSONArray("had_by_names");
                                        for (int k = 0; k < hadByArr.length(); k++) {
                                            hadByNames.add(hadByArr.getString(k));
                                        }
                                    }
                                    r.setHadByNames(hadByNames);
                                    
                                    switch (r.getCategory().toLowerCase()) {
                                        case "breakfast":
                                            breakfastList.add(r);
                                            break;
                                        case "lunch":
                                            lunchList.add(r);
                                            break;
                                        case "dinner":
                                            dinnerList.add(r);
                                            break;
                                    }
                                }
                            }
                            
                            // Display meals in category sections
                            displayMealCategory(breakfastList, mainContainer, 0); // Breakfast
                            displayMealCategory(lunchList, mainContainer, 1);     // Lunch
                            displayMealCategory(dinnerList, mainContainer, 2);    // Dinner
                            
                            // Update nutrition summaries
                            updateNutritionSummaries(mainContainer, breakfastList, lunchList, dinnerList);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("MealPlan", "Error fetching meal plans", error)
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void displayMealCategory(List<recipe> recipes, LinearLayout mainContainer, int categoryIndex) {
        // Get the category view (Breakfast = 0, Lunch = 1, Dinner = 2)
        View categoryView = mainContainer.getChildAt(categoryIndex + 1); // +1 because first child is the day title
        if (categoryView == null) return;
        
        // Set the header color based on category
        LinearLayout categoryHeader = categoryView.findViewById(R.id.categoryHeader);
        int headerColor;
        switch (categoryIndex) {
            case 0: // Breakfast
                headerColor = getResources().getColor(R.color.breakfast_color);
                break;
            case 1: // Lunch
                headerColor = getResources().getColor(R.color.lunch_color);
                break;
            case 2: // Dinner
                headerColor = getResources().getColor(R.color.dinner_color);
                break;
            default:
                headerColor = getResources().getColor(R.color.breakfast_color);
                break;
        }
        categoryHeader.setBackgroundColor(headerColor);
        
        LinearLayout contentContainer = (LinearLayout) categoryView.getTag();
        if (contentContainer == null) return;
        
        // Clear existing meal entries (but keep the "ADD SOMETHING TO EAT" button)
        contentContainer.removeAllViews();
        
        // Add recipe cards
        for (recipe r : recipes) {
            View recipeCardView = LayoutInflater.from(this).inflate(R.layout.meal_plan_recipe_card, contentContainer, false);
            
            // Set recipe name
            TextView recipeName = recipeCardView.findViewById(R.id.recipeName);
            recipeName.setText(r.getName());
            
            // Set serving size (you might want to get this from the recipe data)
            TextView servingSize = recipeCardView.findViewById(R.id.servingSize);
            servingSize.setText("serving size: 1");
            
            // Set "had by" information
            TextView hadBy = recipeCardView.findViewById(R.id.hadByInfo);
            List<String> hadByNames = r.getHadByNames();
            if (hadByNames != null && !hadByNames.isEmpty()) {
                hadBy.setText("had by: " + TextUtils.join(", ", hadByNames));
            } else {
                hadBy.setText("had by: You");
            }
            
            // Load recipe image
            ImageView recipeImage = recipeCardView.findViewById(R.id.recipeImage);
            if (r.getImageUrl() != null && !r.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(r.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(recipeImage);
            } else {
                recipeImage.setImageResource(R.drawable.placeholder_image);
            }
            
            // Set up menu button (three dots)
            ImageView menuButton = recipeCardView.findViewById(R.id.optionsMenu);
            menuButton.setOnClickListener(v -> {
                showMealEntryMenu(r, v);
            });
            
            contentContainer.addView(recipeCardView);
        }
        
        // Add the "ADD SOMETHING TO EAT" button at the end
        TextView addMealButton = new TextView(this);
        addMealButton.setText("ADD SOMETHING TO EAT");
        addMealButton.setTextSize(14);
        addMealButton.setTextColor(getResources().getColor(R.color.black));
        addMealButton.setTypeface(null, android.graphics.Typeface.BOLD);
        addMealButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        addMealButton.setPadding(8, 8, 8, 8);
        addMealButton.setClickable(true);
        addMealButton.setFocusable(true);
        // Properly resolve the selectableItemBackground attribute
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        addMealButton.setBackgroundResource(outValue.resourceId);
        addMealButton.setOnClickListener(v -> {
            Intent intent = new Intent(meal_plan.this, meal_plan_add_recipe.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });
        
        contentContainer.addView(addMealButton);
    }

    private void showMealEntryMenu(recipe r, View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenu().add("Delete Recipe");
        popup.getMenu().add("Edit Recipe");
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Delete Recipe")) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Recipe")
                        .setMessage("Are you sure you want to remove this recipe from the meal plan?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            deleteRecipeFromMealPlan(r);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            } else if (item.getTitle().equals("Edit Recipe")) {
                // Handle edit functionality
                Toast.makeText(this, "Edit functionality coming soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private void deleteRecipeFromMealPlan(recipe r) {
        String url = "http://192.168.0.16/Final%20Year%20Project/delete_meal_plan_recipe.php";
        int userId = sharedPref.getInstance(this).getUserId();
        // You may need to pass meal_date, recipe_id, and category to the backend
        org.json.JSONObject data = new org.json.JSONObject();
        try {
            data.put("user_id", userId);
            data.put("recipe_id", r.getId());
            data.put("category", r.getCategory());
            data.put("id", r.getMealPlanRecipeId());
            // Optionally pass date if needed
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.POST,
                url,
                data,
                response -> {
                    android.widget.Toast.makeText(this, "Recipe removed from meal plan", android.widget.Toast.LENGTH_SHORT).show();
                    displaySelectedDayMealPlan();
                },
                error -> {
                    android.widget.Toast.makeText(this, "Failed to remove recipe", android.widget.Toast.LENGTH_SHORT).show();
                }
        );
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private void initializeHeader() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ImageView menu = findViewById(R.id.menu);
        ImageView accountSetting = findViewById(R.id.account_setting);
        ImageView addRecipeLogo = findViewById(R.id.add_recipe_logo);
        ImageView groceryLogo = findViewById(R.id.grocery_logo);
        TextView textMenu = findViewById(R.id.text_menu);

        // Handle menu drawer toggle
        menu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Navigate to account settings
        accountSetting.setOnClickListener(v -> {
            if (sharedPref.getInstance(this).isLoggedIn()) {
                startActivity(new Intent(this, user_setting.class));
            } else {
                Toast.makeText(this, "Please login to access account settings", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginPage.class));
            }
        });

        // Navigate to Add Recipe
        addRecipeLogo.setOnClickListener(v -> {
            if (sharedPref.getInstance(this).isLoggedIn()) {
                startActivity(new Intent(this, add_recipe_page.class));
            } else {
                Toast.makeText(this, "Please login to add recipes", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginPage.class));
            }
        });

        // Navigate to Grocery List
        groceryLogo.setOnClickListener(v -> startActivity(new Intent(this, groceryList.class)));

        // Return to Main Menu
        textMenu.setOnClickListener(v -> startActivity(new Intent(this, MainScreen.class)));
    }

    private void showMealPlanDialog() {
        // Fetch members first, then show dialog
        fetchMembers(() -> {
            bottomSheetDialog = new BottomSheetDialog(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.meal_plan, null);
            bottomSheetDialog.setContentView(dialogView);

            Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
            TextView selectedRecipeText = dialogView.findViewById(R.id.selectedRecipeText);
            RecyclerView recipeRecyclerView = dialogView.findViewById(R.id.recipeRecyclerView);
            Button selectRecipeButton = dialogView.findViewById(R.id.selectRecipeButton);
            Button confirmButton = dialogView.findViewById(R.id.confirmButton);
            LinearLayout memberCheckboxContainer = dialogView.findViewById(R.id.memberCheckboxContainer);
            List<CheckBox> memberCheckBoxes = new ArrayList<>();
            memberCheckboxContainer.removeAllViews();
            for (int i = 0; i < userMembers.size(); i++) {
                JSONObject member = userMembers.get(i);
                String memberName = member.optString("member_name", "");
                int memberId = member.optInt("member_id", -1);
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(memberName);
                checkBox.setTag(memberId);
                memberCheckboxContainer.addView(checkBox);
                memberCheckBoxes.add(checkBox);
            }
            // Show dialog immediately
            bottomSheetDialog.show();

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.meal_category, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);

            recipeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            recipe_adapter_mealPlan_dialog = new recipe_adapter_mealPlan_dialog(this, recipeList, selected -> {
                selectedRecipe = selected;
                selectedRecipeText.setText("Selected: " + selected.getName());
                selectedRecipeText.setVisibility(View.VISIBLE);
                recipeRecyclerView.setVisibility(View.GONE);
            });
            recipeRecyclerView.setAdapter(recipe_adapter_mealPlan_dialog);

            fetchRecipes();

            selectRecipeButton.setOnClickListener(v -> {
                recipeRecyclerView.setVisibility(recipeRecyclerView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            });

            confirmButton.setOnClickListener(v -> {
                if (selectedRecipe == null) {
                    Toast.makeText(meal_plan.this, "Please select a recipe.", Toast.LENGTH_SHORT).show();
                } else {
                    String selectedCategory = categorySpinner.getSelectedItem().toString();
                    if (selectedCategory.isEmpty()) {
                        Toast.makeText(meal_plan.this, "Please select a meal category.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Gather checked member IDs
                        List<Integer> checkedMemberIds = new ArrayList<>();
                        for (CheckBox cb : memberCheckBoxes) {
                            if (cb.isChecked()) {
                                checkedMemberIds.add((Integer) cb.getTag());
                            }
                        }
                        if (!checkedMemberIds.isEmpty()) {
                            // User + members
                            saveMealPlan(selectedRecipe, selectedCategory, checkedMemberIds);
                        } else {
                            // User only
                            saveMealPlanUserOnly(selectedRecipe, selectedCategory);
                        }
                        bottomSheetDialog.dismiss();
                    }
                }
            });
        });
    }

    private void fetchRecipes() {
        String url = "http://192.168.0.16/Final%20Year%20Project/retrieve_recipe.php";
        int userId = sharedPref.getInstance(this).getUserId();
        url += "?user_id=" + userId;

        Volley.newRequestQueue(this).add(new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        recipeList.clear();
                        if (response.has("own_recipes")) {
                            JSONArray ownRecipes = response.getJSONArray("own_recipes");
                            for (int i = 0; i < ownRecipes.length(); i++) {
                                recipeList.add(createRecipeFromJson(ownRecipes.getJSONObject(i), true));
                            }
                        }
                        if (response.has("other_users_recipes")) {
                            JSONArray otherRecipes = response.getJSONArray("other_users_recipes");
                            for (int i = 0; i < otherRecipes.length(); i++) {
                                recipeList.add(createRecipeFromJson(otherRecipes.getJSONObject(i), false));
                            }
                        }
                        recipe_adapter_mealPlan_dialog.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("MealPlan", "Parse error", e);
                    }
                },
                error -> Log.e("MealPlan", "Fetch error", error)
        ));
    }

    private recipe createRecipeFromJson(JSONObject json, boolean isUser) throws JSONException {
        String id = json.getString("id");
        String title = json.getString("title");
        String image = json.optString("image_url", "");
        String category = json.optString("cuisine_type", "Unknown");
        String time = json.optString("time_recipe", "0");
        recipe r = new recipe(id, title, image, category);
        r.setUserRecipe(isUser);
        r.setReadyInMinutes(parseTimeToMinutes(time));
        return r;
    }

    private int parseTimeToMinutes(String time) {
        if (time == null || time.isEmpty()) return 0;
        try {
            if (time.contains("hour")) return Integer.parseInt(time.replaceAll("[^0-9]", "")) * 60;
            if (time.contains("min")) return Integer.parseInt(time.replaceAll("[^0-9]", ""));
        } catch (Exception ignored) {
        }
        return 0;
    }

    private void saveMealPlan(recipe recipe, String category, List<Integer> memberIds) {
        String url = "http://192.168.0.16/Final%20Year%20Project/save_meal_plan.php";
        int userId = sharedPref.getInstance(this).getUserId();

        // Calculate the selected day's date by adding selectedDayIndex to week start
        Calendar selectedDay = (Calendar) currentWeekStart.clone();
        selectedDay.add(Calendar.DATE, selectedDayIndex);
        String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDay.getTime());
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", userId);
            data.put("recipe_id", recipe.getId());
            data.put("category", category);
            data.put("meal_date", selectedDate);
            // Only add member_ids if a member is selected
            if (!memberIds.isEmpty()) {
                JSONArray memberIdsArray = new JSONArray();
                for (Integer memberId : memberIds) {
                    memberIdsArray.put(memberId);
                }
                data.put("member_ids", memberIdsArray);
            }
            // --- Auto-calculate portion multipliers ---
            JSONObject portionMultipliers = new JSONObject();
            // Add user portion
            double userPortion = 1.0;
            try {
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                int userAge = prefs.getInt("age", 25);
                double userTdee = Double.longBitsToDouble(prefs.getLong("tdee", Double.doubleToLongBits(2000.0)));
                userPortion = userTdee / 2000.0;
                if (userAge < 18) userPortion *= 1.2;
                else if (userAge > 65) userPortion *= 0.9;
                userPortion = Math.max(0.5, Math.min(2.0, userPortion));
            } catch (Exception ignored) {}
            portionMultipliers.put("user", userPortion);
            // Add member portion if a member is selected
            if (!memberIds.isEmpty()) {
                for (JSONObject member : userMembers) {
                    if (memberIds.contains(member.optInt("member_id", -1))) {
                        double tdee = member.optDouble("tdee", 2000.0);
                        int age = member.optInt("age", 25);
                        double portion = tdee / 2000.0;
                        if (age < 18) portion *= 1.2;
                        else if (age > 65) portion *= 0.9;
                        portion = Math.max(0.5, Math.min(2.0, portion));
                        portionMultipliers.put(String.valueOf(member.optInt("member_id", -1)), portion);
                    }
                }
            }
            data.put("portion_multipliers", portionMultipliers);
            // --- End auto-calc ---
            // Show the JSON data being sent in a toast message
            String jsonString = data.toString();
            Toast.makeText(this, "Sending JSON: " + jsonString, Toast.LENGTH_LONG).show();
            Log.d("MealPlanJSON", "Sending: " + jsonString);
        } catch (JSONException e) {
            Log.e("MealPlan", "JSON build error", e);
            Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                response -> {
                    try {
                        String status = response.optString("status", "").trim();
                        if ("success".equalsIgnoreCase(status)) {
                            Toast.makeText(this, "Meal saved!", Toast.LENGTH_SHORT).show();
                            displaySelectedDayMealPlan();
                        } else {
                            String message = response.optString("message", "Unknown backend error");
                            Toast.makeText(this, "Save failed: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    // New method for user only
    private void saveMealPlanUserOnly(recipe recipe, String category) {
        String url = "http://192.168.0.16/Final%20Year%20Project/save_meal_plan_user.php";
        int userId = sharedPref.getInstance(this).getUserId();

        // Calculate the selected day's date by adding selectedDayIndex to week start
        Calendar selectedDay = (Calendar) currentWeekStart.clone();
        selectedDay.add(Calendar.DATE, selectedDayIndex);
        String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDay.getTime());

        // Debug logging
        Log.d("MealPlan", "saveMealPlanUserOnly - Saving for selected day: " + selectedDate + " (Day index: " + selectedDayIndex + ")");

        JSONObject data = new JSONObject();
        try {
            data.put("user_id", userId);
            data.put("recipe_id", recipe.getId());
            data.put("category", category);
            data.put("meal_date", selectedDate);

            // --- Auto-calculate portion multipliers for user only ---
            JSONObject portionMultipliers = new JSONObject();
            // Add user portion
            double userPortion = 1.0;
            try {
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                int userAge = prefs.getInt("age", 25);
                double userTdee = Double.longBitsToDouble(prefs.getLong("tdee", Double.doubleToLongBits(2000.0)));
                userPortion = userTdee / 2000.0;
                if (userAge < 18) userPortion *= 1.2;
                else if (userAge > 65) userPortion *= 0.9;
                userPortion = Math.max(0.5, Math.min(2.0, userPortion));
            } catch (Exception ignored) {}
            portionMultipliers.put("user", userPortion);
            data.put("portion_multipliers", portionMultipliers);
            // --- End auto-calc ---

            // Show the JSON data being sent in a toast message
            String jsonString = data.toString();
            Toast.makeText(this, "Sending JSON: " + jsonString, Toast.LENGTH_LONG).show();
            Log.d("MealPlanJSON", "Sending: " + jsonString);
        } catch (JSONException e) {
            Log.e("MealPlan", "JSON build error", e);
            Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                response -> {
                    try {
                        String status = response.optString("status", "").trim();
                        if ("success".equalsIgnoreCase(status)) {
                            Toast.makeText(this, "Meal saved!", Toast.LENGTH_SHORT).show();
                            displaySelectedDayMealPlan();
                        } else {
                            String message = response.optString("message", "Unknown backend error");
                            Toast.makeText(this, "Save failed: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show()
        );
        Volley.newRequestQueue(this).add(request);
    }


    private void showEditDayDialog(String dbDate, String displayDate) {
        // Allow editing even without member selection - member will be selected in dialog
        // Fetch recipes for the day
        String url = "http://192.168.0.16/Final%20Year%20Project/retrieve_meal_plan.php";
        int userId = sharedPref.getInstance(this).getUserId();
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url + "?user_id=" + userId + "&member_id=" + selectedMemberId + "&meal_date=" + dbDate,
                null,
                response -> {
                    List<recipe> breakfastList = new ArrayList<>();
                    List<recipe> lunchList = new ArrayList<>();
                    List<recipe> dinnerList = new ArrayList<>();
                    try {
                        if (response.has("status") && response.getString("status").equals("success")) {
                            JSONArray mealPlans = response.getJSONArray("meal_plans");
                            for (int i = 0; i < mealPlans.length(); i++) {
                                JSONObject mealPlan = mealPlans.getJSONObject(i);
                                JSONArray recipes = mealPlan.getJSONArray("recipes");
                                for (int j = 0; j < recipes.length(); j++) {
                                    JSONObject recipeObj = recipes.getJSONObject(j);
                                    recipe r = new recipe(
                                            recipeObj.getString("recipe_id"),
                                            recipeObj.getString("title"),
                                            recipeObj.optString("image_url", ""),
                                            recipeObj.getString("category")
                                    );
                                    r.setUserRecipe(recipeObj.getInt("is_user_recipe") == 1);
                                    r.setMealPlanRecipeId(recipeObj.optString("meal_plan_recipe_id", null));
                                    switch (r.getCategory().toLowerCase()) {
                                        case "breakfast":
                                            breakfastList.add(r);
                                            break;
                                        case "lunch":
                                            lunchList.add(r);
                                            break;
                                        case "dinner":
                                            dinnerList.add(r);
                                            break;
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("MealPlan", "Error parsing JSON", e);
                    }
                    // Always show the dialog, even if lists are empty
                    showDayRecipeDialog(dbDate, displayDate, breakfastList, lunchList, dinnerList);
                },
                error -> {
                    Log.e("MealPlan", "Error fetching meal plans", error);
                    // Show dialog with empty lists on error
                    showDayRecipeDialog(dbDate, displayDate, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void showDayRecipeDialog(String dbDate, String displayDate, List<recipe> breakfastList, List<recipe> lunchList, List<recipe> dinnerList) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.mealplan_edit_day_dialog, null);
        dialog.setContentView(dialogView);
        TextView dialogTitle = dialogView.findViewById(R.id.dialogDayTitle);
        dialogTitle.setText("Edit Meal Plan for " + displayDate);
        LinearLayout breakfastContainer = dialogView.findViewById(R.id.breakfastEditContainer);
        LinearLayout lunchContainer = dialogView.findViewById(R.id.lunchEditContainer);
        LinearLayout dinnerContainer = dialogView.findViewById(R.id.dinnerEditContainer);
        Button addRecipeBtn = dialogView.findViewById(R.id.addRecipeBtn);
        // Display current recipes
        displayMealCategory(breakfastList, breakfastContainer, 0);
        displayMealCategory(lunchList, lunchContainer, 1);
        displayMealCategory(dinnerList, dinnerContainer, 2);
        // Add button logic (reuse showMealPlanDialog selection logic)
        addRecipeBtn.setOnClickListener(v -> {
            dialog.dismiss();
            showMealPlanDialogForDate(dbDate);
        });
        dialog.show();
    }

    private void showMealPlanDialogForDate(String dbDate) {
        // Fetch members first, then show dialog
        fetchMembers(() -> {
            bottomSheetDialog = new BottomSheetDialog(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.meal_plan, null);
            bottomSheetDialog.setContentView(dialogView);

            Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
            TextView selectedRecipeText = dialogView.findViewById(R.id.selectedRecipeText);
            RecyclerView recipeRecyclerView = dialogView.findViewById(R.id.recipeRecyclerView);
            Button selectRecipeButton = dialogView.findViewById(R.id.selectRecipeButton);
            Button confirmButton = dialogView.findViewById(R.id.confirmButton);
            LinearLayout memberCheckboxContainer = dialogView.findViewById(R.id.memberCheckboxContainer);
            List<CheckBox> memberCheckBoxes = new ArrayList<>();
            memberCheckboxContainer.removeAllViews();

            // Debug logging
            Log.d("MealPlan", "showMealPlanDialogForDate - Populating member checkboxes. Total members: " + userMembers.size());

            for (int i = 0; i < userMembers.size(); i++) {
                JSONObject member = userMembers.get(i);
                String memberName = member.optString("member_name", "");
                int memberId = member.optInt("member_id", -1);
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(memberName);
                checkBox.setTag(memberId);
                memberCheckboxContainer.addView(checkBox);
                memberCheckBoxes.add(checkBox);
            }
            // Show dialog immediately
            bottomSheetDialog.show();

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.meal_category, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);
            recipeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            recipe_adapter_mealPlan_dialog = new recipe_adapter_mealPlan_dialog(this, recipeList, selected -> {
                selectedRecipe = selected;
                selectedRecipeText.setText("Selected: " + selected.getName());
                selectedRecipeText.setVisibility(View.VISIBLE);
                recipeRecyclerView.setVisibility(View.GONE);
            });
            recipeRecyclerView.setAdapter(recipe_adapter_mealPlan_dialog);
            fetchRecipes();
            selectRecipeButton.setOnClickListener(v -> {
                recipeRecyclerView.setVisibility(recipeRecyclerView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            });
            confirmButton.setOnClickListener(v -> {
                if (selectedRecipe == null) {
                    Toast.makeText(meal_plan.this, "Please select a recipe.", Toast.LENGTH_SHORT).show();
                } else {
                    String selectedCategory = categorySpinner.getSelectedItem().toString();
                    if (selectedCategory.isEmpty()) {
                        Toast.makeText(meal_plan.this, "Please select a meal category.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Gather checked member IDs
                        List<Integer> checkedMemberIds = new ArrayList<>();
                        for (CheckBox cb : memberCheckBoxes) {
                            if (cb.isChecked()) {
                                checkedMemberIds.add((Integer) cb.getTag());
                            }
                        }
                        if (!checkedMemberIds.isEmpty()) {
                            // User + members
                            saveMealPlan(selectedRecipe, selectedCategory, checkedMemberIds);
                        } else {
                            // User only
                            saveMealPlanUserOnly(selectedRecipe, selectedCategory);
                        }
                        bottomSheetDialog.dismiss();
                    }
                }
            });
        });
    }

    private void saveMealPlanForDate(recipe recipe, String category, String dbDate) {

        String url = "http://192.168.0.16/Final%20Year%20Project/save_meal_plan.php";
        int userId = sharedPref.getInstance(this).getUserId();
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", userId);
            data.put("recipe_id", Integer.parseInt(recipe.getId()));
            data.put("category", category);
            data.put("meal_date", dbDate);

            // Create member_ids array as expected by backend
            JSONArray memberIdsArray = new JSONArray();
            memberIdsArray.put(selectedMemberId);
            data.put("member_ids", memberIdsArray);

            // --- Auto-calculate portion multipliers ---
            JSONObject portionMultipliers = new JSONObject();
            // Add user portion
            double userPortion = 1.0;
            try {
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                int userAge = prefs.getInt("age", 25);
                double userTdee = Double.longBitsToDouble(prefs.getLong("tdee", Double.doubleToLongBits(2000.0)));
                userPortion = userTdee / 2000.0;
                if (userAge < 18) userPortion *= 1.2;
                else if (userAge > 65) userPortion *= 0.9;
                userPortion = Math.max(0.5, Math.min(2.0, userPortion));
            } catch (Exception ignored) {}
            portionMultipliers.put("user", userPortion);
            // Add member portion
            for (JSONObject member : userMembers) {
                if (member.optInt("member_id", -1) == selectedMemberId) {
                    double tdee = member.optDouble("tdee", 2000.0);
                    int age = member.optInt("age", 25);
                    double portion = tdee / 2000.0;
                    if (age < 18) portion *= 1.2;
                    else if (age > 65) portion *= 0.9;
                    portion = Math.max(0.5, Math.min(2.0, portion));
                    portionMultipliers.put(String.valueOf(selectedMemberId), portion);
                }
            }
            data.put("portion_multipliers", portionMultipliers);
            // --- End auto-calc ---
            // Show the JSON data being sent in a toast message
            String jsonString = data.toString();
            Toast.makeText(this, "Sending JSON: " + jsonString, Toast.LENGTH_LONG).show();
            Log.d("MealPlanJSON", "Sending: " + jsonString);

        } catch (JSONException e) {
            Log.e("MealPlan", "JSON build error", e);
            Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                response -> {
                    try {
                        String status = response.optString("status", "").trim();
                        if ("success".equalsIgnoreCase(status)) {
                            Toast.makeText(this, "Meal saved!", Toast.LENGTH_SHORT).show();
                            displaySelectedDayMealPlan();
                        } else {
                            String message = response.optString("message", "Unknown backend error");
                            Toast.makeText(this, "Save failed: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    // Replace getAllRecipesForCurrentWeek with this asynchronous version
    private void fetchAllRecipesForCurrentWeek(RecipesCallback callback) {
        List<recipe> allRecipes = new ArrayList<>();
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar day = (Calendar) currentWeekStart.clone();
        int days = 7;
        int[] remaining = {days};

        for (int i = 0; i < days; i++) {
            String dbDate = dbFormat.format(day.getTime());
            String url;
            if (selectedMemberId != -1) {
                url = "http://192.168.0.16/Final%20Year%20Project/retrieve_meal_plan.php?user_id=" + userId + "&member_id=" + selectedMemberId + "&meal_date=" + dbDate;
            } else {
                url = "http://192.168.0.16/Final%20Year%20Project/retrieve_meal_plan.php?user_id=" + userId + "&meal_date=" + dbDate;
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONArray mealPlans = response.getJSONArray("meal_plans");
                                for (int j = 0; j < mealPlans.length(); j++) {
                                    JSONObject mealPlan = mealPlans.getJSONObject(j);
                                    JSONArray recipes = mealPlan.getJSONArray("recipes");
                                    for (int k = 0; k < recipes.length(); k++) {
                                        JSONObject recipeObj = recipes.getJSONObject(k);
                                        recipe r = new recipe(
                                                recipeObj.getString("recipe_id"),
                                                recipeObj.getString("title"),
                                                recipeObj.optString("image_url", ""),
                                                recipeObj.getString("category")
                                        );
                                        r.setUserRecipe(recipeObj.getInt("is_user_recipe") == 1);
                                        r.setMealPlanRecipeId(recipeObj.optString("meal_plan_recipe_id", null));
                                        allRecipes.add(r);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            callback.onRecipesFetched(allRecipes);
                        }
                    },
                    error -> {
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            callback.onRecipesFetched(allRecipes);
                        }
                    }
            );
            Volley.newRequestQueue(this).add(request);
            day.add(Calendar.DATE, 1);
        }
    }

    // Update fetchAndShowGroceryListDialog to use the async fetch
    private void fetchAndShowGroceryListDialog() {
        fetchAllRecipesForCurrentWeek(recipes -> {
            if (recipes.isEmpty()) {
                Toast.makeText(this, "No recipes found for this week.", Toast.LENGTH_SHORT).show();
                return;
            }
            JSONArray recipeIds = new JSONArray();
            for (recipe r : recipes) {
                recipeIds.put(r.getId());
            }
            String url = "http://192.168.0.16/Final%20Year%20Project/retrieve_recipe_ingredient_mealPlan.php";
            JSONObject data = new JSONObject();
            try {
                data.put("recipe_ids", recipeIds);
                data.put("user_id", userId);

                // Calculate week start and end in yyyy-MM-dd format
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String weekStart = dbFormat.format(currentWeekStart.getTime());
                Calendar weekEndCal = (Calendar) currentWeekStart.clone();
                weekEndCal.add(Calendar.DATE, 6);
                String weekEnd = dbFormat.format(weekEndCal.getTime());

                data.put("week_start", weekStart);
                data.put("week_end", weekEnd);

            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                    response -> {
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONArray ingredients = response.getJSONArray("ingredients");
                                List<IngredientDisplayItem> items = new ArrayList<>();
                                for (int i = 0; i < ingredients.length(); i++) {
                                    JSONObject ing = ingredients.getJSONObject(i);
                                    String name = ing.getString("name");
                                    String amount = ing.getString("amount");
                                    String displayName = name.substring(0, 1).toUpperCase() + name.substring(1);
                                    String imageUrl = "https://www.themealdb.com/images/ingredients/" + displayName + ".png";
                                    items.add(new IngredientDisplayItem(name, displayName, imageUrl, amount));
                                }
                                // Inflate custom dialog layout
                                LayoutInflater inflater = LayoutInflater.from(this);
                                View dialogView = inflater.inflate(R.layout.dialog_ingredient_checkbox_list, null);
                                RecyclerView recyclerView = dialogView.findViewById(R.id.ingredient_recycler_view);
                                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                                IngredientCheckboxAdapter adapter = new IngredientCheckboxAdapter(items);
                                recyclerView.setAdapter(adapter);
                                // Show dialog
                                new AlertDialog.Builder(this)
                                        .setTitle("Select ingredients to add to grocery list")
                                        .setView(dialogView)
                                        .setPositiveButton("Add to Grocery List", (dialog, which) -> {
                                            List<Map<String, String>> selectedIngredients = new ArrayList<>();
                                            for (IngredientDisplayItem item : adapter.getSelectedItems()) {
                                                Map<String, String> ing = new HashMap<>();
                                                ing.put("name", item.name);
                                                ing.put("amount", item.amount);
                                                selectedIngredients.add(ing);
                                            }
                                            sendIngredientsToBackend(selectedIngredients);
                                        })
                                        .setNegativeButton("Cancel", null)
                                        .show();
                            } else {
                                Toast.makeText(this, "Failed to retrieve ingredients.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
            );
            Volley.newRequestQueue(this).add(request);
        });
    }

    private void sendIngredientsToBackend(List<Map<String, String>> ingredients) {
        int userId = sharedPref.getInstance(this).getUserId();
        String week = ""; // You can implement getCurrentWeekString() if you want to send a week label
        int recipeId = 0; // Use 0 or a special value if sending multiple recipes
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", userId);
            data.put("recipe_id", recipeId);
            data.put("week", week);
            JSONArray ingArray = new JSONArray();
            for (Map<String, String> ing : ingredients) {
                JSONObject ingObj = new JSONObject();
                ingObj.put("name", ing.get("name"));
                ingObj.put("amount", ing.get("amount"));
                ingArray.put(ingObj);
            }
            data.put("ingredients", ingArray);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        String url = "http://192.168.0.16/Final%20Year%20Project/add_to_grocery_list.php";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                response -> Toast.makeText(this, "Grocery list updated!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Failed to update grocery list", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void fetchMembers(Runnable onComplete) {
        String url = "http://192.168.0.16/Final%20Year%20Project/get_user_member_to_meal_plan.php";
        JSONObject postData = new JSONObject();
        try {
            postData.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray members = response.getJSONArray("members");
                            userMembers.clear();
                            for (int i = 0; i < members.length(); i++) {
                                JSONObject member = members.getJSONObject(i);
                                int age = member.has("age") && !member.isNull("age") ? member.getInt("age") : -1;
                                double tdee = member.has("tdee") && !member.isNull("tdee") ? member.getDouble("tdee") : 2000.0;
                                member.put("age", age);
                                member.put("tdee", tdee); // will be updated below if backend returns a more accurate value
                                userMembers.add(member);
                            }
                            // Now fetch accurate TDEE for each member
                            fetchAllMembersTDEE(0, onComplete);
                        } else {
                            Log.e("MealPlan", "Failed to fetch members: " + response.getString("message"));
                        }
                    } catch (JSONException e) {
                        Log.e("MealPlan", "Error parsing members JSON", e);
                    }
                },
                error -> {
                    Log.e("MealPlan", "Error fetching members", error);
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    // Helper to fetch TDEE for all members sequentially
    private void fetchAllMembersTDEE(int index, Runnable onComplete) {
        if (index >= userMembers.size()) {
            // All done, update UI
            displaySelectedDayMealPlan();
            if (onComplete != null) onComplete.run();
            return;
        }
        JSONObject member = userMembers.get(index);
        int memberId = member.optInt("member_id", -1);
        if (memberId == -1) {
            fetchAllMembersTDEE(index + 1, onComplete);
            return;
        }
        String url = "http://192.168.0.16/Final%20Year%20Project/get_family_member.php?user_id=" + userId + "&member_id=" + memberId;
        JsonObjectRequest tdeeRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    if (response.optBoolean("success")) {
                        JSONArray membersArr = response.optJSONArray("members");
                        if (membersArr != null && membersArr.length() > 0) {
                            JSONObject memberObj = membersArr.optJSONObject(0);
                            if (memberObj != null && memberObj.has("tdee") && !memberObj.isNull("tdee")) {
                                try {
                                    member.put("tdee", memberObj.getDouble("tdee"));
                                } catch (JSONException ignored) {}
                            }
                        }
                    }
                    // Continue to next member
                    fetchAllMembersTDEE(index + 1, onComplete);
                },
                error -> {
                    // On error, just continue
                    fetchAllMembersTDEE(index + 1, onComplete);
                }
        );
        Volley.newRequestQueue(this).add(tdeeRequest);
    }

    private void fetchMembersForDialog(ArrayAdapter<String> memberAdapter, Spinner memberSpinner) {
        String url = "http://192.168.0.16/Final%20Year%20Project/get_user_member_to_meal_plan.php";
        JSONObject postData = new JSONObject();
        try {
            postData.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray members = response.getJSONArray("members");
                            userMembers.clear();
                            List<String> memberNames = new ArrayList<>();

                            for (int i = 0; i < members.length(); i++) {
                                JSONObject member = members.getJSONObject(i);
                                userMembers.add(member);
                                memberNames.add(member.getString("member_name"));
                            }

                            memberAdapter.clear();
                            memberAdapter.addAll(memberNames);
                            memberAdapter.notifyDataSetChanged();

                        } else {
                            Toast.makeText(this, "Failed to fetch members: " + response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("MealPlan", "Error parsing members JSON", e);
                        Toast.makeText(this, "Error parsing members data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("MealPlan", "Error fetching members", error);
                    Toast.makeText(this, "Error fetching members", Toast.LENGTH_SHORT).show();
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    public static double calculateTotalPortions(List<JSONObject> selectedMembers) {
        double totalPortions = 0.0;
        for (JSONObject member : selectedMembers) {
            // Get TDEE and age from the member object (already retrieved from backend)
            double tdee = member.optDouble("tdee", 2000.0); // fallback to 2000 if missing
            int age = member.optInt("age", 25); // fallback to 25 if missing

            // Calculate base portion
            double portion = tdee / 2000.0;

            // Age-based adjustment
            if (age < 18) {
                portion *= 1.2;
            } else if (age > 65) {
                portion *= 0.9;
            }

            // Clamp portion between 0.5 and 2.0
            portion = Math.max(0.5, Math.min(2.0, portion));

            totalPortions += portion;
        }
        return totalPortions;
    }

    private String getCurrentSelectedDate() {
        Calendar selectedDay = (Calendar) currentWeekStart.clone();
        selectedDay.add(Calendar.DATE, selectedDayIndex);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDay.getTime());
    }

    private void updateNutritionSummaries(LinearLayout mainContainer, List<recipe> breakfastList, List<recipe> lunchList, List<recipe> dinnerList) {
        // Get the current selected date
        String currentDate = getCurrentSelectedDate();
        
        // Make API call to get nutrition data
        String url = "http://192.168.0.16/Final%20Year%20Project/retrieve_meal_plan.php?user_id=" + userId + "&meal_date=" + currentDate;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONObject nutritionSummary = response.getJSONObject("nutrition_summary");
                            
                            // Get category-specific nutrition values
                            double breakfastCalories = nutritionSummary.optDouble("breakfast_calories", 0);
                            double breakfastProtein = nutritionSummary.optDouble("breakfast_protein", 0);
                            double breakfastCarbs = nutritionSummary.optDouble("breakfast_carbs", 0);
                            double breakfastFat = nutritionSummary.optDouble("breakfast_fat", 0);
                            
                            double lunchCalories = nutritionSummary.optDouble("lunch_calories", 0);
                            double lunchProtein = nutritionSummary.optDouble("lunch_protein", 0);
                            double lunchCarbs = nutritionSummary.optDouble("lunch_carbs", 0);
                            double lunchFat = nutritionSummary.optDouble("lunch_fat", 0);
                            
                            double dinnerCalories = nutritionSummary.optDouble("dinner_calories", 0);
                            double dinnerProtein = nutritionSummary.optDouble("dinner_protein", 0);
                            double dinnerCarbs = nutritionSummary.optDouble("dinner_carbs", 0);
                            double dinnerFat = nutritionSummary.optDouble("dinner_fat", 0);
                            
                            // Log the category-specific nutrition data for debugging
                            Log.d("NutritionSummary", "Breakfast: " + breakfastCalories + " cal, " + breakfastProtein + "g protein, " + breakfastCarbs + "g carbs, " + breakfastFat + "g fat");
                            Log.d("NutritionSummary", "Lunch: " + lunchCalories + " cal, " + lunchProtein + "g protein, " + lunchCarbs + "g carbs, " + lunchFat + "g fat");
                            Log.d("NutritionSummary", "Dinner: " + dinnerCalories + " cal, " + dinnerProtein + "g protein, " + dinnerCarbs + "g carbs, " + dinnerFat + "g fat");
                            
                            // Update each category with its specific nutrition data
                            String[] categories = {"Breakfast", "Lunch", "Dinner"};
                            List<List<recipe>> allMeals = new ArrayList<>();
                            allMeals.add(breakfastList);
                            allMeals.add(lunchList);
                            allMeals.add(dinnerList);
                            
                            // Nutrition data for each category
                            double[] calories = {breakfastCalories, lunchCalories, dinnerCalories};
                            double[] protein = {breakfastProtein, lunchProtein, dinnerProtein};
                            double[] carbs = {breakfastCarbs, lunchCarbs, dinnerCarbs};
                            double[] fat = {breakfastFat, lunchFat, dinnerFat};
                            
                            for (int i = 0; i < categories.length; i++) {
                                View categoryView = mainContainer.getChildAt(i + 1); // +1 because first child is the day title
                                if (categoryView == null) continue;
                                
                                TextView nutritionSummaryView = categoryView.findViewById(R.id.mealNutritionSummary);
                                
                                String nutritionText = String.format(Locale.getDefault(), 
                                    "%.0f calories, %.1fg protein, %.1fg carbs, %.1fg fat", 
                                    calories[i], protein[i], carbs[i], fat[i]);
                                
                                nutritionSummaryView.setText(nutritionText);
                            }
                        } else {
                            // Set default values if API fails
                            String[] categories = {"Breakfast", "Lunch", "Dinner"};
                            List<List<recipe>> allMeals = new ArrayList<>();
                            allMeals.add(breakfastList);
                            allMeals.add(lunchList);
                            allMeals.add(dinnerList);
                            
                            for (int i = 0; i < categories.length; i++) {
                                View categoryView = mainContainer.getChildAt(i + 1);
                                if (categoryView == null) continue;
                                
                                TextView nutritionSummaryView = categoryView.findViewById(R.id.mealNutritionSummary);
                                nutritionSummaryView.setText("0 calories, 0.0g protein, 0.0g carbs, 0.0g fat");
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("NutritionSummary", "Error parsing nutrition data", e);
                        // Set default values on error
                        String[] categories = {"Breakfast", "Lunch", "Dinner"};
                        List<List<recipe>> allMeals = new ArrayList<>();
                        allMeals.add(breakfastList);
                        allMeals.add(lunchList);
                        allMeals.add(dinnerList);
                        
                        for (int i = 0; i < categories.length; i++) {
                            View categoryView = mainContainer.getChildAt(i + 1);
                            if (categoryView == null) continue;
                            
                            TextView nutritionSummaryView = categoryView.findViewById(R.id.mealNutritionSummary);
                            nutritionSummaryView.setText("0 calories, 0.0g protein, 0.0g carbs, 0.0g fat");
                        }
                    }
                },
                error -> {
                    Log.e("NutritionSummary", "Error fetching nutrition data", error);
                    // Set default values on error
                    String[] categories = {"Breakfast", "Lunch", "Dinner"};
                    List<List<recipe>> allMeals = new ArrayList<>();
                    allMeals.add(breakfastList);
                    allMeals.add(lunchList);
                    allMeals.add(dinnerList);
                    
                    for (int i = 0; i < categories.length; i++) {
                        View categoryView = mainContainer.getChildAt(i + 1);
                        if (categoryView == null) continue;
                        
                        TextView nutritionSummaryView = categoryView.findViewById(R.id.mealNutritionSummary);
                        nutritionSummaryView.setText("0 calories, 0.0g protein, 0.0g carbs, 0.0g fat");
                    }
                });
        
        // Add the request to the queue
        Volley.newRequestQueue(this).add(request);
    }

    private void updateCaloriesTodayDisplay(String date) {
        // Clear the container first
        caloriesTodayContainer.removeAllViews();
        
        // Inflate the calories today display layout
        View caloriesView = LayoutInflater.from(this).inflate(R.layout.calories_today_display, caloriesTodayContainer, false);
        
        // Get references to the TextViews
        TextView totalFoodCalories = caloriesView.findViewById(R.id.totalFoodCalories);
        TextView exerciseBurned = caloriesView.findViewById(R.id.exerciseBurned);
        TextView caloriesToday = caloriesView.findViewById(R.id.caloriesToday);
        TextView tdeeValue = caloriesView.findViewById(R.id.tdeeValue);
        
        // Make API call to get the data
        String url = "http://192.168.0.16/Final%20Year%20Project/retrieve_meal_plan.php?user_id=" + userId + "&meal_date=" + date;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Log the response for debugging
                        Log.d("CaloriesToday", "API Response: " + response.toString());
                        
                        if (response.getString("status").equals("success")) {
                            // Get the nutrition summary from the response
                            JSONObject nutritionSummary = response.getJSONObject("nutrition_summary");
                            
                            // Log the nutrition summary for debugging
                            Log.d("CaloriesToday", "Nutrition Summary: " + nutritionSummary.toString());
                            
                            // Get the values from the nutrition summary
                            double totalFoodCal = nutritionSummary.optDouble("total_calories", 0);
                            double exerciseBurn = nutritionSummary.optDouble("total_calories_burned", 0);
                            double tdee = nutritionSummary.optDouble("user_tdee", 2000.0);
                            
                            // Log the values for debugging
                            Log.d("CaloriesToday", "Total Food Calories: " + totalFoodCal);
                            Log.d("CaloriesToday", "Exercise Burned: " + exerciseBurn);
                            Log.d("CaloriesToday", "TDEE: " + tdee);
                            
                            // Calculate calories today
                            double caloriesTodayValue = totalFoodCal - exerciseBurn;
                            Log.d("CaloriesToday", "Calories Today: " + caloriesTodayValue);
                            
                            // Update the display
                            totalFoodCalories.setText(String.format(Locale.getDefault(), "%.0f", totalFoodCal));
                            exerciseBurned.setText(String.format(Locale.getDefault(), "%.0f", exerciseBurn));
                            caloriesToday.setText(String.format(Locale.getDefault(), "%.0f", caloriesTodayValue));
                            tdeeValue.setText(String.format(Locale.getDefault(), "%.0f", tdee));
                            
                            // Set color based on the rules
                            int color;
                            if (caloriesTodayValue < tdee && (tdee - caloriesTodayValue) >= 100) {
                                // Orange: calories today < tdee AND tdee - calories today >= 100
                                color = getResources().getColor(R.color.orange);
                                Log.d("CaloriesToday", "Setting color to ORANGE");

                            } else if (caloriesTodayValue > tdee && (caloriesTodayValue - tdee) >= 100) {
                                // Red: calories today > tdee AND calories today - tdee >= 100
                                color = getResources().getColor(R.color.red);
                                Log.d("CaloriesToday", "Setting color to RED");
                            } else {
                                // Green: otherwise
                                color = getResources().getColor(R.color.green);
                                Log.d("CaloriesToday", "Setting color to GREEN");
                            }
                            
                            // Update the text color (not background)
                            caloriesToday.setTextColor(android.content.res.ColorStateList.valueOf(color));
                            
                        } else {
                            // Set default values if API fails
                            Log.d("CaloriesToday", "API returned non-success status");
                            totalFoodCalories.setText("0");
                            exerciseBurned.setText("0");
                            caloriesToday.setText("0");
                            tdeeValue.setText("2000");
                            caloriesToday.setTextColor(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.green)));
                        }
                    } catch (JSONException e) {
                        // Set default values if parsing fails
                        Log.e("CaloriesToday", "JSON parsing error: " + e.getMessage());
                        totalFoodCalories.setText("0");
                        exerciseBurned.setText("0");
                        caloriesToday.setText("0");
                        tdeeValue.setText("2000");
                        caloriesToday.setTextColor(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.green)));
                    }
                },
                error -> {
                    // Set default values if network error
                    Log.e("CaloriesToday", "Network error: " + error.toString());
                    totalFoodCalories.setText("0");
                    exerciseBurned.setText("0");
                    caloriesToday.setText("0");
                    tdeeValue.setText("2000");
                    caloriesToday.setTextColor(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.green)));
                }
        );
        Volley.newRequestQueue(this).add(request);
        
        // Add the view to the container
        caloriesTodayContainer.addView(caloriesView);
    }
}

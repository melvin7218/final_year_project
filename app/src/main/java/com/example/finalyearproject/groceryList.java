package com.example.finalyearproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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

public class groceryList extends AppCompatActivity {

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
        setContentView(R.layout.activity_grocery_list);

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

        adapter = new GroceryListAdapter(this, groceryItems); // bind to groceryItems only
        recyclerView.setAdapter(adapter);

        Button pantryList = findViewById(R.id.prantryList);
        pantryList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(groceryList.this, pantry_list.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });
        fetchGroceryList();

        TextView mainPage = findViewById(R.id.text_menu);
        mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(groceryList.this, MainScreen.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView mealPlan = findViewById(R.id.meal_plan);
        mealPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(groceryList.this, meal_plan.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView grocery_list = findViewById(R.id.grocery_logo);
        grocery_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(groceryList.this, groceryList.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView accountPage = findViewById(R.id.account_setting);
        accountPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(groceryList.this, user_setting.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

    }

    private void fetchGroceryList() {
        String url = "http://192.168.0.130/Final%20Year%20Project/get_grocery_list.php?user_id=" + userId;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray data = response.getJSONArray("data");
                            groceryItems.clear();  // Clear adapter list
                            allItems.clear();

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
                                groceryItems.add(groceryItem); // Add to adapter list
                                allItems.add(groceryItem);
                            }
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


}
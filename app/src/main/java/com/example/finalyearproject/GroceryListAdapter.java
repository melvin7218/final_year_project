package com.example.finalyearproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GroceryListAdapter extends RecyclerView.Adapter<GroceryListAdapter.ViewHolder> {

    private Context context;
    private ArrayList<grocery_list_item> groceryItems;
    ImageView btnDelete;


    public GroceryListAdapter(Context context, ArrayList<grocery_list_item> groceryItems) {
        this.context = context;
        this.groceryItems = groceryItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grocery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        grocery_list_item item = groceryItems.get(position);

        holder.ingredientName.setText(item.getIngredientName());
        holder.amount.setText(item.getAmount());
        holder.recipeName.setText(item.getRecipeName());


        String ingredientImageUrl = "https://www.themealdb.com/images/ingredients/" + item.getIngredientName().trim().replace(" ", "%20") + ".png";

        Glide.with(context).load(ingredientImageUrl).error(R.drawable.error_image).into(holder.ingredientImage);

        if(!item.getRecipeImage().isEmpty()){
            Glide.with(context).load(item.getRecipeImage()).into(holder.recipeImage);
        }

        holder.checboxPurchased.setChecked(item.isPurchased());

        holder.checboxPurchased.setOnClickListener(v -> {
            boolean isChecked = holder.checboxPurchased.isChecked();
            item.setPurchased(isChecked);
            updatePurchaseStatusOnServer(item.getIngredientName(), isChecked);
        });

        holder.deleteBtn.setOnClickListener(v ->{
            deleteItemFromServer(item.getIngredientName(), holder.getAdapterPosition());
        });

    }

    private void updatePurchaseStatusOnServer(String ingredientName, boolean purchased) {
        // Send a request to server to update purchased status
        String url = "http://172.16.62.183/Final%20Year%20Project/update_grocery_list_status.php";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("ingredient_name", ingredientName);
            jsonObject.put("purchased", purchased);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    // Handle success
                },
                error -> {
                    // Handle error
                }
        );
        queue.add(request);
    }

    @Override
    public int getItemCount() {
        return groceryItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ingredientName, amount, recipeName;
        ImageView recipeImage, ingredientImage,deleteBtn;
        CheckBox checboxPurchased;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientImage = itemView.findViewById(R.id.ingredientImage);
            ingredientName = itemView.findViewById(R.id.ingredientName);
            amount = itemView.findViewById(R.id.amount);
            recipeName = itemView.findViewById(R.id.recipeName);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            checboxPurchased = itemView.findViewById(R.id.checkboxPurchased);
            deleteBtn = itemView.findViewById(R.id.remove_grocery_item);
        }
    }


    private void deleteItemFromServer(String ingredientName, int position) {
        String url = "http://172.16.62.183/Final%20Year%20Project/delete_grocery_item.php";

        JSONObject jsonObject = new JSONObject();
        try {
            SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            jsonObject.put("ingredient_name", ingredientName);
            jsonObject.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    groceryItems.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Ingredient removed", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(request);
    }

}
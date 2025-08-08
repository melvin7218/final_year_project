package com.example.finalyearproject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private Context context;
    private List<recipe> recipeList;

    // Constants for intent extras
    public static final String EXTRA_RECIPE_ID = "recipe_id";
    public static final String EXTRA_IS_MEALDB = "is_mealdb_recipe";
    public static final String EXTRA_USER_ID = "user_id";

    public RecipeAdapter(Context context, List<recipe> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recipe_item, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        recipe currentRecipe = recipeList.get(position);

        // Validate recipe ID
        if (currentRecipe.getId() == null || currentRecipe.getId().isEmpty()) {
            Log.e("RecipeAdapter", "Invalid recipe ID at position: " + position);
            holder.itemView.setVisibility(View.GONE); // Hide invalid items
            return;
        }

        // Set recipe details
        holder.recipeName.setText(currentRecipe.getName());
        holder.recipeTime.setText(formatRecipeTime(currentRecipe.getReadyInMinutes()));

        // Load recipe image
        loadRecipeImage(holder.recipeImage, currentRecipe.getImageUrl());

        // Set click listener
        holder.itemView.setOnClickListener(v -> navigateToDetailActivity(currentRecipe));

        holder.removeBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Remove Recipe")
                    .setMessage("Are you sure you want to delete this recipe?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        deleteRecipeFromDatabase(String.valueOf(recipeList.get(position).getId()), position);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private String formatRecipeTime(int minutes) {
        if (minutes <= 0) return "N/A";
        if (minutes >= 60) {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            return hours + "h" + (remainingMinutes > 0 ? " " + remainingMinutes + "m" : "");
        }
        return minutes + "m";
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

    private void loadRecipeImage(ImageView imageView, String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.placeholder_image);
            return;
        }

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(imageView);
    }

    private void navigateToDetailActivity(recipe recipe) {
        Intent intent = new Intent(context, detail_recipe.class);

        // Pass the recipe ID and type
        intent.putExtra(EXTRA_RECIPE_ID, String.valueOf(recipe.getId()));
        intent.putExtra(EXTRA_IS_MEALDB, !recipe.isUserRecipe());

        // Optional: Pass recipe name/image for quick display
        intent.putExtra("recipe_name", recipe.getName());
        intent.putExtra("recipe_image", recipe.getImageUrl());

        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public void updateRecipes(List<recipe> newRecipes) {
        recipeList.clear();
        recipeList.addAll(newRecipes);
        notifyDataSetChanged();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage, removeBtn;
        TextView recipeName, recipeTime, recipeServings;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeName = itemView.findViewById(R.id.recipeName);
            recipeTime = itemView.findViewById(R.id.recipeTime);
            recipeServings = itemView.findViewById(R.id.recipeServings);
            removeBtn = itemView.findViewById(R.id.remove_recipes);
        }
    }

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(recipe recipe);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private void deleteRecipeFromDatabase(String recipeId, int position) {
        String url = "http://10.10.6.29/Final%20Year%20Project/delete_recipe.php";

        JSONObject data = new JSONObject();
        try {
            data.put("recipe_id", recipeId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            recipeList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Recipe deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(context).add(request);
    }


}

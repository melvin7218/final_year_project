package com.example.finalyearproject;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MealPlanAddRecipeAdapter extends RecyclerView.Adapter<MealPlanAddRecipeAdapter.RecipeViewHolder> {

    private List<recipe> recipes;
    private Context context;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeAddClick(recipe recipe);
    }

    public MealPlanAddRecipeAdapter(Context context, List<recipe> recipes, OnRecipeClickListener listener) {
        this.context = context;
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal_plan_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        recipe recipe = recipes.get(position);
        
        // Set alternating background colors
        if (position % 2 == 0) {
            // Odd number - dark background
            holder.recipeItemContainer.setBackgroundColor(Color.parseColor("#8770AF"));
            holder.nameRecipe.setTextColor(Color.parseColor("#FFFFFF"));
            holder.preparedTime.setTextColor(Color.parseColor("#FFFFFF"));
            holder.servingSize.setTextColor(Color.parseColor("#FFFFFF"));

        } else {
            // Even number - light background
            holder.recipeItemContainer.setBackgroundColor(Color.parseColor("#D5D3EA"));
            holder.nameRecipe.setTextColor(Color.parseColor("#432C81"));
            holder.preparedTime.setTextColor(Color.parseColor("#432C81"));
            holder.servingSize.setTextColor(Color.parseColor("#432C81"));
            holder.nameText.setTextColor(Color.parseColor("#432C81"));
            holder.prepareTimeText.setTextColor(Color.parseColor("#432C81"));
            holder.servingText.setTextColor(Color.parseColor("#432C81"));
            holder.nutritionText.setTextColor(Color.parseColor("#432C81"));
        }

        // Set recipe data for new layout
        holder.nameRecipe.setText(recipe.getName());
        holder.preparedTime.setText(formatTime(recipe.getReadyInMinutes()));
        holder.servingSize.setText(String.valueOf(recipe.getServings()));
        
        // Set nutrition information
        holder.recipeCalories.setText(String.format("%.0f cal", recipe.getCalories()));
        holder.recipeProtein.setText(String.format("%.0fg protein", recipe.getProtein()));
        holder.recipeFat.setText(String.format("%.0fg fat", recipe.getFat()));
        holder.recipeCarbs.setText(String.format("%.0fg carbs", recipe.getCarbohydrates()));

        // Load image using Glide
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.recipeImage);
        } else {
            holder.recipeImage.setImageResource(R.drawable.placeholder_image);
        }

        // Set click listener for add button
        holder.addRecipeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeAddClick(recipe);
            }
        });
    }

    private String formatTime(int minutes) {
        if (minutes <= 0) {
            return "N/A";
        }
        
        if (minutes < 60) {
            return minutes + " mins";
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours + " hour" + (hours > 1 ? "s" : "");
            } else {
                return hours + "h " + remainingMinutes + "m";
            }
        }
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void updateRecipes(List<recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        LinearLayout recipeItemContainer;
        ImageView recipeImage;
        TextView nameRecipe;
        TextView preparedTime;
        TextView servingSize;
        TextView recipeCalories;
        TextView recipeProtein;
        TextView recipeFat;
        TextView recipeCarbs;
        Button addRecipeButton;
        TextView nameText, prepareTimeText, servingText, nutritionText;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeItemContainer = itemView.findViewById(R.id.recipeItemContainer);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            nameRecipe = itemView.findViewById(R.id.nameRecipe);
            preparedTime = itemView.findViewById(R.id.preparedTime);
            servingSize = itemView.findViewById(R.id.servingSize);
            recipeCalories = itemView.findViewById(R.id.recipeCalories);
            recipeProtein = itemView.findViewById(R.id.recipeProtein);
            recipeFat = itemView.findViewById(R.id.recipeFat);
            recipeCarbs = itemView.findViewById(R.id.recipeCarbs);
            addRecipeButton = itemView.findViewById(R.id.addRecipeButton);
            nameText = itemView.findViewById(R.id.nameRecipeText);
            prepareTimeText = itemView.findViewById(R.id.preparedTimeText);
            servingText = itemView.findViewById(R.id.servingSizeText);
            nutritionText = itemView.findViewById(R.id.nutritionText);
        }
    }
} 
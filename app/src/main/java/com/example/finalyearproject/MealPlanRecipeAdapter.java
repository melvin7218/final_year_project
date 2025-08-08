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

public class MealPlanRecipeAdapter extends RecyclerView.Adapter<MealPlanRecipeAdapter.RecipeViewHolder> {

    private List<recipe> recipes;
    private Context context;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeAddClick(recipe recipe);
    }

    public MealPlanRecipeAdapter(Context context, List<recipe> recipes, OnRecipeClickListener listener) {
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
            // Even number - light background
            holder.recipeItemContainer.setBackgroundColor(Color.parseColor("#D5D3EA"));
            holder.recipeTitle.setTextColor(Color.parseColor("#432C81"));
            holder.recipeServings.setTextColor(Color.parseColor("#432C81"));
        } else {
            // Odd number - dark background
            holder.recipeItemContainer.setBackgroundColor(Color.parseColor("#8770AF"));
            holder.recipeTitle.setTextColor(Color.parseColor("#FFFFFF"));
            holder.recipeServings.setTextColor(Color.parseColor("#FFFFFF"));
        }

        // Set recipe data
        holder.recipeTitle.setText(recipe.getName());
        holder.recipeServings.setText("Servings: " + recipe.getServings());
        
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
        TextView recipeTitle;
        TextView recipeServings;
        TextView recipeCalories;
        TextView recipeProtein;
        TextView recipeFat;
        TextView recipeCarbs;
        Button addRecipeButton;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeItemContainer = itemView.findViewById(R.id.recipeItemContainer);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeTitle = itemView.findViewById(R.id.recipeTitle);
            recipeServings = itemView.findViewById(R.id.recipeServings);
            recipeCalories = itemView.findViewById(R.id.recipeCalories);
            recipeProtein = itemView.findViewById(R.id.recipeProtein);
            recipeFat = itemView.findViewById(R.id.recipeFat);
            recipeCarbs = itemView.findViewById(R.id.recipeCarbs);
            addRecipeButton = itemView.findViewById(R.id.addRecipeButton);
        }
    }
} 
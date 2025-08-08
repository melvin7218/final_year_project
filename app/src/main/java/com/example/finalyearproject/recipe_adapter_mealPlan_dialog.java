package com.example.finalyearproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class recipe_adapter_mealPlan_dialog extends RecyclerView.Adapter<recipe_adapter_mealPlan_dialog.MealPlanRecipeViewHolder> {

    private Context context;
    private List<recipe> recipeList;
    private OnRecipeSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnRecipeSelectedListener {
        void onRecipeSelected(recipe recipe);
    }

    public recipe_adapter_mealPlan_dialog(Context context, List<recipe> recipeList, OnRecipeSelectedListener listener) {
        this.context = context;
        this.recipeList = recipeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MealPlanRecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recipe_item_selectable, parent, false);
        return new MealPlanRecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealPlanRecipeViewHolder holder, int position) {
        recipe currentRecipe = recipeList.get(position);

        // Set recipe details
        holder.recipeName.setText(currentRecipe.getName());
        holder.recipeTime.setText(formatRecipeTime(currentRecipe.getReadyInMinutes()));

        // Load recipe image
        if (currentRecipe.getImageUrl() != null && !currentRecipe.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(currentRecipe.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(holder.recipeImage);
        } else {
            holder.recipeImage.setImageResource(R.drawable.placeholder_image);
        }

        // Highlight selected item
        holder.itemView.setSelected(selectedPosition == position);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            // Update selected position
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Notify changes
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            // Notify listener
            if (listener != null) {
                listener.onRecipeSelected(currentRecipe);
            }
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

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public void updateRecipes(List<recipe> newRecipes) {
        recipeList.clear();
        recipeList.addAll(newRecipes);
        notifyDataSetChanged();
    }

    public static class MealPlanRecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        TextView recipeName, recipeTime;

        public MealPlanRecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeName = itemView.findViewById(R.id.recipeName);
            recipeTime = itemView.findViewById(R.id.recipeTime);
        }
    }
}
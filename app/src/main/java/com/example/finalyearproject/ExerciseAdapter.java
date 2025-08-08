package com.example.finalyearproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Exercise> exerciseList;
    private Context context;
    private OnExerciseClickListener listener;
    
    private static final int VIEW_TYPE_EXERCISE = 0;
    private static final int VIEW_TYPE_ADD_EXERCISE = 1;

    public interface OnExerciseClickListener {
        void onAddExerciseClick();
        void onExerciseMenuClick(Exercise exercise, View view);
    }

    public ExerciseAdapter(Context context, List<Exercise> exerciseList, OnExerciseClickListener listener) {
        this.context = context;
        this.exerciseList = exerciseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.util.Log.d("ExerciseAdapter", "onCreateViewHolder called with viewType: " + viewType);
        if (viewType == VIEW_TYPE_ADD_EXERCISE) {
            View view = LayoutInflater.from(context).inflate(R.layout.exercise_card, parent, false);
            return new AddExerciseViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.exercise_card, parent, false);
            return new ExerciseViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        android.util.Log.d("ExerciseAdapter", "onBindViewHolder called for position: " + position);
        if (holder instanceof AddExerciseViewHolder) {
            ((AddExerciseViewHolder) holder).bind();
        } else if (holder instanceof ExerciseViewHolder) {
            Exercise exercise = exerciseList.get(position);
            ((ExerciseViewHolder) holder).bind(exercise);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (exerciseList.isEmpty() && position == 0) {
            return VIEW_TYPE_ADD_EXERCISE;
        }
        return VIEW_TYPE_EXERCISE;
    }

    @Override
    public int getItemCount() {
        // Always show at least one card (the add exercise card)
        int count = Math.max(1, exerciseList.size());
        android.util.Log.d("ExerciseAdapter", "getItemCount called, returning: " + count + " (exerciseList.size: " + exerciseList.size() + ")");
        return count;
    }

    public void updateExercises(List<Exercise> newExercises) {
        this.exerciseList = newExercises;
        android.util.Log.d("ExerciseAdapter", "Updating exercises, count: " + getItemCount());
        notifyDataSetChanged();
    }

    // ViewHolder for the "Add Exercise" card (when no exercises exist)
    class AddExerciseViewHolder extends RecyclerView.ViewHolder {
        private TextView exerciseName;
        private TextView caloriesBurned;
        private TextView metLabel;
        private TextView durationLabel;
        private TextView hadByLabel;
        private TextView addExerciseButton;
        private ImageView exerciseMenuButton;

        public AddExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseName = itemView.findViewById(R.id.exerciseName);
            caloriesBurned = itemView.findViewById(R.id.caloriesBurned);
            metLabel = itemView.findViewById(R.id.timeLabel);
            durationLabel = itemView.findViewById(R.id.durationLabel);
            hadByLabel = itemView.findViewById(R.id.hadByLabel);
            addExerciseButton = itemView.findViewById(R.id.addExerciseButton);
            exerciseMenuButton = itemView.findViewById(R.id.exerciseMenuButton);
        }

        public void bind() {
            android.util.Log.d("ExerciseAdapter", "AddExerciseViewHolder.bind() called");
            // Set default values for the "Add Exercise" card
            exerciseName.setText("No exercises added");
            caloriesBurned.setText("0 calories burned");
            metLabel.setText("Met: 0.0");
            durationLabel.setText("Duration: 0h 0m");
            hadByLabel.setText("Had by: You");
            
            // Hide the menu button for the add exercise card
            exerciseMenuButton.setVisibility(View.GONE);

            // Set up click listener for add exercise button
            addExerciseButton.setOnClickListener(v -> {
                android.util.Log.d("ExerciseAdapter", "Add exercise button clicked");
                if (listener != null) {
                    listener.onAddExerciseClick();
                }
            });
        }
    }

    // ViewHolder for actual exercise cards
    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private TextView exerciseName;
        private TextView caloriesBurned;
        private TextView metLabel;
        private TextView durationLabel;
        private TextView hadByLabel;
        private TextView addExerciseButton;
        private ImageView exerciseMenuButton;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseName = itemView.findViewById(R.id.exerciseName);
            caloriesBurned = itemView.findViewById(R.id.caloriesBurned);
            metLabel = itemView.findViewById(R.id.timeLabel);
            durationLabel = itemView.findViewById(R.id.durationLabel);
            hadByLabel = itemView.findViewById(R.id.hadByLabel);
            addExerciseButton = itemView.findViewById(R.id.addExerciseButton);
            exerciseMenuButton = itemView.findViewById(R.id.exerciseMenuButton);
        }

        public void bind(Exercise exercise) {
            exerciseName.setText(exercise.getName());
            caloriesBurned.setText(String.format("%.0f calories burned", exercise.getCaloriesBurned()));
            
            // Set MET value
            metLabel.setText("Met: " + String.format("%.1f", exercise.getMet()));
            
            // Set duration
            durationLabel.setText("Duration: " + exercise.getStartTime() + " - " + exercise.getEndTime());
            
            // Set "Had by" information
            List<String> hadByNames = exercise.getHadByNames();
            if (hadByNames != null && !hadByNames.isEmpty()) {
                hadByLabel.setText("Had by: " + String.join(", ", hadByNames));
            } else {
                hadByLabel.setText("Had by: You");
            }

            // Show the menu button for actual exercise cards
            exerciseMenuButton.setVisibility(View.VISIBLE);

            // Set up click listeners
            addExerciseButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddExerciseClick();
                }
            });

            exerciseMenuButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseMenuClick(exercise, v);
                }
            });
        }
    }
} 
package com.example.finalyearproject;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class exercise_page extends AppCompatActivity implements ExerciseAdapter.OnExerciseClickListener {

    private LinearLayout weekMealPlanContainer;
    private Spinner weekSelectorSpinner;
    private List<Calendar> weekStartDates = new ArrayList<>();
    private ArrayAdapter<String> weekAdapter;
    private int userId;
    private Calendar currentWeekStart;
    private int selectedDayIndex = -1;
    private Button[] dayButtons = new Button[7];
    private FrameLayout caloriesTodayContainer;
    private RecyclerView exerciseRecyclerView;
    private ExerciseAdapter exerciseAdapter;
    private List<Exercise> exerciseList = new ArrayList<>();
    private List<JSONObject> userMembers = new ArrayList<>();
    private BottomSheetDialog exerciseDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_page2);

        // Initialize all UI elements
        weekMealPlanContainer = findViewById(R.id.weekMealPlanContainer);
        weekSelectorSpinner = findViewById(R.id.weekSelectorSpinner);
        exerciseRecyclerView = findViewById(R.id.mealPlanRecyclerView);

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
        Log.d("ExercisePage", "Today: " + sdf.format(today.getTime()) + ", Week Start: " + sdf.format(weekStart.getTime()) + ", Selected Day Index: " + selectedDayIndex);

        // Initialize currentWeekStart before calling updateDayButtonStyles
        currentWeekStart = (Calendar) weekStart.clone();
        updateDayButtonStyles();

        for (int i = 0; i < 7; i++) {
            final int idx = i;
            dayButtons[i].setOnClickListener(v -> {
                selectedDayIndex = idx;
                updateDayButtonStyles();
                displaySelectedDayExercise();
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
                displaySelectedDayExercise();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        caloriesTodayContainer = findViewById(R.id.caloriesTodayContainer);

        // Set up RecyclerView
        exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exerciseAdapter = new ExerciseAdapter(this, exerciseList, this);
        exerciseRecyclerView.setAdapter(exerciseAdapter);
        
        // Debug: Force the adapter to show at least one item
        exerciseAdapter.updateExercises(exerciseList);

        // Display exercise for selected day
        displaySelectedDayExercise();

        // Navigation setup
        setupNavigation();

        Button recipe_page = findViewById(R.id.recipe_page);
        recipe_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(exercise_page.this, meal_plan.class);
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
            Log.e("ExercisePage", "currentWeekStart is null in updateDayButtonStyles");
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

    private void displaySelectedDayExercise() {
        // Add null check to prevent NullPointerException
        if (currentWeekStart == null) {
            Log.e("ExercisePage", "currentWeekStart is null in displaySelectedDayExercise");
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
        Log.d("ExercisePage", "Displaying exercise for: " + dbDate + " (" + displayDate + "), Selected Day Index: " + selectedDayIndex);

        // Create main container
        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        mainContainer.setOrientation(LinearLayout.VERTICAL);

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
        dayTitle.setPadding(16, 16, 16, 16);
        mainContainer.addView(dayTitle);

        weekMealPlanContainer.addView(mainContainer);

        // Fetch and display exercise data
        fetchAndDisplayExercises(dbDate);
        
        // Update calories display
        updateCaloriesTodayDisplay(dbDate);
    }

    private void fetchAndDisplayExercises(String date) {
        String url = "http://192.168.0.16/final%20year%20project/retrieve_exercise_user.php";
        String requestUrl = url + "?user_id=" + userId + "&exercise_date=" + date;
        Log.d("ExerciseRequest", "Requesting: " + requestUrl);
        
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                requestUrl,
                null,
                response -> {
                    try {
                        Log.d("ExercisePage", "Fetch exercises response: " + response.toString());
                        if (response.getString("status").equals("success")) {
                            JSONArray exercises = response.getJSONArray("exercises");
                            exerciseList.clear();
                            
                            for (int i = 0; i < exercises.length(); i++) {
                                JSONObject exerciseObj = exercises.getJSONObject(i);
                                
                                // Convert duration_minutes to duration string
                                int durationMinutes = exerciseObj.getInt("duration_minutes");
                                String durationString = formatDurationString(durationMinutes);
                                
                                Exercise exercise = new Exercise(
                                        exerciseObj.getString("exercise_record_id"),
                                        exerciseObj.getString("exercise_name"),
                                        exerciseObj.getDouble("calories_burned"),
                                        exerciseObj.getDouble("met"),
                                        durationString,
                                        exerciseObj.getString("starting_time"),
                                        exerciseObj.getString("ending_time")
                                );
                                
                                // Parse participants
                                List<String> hadByNames = new ArrayList<>();
                                if (exerciseObj.has("participants")) {
                                    JSONArray participantsArr = exerciseObj.getJSONArray("participants");
                                    for (int j = 0; j < participantsArr.length(); j++) {
                                        JSONObject participant = participantsArr.getJSONObject(j);
                                        hadByNames.add(participant.getString("name"));
                                    }
                                }
                                exercise.setHadByNames(hadByNames);
                                
                                exerciseList.add(exercise);
                            }
                            
                            Log.d("ExercisePage", "Success: Found " + exerciseList.size() + " exercises");
                            exerciseAdapter.updateExercises(exerciseList);
                        } else {
                            Log.d("ExercisePage", "No exercises found, showing empty state");
                            exerciseList.clear();
                            exerciseAdapter.updateExercises(exerciseList);
                        }
                    } catch (JSONException e) {
                        Log.e("ExercisePage", "Error parsing exercise data", e);
                        exerciseList.clear();
                        exerciseAdapter.updateExercises(exerciseList);
                    }
                },
                error -> {
                    Log.e("ExercisePage", "Error fetching exercises", error);
                    Log.d("ExercisePage", "Network error, showing empty state");
                    exerciseList.clear();
                    exerciseAdapter.updateExercises(exerciseList);
                }
        );
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
        String url = "http://192.168.0.16/final%20year%20project/retrieve_meal_plan.php?user_id=" + userId + "&meal_date=" + date;
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

    // ExerciseAdapter.OnExerciseClickListener implementation
    @Override
    public void onAddExerciseClick() {
        showAddExerciseDialog();
    }

    @Override
    public void onExerciseMenuClick(Exercise exercise, View view) {
        showExerciseMenu(exercise, view);
    }

    private void showAddExerciseDialog() {
        // Fetch members first, then show dialog
        fetchMembers(() -> {
            exerciseDialog = new BottomSheetDialog(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_exercise_dialog, null);
            exerciseDialog.setContentView(dialogView);

            Spinner exerciseTypeSpinner = dialogView.findViewById(R.id.exerciseTypeSpinner);
            Button startTimeButton = dialogView.findViewById(R.id.startTimeButton);
            Button endTimeButton = dialogView.findViewById(R.id.endTimeButton);
            TextView selectedTimesText = dialogView.findViewById(R.id.selectedTimesText);
            LinearLayout memberCheckboxContainer = dialogView.findViewById(R.id.memberCheckboxContainer);
            Button confirmExerciseButton = dialogView.findViewById(R.id.confirmExerciseButton);

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
            exerciseDialog.show();

            // Set up exercise type spinner
            fetchExerciseTypes(exerciseTypeSpinner, userId);

            // Initialize time variables
            final String[] selectedStartTime = {""};
            final String[] selectedEndTime = {""};

            // Set up start time button
            startTimeButton.setOnClickListener(v -> {
                Calendar currentTime = Calendar.getInstance();
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        this,
                        (view, hourOfDay, minute) -> {
                            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                            selectedStartTime[0] = time;
                            startTimeButton.setText("Start: " + time);
                            updateSelectedTimesText(selectedTimesText, selectedStartTime[0], selectedEndTime[0]);
                        },
                        currentTime.get(Calendar.HOUR_OF_DAY),
                        currentTime.get(Calendar.MINUTE),
                        true
                );
                timePickerDialog.show();
            });

            // Set up end time button
            endTimeButton.setOnClickListener(v -> {
                Calendar currentTime = Calendar.getInstance();
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        this,
                        (view, hourOfDay, minute) -> {
                            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                            selectedEndTime[0] = time;
                            endTimeButton.setText("End: " + time);
                            updateSelectedTimesText(selectedTimesText, selectedStartTime[0], selectedEndTime[0]);
                        },
                        currentTime.get(Calendar.HOUR_OF_DAY),
                        currentTime.get(Calendar.MINUTE),
                        true
                );
                timePickerDialog.show();
            });

            // Set up confirm button
            confirmExerciseButton.setOnClickListener(v -> {
                if (exerciseTypeSpinner.getSelectedItem() == null) {
                    Toast.makeText(this, "Please select an exercise type.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedStartTime[0].isEmpty()) {
                    Toast.makeText(this, "Please select a start time.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedEndTime[0].isEmpty()) {
                    Toast.makeText(this, "Please select an end time.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Calculate duration from start and end time
                String duration = calculateDurationFromTimes(selectedStartTime[0], selectedEndTime[0]);
                if (duration == null) {
                    Toast.makeText(this, "End time must be after start time.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Gather checked member IDs
                List<Integer> checkedMemberIds = new ArrayList<>();
                for (CheckBox cb : memberCheckBoxes) {
                    if (cb.isChecked()) {
                        checkedMemberIds.add((Integer) cb.getTag());
                    }
                }

                String exerciseType = exerciseTypeSpinner.getSelectedItem().toString();
                String selectedDate = getCurrentSelectedDate();

                if (!checkedMemberIds.isEmpty()) {
                    // User + members
                    saveExerciseWithMembers(exerciseType, selectedStartTime[0], duration, selectedDate, checkedMemberIds);
                } else {
                    // User only
                    saveExerciseUserOnly(exerciseType, selectedStartTime[0], duration, selectedDate);
                }
                exerciseDialog.dismiss();
            });
        });
    }

    private void fetchMembers(Runnable onComplete) {
        String url = "http://192.168.0.16/final%20year%20project/get_user_member_to_meal_plan.php";
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
                                userMembers.add(member);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("ExercisePage", "Error parsing members JSON", e);
                    }
                    if (onComplete != null) onComplete.run();
                },
                error -> {
                    Log.e("ExercisePage", "Error fetching members", error);
                    if (onComplete != null) onComplete.run();
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void fetchExerciseTypes(Spinner spinner, int userId) {
        String url = "http://192.168.0.16/final%20year%20project/retrieve_exercise_type.php";
        
        // Try GET request first, as most retrieve endpoints use GET
        String requestUrl = url + "?user_id=" + userId;
        
        Log.d("ExercisePage", "Fetching exercise types with GET request: " + requestUrl);
        Log.d("ExercisePage", "User ID being sent: " + userId);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestUrl, null,
                response -> {
                    try {
                        Log.d("ExercisePage", "Exercise types response: " + response.toString());
                        if (response.getString("status").equals("success")) {
                            JSONArray exercises = response.getJSONArray("exercises");
                            List<String> exerciseNames = new ArrayList<>();
                            for (int i = 0; i < exercises.length(); i++) {
                                JSONObject exercise = exercises.getJSONObject(i);
                                exerciseNames.add(exercise.getString("name"));
                            }
                            Log.d("ExercisePage", "Found " + exerciseNames.size() + " exercise types: " + exerciseNames);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exerciseNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter);
                        } else {
                            Log.e("ExercisePage", "Exercise types API returned non-success status");
                        }
                    } catch (JSONException e) {
                        Log.e("ExercisePage", "Error parsing exercise types", e);
                    }
                },
                error -> {
                    Log.e("ExercisePage", "Error fetching exercise types with user_id", error);
                    Log.e("ExercisePage", "Error details: " + error.toString());
                    
                    // Try without user_id as fallback
                    Log.d("ExercisePage", "Trying fallback request without user_id");
                    String fallbackUrl = "http://192.168.0.16/final%20year%20project/retrieve_exercise_type.php";
                    JsonObjectRequest fallbackRequest = new JsonObjectRequest(Request.Method.GET, fallbackUrl, null,
                            fallbackResponse -> {
                                try {
                                    Log.d("ExercisePage", "Fallback response: " + fallbackResponse.toString());
                                    if (fallbackResponse.getString("status").equals("success")) {
                                        JSONArray exercises = fallbackResponse.getJSONArray("exercises");
                                        List<String> exerciseNames = new ArrayList<>();
                                        for (int i = 0; i < exercises.length(); i++) {
                                            JSONObject exercise = exercises.getJSONObject(i);
                                            exerciseNames.add(exercise.getString("name"));
                                        }
                                        Log.d("ExercisePage", "Fallback found " + exerciseNames.size() + " exercise types: " + exerciseNames);
                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exerciseNames);
                                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spinner.setAdapter(adapter);
                                    }
                                } catch (JSONException e) {
                                    Log.e("ExercisePage", "Error parsing fallback response", e);
                                }
                            },
                            fallbackError -> {
                                Log.e("ExercisePage", "Fallback request also failed", fallbackError);
                                Log.e("ExercisePage", "Fallback error details: " + fallbackError.toString());
                            }
                    );
                    Volley.newRequestQueue(this).add(fallbackRequest);
                }
        );

        Volley.newRequestQueue(this).add(request);
    }




    private void updateSelectedTimesText(TextView textView, String startTime, String endTime) {
        if (!startTime.isEmpty() && !endTime.isEmpty()) {
            String duration = calculateDurationFromTimes(startTime, endTime);
            if (duration != null) {
                textView.setText("Time: " + startTime + " - " + endTime + " (" + duration + ")");
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setText("Invalid time range");
                textView.setVisibility(View.VISIBLE);
            }
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    private String calculateDurationFromTimes(String startTime, String endTime) {
        try {
            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");
            
            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);
            
            // Convert to minutes for easier calculation
            int startTotalMinutes = startHour * 60 + startMinute;
            int endTotalMinutes = endHour * 60 + endMinute;
            
            // Handle case where end time is on the next day
            if (endTotalMinutes <= startTotalMinutes) {
                endTotalMinutes += 24 * 60; // Add 24 hours
            }
            
            int durationMinutes = endTotalMinutes - startTotalMinutes;
            
            if (durationMinutes <= 0) {
                return null; // Invalid duration
            }
            
            int hours = durationMinutes / 60;
            int minutes = durationMinutes % 60;
            
            if (hours > 0 && minutes > 0) {
                return hours + "h " + minutes + "m";
            } else if (hours > 0) {
                return hours + "h";
            } else {
                return minutes + "m";
            }
        } catch (Exception e) {
            Log.e("ExercisePage", "Error calculating duration", e);
            return null;
        }
    }

    private int convertDurationToMinutes(String duration) {
        try {
            int totalMinutes = 0;
            String[] parts = duration.split(" ");
            
            for (String part : parts) {
                if (part.endsWith("h")) {
                    int hours = Integer.parseInt(part.replace("h", ""));
                    totalMinutes += hours * 60;
                } else if (part.endsWith("m")) {
                    int minutes = Integer.parseInt(part.replace("m", ""));
                    totalMinutes += minutes;
                }
            }
            
            return totalMinutes;
        } catch (Exception e) {
            Log.e("ExercisePage", "Error converting duration to minutes", e);
            return 0;
        }
    }

    private String formatDurationString(int minutes) {
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        
        if (hours > 0 && remainingMinutes > 0) {
            return hours + "h " + remainingMinutes + "m";
        } else if (hours > 0) {
            return hours + "h";
        } else {
            return remainingMinutes + "m";
        }
    }

    private String getCurrentSelectedDate() {
        Calendar selectedDay = (Calendar) currentWeekStart.clone();
        selectedDay.add(Calendar.DATE, selectedDayIndex);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDay.getTime());
    }

    private void saveExerciseWithMembers(String exerciseType, String startTime, String duration, String date, List<Integer> memberIds) {
        String url = "http://192.168.0.16/final%20year%20project/save_exercise.php";
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", userId);
            data.put("exercise_name", exerciseType);
            data.put("start_time", startTime);
            // Convert duration string to minutes
            int durationMinutes = convertDurationToMinutes(duration);
            data.put("duration_minutes", durationMinutes);
            data.put("exercise_date", date);
            
            JSONArray memberIdsArray = new JSONArray();
            for (Integer memberId : memberIds) {
                memberIdsArray.put(memberId);
            }
            data.put("member_ids", memberIdsArray);
            
            Log.d("ExercisePage", "Saving exercise with members - URL: " + url);
            Log.d("ExercisePage", "Saving exercise with members - Data: " + data.toString());
        } catch (JSONException e) {
            Log.e("ExercisePage", "JSON build error", e);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            Toast.makeText(this, "Exercise saved!", Toast.LENGTH_SHORT).show();
                            displaySelectedDayExercise();
                        } else {
                            Toast.makeText(this, "Failed to save exercise", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error saving exercise", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void saveExerciseUserOnly(String exerciseType, String startTime, String duration, String date) {
        String url = "http://192.168.0.16/final%20year%20project/save_exercise_user.php";
        JSONObject data = new JSONObject();
        try {
            data.put("user_id", userId);
            data.put("exercise_name", exerciseType);
            data.put("start_time", startTime);
            // Convert duration string to minutes
            int durationMinutes = convertDurationToMinutes(duration);
            data.put("duration_minutes", durationMinutes);
            data.put("exercise_date", date);
            
            Log.d("ExercisePage", "Saving exercise user only - URL: " + url);
            Log.d("ExercisePage", "Saving exercise user only - Data: " + data.toString());
        } catch (JSONException e) {
            Log.e("ExercisePage", "JSON build error", e);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            Toast.makeText(this, "Exercise saved!", Toast.LENGTH_SHORT).show();
                            displaySelectedDayExercise();
                        } else {
                            Toast.makeText(this, "Failed to save exercise", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error saving exercise", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void showExerciseMenu(Exercise exercise, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("Delete Exercise");
        popup.getMenu().add("Edit Exercise");
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Delete Exercise")) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Exercise")
                        .setMessage("Are you sure you want to delete this exercise?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            deleteExercise(exercise);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            } else if (item.getTitle().equals("Edit Exercise")) {
                Toast.makeText(this, "Edit functionality coming soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private void deleteExercise(Exercise exercise) {
        String url = "http://192.168.0.16/final%20year%20project/delete_exercise.php";
        JSONObject data = new JSONObject();
        try {
            data.put("exercise_id", exercise.getId());
            data.put("user_id", userId);
        } catch (JSONException e) {
            Log.e("ExercisePage", "JSON build error", e);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            Toast.makeText(this, "Exercise deleted", Toast.LENGTH_SHORT).show();
                            displaySelectedDayExercise();
                        } else {
                            Toast.makeText(this, "Failed to delete exercise", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error deleting exercise", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void setupNavigation() {
        TextView mainPage = findViewById(R.id.text_menu);
        mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(exercise_page.this, MainScreen.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView mealPlan = findViewById(R.id.meal_plan);
        mealPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(exercise_page.this, meal_plan.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView main_screen = findViewById(R.id.home_logo_footer);
        main_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(exercise_page.this, MainScreen.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView grocery_list = findViewById(R.id.grocery_logo);
        grocery_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(exercise_page.this, groceryList.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView accountPage = findViewById(R.id.account_setting);
        accountPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(exercise_page.this, user_setting.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });



        Button nutrition_analysis = findViewById(R.id.nutrition_analysis);
        nutrition_analysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(exercise_page.this, mealPlan_nutrition_analysis.class);
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
                Intent intent = new Intent(exercise_page.this, com.example.finalyearproject.exercise_page.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });
    }
}
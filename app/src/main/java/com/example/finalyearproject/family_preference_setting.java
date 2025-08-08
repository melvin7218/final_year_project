package com.example.finalyearproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class family_preference_setting extends AppCompatActivity {

    private EditText etMemberName;
    private EditText etAllergy;
    private RadioGroup rgGender;
    private Spinner spActivityLevel;
    private Button btnSave;
    private Button btnSelectAge;
    private TextView tvSelectedAge;

    private Button btnSelectHeight;
    private TextView tvSelectedHeight;

    private Button btnSelectWeight;
    private TextView tvSelectedWeight;
    private Button btnSelectAllergy;
    private TextView tvSelectedAllergy;

    private Button btnSelectCuisineType;
    private TextView tvSelectedCuisineType;

    private int userId;
    private int memberId = -1; // Will be set if editing existing member
    private boolean isEditing = false;

    private static final String PREFS_NAME = "user_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_preference_setting);

        // Initialize UI elements
        etMemberName = findViewById(R.id.etMemberName);
        btnSelectHeight = findViewById(R.id.btnSelectHeight);
        tvSelectedHeight = findViewById(R.id.tvSelectedHeight);
        btnSelectAllergy = findViewById(R.id.btnSelectAllergy);
        tvSelectedAllergy = findViewById(R.id.tvSelectedAllergy);
        btnSelectCuisineType = findViewById(R.id.btnSelectCuisineType);
        tvSelectedCuisineType = findViewById(R.id.tvSelectedCuisineType);
        btnSelectWeight = findViewById(R.id.btnSelectWeight);
        tvSelectedWeight = findViewById(R.id.tvSelectedWeight);
        btnSelectAge = findViewById(R.id.btnSelectAge);
        tvSelectedAge = findViewById(R.id.tvSelectedAge);
        rgGender = findViewById(R.id.rgGender);
        spActivityLevel = findViewById(R.id.spActivityLevel);
        btnSave = findViewById(R.id.btnSave);

        // Get user ID from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if we're editing an existing member
        Intent intent = getIntent();
        if (intent.hasExtra("member_id")) {
            memberId = intent.getIntExtra("member_id", -1);
            isEditing = true;
            
            // Set member name if provided
            if (intent.hasExtra("member_name")) {
                String memberName = intent.getStringExtra("member_name");
                etMemberName.setText(memberName);
            }
            
            loadMemberData(memberId);
        }

        // Set up click listeners
        btnSelectAge.setOnClickListener(v -> showAgePickerDialog());
        btnSelectHeight.setOnClickListener(v -> showHeightPickerDialog());
        btnSelectWeight.setOnClickListener(v -> showWeightPickerDialog());
        btnSelectAllergy.setOnClickListener(v -> showAllergyDialog());
        btnSelectCuisineType.setOnClickListener(v -> showCuisineDialog());
        btnSave.setOnClickListener(v -> saveMemberPreference());

        // Back button
        ImageView backButton = findViewById(R.id.userPreference_back);
        backButton.setOnClickListener(v -> finish());
    }

    private void loadMemberData(int memberId) {
        String url = "http://172.16.62.183/Final%20Year%20Project/get_family_member.php?user_id=" + userId + "&member_id=" + memberId;
        
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray membersArray = response.getJSONArray("members");
                            if (membersArray.length() > 0) {
                                JSONObject member = membersArray.getJSONObject(0);
                                
                                // Set member name
                                String memberName = member.getString("member_name");
                                etMemberName.setText(memberName);
                                
                                // Set age
                                if (member.has("age") && !member.isNull("age")) {
                                    int age = member.getInt("age");
                                    tvSelectedAge.setText("Age: " + age);
                                }
                                
                                // Set height
                                if (member.has("height") && !member.isNull("height")) {
                                    int height = member.getInt("height");
                                    tvSelectedHeight.setText("Height: " + height);
                                }
                                
                                // Set weight
                                if (member.has("weight") && !member.isNull("weight")) {
                                    int weight = member.getInt("weight");
                                    tvSelectedWeight.setText("Weight: " + weight);
                                }
                                
                                // Set gender
                                if (member.has("gender") && !member.isNull("gender")) {
                                    String gender = member.getString("gender");
                                    if (gender.equalsIgnoreCase("male")) {
                                        rgGender.check(R.id.rbMale);
                                    } else if (gender.equalsIgnoreCase("female")) {
                                        rgGender.check(R.id.rbFemale);
                                    }
                                }
                                
                                // Set activity level
                                if (member.has("activity_factor") && !member.isNull("activity_factor")) {
                                    double activityFactor = member.getDouble("activity_factor");
                                    setActivityLevelFromFactor(activityFactor);
                                }
                                
                                // Set cuisine type
                                if (member.has("cuisine_type") && !member.isNull("cuisine_type")) {
                                    String cuisineType = member.getString("cuisine_type");
                                    tvSelectedCuisineType.setText(cuisineType);
                                }
                                
                                // Set allergy ingredients
                                if (member.has("allergy_ingredients") && !member.isNull("allergy_ingredients")) {
                                    String allergyIngredients = member.getString("allergy_ingredients");
                                    if (!allergyIngredients.equals("null")) {
                                        tvSelectedAllergy.setText(allergyIngredients);
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Failed to load member data", Toast.LENGTH_SHORT).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void setActivityLevelFromFactor(double activityFactor) {
        String activityLevel;
        if (activityFactor == 1.2) activityLevel = "Sedentary";
        else if (activityFactor == 1.375) activityLevel = "Lightly Active";
        else if (activityFactor == 1.550) activityLevel = "Moderately Active";
        else if (activityFactor == 1.725) activityLevel = "Very Active";
        else if (activityFactor == 1.9) activityLevel = "Extra Active";
        else activityLevel = "Sedentary";

        // Set spinner to the correct activity level
        String[] activityLevels = getResources().getStringArray(R.array.activity_levels);
        for (int i = 0; i < activityLevels.length; i++) {
            if (activityLevels[i].equals(activityLevel)) {
                spActivityLevel.setSelection(i);
                break;
            }
        }
    }

    private void showAgePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Age");

        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(100);
        numberPicker.setValue(20);
        numberPicker.setWrapSelectorWheel(false);

        builder.setView(numberPicker);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            int selectAge = numberPicker.getValue();
            tvSelectedAge.setText("Age: " + selectAge);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showHeightPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Height(cm)");

        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(100);
        numberPicker.setMaxValue(200);
        numberPicker.setValue(160);
        numberPicker.setWrapSelectorWheel(false);

        builder.setView(numberPicker);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            int selectHeight = numberPicker.getValue();
            tvSelectedHeight.setText("Height: " + selectHeight);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showWeightPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Weight(kg)");

        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(30);
        numberPicker.setMaxValue(200);
        numberPicker.setValue(50);
        numberPicker.setWrapSelectorWheel(false);

        builder.setView(numberPicker);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            int selectWeight = numberPicker.getValue();
            tvSelectedWeight.setText("Weight: " + selectWeight);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showAllergyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Allergy Ingredients");

        String[] commonAllergies = {
                "Peanuts", "Tree nuts", "Milk", "Eggs", "Wheat", "Soy",
                "Fish", "Shellfish", "Sesame", "Gluten", "Corn", "Mustard"
        };
        boolean[] checkedItems = new boolean[commonAllergies.length];
        List<String> selectedItems = new ArrayList<>();

        // Layout container
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Create list of checkboxes manually
        for (int i = 0; i < commonAllergies.length; i++) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(commonAllergies[i]);
            int index = i;
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedItems.add(commonAllergies[index]);
                } else {
                    selectedItems.remove(commonAllergies[index]);
                }
            });
            layout.addView(checkBox);
        }

        // Custom EditText
        EditText inputOther = new EditText(this);
        inputOther.setHint("Other allergy (optional)");
        layout.addView(inputOther);

        builder.setView(layout);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String other = inputOther.getText().toString().trim();
            if (!other.isEmpty()) selectedItems.add(other);

            String result = String.join(", ", selectedItems);
            tvSelectedAllergy.setText(result.isEmpty() ? "None selected" : result);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showCuisineDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Favorite Type of Cuisine");

        String[] cuisineType = {
                "chinese", "japanese", "korean", "thai", "indian"
        };
        boolean[] checkedItems = new boolean[cuisineType.length];
        List<String> selectedItems = new ArrayList<>();

        // Layout container
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        // Create checkboxes
        for (int i = 0; i < cuisineType.length; i++) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(cuisineType[i]);
            int index = i;
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedItems.add(cuisineType[index]);
                } else {
                    selectedItems.remove(cuisineType[index]);
                }
            });
            layout.addView(checkBox);
        }

        builder.setView(layout);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String result = String.join(", ", selectedItems);
            tvSelectedCuisineType.setText(result.isEmpty() ? "None selected" : result);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void saveMemberPreference() {
        try {
            String memberName = etMemberName.getText().toString().trim();
            if (memberName.isEmpty()) {
                Toast.makeText(this, "Please enter member name", Toast.LENGTH_SHORT).show();
                return;
            }

            String allergy = tvSelectedAllergy.getText().toString();
            if (allergy.equals("None selected") || allergy.isEmpty()) {
                allergy = "null";
            }

            String cuisineType = tvSelectedCuisineType.getText().toString();
            if (cuisineType.equals("None selected") || cuisineType.isEmpty()) {
                cuisineType = "null";
            }

            String ageText = tvSelectedAge.getText().toString().replace("Age: ", "").trim();
            int age;
            try {
                age = Integer.parseInt(ageText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please select a valid age", Toast.LENGTH_SHORT).show();
                return;
            }

            String heightText = tvSelectedHeight.getText().toString().replace("Height: ", "").trim();
            int height;
            try {
                height = Integer.parseInt(heightText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please select a valid height", Toast.LENGTH_SHORT).show();
                return;
            }

            String weightText = tvSelectedWeight.getText().toString().replace("Weight: ", "").trim();
            int weight;
            try {
                weight = Integer.parseInt(weightText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please select a valid weight", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedGenderID = rgGender.getCheckedRadioButtonId();
            if (selectedGenderID == -1) {
                Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton rbGender = findViewById(selectedGenderID);
            String gender = rbGender.getText().toString();

            String activityLevel = spActivityLevel.getSelectedItem().toString();
            double activityFactor = getActivityFactor(activityLevel);

            // Create JSON object for the member data
            JSONObject memberData = new JSONObject();
            memberData.put("user_id", userId);
            memberData.put("member_name", memberName);
            memberData.put("age", age);
            memberData.put("height", height);
            memberData.put("weight", weight);
            memberData.put("gender", gender);
            memberData.put("activity_factor", activityFactor);
            memberData.put("cuisine_type", cuisineType);
            memberData.put("allergy_ingredients", allergy);

            if (isEditing && memberId != -1) {
                memberData.put("member_id", memberId);
            }

            sendToServer(memberData);

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private double getActivityFactor(String activityLevel) {
        switch (activityLevel) {
            case "Sedentary": return 1.2;
            case "Lightly Active": return 1.375;
            case "Moderately Active": return 1.550;
            case "Very Active": return 1.725;
            case "Extra Active": return 1.9;
            default: return 1.2;
        }
    }

    private void sendToServer(JSONObject memberData) {
        String url = "http://172.16.62.183/Final%20Year%20Project/save_family_preference.php";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                memberData,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Member preferences saved!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String message = response.getString("message");
                            Toast.makeText(this, "Failed: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Response error", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                });

        queue.add(request);
    }
}
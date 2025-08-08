package com.example.finalyearproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class userPreferenceActivity extends AppCompatActivity {

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


    private static final String PREFS_NAME = "user_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_preference);
        btnSelectHeight= findViewById(R.id.btnSelectHeight);
        tvSelectedHeight = findViewById(R.id.tvSelectedHeight);
        btnSelectAllergy = findViewById(R.id.btnSelectAllergy);
        tvSelectedAllergy = findViewById(R.id.tvSelectedAllergy);
        btnSelectCuisineType= findViewById(R.id.btnSelectCuisineType);
        tvSelectedCuisineType= findViewById(R.id.tvSelectedCuisineType);
        btnSelectWeight= findViewById(R.id.btnSelectWeight);
        tvSelectedWeight = findViewById(R.id.tvSelectedWeight);
        btnSelectAge = findViewById(R.id.btnSelectAge);
        tvSelectedAge = findViewById(R.id.tvSelectedAge);
        rgGender = findViewById(R.id.rgGender);
        spActivityLevel = findViewById(R.id.spActivityLevel);
        btnSave = findViewById(R.id.btnSave);



        //handle the dialog
        btnSelectAge.setOnClickListener(v -> showAgePickerDialog());
        btnSelectHeight.setOnClickListener(v -> showHeightPickerDialog());
        btnSelectWeight.setOnClickListener(v -> showWeightPickerDialog());
        btnSelectAllergy.setOnClickListener(v -> showAllergyDialog());
        btnSelectCuisineType.setOnClickListener(v -> showCuisineDialog());

        btnSave.setOnClickListener(v -> saveUserPreference());
    }



    private void showAgePickerDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Age");

        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(100);
        numberPicker.setValue(20);
        numberPicker.setWrapSelectorWheel(false);

        builder.setView(numberPicker);

        builder.setPositiveButton("confirm", (dialog, which) -> {
            int selectAge = numberPicker.getValue();
            tvSelectedAge.setText("Age: "+selectAge);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showHeightPickerDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Height(cm)");

        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(100);
        numberPicker.setMaxValue(200);
        numberPicker.setValue(160);
        numberPicker.setWrapSelectorWheel(false);

        builder.setView(numberPicker);

        builder.setPositiveButton("confirm", (dialog, which) -> {
            int selectHeight = numberPicker.getValue();
            tvSelectedHeight.setText("Height: "+selectHeight);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showWeightPickerDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Weight(kg)");

        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(30);
        numberPicker.setMaxValue(200);
        numberPicker.setValue(50);
        numberPicker.setWrapSelectorWheel(false);

        builder.setView(numberPicker);

        builder.setPositiveButton("confirm", (dialog, which) -> {
            int selectWeight = numberPicker.getValue();
            tvSelectedWeight.setText("Weight: "+selectWeight);
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

        builder.setView(layout); // Attach layout to dialog

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String result = String.join(", ", selectedItems);
            tvSelectedCuisineType.setText(result.isEmpty() ? "None selected" : result);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }




    private void saveUserPreference() {
        try {
            String allergy = tvSelectedAllergy.getText().toString();
            if (allergy.isEmpty()) {
                allergy = "null";
            }

            String cuisineType = tvSelectedCuisineType.getText().toString();
            if (cuisineType.isEmpty()) {
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

            userPreference userPreference = new userPreference(
                    allergy, cuisineType, age, height, weight, gender, activityFactor);
            sendToServer(userPreference);

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

    private void sendToServer(userPreference userPreference) {
        String url = "http://192.168.0.130/Final%20Year%20Project/save_user_preference.php";

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject requestData = userPreference.toJson();
            requestData.put("user_id", userId);

            RequestQueue queue = Volley.newRequestQueue(this);
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    requestData,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show();
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
        } catch (JSONException e) {
            Toast.makeText(this, "Data preparation error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}

package com.example.finalyearproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class add_recipe_page extends AppCompatActivity {

    private RelativeLayout ingredientLayout;
    private RelativeLayout instructionLayout;
    private Button btnAddIngredient;
    private Button btnAddInstruction;
    private int ingredientCount = 1;
    private int instructionCount = 1;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private Uri imageUri;
    private ImageView addRecipeImage;
    private Bitmap selectedBitmap;
    private AutoCompleteTextView ingredientInput;
    private List<String> ingredientSuggestions = new ArrayList<>();
    private ArrayAdapter<String> ingredientAdapter;

    private Button btnUpload;
    private EditText titleEditText, descriptionEditText, timeEditText;
    private static final String UPLOAD_URL = "http://172.16.62.183/Final%20Year%20Project/add_recipe.php";

    // Constants for JSON keys
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_TIME_RECIPE = "time_recipe";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_INGREDIENTS = "ingredients";
    private static final String KEY_INSTRUCTIONS = "instructions";
    private static final String KEY_VISIBILITY = "visibility";

    private static final String SPOONACULAR_API_KEY = "d4574df105914e0fa1cb3c47bd80ea00";
    private static final String INGREDIENT_SUGGEST_URL = "http://172.16.62.183/Final%20Year%20Project/autocomplete_ingredient.php";
    private int userId;
    private JSONArray ingredientArray;
    private String ingredientListString;
    private Spinner timeSpinner, cuisineSpinner, measurementSpinner;

    private ProgressDialog progressDialog;
    private double totalCalories = 0, totalFat = 0, totalProtein = 0, totalCarbs = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe_page);

        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ingredientLayout = findViewById(R.id.ingredient_layout);
        instructionLayout = findViewById(R.id.instruction_layout);
        btnAddIngredient = findViewById(R.id.btnAddIngredient);
        btnAddInstruction = findViewById(R.id.btnAddInstruction);
        titleEditText = findViewById(R.id.addRecipe_etTitle);
        descriptionEditText = findViewById(R.id.addRecipe_etDescription);
        timeEditText = findViewById(R.id.addRecipe_etTime);
        btnUpload = findViewById(R.id.addRecipe_btnUpload);
        timeSpinner = findViewById(R.id.addRecipe_timeUnit);
        cuisineSpinner = findViewById(R.id.addRecipe_spCuisine);
        measurementSpinner = findViewById(R.id.addRecipe_measurement);

        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this,
                R.array.time_units, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);

        ArrayAdapter<CharSequence> cuisineAdapter = ArrayAdapter.createFromResource(this, R.array.cuisine_type, android.R.layout.simple_spinner_item);
        cuisineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cuisineSpinner.setAdapter(cuisineAdapter);

        ArrayAdapter<CharSequence> measurementAdapter = ArrayAdapter.createFromResource(this, R.array.ingredient_measurement, android.R.layout.simple_spinner_item);
        measurementAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        measurementSpinner.setAdapter(measurementAdapter);

        addRecipeImage = findViewById(R.id.addRecipe_image);
        addRecipeImage.setOnClickListener(view -> showImagePickerDialog());

        btnAddIngredient.setOnClickListener(v -> addNewIngredientField());
        btnAddInstruction.setOnClickListener(v -> addNewInstructionField());

        btnUpload.setOnClickListener(view -> uploadRecipe());

        Button back = findViewById(R.id.addRecipe_btnCancel);
        back.setOnClickListener(view -> {
            Intent intent = new Intent(add_recipe_page.this, MainScreen.class);
            startActivity(intent);
        });

        ingredientInput = findViewById(R.id.name_ingeredient1);
        ingredientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ingredientSuggestions);
        ingredientInput.setAdapter(ingredientAdapter);
        ingredientInput.setThreshold(1);
        ingredientInput.setDropDownHeight(200);
        ingredientInput.setDropDownAnchor(R.id.ingredient_layout);

        ingredientInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 1) {
                    fetchIngredientSuggestions(s.toString(), ingredientAdapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ingredientInput.setOnItemClickListener((parent, view, position, id) -> {
            String selected = parent.getItemAtPosition(position).toString();
            ingredientInput.setText(selected);
            ingredientInput.clearFocus();
        });
    }

    private void fetchIngredientSuggestions(String query, ArrayAdapter<String> adapter) {
        if (query.length() < 1) return;
        String url = INGREDIENT_SUGGEST_URL + "?q=" + java.net.URLEncoder.encode(query);
        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.GET, url,
                response -> {
                    try {
                        org.json.JSONArray arr = new org.json.JSONArray(response);
                        List<String> suggestions = new ArrayList<>();
                        for (int i = 0; i < arr.length() && i < 3; i++) {
                            suggestions.add(arr.getString(i));
                        }
                        adapter.clear();
                        adapter.addAll(suggestions);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        // Ignore parse errors
                    }
                },
                error -> {
                    // Ignore network errors
                }
        );
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private String capitalizeWords(String input) {
        String[] words = input.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            sb.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private boolean validateAllFields() {
        if (!validateTextField(titleEditText, "Recipe title is required")) {
            return false;
        }

        if (!validateTextField(descriptionEditText, "Recipe Description is required")) {
            return false;
        }

        String timeValue = timeEditText.getText().toString().trim();
        if (timeValue.isEmpty()) {
            timeEditText.setError("Preparation time is required");
            timeEditText.requestFocus();
            return false;
        }
        try {
            Integer.parseInt(timeValue);
        } catch (NumberFormatException e) {
            timeEditText.setError("Please enter a valid number");
            timeEditText.requestFocus();
            return false;
        }

        if (!validateIngredients()) {
            return false;
        }

        if (!validateInstruction()) {
            return false;
        }

        return true;
    }

    private boolean validateTextField(EditText editText, String errorMessage) {
        String text = editText.getText().toString().trim();
        if (text.isEmpty()) {
            editText.setError(errorMessage);
            editText.requestFocus();
            return false;
        }

        if (text.matches("\\d+")) {
            editText.setError("This field required text, not just numbers");
            editText.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateIngredients() {
        boolean isValid = true;

        for (int i = 0; i < ingredientLayout.getChildCount() - 1; i++) {
            View view = ingredientLayout.getChildAt(i);
            if (view instanceof RelativeLayout) {
                EditText amountEditText = (EditText) ((RelativeLayout) view).getChildAt(0);
                EditText ingredientET = (EditText) ((RelativeLayout) view).getChildAt(1);

                String amount = amountEditText.getText().toString().trim();
                if (amount.isEmpty()) {
                    amountEditText.setError("Amount is required");
                    amountEditText.requestFocus();
                    isValid = false;
                }

                String ingredient = ingredientET.getText().toString().trim();
                if (ingredient.isEmpty()) {
                    ingredientET.setError("Ingredient name is required");
                    if (isValid) {
                        ingredientET.requestFocus();
                    }
                    isValid = false;
                } else if (ingredient.matches("\\d+")) {
                    ingredientET.setError("Ingredient name must contain text");
                    if (isValid) {
                        ingredientET.requestFocus();
                    }
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    private boolean validateInstruction() {
        boolean isValid = true;

        for (int i = 0; i < instructionLayout.getChildCount() - 1; i++) {
            View view = instructionLayout.getChildAt(i);
            if (view instanceof RelativeLayout) {
                EditText instructionET = (EditText) ((RelativeLayout) view).getChildAt(0);
                String instruction = instructionET.getText().toString().trim();
                if (instruction.isEmpty()) {
                    instructionET.setError("Instruction is required");
                    if (isValid) {
                        instructionET.requestFocus();
                    }
                    isValid = false;
                } else if (instruction.matches("\\d+")) {
                    instructionET.setError("Instruction must contain text");
                    if (isValid) {
                        instructionET.requestFocus();
                    }
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    private void uploadRecipe() {
        if (!validateAllFields()) return;

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String timeValue = timeEditText.getText().toString().trim();
        String encodedImage = selectedBitmap != null ? encodeImage(selectedBitmap) : "";
        String timeUnit = timeSpinner.getSelectedItem().toString();
        String time = timeValue + " " + timeUnit;
        String cuisineType = cuisineSpinner.getSelectedItem().toString();

        RadioGroup dietaryGroup = findViewById(R.id.addRecipe_rgDietary);
        int selectedDietaryId = dietaryGroup.getCheckedRadioButtonId();
        String dietaryType = ((RadioButton) findViewById(selectedDietaryId)).getText().toString();

        RadioGroup visibilityGroup = findViewById(R.id.addRecipe_rgVisibility);
        int selectedVisibilityId = visibilityGroup.getCheckedRadioButtonId();
        String visibility = ((RadioButton) findViewById(selectedVisibilityId)).getText().toString();

        // Ingredients
        JSONArray ingredientArray = new JSONArray();
        for (int i = 0; i < ingredientLayout.getChildCount() - 1; i++) {
            View view = ingredientLayout.getChildAt(i);
            if (view instanceof RelativeLayout) {
                EditText amountEditText = (EditText) ((RelativeLayout) view).getChildAt(0);
                AutoCompleteTextView ingredientAutoCompleteTextView = (AutoCompleteTextView) ((RelativeLayout) view).getChildAt(1);
                Spinner measurementSpinner = (Spinner) ((RelativeLayout) view).getChildAt(2);

                String amount = amountEditText.getText().toString().trim();
                String ingredientName = ingredientAutoCompleteTextView.getText().toString().trim();
                String measurement = measurementSpinner.getSelectedItem().toString();

                if (amount.isEmpty() || ingredientName.isEmpty()) {
                    Toast.makeText(add_recipe_page.this, "Please enter a valid amount and ingredient name for item " + (i + 1), Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    JSONObject ingredientObject = new JSONObject();
                    ingredientObject.put("amount", amount);
                    ingredientObject.put("unit", measurement);
                    ingredientObject.put("ingredient_name", ingredientName);
                    ingredientArray.put(ingredientObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(add_recipe_page.this, "Error adding ingredient: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        // Instructions
        JSONArray instructionArray = new JSONArray();
        for (int i = 0; i < instructionLayout.getChildCount() - 1; i++) {
            View view = instructionLayout.getChildAt(i);
            if (view instanceof RelativeLayout) {
                EditText instructionEditText = (EditText) ((RelativeLayout) view).getChildAt(0);
                String instruction = instructionEditText.getText().toString().trim();

                if (instruction.isEmpty()) {
                    Toast.makeText(add_recipe_page.this, "Please enter an instruction for step " + (i + 1), Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    JSONObject instructionObject = new JSONObject();
                    instructionObject.put("instruction", instruction);
                    instructionArray.put(instructionObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(add_recipe_page.this, "Error adding instruction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        // Show loading
        showProgressDialog();
        btnUpload.setEnabled(false);

        // Upload full recipe with nutrition
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                response -> {
                    hideProgressDialog();
                    btnUpload.setEnabled(true);
                    Log.d("UploadRecipe", "Server Response: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");
                        Toast.makeText(add_recipe_page.this, message, Toast.LENGTH_SHORT).show();

                        if (status.equals("success")) {
                            finish(); // Close the page
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(add_recipe_page.this, "Error parsing server response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    hideProgressDialog();
                    btnUpload.setEnabled(true);
                    String errorMessage = error.getMessage();
                    if (error instanceof TimeoutError) {
                        errorMessage = "Request timeout. Please try again later.";
                    } else if (error instanceof NoConnectionError) {
                        errorMessage = "No internet connection. Please check your network.";
                    }
                    Toast.makeText(add_recipe_page.this, "Upload failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }

            @Override
            public byte[] getBody() {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("user_id", userId);
                    jsonBody.put("title", title);
                    jsonBody.put("description", description);
                    jsonBody.put("time_recipe", time);
                    if (!encodedImage.isEmpty()) {
                        jsonBody.put("image", encodedImage);
                    }
                    jsonBody.put("ingredients", ingredientArray);
                    jsonBody.put("instructions", instructionArray);
                    jsonBody.put("cuisine_type", cuisineType);
                    jsonBody.put("dietary", dietaryType);
                    jsonBody.put("visibility", visibility);



                    Log.d("UploadRecipe", "JSON Payload: " + jsonBody.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonBody.toString().getBytes(StandardCharsets.UTF_8);
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading recipe...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image")
                .setItems(new CharSequence[]{"Choose from Gallery", "Take a photo"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (which == 0) {
                            openGallery();
                        } else {
                            openCamera();
                        }
                    }
                })
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                imageUri = data.getData();
                try {
                    selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    addRecipeImage.setImageBitmap(selectedBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CAPTURE_IMAGE_REQUEST && data.getExtras() != null) {
                selectedBitmap = (Bitmap) data.getExtras().get("data");
                addRecipeImage.setImageBitmap(selectedBitmap);
            }
        }
    }

    private void addNewIngredientField() {
        ingredientCount++;

        RelativeLayout newIngredientLayout = new RelativeLayout(this);
        newIngredientLayout.setId(View.generateViewId());

        // Amount
        EditText amountEditText = new EditText(this);
        amountEditText.setId(View.generateViewId());
        amountEditText.setHint("Amount");
        amountEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amountEditText.setBackgroundResource(R.drawable.button_passthrough_state);
        amountEditText.setBackgroundTintList(getResources().getColorStateList(R.color.editTextColor));
        RelativeLayout.LayoutParams amountParams = new RelativeLayout.LayoutParams(dpToPx(80), ViewGroup.LayoutParams.WRAP_CONTENT);
        amountParams.setMargins(0, dpToPx(10), 0, 0);
        amountEditText.setLayoutParams(amountParams);

        // Ingredient name
        AutoCompleteTextView nameEditText = new AutoCompleteTextView(this);
        nameEditText.setId(View.generateViewId());
        nameEditText.setHint("Ingredient");
        nameEditText.setBackgroundResource(R.drawable.button_passthrough_state);
        nameEditText.setBackgroundTintList(getResources().getColorStateList(R.color.editTextColor));

        // Create a new adapter for each field
        ArrayAdapter<String> newAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        nameEditText.setAdapter(newAdapter);

        nameEditText.setThreshold(1);

        // Use backend-powered autocomplete for each new field
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 1) {
                    fetchIngredientSuggestions(s.toString(), newAdapter);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        nameEditText.setOnItemClickListener((parent, view, position, id) -> {
            String selected = parent.getItemAtPosition(position).toString();
            nameEditText.setText(selected);
            nameEditText.clearFocus();
        });

        RelativeLayout.LayoutParams nameParams = new RelativeLayout.LayoutParams(dpToPx(170), ViewGroup.LayoutParams.WRAP_CONTENT);
        nameParams.setMargins(dpToPx(10), dpToPx(10), 0, 0);
        nameParams.addRule(RelativeLayout.RIGHT_OF, amountEditText.getId());
        nameEditText.setLayoutParams(nameParams);

        // Spinner for measurement unit
        Spinner measurementSpinner = new Spinner(this);
        measurementSpinner.setId(View.generateViewId());
        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(this,
                R.array.ingredient_measurement, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        measurementSpinner.setAdapter(unitAdapter);
        RelativeLayout.LayoutParams spinnerParams = new RelativeLayout.LayoutParams(dpToPx(80), ViewGroup.LayoutParams.WRAP_CONTENT);
        spinnerParams.setMargins(dpToPx(10), dpToPx(10), dpToPx(10), 0);
        spinnerParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        measurementSpinner.setLayoutParams(spinnerParams);
        measurementSpinner.setBackgroundResource(R.drawable.button_passthrough_state);

        // Delete button
        ImageView deleteImageView = new ImageView(this);
        deleteImageView.setId(View.generateViewId());
        deleteImageView.setImageResource(R.drawable.dustbin_logo);
        deleteImageView.setBackgroundResource(R.drawable.button_passthrough_state);
        deleteImageView.setBackgroundTintList(getResources().getColorStateList(R.color.editTextColor));
        RelativeLayout.LayoutParams deleteParams = new RelativeLayout.LayoutParams(dpToPx(80), dpToPx(30));
        deleteParams.setMargins(dpToPx(5), dpToPx(10), 0, 0);
        deleteParams.addRule(RelativeLayout.BELOW, amountEditText.getId());
        deleteParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        deleteImageView.setLayoutParams(deleteParams);

        // Add views to the layout
        newIngredientLayout.addView(amountEditText);
        newIngredientLayout.addView(nameEditText);
        newIngredientLayout.addView(measurementSpinner);
        newIngredientLayout.addView(deleteImageView);

        // Position layout
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (ingredientLayout.getChildCount() > 1) {
            View last = ingredientLayout.getChildAt(ingredientLayout.getChildCount() - 2);
            layoutParams.addRule(RelativeLayout.BELOW, last.getId());
        } else {
            layoutParams.addRule(RelativeLayout.BELOW, R.id.ingredient1);
        }
        newIngredientLayout.setLayoutParams(layoutParams);

        ingredientLayout.addView(newIngredientLayout, ingredientLayout.getChildCount() - 1);

        // Reposition +Add Ingredient button
        RelativeLayout.LayoutParams buttonParams = (RelativeLayout.LayoutParams) btnAddIngredient.getLayoutParams();
        buttonParams.addRule(RelativeLayout.BELOW, newIngredientLayout.getId());
        btnAddIngredient.setLayoutParams(buttonParams);

        // Delete logic
        deleteImageView.setOnClickListener(v -> {
            ingredientLayout.removeView(newIngredientLayout);
            View last = (ingredientLayout.getChildCount() > 1)
                    ? ingredientLayout.getChildAt(ingredientLayout.getChildCount() - 2)
                    : findViewById(R.id.ingredient1);

            RelativeLayout.LayoutParams newButtonParams = (RelativeLayout.LayoutParams) btnAddIngredient.getLayoutParams();
            newButtonParams.addRule(RelativeLayout.BELOW, last.getId());
            btnAddIngredient.setLayoutParams(newButtonParams);
        });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void addNewInstructionField() {
        instructionCount++;

        RelativeLayout newInstructionLayout = new RelativeLayout(this);
        newInstructionLayout.setId(View.generateViewId());

        EditText instructionEditText = new EditText(this);
        instructionEditText.setId(View.generateViewId());
        RelativeLayout.LayoutParams instructionParams = new RelativeLayout.LayoutParams(
                dpToPx(310),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        instructionParams.setMargins(0, 10, 0, 0);
        instructionEditText.setLayoutParams(instructionParams);
        instructionEditText.setHint("Instruction");
        instructionEditText.setBackgroundResource(R.drawable.button_passthrough_state);
        instructionEditText.setBackgroundTintList(getResources().getColorStateList(R.color.editTextColor));

        ImageView deleteImageView = new ImageView(this);
        deleteImageView.setId(View.generateViewId());
        RelativeLayout.LayoutParams deleteParams = new RelativeLayout.LayoutParams(
                60,
                60
        );
        deleteParams.setMargins(20, 10, 0, 0);
        deleteParams.addRule(RelativeLayout.RIGHT_OF, instructionEditText.getId());
        deleteImageView.setLayoutParams(deleteParams);
        deleteImageView.setImageResource(R.drawable.dustbin_logo);
        deleteImageView.setBackgroundResource(R.drawable.button_passthrough_state);
        deleteImageView.setBackgroundTintList(getResources().getColorStateList(R.color.editTextColor));

        newInstructionLayout.addView(instructionEditText);
        newInstructionLayout.addView(deleteImageView);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        if (instructionLayout.getChildCount() > 1) {
            View lastInstruction = instructionLayout.getChildAt(instructionLayout.getChildCount() - 2);
            layoutParams.addRule(RelativeLayout.BELOW, lastInstruction.getId());
        } else {
            layoutParams.addRule(RelativeLayout.BELOW, R.id.instruction1);
        }

        newInstructionLayout.setLayoutParams(layoutParams);
        instructionLayout.addView(newInstructionLayout, instructionLayout.getChildCount() - 1);

        RelativeLayout.LayoutParams buttonParams = (RelativeLayout.LayoutParams) btnAddInstruction.getLayoutParams();
        buttonParams.addRule(RelativeLayout.BELOW, newInstructionLayout.getId());
        btnAddInstruction.setLayoutParams(buttonParams);

        deleteImageView.setOnClickListener(v -> {
            instructionLayout.removeView(newInstructionLayout);
            if (instructionLayout.getChildCount() > 1) {
                View lastInstruction = instructionLayout.getChildAt(instructionLayout.getChildCount() - 2);
                RelativeLayout.LayoutParams newButtonParams = (RelativeLayout.LayoutParams) btnAddInstruction.getLayoutParams();
                newButtonParams.addRule(RelativeLayout.BELOW, lastInstruction.getId());
                btnAddInstruction.setLayoutParams(newButtonParams);
            } else {
                RelativeLayout.LayoutParams newButtonParams = (RelativeLayout.LayoutParams) btnAddInstruction.getLayoutParams();
                newButtonParams.addRule(RelativeLayout.BELOW, R.id.instruction1);
                btnAddInstruction.setLayoutParams(newButtonParams);
            }
        });
    }
}
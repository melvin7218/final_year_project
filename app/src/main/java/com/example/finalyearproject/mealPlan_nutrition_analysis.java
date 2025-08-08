package com.example.finalyearproject;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class mealPlan_nutrition_analysis extends AppCompatActivity {

    private double userTDEE = 0;
    private Spinner selectUserSpinner;
    private ArrayAdapter<String> userAdapter;
    private List<String> userDisplayList = new ArrayList<>();
    private List<Integer> userIdList = new ArrayList<>(); // 0 for user, member_id for members
    private int selectedPersonId = 0; // 0 means user, else member_id
    private int userId;
    private String startDate, endDate;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan_nutrition_analysis);

        barChart = findViewById(R.id.barChart);
        selectUserSpinner = findViewById(R.id.select_user);
        userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userDisplayList);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectUserSpinner.setAdapter(userAdapter);

        // Get user_id and week range from Intent extras
        userId = getIntent().getIntExtra("user_id", -1);
        startDate = getIntent().getStringExtra("start_date");
        endDate = getIntent().getStringExtra("end_date");
        if (userId == -1 || startDate == null || endDate == null) {
            Toast.makeText(this, "Missing data for nutrition analysis", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        displayWeekRange(startDate, endDate);
        fetchUserAndMembers();
    }

    private void displayWeekRange(String startDate, String endDate) {
        TextView tvWeekRange = findViewById(R.id.tvWeekRange);
        String formattedStartDate = formatDateForDisplay(startDate);
        String formattedEndDate = formatDateForDisplay(endDate);
        String weekRangeText = formattedStartDate + " - " + formattedEndDate;
        tvWeekRange.setText(weekRangeText);
    }

    private void fetchUserAndMembers() {
        // Add user as first option
        userDisplayList.clear();
        userIdList.clear();
        userDisplayList.add("You");
        userIdList.add(0); // 0 means user
        // Fetch members from backend
        String url = "http://192.168.0.130/Final%20Year%20Project/get_user_member_to_meal_plan.php";
        JSONObject postData = new JSONObject();
        try { postData.put("user_id", userId); } catch (JSONException e) { e.printStackTrace(); }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
            response -> {
                if (response.optString("status").equals("success")) {
                    JSONArray members = response.optJSONArray("members");
                    if (members != null) {
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject member = members.optJSONObject(i);
                            if (member != null) {
                                userDisplayList.add(member.optString("member_name", "Member"));
                                userIdList.add(member.optInt("member_id", -1));
                            }
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                }
                // Set listener after data loaded
                selectUserSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedPersonId = userIdList.get(position);
                        if (selectedPersonId == 0) {
                            // User
                            fetchUserTDEE(userId, startDate, endDate, barChart);
                        } else {
                            // Member
                            fetchMemberTDEEAndNutrition(selectedPersonId, startDate, endDate, barChart);
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
                // Trigger initial selection
                selectUserSpinner.setSelection(0);
            },
            error -> {
                Toast.makeText(this, "Error fetching members", Toast.LENGTH_SHORT).show();
            }
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void fetchUserTDEE(int userId, String startDate, String endDate, BarChart barChart) {
        String url = "http://192.168.0.130/Final%20Year%20Project/retrieve_tdee.php";
        JSONObject postData = new JSONObject();
        try {
            postData.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                postData,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            userTDEE = response.getDouble("tdee");
                            // Now fetch weekly calories with TDEE available
                            fetchWeeklyCaloriesForUser(userId, startDate, endDate, barChart);
                        } else {
                            Toast.makeText(this, "Error fetching TDEE: " + response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing TDEE data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error fetching TDEE", Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(jsonObjectRequest);
    }

    private void fetchMemberTDEEAndNutrition(int memberId, String startDate, String endDate, BarChart barChart) {
        // Fetch member TDEE
        String url = "http://192.168.0.130/Final%20Year%20Project/get_family_member.php?user_id=" + userId + "&member_id=" + memberId;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                if (response.optBoolean("success")) {
                    JSONArray members = response.optJSONArray("members");
                    if (members != null && members.length() > 0) {
                        JSONObject member = members.optJSONObject(0);
                        userTDEE = member.optDouble("tdee", 2000.0);
                        fetchWeeklyCaloriesForMember(memberId, startDate, endDate, barChart);
                    }
                }
            },
            error -> Toast.makeText(this, "Error fetching member TDEE", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void fetchWeeklyCaloriesForMember(int memberId, String startDate, String endDate, BarChart barChart) {
        String url = "http://192.168.0.130/Final%20Year%20Project/mealPlan_retrieve_nutrition.php";
        JSONObject postData = new JSONObject();
        try {
            postData.put("user_id", userId);
            postData.put("member_id", memberId);
            postData.put("start_date", startDate);
            postData.put("end_date", endDate);
        } catch (JSONException e) { e.printStackTrace(); }
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                postData,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONObject data = response.getJSONObject("data");
                            List<String> weekDays = Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
                            List<String> dateOrder = Arrays.asList(
                                startDate,
                                new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(java.sql.Date.valueOf(startDate).getTime() + 1*24*60*60*1000)),
                                new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(java.sql.Date.valueOf(startDate).getTime() + 2*24*60*60*1000)),
                                new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(java.sql.Date.valueOf(startDate).getTime() + 3*24*60*60*1000)),
                                new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(java.sql.Date.valueOf(startDate).getTime() + 4*24*60*60*1000)),
                                new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(java.sql.Date.valueOf(startDate).getTime() + 5*24*60*60*1000)),
                                new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(java.sql.Date.valueOf(startDate).getTime() + 6*24*60*60*1000))
                            );
                            ArrayList<BarEntry> entries = new ArrayList<>();
                            ArrayList<String> xLabels = new ArrayList<>();
                            ArrayList<Integer> colors = new ArrayList<>();
                            float maxCalories = 0;
                            for (int i = 0; i < dateOrder.size(); i++) {
                                String date = dateOrder.get(i);
                                float calories = (float) data.optDouble(date, 0);
                                entries.add(new BarEntry(i, calories));
                                xLabels.add(weekDays.get(i));
                                if (calories > maxCalories) maxCalories = calories;
                                int color;
                                if (calories < userTDEE) color = ContextCompat.getColor(this, R.color.yellow);
                                else if (calories <= userTDEE + 250) color = ContextCompat.getColor(this, R.color.green);
                                else color = ContextCompat.getColor(this, R.color.red);
                                colors.add(color);
                            }
                            BarDataSet dataSet = new BarDataSet(entries, "Calories per Day");
                            dataSet.setColors(colors);
                            BarData barData = new BarData(dataSet);
                            barChart.setData(barData);
                            barChart.getDescription().setText("Weekly Calories");
                            YAxis leftAxis = barChart.getAxisLeft();
                            float yAxisMax = Math.max((float) userTDEE, maxCalories + 100);
                            leftAxis.setAxisMaximum(yAxisMax);
                            leftAxis.setAxisMinimum(0f);
                            barChart.getXAxis().setValueFormatter(new ValueFormatter() {
                                @Override
                                public String getFormattedValue(float value) {
                                    int index = (int) value;
                                    if (index >= 0 && index < xLabels.size()) {
                                        return xLabels.get(index);
                                    } else {
                                        return "";
                                    }
                                }
                            });
                            barChart.getXAxis().setGranularity(1f);
                            barChart.getXAxis().setLabelCount(xLabels.size());
                            barChart.animateY(1000);
                            barChart.invalidate();
                        } else {
                            Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(jsonObjectRequest);
    }

    private void fetchWeeklyCaloriesForUser(int userId, String startDate, String endDate, BarChart barChart) {
        String url = "http://192.168.0.130/Final%20Year%20Project/mealPlan_retrieve_nutrition.php";
        JSONObject postData = new JSONObject();
        try {
            postData.put("user_id", userId);
            postData.put("start_date", startDate);
            postData.put("end_date", endDate);
            // Do NOT put member_id for user
        } catch (JSONException e) { e.printStackTrace(); }
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                postData,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONObject data = response.getJSONObject("data");
                            List<String> weekDays = Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
                            List<String> dateOrder = Arrays.asList(
                                startDate,
                                new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(java.sql.Date.valueOf(startDate).getTime() + 1*24*60*60*1000)),
                                new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(java.sql.Date.valueOf(startDate).getTime() + 2*24*60*60*1000)),
                                new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(java.sql.Date.valueOf(startDate).getTime() + 3*24*60*60*1000)),
                                new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(java.sql.Date.valueOf(startDate).getTime() + 4*24*60*60*1000)),
                                new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(java.sql.Date.valueOf(startDate).getTime() + 5*24*60*60*1000)),
                                new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(java.sql.Date.valueOf(startDate).getTime() + 6*24*60*60*1000))
                            );
                            ArrayList<BarEntry> entries = new ArrayList<>();
                            ArrayList<String> xLabels = new ArrayList<>();
                            ArrayList<Integer> colors = new ArrayList<>();
                            float maxCalories = 0;
                            for (int i = 0; i < dateOrder.size(); i++) {
                                String date = dateOrder.get(i);
                                float calories = (float) data.optDouble(date, 0);
                                entries.add(new BarEntry(i, calories));
                                xLabels.add(weekDays.get(i));
                                if (calories > maxCalories) maxCalories = calories;
                                int color;
                                if (calories < userTDEE) color = android.graphics.Color.YELLOW;
                                else if (calories <= userTDEE + 250) color = android.graphics.Color.GREEN;
                                else color = android.graphics.Color.RED;
                                colors.add(color);
                            }
                            BarDataSet dataSet = new BarDataSet(entries, "Calories per Day");
                            dataSet.setColors(colors);
                            BarData barData = new BarData(dataSet);
                            barChart.setData(barData);
                            barChart.getDescription().setText("Weekly Calories");
                            YAxis leftAxis = barChart.getAxisLeft();
                            float yAxisMax = Math.max((float) userTDEE, maxCalories + 100);
                            leftAxis.setAxisMaximum(yAxisMax);
                            leftAxis.setAxisMinimum(0f);
                            barChart.getXAxis().setValueFormatter(new ValueFormatter() {
                                @Override
                                public String getFormattedValue(float value) {
                                    int index = (int) value;
                                    if (index >= 0 && index < xLabels.size()) {
                                        return xLabels.get(index);
                                    } else {
                                        return "";
                                    }
                                }
                            });
                            barChart.getXAxis().setGranularity(1f);
                            barChart.getXAxis().setLabelCount(xLabels.size());
                            barChart.animateY(1000);
                            barChart.invalidate();
                        } else {
                            Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(jsonObjectRequest);
    }

    private String formatDateForDisplay(String date) {
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.util.Date parsedDate = inputFormat.parse(date);
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM");
            return outputFormat.format(parsedDate);
        } catch (Exception e) {
            e.printStackTrace();
            return date; // Return original if formatting fails
        }
    }
}
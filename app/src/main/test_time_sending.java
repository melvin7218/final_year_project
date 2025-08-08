// Test file to verify time parameter sending
// This simulates the JSON data that would be sent from meal_plan.java

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Locale;

public class TestTimeSending {
    public static void main(String[] args) {
        // Simulate the saveMealPlan method with different hour values
        testTimeSending(8);  // 8:00 AM
        testTimeSending(12); // 12:00 PM
        testTimeSending(-1); // No specific time
    }
    
    public static void testTimeSending(int hour) {
        try {
            JSONObject data = new JSONObject();
            
            // Simulate the data that would be sent
            data.put("user_id", 2);
            data.put("recipe_id", 1);
            data.put("category", "Breakfast");
            data.put("meal_date", "2025-01-20");
            
            // Add time if hour is specified (same logic as in meal_plan.java)
            if (hour >= 0) {
                String timeString = String.format(Locale.getDefault(), "%02d:00:00", hour);
                data.put("meal_time", timeString);
                System.out.println("✅ Time parameter ADDED: " + timeString + " (hour: " + hour + ")");
            } else {
                System.out.println("❌ Time parameter NOT ADDED (hour: " + hour + ")");
            }
            
            // Add member_ids (simulated)
            JSONArray memberIdsArray = new JSONArray();
            memberIdsArray.put(1);
            memberIdsArray.put(3);
            data.put("member_ids", memberIdsArray);
            
            // Add portion_multipliers (simulated)
            JSONObject portionMultipliers = new JSONObject();
            portionMultipliers.put("user", 1.0);
            portionMultipliers.put("1", 1.2);
            portionMultipliers.put("3", 0.8);
            data.put("portion_multipliers", portionMultipliers);
            
            System.out.println("📤 JSON Data for hour " + hour + ":");
            System.out.println(data.toString(2));
            System.out.println("---");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
} 
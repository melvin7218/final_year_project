package com.example.finalyearproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class family_preference extends AppCompatActivity {
    private MemberAdapter memberAdapter;
    private int userId; // Set this to the current user's ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_preference);

        ImageView add_family_member = findViewById(R.id.add_member);
        add_family_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(family_preference.this, family_preference_setting.class);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.memberRecyclerView);
        memberAdapter = new MemberAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(memberAdapter);
        
        // Set click listener for member items
        memberAdapter.setOnMemberClickListener(new MemberAdapter.OnMemberClickListener() {
            @Override
            public void onMemberClick(int memberId, String memberName) {
                Intent intent = new Intent(family_preference.this, family_preference_setting.class);
                intent.putExtra("member_id", memberId);
                intent.putExtra("member_name", memberName);
                startActivity(intent);
            }
        });

        // Get user ID from shared preferences or intent
        userId = getUserId();
        if (userId == -1) {
            // Handle case where user is not logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        fetchMemberNames(userId);
    }

    private int getUserId() {
        // Get user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);
        if (userId == -1) {
            // If no user ID found, try to get from intent
            userId = getIntent().getIntExtra("userId", -1);
        }
        return userId;
    }

    private void fetchMemberNames(int userId) {
        String url = "http://172.16.62.183/Final%20Year%20Project/get_family_member.php?user_id=" + userId;
        System.out.println("Fetching members for user ID: " + userId);
        System.out.println("URL: " + url);
        
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    System.out.println("Response received: " + response.toString());
                    if (response.getBoolean("success")) {
                        JSONArray membersArray = response.getJSONArray("members");
                        List<String> memberNames = new ArrayList<>();
                        List<Integer> memberIds = new ArrayList<>();
                        for (int i = 0; i < membersArray.length(); i++) {
                            JSONObject member = membersArray.getJSONObject(i);
                            String memberName = member.getString("member_name");
                            int memberId = member.getInt("member_id");
                            memberNames.add(memberName);
                            memberIds.add(memberId);
                            System.out.println("Found member: " + memberName + " (ID: " + memberId + ")");
                        }
                        System.out.println("Total members found: " + memberNames.size());
                        memberAdapter.setMembers(memberNames, memberIds);
                    } else {
                        System.out.println("API returned success=false");
                        Toast.makeText(this, "Failed to fetch members", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    System.out.println("JSON parsing error: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                }
            },
            error -> {
                // Handle error
                System.out.println("Error fetching members: " + error.toString());
                Toast.makeText(this, "Network error: " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        );
        queue.add(request);
    }
}
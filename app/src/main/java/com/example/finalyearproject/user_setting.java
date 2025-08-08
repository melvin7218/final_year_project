package com.example.finalyearproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class user_setting extends AppCompatActivity {

    private int userId;
    private CardView userInfor, userPreference, updatePassword, logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);

        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);
        if(userId == -1){
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        userInfor=findViewById(R.id.user_information_button);
        userPreference = findViewById(R.id.user_preference_button);
        updatePassword = findViewById(R.id.update_password);
        logout = findViewById(R.id.logout);

        userInfor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(user_setting.this, account_page.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });

        userPreference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(user_setting.this, user_preference_afterLogin.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });

        updatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(user_setting.this, forget_Password.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });

        CardView member_page = findViewById(R.id.member_preference);
        member_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(user_setting.this, family_preference.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });

        logout.setOnClickListener( v -> {
            sharedPreferences.edit().clear().apply();

            startActivity(new Intent(this, LoginPage.class));
            finish();
        });



        

        TextView mainPage = findViewById(R.id.text_menu);
        mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(user_setting.this, MainScreen.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView mealPlan = findViewById(R.id.meal_plan);
        mealPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(user_setting.this, meal_plan.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView grocery_list = findViewById(R.id.grocery_logo);
        grocery_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(user_setting.this, groceryList.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        ImageView accountPage = findViewById(R.id.account_setting);
        accountPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(user_setting.this, user_setting.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });
    }
}
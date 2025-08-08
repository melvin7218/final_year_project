package com.example.finalyearproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginPage extends AppCompatActivity {
    EditText etUsername, etPassword;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Auto login if user is already logged in
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        if (isLoggedIn) {
            startActivity(new Intent(LoginPage.this, MainScreen.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.button_login);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        TextView skip_login = findViewById(R.id.text_skip_login);

        Button signUp = findViewById(R.id.button_signUp);
        signUp.setOnClickListener(view -> {
            Intent intent = new Intent(LoginPage.this, RegisterPage.class);
            startActivity(intent);
        });

        skip_login.setOnClickListener(view -> {
            Intent intent = new Intent(LoginPage.this, MainScreen.class);
            startActivity(intent);
            finish();
        });

        btnLogin.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                new LoginTask().execute(username, password);
            } else {
                Toast.makeText(LoginPage.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        TextView btnForgetPassword = findViewById(R.id.button_forgotPassword);
        btnForgetPassword.setOnClickListener(view -> {
            Intent intent1 = new Intent(LoginPage.this, forget_Password.class);
            startActivity(intent1);
        });
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];

            try {
                URL url = new URL("http://192.168.0.16/Final%20Year%20Project/login.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("username=" + username + "&password=" + password);
                writer.flush();
                writer.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                return result.toString().trim();
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonResponse = new JSONObject(result);

                if (jsonResponse.has("status")) {
                    String status = jsonResponse.getString("status");

                    if (status.equals("success")) {
                        Toast.makeText(LoginPage.this, "Login Successful", Toast.LENGTH_SHORT).show();

                        int userId = jsonResponse.getInt("user_id");

                        // ✅ Save login status and user ID
                        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("user_id", userId);
                        editor.putBoolean("is_logged_in", true); // <- Key line
                        editor.apply();

                        // Redirect
                        Intent intent = new Intent(LoginPage.this, MainScreen.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMessage = jsonResponse.getString("message");
                        Toast.makeText(LoginPage.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginPage.this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.e("JSONError", "Error parsing JSON: " + e.toString());
                Toast.makeText(LoginPage.this, "Error parsing login response", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

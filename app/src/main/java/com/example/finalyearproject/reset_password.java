package com.example.finalyearproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class reset_password extends AppCompatActivity {
    EditText etNewPassword, etConfirmPassword;
    Button btnResetPassword;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        //Handle Back button
        Button back = findViewById(R.id.reset_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(reset_password.this, forget_Password.class);
                startActivity(intent);
            }
        });

        //Handle Reset Button
        etNewPassword = findViewById(R.id.reset_etPassword1);
        etConfirmPassword = findViewById(R.id.reset_etPassword2);
        btnResetPassword = findViewById(R.id.reset_btnUpdatedPassword);

        //GEt email from intent
        email = getIntent().getStringExtra("email");

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = getIntent().getStringExtra("email");
                String newPassword = etNewPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if(!newPassword.isEmpty() && newPassword.equals(confirmPassword)){
                    new ResetPasswordTask().execute(email, newPassword);
                }else{
                    Toast.makeText(reset_password.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private class ResetPasswordTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            String newPassword = params[1];

            try {
                URL url = new URL("http://192.168.0.130/Final%20Year%20Project/reset_password.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("email=" + email + "&password=" + newPassword);
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
        protected void onPostExecute(String result){
            if(result.equals("Password Updated")){
                Toast.makeText(reset_password.this, "Password Successfully Reset!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(reset_password.this, LoginPage.class);
                startActivity(intent);
                finish();
            }else{
                Toast.makeText(reset_password.this, "Error Resetting Password", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
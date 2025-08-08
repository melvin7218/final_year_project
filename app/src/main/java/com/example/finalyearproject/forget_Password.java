package com.example.finalyearproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class forget_Password extends AppCompatActivity {

    EditText etEmail;
    Button btnVerifyEmail, btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        //handle the back button
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(forget_Password.this, LoginPage.class);
                startActivity(intent);
            }
        });


        //handle email validation
        etEmail = findViewById(R.id.forgot_etEmail);
        btnVerifyEmail = findViewById(R.id.btn_checkEmail);

        btnVerifyEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString().trim();

                if(!email.isEmpty()){
                    new VerifyEmailTask().execute(email);
                }else{
                    Toast.makeText(forget_Password.this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class VerifyEmailTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String email = params[0];

            try {
                URL url = new URL("http://172.16.62.183/Final%20Year%20Project/forgot_password.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("email=" + email);
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
            if(result.equals("Email Exists")){
                Toast.makeText(forget_Password.this, "Email Verified!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(forget_Password.this, reset_password.class);
                intent.putExtra("email", etEmail.getText().toString().trim());
                startActivity(intent);
            }else{
                Toast.makeText(forget_Password.this, "Email not found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
package com.example.finalyearproject;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class account_page extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 101;
    private ImageView profileImageView;
    private Button btnChangePicture;
    private Bitmap selectedProfileBitmap;
    private int userId;
    private TextView tvUsername, tvEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_page);

        profileImageView = findViewById(R.id.profile_image);
        btnChangePicture = findViewById(R.id.change_profile_picture);
        tvUsername = findViewById(R.id.textfield_name);
        tvEmail = findViewById(R.id.textfield_email);

        btnChangePicture.setOnClickListener(v -> openGallery());

        loadProfileImage();

        ImageView back = findViewById(R.id.picture_return);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(account_page.this, user_setting.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedProfileBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImageView.setImageBitmap(selectedProfileBitmap);
                uploadProfilePicture();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadProfilePicture() {
        if (selectedProfileBitmap == null) return;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        selectedProfileBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) return;

        String url = "http://172.16.62.183/Final%20Year%20Project/profile_picture.php";

        JSONObject body = new JSONObject();
        try {
            body.put("user_id", userId);
            body.put("profile_picture", encodedImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void loadProfileImage() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) return;

        String url = "http://172.16.62.183/Final%20Year%20Project/get_user_profile.php?user_id=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject data = response.getJSONObject("data");
                            String imageUrl = data.getString("profile_picture");

                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.placeholder_image)
                                    .into(profileImageView);

                            String name = data.getString("username");
                            String email = data.getString("email");

                            tvUsername.setText(name);
                            tvEmail.setText(email);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(request);
    }
}



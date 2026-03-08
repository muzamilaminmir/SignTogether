package com.signtogether;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView tvName = findViewById(R.id.tvName);
        TextView tvGender = findViewById(R.id.tvGender);
        TextView tvDOB = findViewById(R.id.tvDOB);
        TextView tvBloodGroup = findViewById(R.id.tvBloodGroup);
        TextView tvAddress = findViewById(R.id.tvAddress);
        TextView tvEmergency = findViewById(R.id.tvEmergency);
        android.widget.ImageView imgProfile = findViewById(R.id.imgProfile);

        Button btnEdit = findViewById(R.id.btnEdit);
        android.widget.ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        tvName.setText(prefs.getString("name", "N/A"));
        tvGender.setText(prefs.getString("gender", "N/A"));
        tvDOB.setText(prefs.getString("dob", "N/A"));
        tvBloodGroup.setText(prefs.getString("blood_group", "N/A"));
        tvAddress.setText(prefs.getString("address", "N/A"));
        tvEmergency.setText(prefs.getString("emergency", "N/A"));

        String imageUriString = prefs.getString("profile_image", null);
        if (imageUriString != null && imgProfile != null) {
            try {
                android.net.Uri imageUri = android.net.Uri.parse(imageUriString);
                if ("file".equals(imageUri.getScheme())) {
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imageUri.getPath());
                    if (bitmap != null) {
                        imgProfile.setImageBitmap(bitmap);
                    }
                } else {
                    imgProfile.setImageURI(imageUri);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}

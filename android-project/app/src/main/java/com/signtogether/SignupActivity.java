package com.signtogether;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class SignupActivity extends BaseActivity {

    private EditText etName, etAddress, etEmergency, etDOB;
    private Spinner spinnerGender, spinnerBloodGroup;
    private Button btnContinue, btnUpload;
    private ImageView profileImage;
    private android.net.Uri imageUri;

    private final androidx.activity.result.ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    android.net.Uri selectedUri = result.getData().getData();
                    // Launch uCrop for circular cropping
                    launchImageCrop(selectedUri);
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<Intent> cropImageLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    android.net.Uri croppedUri = com.yalantis.ucrop.UCrop.getOutput(result.getData());
                    if (croppedUri != null) {
                        try {
                            // Copy cropped image to internal storage
                            java.io.InputStream inputStream = getContentResolver().openInputStream(croppedUri);
                            java.io.File internalFile = new java.io.File(getFilesDir(), "profile_pic.jpg");
                            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(internalFile);

                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = inputStream.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, length);
                            }

                            outputStream.close();
                            inputStream.close();

                            imageUri = android.net.Uri.fromFile(internalFile);
                            profileImage.setImageURI(imageUri);
                            profileImage.setAlpha(1.0f);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    private void launchImageCrop(android.net.Uri sourceUri) {
        String destinationFileName = "cropped_" + System.currentTimeMillis() + ".jpg";
        android.net.Uri destinationUri = android.net.Uri.fromFile(new java.io.File(getCacheDir(), destinationFileName));

        com.yalantis.ucrop.UCrop.Options options = new com.yalantis.ucrop.UCrop.Options();
        options.setCircleDimmedLayer(true); // Enable circular crop
        options.setShowCropFrame(false);
        options.setShowCropGrid(false);
        options.setCompressionQuality(90);

        Intent cropIntent = com.yalantis.ucrop.UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(500, 500)
                .withOptions(options)
                .getIntent(this);

        cropImageLauncher.launch(cropIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etName = findViewById(R.id.etName);
        etAddress = findViewById(R.id.etAddress);
        etEmergency = findViewById(R.id.etEmergency);
        etEmergency.setFilters(new android.text.InputFilter[] { new android.text.InputFilter.LengthFilter(10) });
        etDOB = findViewById(R.id.etDOB);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerBloodGroup = findViewById(R.id.spinnerBloodGroup);
        btnContinue = findViewById(R.id.btnContinue);
        btnUpload = findViewById(R.id.btnUpload);
        profileImage = findViewById(R.id.profileImage);

        // Image Picker
        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Date Picker
        etDOB.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, month1, dayOfMonth) -> etDOB.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1),
                    year, month, day);
            datePickerDialog.show();
        });

        // Pre-fill data if available (Edit Mode)
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String savedName = prefs.getString("name", null);
        if (savedName != null) {
            etName.setText(savedName);
            etAddress.setText(prefs.getString("address", ""));
            etEmergency.setText(prefs.getString("emergency", ""));
            etDOB.setText(prefs.getString("dob", ""));

            // Pre-fill Spinners
            setSpinnerSelection(spinnerGender, prefs.getString("gender", ""));
            setSpinnerSelection(spinnerBloodGroup, prefs.getString("blood_group", ""));

            // Image pre-fill
            String savedImage = prefs.getString("profile_image", null);
            if (savedImage != null) {
                try {
                    imageUri = android.net.Uri.parse(savedImage);
                    if ("file".equals(imageUri.getScheme())) {
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imageUri.getPath());
                        if (bitmap != null) {
                            profileImage.setImageBitmap(bitmap);
                        }
                    } else {
                        profileImage.setImageURI(imageUri);
                    }
                    profileImage.setAlpha(1.0f);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            btnContinue.setText("Save Changes");
        }

        btnContinue.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String address = etAddress.getText().toString();
            String emergency = etEmergency.getText().toString();
            String dob = etDOB.getText().toString();
            String gender = spinnerGender.getSelectedItem().toString();
            String bloodGroup = spinnerBloodGroup.getSelectedItem().toString();

            if (name.isEmpty() || address.isEmpty() || emergency.isEmpty() || dob.isEmpty() ||
                    gender.equals("Select Gender") || bloodGroup.equals("Select Blood Group")) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (emergency.length() > 10) {
                etEmergency.setError("Max 10 digits allowed");
                return;
            }

            // Save Data
            SharedPreferences savePrefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
            SharedPreferences.Editor editor = savePrefs.edit();
            editor.putString("name", name);
            editor.putString("address", address);
            editor.putString("emergency", emergency);
            editor.putString("dob", dob);
            editor.putString("gender", gender);
            editor.putString("blood_group", bloodGroup);
            if (imageUri != null) {
                editor.putString("profile_image", imageUri.toString());
            }
            editor.apply();

            // Move to Home
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}

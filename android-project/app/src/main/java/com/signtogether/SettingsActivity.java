package com.signtogether;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends BaseActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        android.widget.ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> handleBackNavigation());
        }

        prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);

        // Dark Mode Switch
        Switch switchDarkMode = findViewById(R.id.switchDarkMode);
        boolean isDarkModeSaved = prefs.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDarkModeSaved);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
        });

        // Accessibility Switches
        Switch switchHighContrast = findViewById(R.id.switchHighContrast);
        if (switchHighContrast != null) {
            switchHighContrast.setChecked(prefs.getBoolean("high_contrast", false));
            switchHighContrast.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("high_contrast", isChecked).apply();
                recreate(); // Recreate to apply changes
            });
        }

        Switch switchLargeText = findViewById(R.id.switchLargeText);
        if (switchLargeText != null) {
            switchLargeText.setChecked(prefs.getBoolean("large_text", false));
            switchLargeText.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("large_text", isChecked).apply();
                recreate(); // Recreate to apply changes
            });
        }

        Switch switchReduceMotion = findViewById(R.id.switchReduceMotion);
        if (switchReduceMotion != null) {
            switchReduceMotion.setChecked(prefs.getBoolean("reduce_motion", false));
            switchReduceMotion.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("reduce_motion", isChecked).apply();
                // Reduce motion might not need recreate if handled dynamically, but for safety:
                recreate();
            });
        }

        // Language Spinner
        Spinner spinnerLanguage = findViewById(R.id.spinnerLanguage);
        if (spinnerLanguage != null) {
            int savedLanguagePosition = prefs.getInt("language_position", 0);
            spinnerLanguage.setSelection(savedLanguagePosition);

            spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != savedLanguagePosition) {
                        prefs.edit().putInt("language_position", position).apply();
                        recreate();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }

    private void handleBackNavigation() {
        android.content.Intent intent = new android.content.Intent(this, MainActivity.class);
        intent.addFlags(
                android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        handleBackNavigation();
    }
}

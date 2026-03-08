package com.signtogether;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Apply Dark Mode before super.onCreate
        SharedPreferences settingsPrefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isDarkMode = settingsPrefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("AppSettings", MODE_PRIVATE);
        Configuration configuration = new Configuration(newBase.getResources().getConfiguration());

        // Apply Large Text
        boolean isLargeText = prefs.getBoolean("large_text", false);
        configuration.fontScale = isLargeText ? 1.3f : 1.0f;

        // Apply Language
        int langPos = prefs.getInt("language_position", 0); // 0=En, 1=Hi
        java.util.Locale locale;
        if (langPos == 1) {
            locale = new java.util.Locale("hi");
        } else {
            locale = java.util.Locale.US;
        }
        java.util.Locale.setDefault(locale);
        configuration.setLocale(locale);

        Context context = newBase.createConfigurationContext(configuration);
        super.attachBaseContext(context);
    }
}

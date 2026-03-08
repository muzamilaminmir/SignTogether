package com.signtogether;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class EmergencyActivity extends BaseActivity {

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        // Initialize TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        // Bind Buttons
        Button btnHelp = findViewById(R.id.btnHelp);
        Button btnDoctor = findViewById(R.id.btnDoctor);
        Button btnLost = findViewById(R.id.btnLost);
        Button btnMedical = findViewById(R.id.btnMedical);
        Button btnFamily = findViewById(R.id.btnFamily);
        Button btnPolice = findViewById(R.id.btnPolice);

        // Set Listeners
        btnHelp.setOnClickListener(v -> triggerEmergency("I need help!", "112"));
        btnDoctor.setOnClickListener(v -> triggerEmergency("Please call a doctor.", "108"));
        btnLost.setOnClickListener(v -> triggerEmergency("I am lost, please help me.", "112"));
        btnMedical.setOnClickListener(v -> triggerEmergency("This is a medical emergency.", "108"));
        btnPolice.setOnClickListener(v -> triggerEmergency("I need police assistance.", "112"));

        // Call Family (No specific number, just open dialer)
        // Call Family (Use number from profile)
        btnFamily.setOnClickListener(v -> {
            android.content.SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
            String emergencyNumber = prefs.getString("emergency", null);

            if (emergencyNumber != null && !emergencyNumber.isEmpty()) {
                speak("Calling family.");
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + emergencyNumber));
                startActivity(intent);
            } else {
                speak("No family number found.");
                Toast.makeText(this, "Please set emergency number in profile", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void triggerEmergency(String text, String number) {
        speak(text);
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
        Toast.makeText(this, "Speaking: " + text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}

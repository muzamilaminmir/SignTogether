package com.signtogether;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class AvatarSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_selection);

        findViewById(R.id.cardRealisticAvatar).setOnClickListener(v -> {
            Intent intent = new Intent(AvatarSelectionActivity.this, NativeModeActivity.class);
            intent.putExtra("MODE", "SPEECH_TO_SIGN");
            startActivity(intent);
        });

        findViewById(R.id.cardCartoonAvatar).setOnClickListener(v -> {
            // For now, also launch Speech to Sign but maybe with a different extra if we
            // had a different logic
            // Or just show a toast "Coming Soon" if it's a placeholder
            Intent intent = new Intent(AvatarSelectionActivity.this, NativeModeActivity.class);
            intent.putExtra("MODE", "SPEECH_TO_SIGN");
            // intent.putExtra("AVATAR", "CARTOON"); // Future extensibility
            startActivity(intent);
        });
    }
}

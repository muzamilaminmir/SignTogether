package com.signtogether

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.signtogether.navigation.SetupNavGraph
import com.signtogether.ui.theme.SignTogetherTheme
import com.signtogether.data.StoreUserProfile
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request Permissions for the ML features just in case
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                101
            )
        }
        
        setContent {
            val dataStore = remember { StoreUserProfile(this@MainActivity) }
            val isDarkMode by dataStore.darkModeFlow.collectAsState(initial = false)
            
            SignTogetherTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SetupNavGraph(navController = rememberNavController())
                }
            }
        }
    }

    // Helper functions to bridge Jetpack Compose UI with the existing Native Core ML Activities
    fun launchSpeechToSign() {
        val intent = Intent(this, NativeModeActivity::class.java)
        intent.putExtra("MODE", "SPEECH_TO_SIGN")
        startActivity(intent)
    }

    fun launchSignToSpeech() {
        val intent = Intent(this, NativeModeActivity::class.java)
        intent.putExtra("MODE", "SIGN_TO_SPEECH")
        startActivity(intent)
    }
}

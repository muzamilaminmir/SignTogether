package com.signtogether.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import android.telephony.SmsManager
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SOSScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = remember { com.signtogether.data.StoreUserProfile(context) }
    val appMode by dataStore.appModeFlow.collectAsState(initial = "STANDARD")
    
    // Permissions
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    // Load actual contacts
    val sharedPreferences = remember { context.getSharedPreferences("sos_prefs", Context.MODE_PRIVATE) }
    val gson = remember { Gson() }
    val contacts: List<EmergencyContact> = remember {
        val json = sharedPreferences.getString("contacts_list", null)
        val type = object : TypeToken<List<EmergencyContact>>() {}.type
        if (json != null) gson.fromJson(json, type) else emptyList()
    }
    
    val activeContacts = if (appMode == "KID") contacts.take(1) else contacts

    // Text to Speech
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isEmergencyPlaying by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose { tts?.shutdown() }
    }

    // Flashlight Strobe
    var isStrobing by remember { mutableStateOf(false) }
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val cameraId = remember { 
        try { cameraManager.cameraIdList.firstOrNull() } catch(e: Exception) { null } 
    }

    // SOS Cooldown Timer
    var isCooldown by remember { mutableStateOf(false) }
    var cooldownSeconds by remember { mutableStateOf(0) }
    
    LaunchedEffect(isCooldown) {
        if (isCooldown) {
            cooldownSeconds = 30
            while (cooldownSeconds > 0) {
                delay(1000)
                cooldownSeconds--
            }
            isCooldown = false
        }
    }

    LaunchedEffect(isStrobing) {
        if (isStrobing && cameraId != null) {
            while(isActive) {
                try {
                    cameraManager.setTorchMode(cameraId, true)
                    delay(100)
                    cameraManager.setTorchMode(cameraId, false)
                    delay(100)
                } catch(e: Exception) {
                    isStrobing = false
                }
            }
        } else if (cameraId != null) {
            try { cameraManager.setTorchMode(cameraId, false) } catch(e: Exception) {}
        }
    }

    MainScaffold(navController = navController!!) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(if (appMode == "KID") Color(0xFFFFF9E6) else MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = if (appMode == "KID") "I NEED HELP" else "EMERGENCY SOS",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (appMode == "KID") "Press the button to message your guardian." else "Press the button below to instantly notify ${activeContacts.size} saved emergency contacts.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(48.dp))

                SosButton(onClick = {
                    if (isCooldown) {
                        Toast.makeText(context, "Please wait ${cooldownSeconds}s before sending again.", Toast.LENGTH_SHORT).show()
                        return@SosButton
                    }
                    if (permissionsState.permissions.first { it.permission == Manifest.permission.SEND_SMS }.status.isGranted) {
                        try {
                            val smsManager = context.getSystemService(SmsManager::class.java)
                            
                            if (activeContacts.isEmpty()) {
                                Toast.makeText(context, "No emergency contacts set!", Toast.LENGTH_SHORT).show()
                                return@SosButton
                            }

                            val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                            
                            // Permission is checked above
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                val mapUrl = if (location != null) {
                                    "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                                } else {
                                    "Unknown Location (GPS unavailable)"
                                }
                                
                                val message = "EMERGENCY! I need immediate assistance. My approximate location: $mapUrl (Sent via SignTogether SOS)"
                                
                                activeContacts.forEach { contact ->
                                    smsManager.sendTextMessage(contact.phone, null, message, null, null)
                                }
                                Toast.makeText(context, "SOS Sent to ${if (appMode == "KID") "your Guardian!" else "${activeContacts.size} contacts!"}", Toast.LENGTH_LONG).show()
                                
                                isCooldown = true
                                
                                if (appMode != "KID") {
                                    isEmergencyPlaying = true
                                    isStrobing = true
                                }
                            }.addOnFailureListener {
                                val message = "EMERGENCY! I need immediate assistance. Cannot fetch GPS. (Sent via SignTogether SOS)"
                                activeContacts.forEach { contact ->
                                    smsManager.sendTextMessage(contact.phone, null, message, null, null)
                                }
                                Toast.makeText(context, "SOS Sent (No GPS) to ${if (appMode == "KID") "your Guardian!" else "${activeContacts.size} contacts!"}", Toast.LENGTH_LONG).show()
                                
                                isCooldown = true
                                
                                if (appMode != "KID") {
                                    isEmergencyPlaying = true
                                    isStrobing = true
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to send SOS.", Toast.LENGTH_LONG).show()
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(context, "Cannot send SOS without SMS Permissions.", Toast.LENGTH_LONG).show()
                        permissionsState.launchMultiplePermissionRequest()
                    }
                })
                
                // Cooldown indicator
                if (isCooldown) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cooldown: ${cooldownSeconds}s",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
                
                if (appMode != "KID") {
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // Advanced Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SosFeatureButton(
                            icon = Icons.Default.Call,
                            text = "Dial 112",
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                                context.startActivity(intent)
                            }
                        )
                        
                        SosFeatureButton(
                            icon = Icons.Default.Notifications,
                            text = if(isEmergencyPlaying) "Stop Siren" else "Play Siren",
                            isActive = isEmergencyPlaying,
                            onClick = {
                                isEmergencyPlaying = !isEmergencyPlaying
                                if (isEmergencyPlaying) {
                                    scope.launch {
                                        while(isEmergencyPlaying && isActive) {
                                            tts?.speak("I am deaf and I need help. Emergency. Emergency.", TextToSpeech.QUEUE_FLUSH, null, null)
                                            delay(4000)
                                        }
                                    }
                                }
                            }
                        )
                        
                        SosFeatureButton(
                            icon = Icons.Default.Info,
                            text = if(isStrobing) "Stop Light" else "Flashlight",
                            isActive = isStrobing,
                            onClick = {
                                isStrobing = !isStrobing
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SosFeatureButton(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, isActive: Boolean = false, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if(isActive) Color.Red else MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if(isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun SosButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(200.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(Color.Red)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "SOS Alert",
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
            Text("PRESS", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }
    }
}

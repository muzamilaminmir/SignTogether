package com.signtogether.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.signtogether.navigation.Screen

import androidx.compose.runtime.collectAsState
import com.signtogether.data.StoreUserProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = remember { StoreUserProfile(context) }
    val userName by dataStore.userNameFlow.collectAsState(initial = "User")
    val initial = userName?.take(1)?.uppercase() ?: "U"

    val isDarkMode by dataStore.darkModeFlow.collectAsState(initial = false)
    val appMode by dataStore.appModeFlow.collectAsState(initial = "STANDARD")
    val parentalPin by dataStore.parentalPinFlow.collectAsState(initial = null)
    
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSetPinDialog by remember { mutableStateOf(false) }
    var showVerifyPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    // Local cache so PIN is immediately available after setting (avoids DataStore Flow delay)
    var localPinCache by remember { mutableStateOf(parentalPin) }
    // Sync localPinCache when DataStore emits
    LaunchedEffect(parentalPin) { localPinCache = parentalPin }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = { Text("Settings", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )
        
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                title = { Text("Privacy Policy") },
                text = { Text("Sign Together does not collect, store, or transmit your data to any external servers. Your profile information, emergency contacts, and location data are stored securely on your local device and only accessed when you explicitly trigger an SOS message via SMS.") },
                confirmButton = {
                    TextButton(onClick = { showPrivacyDialog = false }) {
                        Text("I Understand")
                    }
                }
            )
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Logout?", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to logout? Your profile data will be cleared from this device.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            scope.launch {
                                dataStore.clearUserProfile()
                                val sharedPreferences = context.getSharedPreferences("sos_prefs", android.content.Context.MODE_PRIVATE)
                                sharedPreferences.edit().clear().apply()
                                navController?.navigate(Screen.ModeSelection.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Logout", color = MaterialTheme.colorScheme.onError)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // About SignTogether Dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("✋ SignTogether", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("v3.0", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                },
                text = {
                    Column {
                        Text(
                            "Empowering Inclusivity Through Technology",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "SignTogether bridges the communication gap between the deaf community and the hearing world using real-time sign language translation, AI-powered text-to-sign conversion, and inclusive educational tools.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Built with ❤\uFE0F by", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Muzamil Amin Mir", fontWeight = FontWeight.SemiBold)
                        Text("Simar Kaur", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("© 2026 SignTogether. All rights reserved.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // Set Parental PIN Dialog
        if (showSetPinDialog) {
            AlertDialog(
                onDismissRequest = { showSetPinDialog = false; pinInput = "" },
                icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text("Set Parental PIN", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Create a 4-digit PIN to protect Kid Mode settings.")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                            label = { Text("4-digit PIN") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (pinInput.length == 4) {
                                scope.launch { dataStore.setParentalPin(pinInput) }
                                localPinCache = pinInput // Update immediately
                                showSetPinDialog = false
                                pinInput = ""
                                Toast.makeText(context, "PIN set successfully!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = pinInput.length == 4
                    ) { Text("Set PIN") }
                },
                dismissButton = {
                    TextButton(onClick = { showSetPinDialog = false; pinInput = "" }) { Text("Cancel") }
                }
            )
        }

        // Verify Parental PIN Dialog (for Kid Mode logout)
        if (showVerifyPinDialog) {
            AlertDialog(
                onDismissRequest = { showVerifyPinDialog = false; pinInput = ""; pinError = false },
                icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text("Enter PIN", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Enter your parental PIN to continue.")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) { pinInput = it; pinError = false } },
                            label = { Text("4-digit PIN") },
                            isError = pinError,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (pinError) {
                            Text("Incorrect PIN", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (pinInput == (localPinCache ?: parentalPin)) {
                                showVerifyPinDialog = false
                                pinInput = ""
                                pinError = false
                                showLogoutDialog = true
                            } else {
                                pinError = true
                            }
                        },
                        enabled = pinInput.length == 4
                    ) { Text("Verify") }
                },
                dismissButton = {
                    TextButton(onClick = { showVerifyPinDialog = false; pinInput = ""; pinError = false }) { Text("Cancel") }
                }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Profile Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            navController?.navigate(Screen.ProfileSetup.route)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initial, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(userName ?: "User", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Profile Active", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary)
                }
                Divider()
            }

            // Preferences
            item {
                SettingsGroupHeader("Preferences")
                
                SettingsItem(
                    icon = Icons.Default.Check,
                    title = "Dark Mode",
                    trailing = {
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { scope.launch { dataStore.setDarkMode(it) } }
                        )
                    }
                )
                
                // Only show Help Desk toggle for Standard users to prevent Kid Mode bypass
                if (appMode == "STANDARD") {
                    SettingsItem(
                        icon = Icons.Default.BusinessCenter,
                        title = "Access Help Desk Mode",
                        trailing = {
                            TextButton(onClick = { scope.launch { dataStore.setAppMode("HELP_DESK") } }) {
                                Text("ENABLE", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                    Divider()
                }
                SettingsItem(
                    icon = Icons.Default.Place,
                    title = "Sign Language",
                    subtitle = "Indian Sign Language (ISL)",
                    onClick = {
                        Toast.makeText(context, "Currently only ISL is supported.", Toast.LENGTH_SHORT).show()
                    }
                )
                Divider()
            }

            // Support & About
            item {
                SettingsGroupHeader("Support & About")
                
                SettingsItem(
                    icon = Icons.Default.Info, 
                    title = "Help & Support",
                    subtitle = "mirmuzamil962@gmail.com\nsimarkaur0217@gmail.com",
                    onClick = {
                        Toast.makeText(context, "Please email us at the provided addresses for support.", Toast.LENGTH_LONG).show()
                    }
                )
                SettingsItem(
                    icon = Icons.Default.Info, 
                    title = "About SignTogether", 
                    subtitle = "Version 3.0",
                    onClick = {
                        showAboutDialog = true
                    }
                )
                SettingsItem(
                    icon = Icons.Default.Lock, 
                    title = "Privacy Policy",
                    onClick = {
                        showPrivacyDialog = true
                    }
                )
                Divider()
            }

            // Account
            item {
                SettingsGroupHeader("Account")
                
                // Parental PIN for Kid Mode
                if (appMode == "KID") {
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = if (parentalPin.isNullOrBlank()) "Set Parental PIN" else "Change Parental PIN",
                        subtitle = if (parentalPin.isNullOrBlank()) "Protect settings from kids" else "PIN is set ✓",
                        onClick = { showSetPinDialog = true }
                    )
                }
                
                SettingsItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Logout",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = {
                        // In Kid Mode with PIN set, require PIN first
                        // Read PIN directly from SharedPreferences as a synchronous fallback
                        val currentMode = appMode ?: "STANDARD"
                        val currentPin = localPinCache ?: parentalPin
                        
                        if (currentMode == "KID" && !currentPin.isNullOrBlank()) {
                            showVerifyPinDialog = true
                        } else {
                            showLogoutDialog = true
                        }
                    }
                )
            }

            // Version Footer
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "SignTogether v3.0",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "Empowering Inclusivity",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = titleColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = titleColor)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}

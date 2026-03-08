package com.signtogether.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.signtogether.data.StoreUserProfile
import com.signtogether.navigation.Screen
import com.signtogether.ui.components.AuthTextField
import com.signtogether.ui.components.PrimaryButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionalRegistrationScreen(navController: NavController) {
    val context = LocalContext.current
    val dataStore = remember { StoreUserProfile(context) }
    val scope = rememberCoroutineScope()

    var instName by remember { mutableStateOf("") }
    var instType by remember { mutableStateOf("") }
    var instContact by remember { mutableStateOf("") }
    var instAddress by remember { mutableStateOf("") }
    var instAdmin by remember { mutableStateOf("") }
    var subscriptionType by remember { mutableStateOf("Basic Institutional") }

    val institutionTypes = listOf("Hospital", "School", "Bank", "Office", "Public Square", "Other")
    var showTypeDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Institutional Registration") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Register Your Institution",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Set up your Help Desk for inclusive communication.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            AuthTextField(value = instName, onValueChange = { instName = it }, label = "Institution Name")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Institution Type Dropdown
            ExposedDropdownMenuBox(
                expanded = showTypeDropdown,
                onExpandedChange = { showTypeDropdown = !showTypeDropdown },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = instType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Institution Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false }
                ) {
                    institutionTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                instType = type
                                showTypeDropdown = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(value = instAdmin, onValueChange = { instAdmin = it }, label = "Admin/Manager Name")
            
            
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(value = instContact, onValueChange = { instContact = it }, label = "Contact Number")
            
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(value = instAddress, onValueChange = { instAddress = it }, label = "Full Address")
            
            Spacer(modifier = Modifier.height(32.dp))
            
            PrimaryButton(
                text = "Complete Registration",
                onClick = {
                    if (instName.isBlank() || instType.isBlank() || instContact.isBlank() || 
                        instAddress.isBlank() || instAdmin.isBlank()) {
                        Toast.makeText(context, "All fields are mandatory", Toast.LENGTH_SHORT).show()
                        return@PrimaryButton
                    }
                    
                    scope.launch {
                        // Save to Local Cache
                        dataStore.saveInstitutionalProfile(
                            name = instName,
                            type = instType,
                            contact = instContact,
                            address = instAddress,
                            admin = instAdmin,
                            email = "",
                            subscription = subscriptionType
                        )
                        
                        Toast.makeText(context, "Registration Complete (Local Only)", Toast.LENGTH_SHORT).show()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.ModeSelection.route) { inclusive = true }
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "By registering, you agree to the Institutional Terms of Service.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

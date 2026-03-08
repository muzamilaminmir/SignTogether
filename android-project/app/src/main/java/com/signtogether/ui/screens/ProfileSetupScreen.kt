package com.signtogether.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.ui.text.style.TextAlign
import com.signtogether.data.StoreUserProfile
import com.signtogether.navigation.Screen
import com.signtogether.ui.components.AuthTextField
import com.signtogether.ui.components.PrimaryButton
import android.net.Uri
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(navController: NavController) {
    val context = LocalContext.current
    val dataStore = remember { StoreUserProfile(context) }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    
    // Pickers State
    var gender by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    
    // New Kid Mode Fields
    var userClass by remember { mutableStateOf("") }
    var guardianContact by remember { mutableStateOf("") }
    
    val savedDob by dataStore.userDobFlow.collectAsState(initial = null)

    val savedAge by dataStore.userAgeFlow.collectAsState(initial = null)
    val savedClass by dataStore.userClassFlow.collectAsState(initial = null)
    val savedName by dataStore.userNameFlow.collectAsState(initial = null)

    LaunchedEffect(savedName) {
        if (!savedName.isNullOrBlank()) {
            // Pre-fill if needed, but the user wants Age/Class locked
        }
    }

    var showGenderDropdown by remember { mutableStateOf(false) }
    var showBloodDropdown by remember { mutableStateOf(false) }
    var showClassDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val genderOptions = listOf("Male", "Female", "Non-Binary", "Other", "Prefer not to say")
    val bloodOptions = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
    val classOptions = (1..12).map { "Class $it" } + "College/Higher"
    
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let {
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        dob = formatter.format(Date(it))
                        
                        // Immediate Age Calculation
                        val birthDate = formatter.parse(dob)
                        if (birthDate != null) {
                            val today = java.util.Calendar.getInstance()
                            val birth = java.util.Calendar.getInstance()
                            birth.time = birthDate
                            var calculatedAge = today.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
                            if (today.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) {
                                calculatedAge--
                            }
                            // Store in a local temp state or directly use if needed
                        }
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Profile Pic Picker
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.CircleShape)
                .clickable { imagePickerLauncher.launch("image/*") }
                .clip(androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    "Tap to add\nPhoto", 
                    style = MaterialTheme.typography.labelSmall, 
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Complete Your Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Details marked with * are mandatory and used for Mode Selection.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        AuthTextField(value = name, onValueChange = { name = it }, label = "Full Name *")
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // DOB Picker Trigger
            OutlinedTextField(
                value = dob,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date of Birth *") },
                modifier = Modifier.fillMaxWidth().clickable(enabled = savedDob.isNullOrBlank()) { showDatePicker = true },
                enabled = false, // Prevents keyboard
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Class/Education Level selection moved inside isMinor block


        val isMinor = remember(dob) {
            if (dob.isEmpty()) return@remember false
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val birthDate = sdf.parse(dob)
                if (birthDate != null) {
                    val today = java.util.Calendar.getInstance()
                    val birth = java.util.Calendar.getInstance()
                    birth.time = birthDate
                    var calculatedAge = today.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
                    if (today.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) {
                        calculatedAge--
                    }
                    calculatedAge < 18
                } else false
            } catch (e: Exception) { false }
        }

        if (isMinor) {
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(value = guardianContact, onValueChange = { guardianContact = it }, label = "Guardian Contact Number *")
            Spacer(modifier = Modifier.height(16.dp))
            // Class Selection (Locked if saved)
            ExposedDropdownMenuBox(
                expanded = showClassDropdown && savedClass.isNullOrBlank(),
                onExpandedChange = { if(savedClass.isNullOrBlank()) showClassDropdown = !showClassDropdown },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = if (!savedClass.isNullOrBlank()) savedClass!! else userClass,
                    onValueChange = {},
                    readOnly = true,
                    enabled = savedClass.isNullOrBlank(),
                    label = { Text("Class (e.g. 5th Grade) *") },
                    trailingIcon = { if(savedClass.isNullOrBlank()) ExposedDropdownMenuDefaults.TrailingIcon(expanded = showClassDropdown) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showClassDropdown,
                    onDismissRequest = { showClassDropdown = false }
                ) {
                    classOptions.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                userClass = selection
                                showClassDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Gender Dropdown
            ExposedDropdownMenuBox(
                expanded = showGenderDropdown,
                onExpandedChange = { showGenderDropdown = !showGenderDropdown },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGenderDropdown) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showGenderDropdown,
                    onDismissRequest = { showGenderDropdown = false }
                ) {
                    genderOptions.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                gender = selection
                                showGenderDropdown = false
                            }
                        )
                    }
                }
            }

            // Blood Group Dropdown
            ExposedDropdownMenuBox(
                expanded = showBloodDropdown,
                onExpandedChange = { showBloodDropdown = !showBloodDropdown },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = bloodGroup,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Blood") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBloodDropdown) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showBloodDropdown,
                    onDismissRequest = { showBloodDropdown = false }
                ) {
                    bloodOptions.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                bloodGroup = selection
                                showBloodDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(value = address, onValueChange = { address = it }, label = "Full Address")

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            text = "Save Profile",
            onClick = {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                var age = 0
                try {
                    val birthDate = sdf.parse(dob)
                    if (birthDate != null) {
                        val today = java.util.Calendar.getInstance()
                        val birth = java.util.Calendar.getInstance()
                        birth.time = birthDate
                        age = today.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
                        if (today.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) {
                            age--
                        }
                    }
                } catch(e: Exception) {}

                val isMinorCheck = age < 18

                if(name.isBlank() || dob.isBlank() || (isMinorCheck && (guardianContact.isBlank() || userClass.isBlank()))) {
                    Toast.makeText(context, "Please fill all mandatory fields (*)", Toast.LENGTH_SHORT).show()
                    return@PrimaryButton
                }
                
                scope.launch {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val birthDate = sdf.parse(dob)
                    val age = if (birthDate != null) {
                        val today = java.util.Calendar.getInstance()
                        val birth = java.util.Calendar.getInstance()
                        birth.time = birthDate
                        var age = today.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
                        if (today.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) {
                            age--
                        }
                        age
                    } else 0

                    val calculatedAppMode = if (age < 18) "KID" else "STANDARD"
                    
                    // Use local UUID or existing ID
                    val profileId = java.util.UUID.randomUUID().toString() 
                    
                    // Update Local Cache (DataStore)
                    dataStore.saveUserProfile(
                        name = name,
                        gender = gender,
                        dob = dob,
                        bloodGroup = bloodGroup,
                        address = address,
                        picUri = selectedImageUri?.toString() ?: "",
                        appMode = calculatedAppMode,
                        age = age.toString(),
                        userClass = userClass,
                        guardianContact = guardianContact
                    )

                    Toast.makeText(context, "Profile Saved (Local Only)", Toast.LENGTH_SHORT).show()
                    
                    navController.navigate(Screen.EmergencyContacts.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            }
        )
    }
}

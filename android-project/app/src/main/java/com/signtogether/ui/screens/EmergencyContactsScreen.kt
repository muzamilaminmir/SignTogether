package com.signtogether.ui.screens

import android.widget.Toast
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.signtogether.navigation.Screen
import com.signtogether.ui.components.AuthTextField
import com.signtogether.ui.components.PrimaryButton

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

data class EmergencyContact(val name: String, val phone: String, val relationship: String)


@Composable
fun EmergencyContactsScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("sos_prefs", Context.MODE_PRIVATE) }
    val gson = remember { Gson() }

    // Load initial contacts from SharedPreferences
    var contacts by remember {
        val json = sharedPreferences.getString("contacts_list", null)
        val type = object : TypeToken<List<EmergencyContact>>() {}.type
        val initialContacts: List<EmergencyContact> = if (json != null) gson.fromJson(json, type) else emptyList()
        mutableStateOf(initialContacts)
    }
    
    var showAddDialog by remember { mutableStateOf(false) }

    fun saveContacts(newList: List<EmergencyContact>) {
        contacts = newList
        val jsonString = gson.toJson(newList)
        sharedPreferences.edit().putString("contacts_list", jsonString).apply()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, "Add Contact", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                text = "Emergency Contacts",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "These contacts will receive an SMS with your location when you trigger an SOS.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            if(showAddDialog) {
                AddContactForm(
                    onAdd = { newContact -> 
                        saveContacts(contacts + newContact)
                        showAddDialog = false
                    },
                    onCancel = { showAddDialog = false }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (contacts.isEmpty() && !showAddDialog) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No emergency contacts added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(contacts) { contact ->
                        ContactItem(
                            contact = contact, 
                            onDelete = { saveContacts(contacts.filter { it != contact }) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    text = "Continue to Home",
                    onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddContactForm(onAdd: (EmergencyContact) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    val context = LocalContext.current

    val contactsPermission = rememberPermissionState(Manifest.permission.READ_CONTACTS)

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                val cursor: Cursor? = context.contentResolver.query(
                    uri,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null, null, null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (nameIndex >= 0 && numberIndex >= 0) {
                            name = it.getString(nameIndex) ?: ""
                            phone = it.getString(numberIndex) ?: ""
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Text("Add New Contact", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = {
                if (contactsPermission.status.isGranted) {
                    val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                    contactPickerLauncher.launch(intent)
                } else {
                    contactsPermission.launchPermissionRequest()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pick from Contacts")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AuthTextField(value = name, onValueChange = { name = it }, label = "Name")
        Spacer(modifier = Modifier.height(8.dp))
        AuthTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number", keyboardType = KeyboardType.Phone)
        Spacer(modifier = Modifier.height(8.dp))
        AuthTextField(value = relationship, onValueChange = { relationship = it }, label = "Relationship (e.g. Parent)")
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(
                text = "Cancel", 
                onClick = onCancel, 
                modifier = Modifier.weight(1f)
            )
            PrimaryButton(
                text = "Save", 
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onAdd(EmergencyContact(name, phone, relationship))
                    } else {
                        Toast.makeText(context, "Name and Phone required", Toast.LENGTH_SHORT).show()
                    }
                }, 
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ContactItem(contact: EmergencyContact, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(contact.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${contact.relationship} • ${contact.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f))
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete Contact", tint = MaterialTheme.colorScheme.error)
        }
    }
}

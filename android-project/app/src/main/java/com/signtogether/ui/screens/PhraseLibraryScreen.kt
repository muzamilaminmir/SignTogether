package com.signtogether.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// For MVP, maintaining an in-memory or DataStore driven list of phrases
val defaultPhrases = listOf(
    "How can I help you today?",
    "Please show your ID.",
    "Your appointment is confirmed.",
    "Wait here for a moment."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhraseLibraryScreen(navController: NavController) {
    var phrases by remember { mutableStateOf(defaultPhrases) }
    var newPhrase by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom Phrase Library", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Manage Quick Phrases",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Add Phrase Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newPhrase,
                    onValueChange = { newPhrase = it },
                    label = { Text("New Phrase") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newPhrase.isNotBlank() && !phrases.contains(newPhrase)) {
                            phrases = phrases + newPhrase
                            newPhrase = ""
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Phrase", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Phrase List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(phrases) { phrase ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(phrase, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            IconButton(onClick = { phrases = phrases.filter { it != phrase } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove Phrase", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

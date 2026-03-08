package com.signtogether.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.signtogether.data.room.ConversationMessage
import com.signtogether.ui.viewmodels.ConversationViewModel
import com.signtogether.utils.PdfExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSessionScreen(
    navController: NavController,
    sessionId: String
) {
    val viewModel: ConversationViewModel = viewModel()
    val context = LocalContext.current
    
    val allSessions by viewModel.allSessions.collectAsState(initial = emptyList())
    val messages by viewModel.getMessagesForSession(sessionId).collectAsState(initial = emptyList())
    
    val currentSession = allSessions.find { it.sessionId == sessionId }
    val title = currentSession?.participantName ?: "Chat Details"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (messages.isNotEmpty() && currentSession != null) {
                        IconButton(onClick = { 
                            PdfExporter.exportSessionToPdf(context, currentSession, messages)
                        }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))
                .padding(padding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ConversationMessage) {
    val isUser = message.sender == "USER" // Deaf person translates signing to text
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f) // Max width for bubble
                .clip(shape)
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Text(
                text = message.input,
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.translatedOutput,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            if (message.confidenceScore != null && message.confidenceScore < 0.7) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Low Confidence (${(message.confidenceScore * 100).toInt()}%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Red.copy(alpha=0.8f)
                )
            }
        }
    }
}

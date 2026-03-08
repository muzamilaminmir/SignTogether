package com.signtogether.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TextToSignScreen(isHelpDesk: Boolean = false) {
    val context = LocalContext.current
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    if (audioPermissionState.status.isGranted) {
        TextToSignContent(isHelpDesk)
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Audio permission is required for Speech to Sign.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { audioPermissionState.launchPermissionRequest() }) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
fun TextToSignContent(isHelpDesk: Boolean = false) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    
    var inputText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    
    val words = translatedText.split(" ").filter { it.isNotBlank() }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    
    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isListening = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) { isListening = false }
            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    inputText = matches[0]
                    translatedText = matches[0]
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)
        
        onDispose {
            speechRecognizer.destroy()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        val saveToHistory = { text: String ->
            val dao = (context.applicationContext as com.signtogether.SignTogetherApp).database.conversationDao()
            val sessionId = java.util.UUID.randomUUID().toString()
            val session = com.signtogether.data.room.ConversationSession(sessionId = sessionId, timestamp = System.currentTimeMillis(), participantName = if(isHelpDesk) "Staff Member" else "Hearing User", isHelpDeskMode = isHelpDesk)
            val message = com.signtogether.data.room.ConversationMessage(sessionId = sessionId, timestamp = System.currentTimeMillis(), sender = "STAFF", input = text, translatedOutput = "[Sign Animation]", confidenceScore = 1.0, mode = "TextToSign")
            kotlinx.coroutines.GlobalScope.launch {
                dao.insertSession(session)
                dao.insertMessage(message)
            }
        }

        // Avatar View Area with Pulse/Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // AndroidView for WebView
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(8.dp).clip(RoundedCornerShape(16.dp)),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                                super.onPageFinished(view, url)
                                val css = "#cc--main, #cc-main, .cc-window, .cc-banner { display: none !important; }"
                                val js = "var style = document.createElement('style');" +
                                        "style.innerHTML = '$css';" +
                                        "document.head.appendChild(style);"
                                view.evaluateJavascript(js, null)
                            }
                        }
                        loadUrl("https://sign.mt/translate")
                    }
                },
                update = { webView ->
                    if (translatedText.isNotEmpty()) {
                        val encodedText = android.net.Uri.encode(translatedText)
                        webView.loadUrl("https://sign.mt/translate?text=$encodedText")
                    }
                }
            )
            
            // Floating Status Indicator
            if (isListening) {
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                    PulseAnimation()
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Word Tokens
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(words) { word ->
                SuggestionChip(
                    onClick = { /* Could replay specific word */ },
                    label = { Text(word, fontWeight = FontWeight.Bold) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Area
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type or speak to translate...") },
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            translatedText = inputText
                            saveToHistory(inputText)
                        }
                        focusManager.clearFocus()
                    }
                ),
                trailingIcon = {
                    if (inputText.isNotEmpty()) {
                        IconButton(onClick = { inputText = ""; translatedText = "" }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            FloatingActionButton(
                onClick = {
                    if (inputText.isNotEmpty()) {
                        translatedText = inputText
                        saveToHistory(inputText)
                        focusManager.clearFocus()
                    } else {
                        // Start listening
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        }
                        speechRecognizer.startListening(intent)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (inputText.isNotEmpty()) Icons.Default.Send else Icons.Default.Add,
                    contentDescription = if (inputText.isNotEmpty()) "Translate" else "Speak"
                )
            }
        }
    }
}

@Composable
fun PulseAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
        Box(
            modifier = Modifier
                .size(16.dp * scale)
                .clip(CircleShape)
                .background(Color.Red.copy(alpha = alpha))
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color.Red)
        )
    }
}

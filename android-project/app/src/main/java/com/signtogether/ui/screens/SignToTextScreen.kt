package com.signtogether.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.*
import com.google.mediapipe.solutions.hands.Hands
import com.google.mediapipe.solutions.hands.HandsOptions
import com.signtogether.SignLanguageProcessor
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SignToTextScreen(isHelpDesk: Boolean = false) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    if (cameraPermissionState.status.isGranted) {
        SignToTextCameraContent(isHelpDesk)
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Camera permission is required for Sign Language Translation.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
fun SignToTextCameraContent(isHelpDesk: Boolean = false) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var detectedSign by remember { mutableStateOf("...") }
    var sentence by remember { mutableStateOf("") }
    var confidence by remember { mutableStateOf(0f) }
    var smartSuggestion by remember { mutableStateOf<String?>(null) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_FRONT_CAMERA) }

    val processor = remember { SignLanguageProcessor() }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    
    DisposableEffect(Unit) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.apply {
                    language = Locale.US
                    setPitch(1.2f) // Slightly higher pitch for clarity
                    setSpeechRate(0.9f) // Slightly slower for better understanding in noisy environments
                }
            }
        }
        tts = textToSpeech

        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val hands = remember {
        val options = HandsOptions.builder()
            .setStaticImageMode(false)
            .setMaxNumHands(1)
            .setRunOnGpu(true)
            .build()
        Hands(context, options).apply {
            setErrorListener { message, _ -> Log.e("MediaPipe", "Error: $message") }
            setResultListener { result ->
                if (result.multiHandLandmarks() != null && result.multiHandLandmarks().isNotEmpty()) {
                    val landmarksList = mutableListOf<SignLanguageProcessor.Landmark>()
                    val mpLandmarks = result.multiHandLandmarks()[0].landmarkList
                    for (lm in mpLandmarks) {
                        landmarksList.add(SignLanguageProcessor.Landmark(lm.x, lm.y, lm.z))
                    }
                    val procResult = processor.process(landmarksList)
                    
                    detectedSign = procResult.sign ?: "..."
                    confidence = procResult.confidence
                    
                    if (procResult.isSentenceComplete && procResult.completedSentence != null) {
                        sentence = procResult.completedSentence
                    } else {
                        sentence = processor.fullSentence
                    }
                    
                    // Mock Smart Correction Layer
                    val mockCorrections = mapOf(
                        "HELLO HOW ARE YOU" to "Hello, how are you doing today?",
                        "I NEED HELP" to "Can someone please help me?",
                        "WATER PLEASE" to "Could I get some water, please?",
                        "ME HUNGRY" to "I am feeling hungry.",
                        "WHAT YOUR NAME" to "What is your name?"
                    )
                    
                    val trimmedSent = sentence.trim().uppercase(Locale.US)
                    smartSuggestion = mockCorrections[trimmedSent] ?: if (confidence > 0.1f && confidence < 0.6f) {
                        "Try signing slower. Did you mean '${detectedSign}'?"
                    } else null
                } else {
                    val procResult = processor.process(null)
                    detectedSign = "..."
                    if (procResult.isSentenceComplete && procResult.completedSentence != null) {
                        sentence = procResult.completedSentence
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            hands.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val bitmap = imageProxy.toBitmapCustom()
                        if (bitmap != null) {
                            hands.send(bitmap, System.currentTimeMillis())
                        }
                        imageProxy.close()
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("Camera", "Use case binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val bitmap = imageProxy.toBitmapCustom()
                        if (bitmap != null) {
                            hands.send(bitmap, System.currentTimeMillis())
                        }
                        imageProxy.close()
                    }
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                    } catch (e: Exception) {}
                }, ContextCompat.getMainExecutor(context))
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.8f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(androidx.compose.foundation.shape.CircleShape).background(androidx.compose.ui.graphics.Color.Red))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Live Translation", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                IconButton(
                    onClick = {
                        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        } else {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        }
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.8f), androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Switch Camera", tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Detected Sign: $detectedSign", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("${(confidence * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (sentence.isEmpty()) "Waiting for signs..." else sentence,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (smartSuggestion != null && sentence.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().clickable {
                                sentence = smartSuggestion!!
                                smartSuggestion = null
                            }
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("✨ Did you mean?", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha=0.7f))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(smartSuggestion!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                if (sentence.isNotEmpty()) {
                                    val dao = (context.applicationContext as com.signtogether.SignTogetherApp).database.conversationDao()
                                    val sessionId = java.util.UUID.randomUUID().toString()
                                    val session = com.signtogether.data.room.ConversationSession(sessionId = sessionId, timestamp = System.currentTimeMillis(), participantName = if(isHelpDesk) "Institutional Client" else "Deaf User", isHelpDeskMode = isHelpDesk)
                                    val message = com.signtogether.data.room.ConversationMessage(sessionId = sessionId, timestamp = System.currentTimeMillis(), sender = "USER", input = "[Sign Language]", translatedOutput = sentence, confidenceScore = confidence.toDouble(), mode = "SignToText")
                                    
                                    kotlinx.coroutines.GlobalScope.launch {
                                        dao.insertSession(session)
                                        dao.insertMessage(message)
                                    }
                                    android.widget.Toast.makeText(context, "Saved to History", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(androidx.compose.material.icons.Icons.Default.Save, contentDescription = "Save")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save")
                        }
                    
                        Button(onClick = {
                            if (sentence.isNotEmpty()) {
                                val params = Bundle()
                                params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                                tts?.speak(sentence, TextToSpeech.QUEUE_FLUSH, params, null)
                            }
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Speak")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Speak")
                        }
                    }
                }
            }
        }
    }
}

fun ImageProxy.toBitmapCustom(): Bitmap? {
    if (this.format != ImageFormat.YUV_420_888) return null
    
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 75, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

package com.signtogether.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class VideoLesson(val title: String, val description: String, val url: String)

@Composable
fun LearnScreen() {
    val context = LocalContext.current
    val dataStore = remember { com.signtogether.data.StoreUserProfile(context) }
    val appMode by dataStore.appModeFlow.collectAsState(initial = "STANDARD")
    
    val lessons = listOf(
        VideoLesson(
            "ASL Alphabet", 
            "Master the American Sign Language alphabets (A-Z).", 
            "https://www.youtube.com/watch?v=DBQINq0SsAw"
        ),
        VideoLesson(
            "Common Greetings", 
            "Learn essential signs for Hello, Thank You, and Nice to meet you.", 
            "https://www.youtube.com/watch?v=nJx-XsxeajQ"
        ),
        VideoLesson(
            "Emergency Signs", 
            "Critical ASL signs for Help, Doctor, and Emergency.", 
            "https://www.youtube.com/watch?v=zht0ia5Vq1U"
        ),
        VideoLesson(
            "Family Signs", 
            "Refer to Mother, Father, Brother, and Sister in ASL.", 
            "https://www.youtube.com/watch?v=VOnHnaNiVSM"
        )
    )

    var selectedLesson by remember { mutableStateOf<VideoLesson?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (appMode == "KID") "Sign Games & Videos" else "ASL Learning Center",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        if (selectedLesson != null) {
            // Video Thumbnail with Play Button
            val videoId = selectedLesson!!.url.substringAfter("v=").substringBefore("&")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(selectedLesson!!.url))
                        context.startActivity(intent)
                    },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    // YouTube Thumbnail
                    val thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { ctx ->
                            android.widget.ImageView(ctx).apply {
                                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                                // Load thumbnail
                                Thread {
                                    try {
                                        val stream = java.net.URL(thumbnailUrl).openStream()
                                        val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                                        post { setImageBitmap(bitmap) }
                                    } catch (e: Exception) { e.printStackTrace() }
                                }.start()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    // Play overlay
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color.Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    // Title overlay at bottom
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(12.dp)
                    ) {
                        Text(
                            selectedLesson!!.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            if (appMode == "KID") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4D2))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌟 3", style = MaterialTheme.typography.titleLarge)
                            Text("Badges", style = MaterialTheme.typography.labelMedium, color = Color.DarkGray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔥 5", style = MaterialTheme.typography.titleLarge)
                            Text("Day Streak", style = MaterialTheme.typography.labelMedium, color = Color.DarkGray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎯 2/3", style = MaterialTheme.typography.titleLarge)
                            Text("Daily Goal", style = MaterialTheme.typography.labelMedium, color = Color.DarkGray)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Text(
                    text = "Watch these curated American Sign Language (ASL) tutorials.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(lessons) { lesson ->
                LessonCard(lesson = lesson, isSelected = selectedLesson?.url == lesson.url) {
                    selectedLesson = lesson
                }
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}




@Composable
fun LessonCard(lesson: VideoLesson, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow, 
                    contentDescription = "Play Video", 
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = lesson.title, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = lesson.description, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

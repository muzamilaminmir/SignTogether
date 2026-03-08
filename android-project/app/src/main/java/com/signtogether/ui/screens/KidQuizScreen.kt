package com.signtogether.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.signtogether.data.StoreUserProfile
import kotlinx.coroutines.launch

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidQuizScreen(navController: NavController) {
    val context = LocalContext.current
    val dataStore = remember { StoreUserProfile(context) }
    val userClass by dataStore.userClassFlow.collectAsState(initial = "Class 1")
    
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    
    val questions = remember(userClass) {
        getQuestionsForClass(userClass ?: "Class 1")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign Quiz - $userClass") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF9DB)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFFF9DB))
        ) {
            if (showResult) {
                QuizResultContent(score, questions.size, navController)
            } else if (questions.isNotEmpty()) {
                val currentQuestion = questions[currentQuestionIndex]
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Progress Bar
                    LinearProgressIndicator(
                        progress = (currentQuestionIndex + 1).toFloat() / questions.size,
                        modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                        color = Color(0xFFFE7F2D)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = currentQuestion.question,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    currentQuestion.options.forEachIndexed { index, option ->
                        Button(
                            onClick = {
                                if (index == currentQuestion.correctAnswer) {
                                    score++
                                }
                                if (currentQuestionIndex < questions.size - 1) {
                                    currentQuestionIndex++
                                } else {
                                    showResult = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (index % 2 == 0) Color(0xFF91C6BC) else Color(0xFF215E61)
                            )
                        ) {
                            Text(text = option, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizResultContent(score: Int, total: Int, navController: NavController) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Great Job!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF215E61)
        )
        
        Text(
            text = "You scored $score out of $total",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(3) { i ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (i < (score * 3 / total)) Color(0xFFFFD700) else Color.LightGray,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "I just scored $score/$total on the SignTogether App! I'm learning Sign Language! 🤟")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Score"))
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE7F2D)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share Score", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Back to Home", fontWeight = FontWeight.Bold)
        }
    }
}

fun getQuestionsForClass(userClass: String): List<QuizQuestion> {
    val level = userClass.filter { it.isDigit() }.toIntOrNull() ?: 1
    
    val questions = when {
        level <= 3 -> listOf(
            QuizQuestion("Which hand shape represents the letter 'A' in ISL?", listOf("Closed Fist with thumb beside", "Open Palm", "Flat Hand", "Pointed Finger"), 0),
            QuizQuestion("How do you sign 'Hello' in ISL?", listOf("Clap hands", "Touch forehead", "Wave your open hand", "Point upward"), 2),
            QuizQuestion("What does tapping your chin with fingers mean?", listOf("Water", "Food", "Thank You", "Sorry"), 0),
            QuizQuestion("How do you sign the number '5'?", listOf("Closed fist", "Show all five fingers open", "Two fingers up", "Thumb up only"), 1),
            QuizQuestion("Which body part is used to sign 'Mother' in ISL?", listOf("Forehead", "Chin", "Cheek with open palm", "Shoulder"), 2),
            QuizQuestion("To sign 'Yes' in ISL, you:", listOf("Shake head side to side", "Nod your fist up and down", "Wave both hands", "Point to yourself"), 1),
            QuizQuestion("What is the sign for 'No'?", listOf("Nod your head", "Snap index and middle finger to thumb", "Clap once", "Cross your arms"), 1),
            QuizQuestion("How is 'Father' signed in ISL?", listOf("Touch chin with thumb", "Touch forehead with thumb", "Wave hand", "Pat shoulder"), 1),
            QuizQuestion("What does touching your lips and moving hand outward mean?", listOf("Eat", "Speak", "Thank You", "Sing"), 2),
            QuizQuestion("Which hand gesture means 'Good' in ISL?", listOf("Thumbs down", "Thumbs up", "Closed fist", "Open palm push"), 1)
        )
        level <= 6 -> listOf(
            QuizQuestion("Sign for 'Thank You' involves which movement?", listOf("Wave hand", "Touch chin, move forward", "Clap hands", "Point to person"), 1),
            QuizQuestion("Sign for 'Help' in ISL uses:", listOf("Fist on open palm, lift up", "Pointed finger", "Two fingers wave", "Arms crossed"), 0),
            QuizQuestion("Where is the sign for 'School' located?", listOf("One palm claps on the other", "Forehead", "Chest", "Shoulder"), 0),
            QuizQuestion("To sign 'Friend' you:", listOf("Shake fists", "Link index fingers together", "Wave goodbye", "Cross hands"), 1),
            QuizQuestion("How do you express 'I don't understand' in ISL?", listOf("Nod your head", "Point to your ear", "Touch forehead and shake finger", "Cross arms"), 2),
            QuizQuestion("The sign for 'Please' is made by:", listOf("Clapping", "Rubbing open palm on chest in circles", "Snapping fingers", "Waving"), 1),
            QuizQuestion("How is 'Doctor' signed in ISL?", listOf("Tap wrist with two fingers (like taking pulse)", "Point to heart", "Touch forehead", "Wave stethoscope shape"), 0),
            QuizQuestion("Sign for 'Book' imitates:", listOf("Writing on paper", "Opening and closing palms together", "Turning pages in the air", "Holding a pen"), 1),
            QuizQuestion("To express 'Sorry' in ISL, you:", listOf("Wave hand", "Touch ear", "Make a fist and rub it in circles on chest", "Point down"), 2),
            QuizQuestion("What facial expression accompanies questions in ISL?", listOf("Smile always", "Raised eyebrows for yes/no questions", "Close eyes", "Puff cheeks"), 1)
        )
        else -> listOf(
            QuizQuestion("'Emergency' in ISL is signed:", listOf("Slow and smooth", "With a calm face", "Fast with repeated waving of 'E' hand", "Holding still"), 2),
            QuizQuestion("How is 'Hospital' signed in ISL?", listOf("Draw a cross shape on upper arm", "H shape in air", "Point to heart", "Sleeping head"), 0),
            QuizQuestion("ISL sentence structure typically follows:", listOf("Subject-Verb-Object", "Subject-Object-Verb (SOV)", "Verb-Subject-Object", "Random order"), 1),
            QuizQuestion("Non-manual signals in ISL include:", listOf("Only hand shapes", "Facial expressions, head tilts, body posture", "Foot tapping", "Clapping"), 1),
            QuizQuestion("The sign for 'Danger' involves:", listOf("Gentle wave", "Fist pushing up through other palm forcefully", "Thumbs up", "Blowing air"), 1),
            QuizQuestion("Fingerspelling in ISL is typically used for:", listOf("All words", "Proper nouns, technical terms, and names", "Only verbs", "Only adjectives"), 1),
            QuizQuestion("To sign 'Police' in ISL, you:", listOf("Salute gesture near forehead", "Wave hand", "Cross arms", "Point down"), 0),
            QuizQuestion("Classifiers in ISL are:", listOf("Only nouns", "Hand shapes that represent categories of objects", "Written symbols", "Sounds"), 1),
            QuizQuestion("How would you sign 'I am hungry'?", listOf("Pat your stomach", "Touch throat and pull hand down (empty feeling)", "Point to food", "Rub hands together"), 1),
            QuizQuestion("The difference between ISL and ASL is:", listOf("They are identical", "ISL has its own grammar and vocabulary distinct from ASL", "Only speed differs", "ISL uses only one hand"), 1)
        )
    }
    
    return questions.shuffled()
}

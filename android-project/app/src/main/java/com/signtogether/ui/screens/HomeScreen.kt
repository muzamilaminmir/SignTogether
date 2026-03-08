package com.signtogether.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.signtogether.data.StoreUserProfile
import com.signtogether.navigation.Screen
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(Screen.Home.route, "Home", Icons.Default.Home)
    object Learn : BottomNavItem(Screen.Learn.route, "Learn", Icons.Default.School)
    object SOS : BottomNavItem(Screen.SOS.route, "SOS", Icons.Default.Warning)
    object History : BottomNavItem(Screen.History.route, "History", Icons.Default.Info)
    object Settings : BottomNavItem(Screen.Settings.route, "Settings", Icons.Default.Settings)
}

@Composable
fun MainScaffold(navController: NavController, showBottomBar: Boolean = true, content: @Composable (PaddingValues) -> Unit) {
    val context = LocalContext.current
    val dataStore = remember { StoreUserProfile(context) }
    val appMode by dataStore.appModeFlow.collectAsState(initial = "STANDARD")

    val items = when (appMode) {
        "KID" -> listOf(
            BottomNavItem.Home,
            BottomNavItem.Learn,
            BottomNavItem.Settings
        )
        "HELP_DESK" -> listOf(
            BottomNavItem.Home,
            BottomNavItem.Learn,
            BottomNavItem.History,
            BottomNavItem.Settings
        )
        else -> listOf(
            BottomNavItem.Home,
            BottomNavItem.Learn,
            BottomNavItem.SOS,
            BottomNavItem.History,
            BottomNavItem.Settings
        )
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    items.forEach { item ->
                        val isSos = item == BottomNavItem.SOS
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    imageVector = item.icon, 
                                    contentDescription = item.title,
                                    tint = if (isSos) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                            },
                            label = { 
                                Text(
                                    text = item.title, 
                                    color = if (isSos) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isSos) Color.White else MaterialTheme.colorScheme.primary,
                                selectedTextColor = if (isSos) Color.Red else MaterialTheme.colorScheme.primary,
                                indicatorColor = if (isSos) Color.Red.copy(alpha=0.2f) else MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

// Update the HomeScreen to use Scaffold if it is the entry point, or wrap the NavGraph in it.
// For now, let's keep the Scaffold scoped so we can use it on the main 5 tabs.

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val dataStore = remember { StoreUserProfile(context) }
    val appMode by dataStore.appModeFlow.collectAsState(initial = "STANDARD")
    val instName by dataStore.instNameFlow.collectAsState(initial = "")

    var hideBottomBar by remember { mutableStateOf(false) }

    MainScaffold(navController = navController, showBottomBar = !hideBottomBar) { paddingValues ->
        Box(modifier = Modifier.padding(if (hideBottomBar) PaddingValues(0.dp) else paddingValues).fillMaxSize()) {
            if (appMode == "KID") {
                KidModeHomeScreenContent(navController, onSubModeChange = { hideBottomBar = it })
            } else if (appMode == "HELP_DESK") {
                HelpDeskHomeScreenContent(navController, instName ?: "Institution", onSessionChange = { hideBottomBar = it })
            } else {
                StandardHomeScreenContent()
            }
        }
    }
}

@Composable
fun HelpDeskHomeScreenContent(navController: NavController, institutionName: String, onSessionChange: (Boolean) -> Unit) {
    var isSessionActive by remember { mutableStateOf(false) }
    
    LaunchedEffect(isSessionActive) {
        onSessionChange(isSessionActive)
    }
    val context = LocalContext.current
    val presetPhrases = listOf("How can I help you?", "Please wait a moment.", "Doctor is coming.", "Sign here please.", "Thank you.")

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)) // Professional Dark Background
    ) {
        if (isSessionActive) {
            var selectedHelpDeskTab by remember { mutableStateOf(0) }
            
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = selectedHelpDeskTab,
                    containerColor = Color(0xFF1A1A1A),
                    contentColor = Color(0xFFFFD700),
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedHelpDeskTab]),
                            color = Color(0xFFFFD700)
                        )
                    }
                ) {
                    Tab(
                        selected = selectedHelpDeskTab == 0,
                        onClick = { selectedHelpDeskTab = 0 },
                        text = { Text("PROVIDER PANEL", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedHelpDeskTab == 1,
                        onClick = { selectedHelpDeskTab = 1 },
                        text = { Text("USER CAMERA", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedHelpDeskTab) {
                        0 -> {
                            // PROVIDER AREA (Hearing Person)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF1E1E1E))
                                    .padding(16.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("TRANSCRIPTION / AVATAR", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFFD700))
                                        TextButton(onClick = { isSessionActive = false }) {
                                            Text("EXIT", color = Color.Red, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Preset Phrases Grid
                                    Text("QUICK PRESETS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha=0.5f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(presetPhrases) { phrase ->
                                            AssistChip(
                                                onClick = { /* TODO: Trigger TTS/Avatar */ },
                                                label = { Text(phrase) },
                                                colors = AssistChipDefaults.assistChipColors(labelColor = Color.White, containerColor = Color(0xFF333333))
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    // Avatar Output - Expanded to fill space
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.Black)) {
                                        TextToSignContent() 
                                    }
                                }
                            }
                        }
                        1 -> {
                            // DEAF USER AREA (Live Camera)
                            Box(modifier = Modifier.fillMaxSize()) {
                                SignToTextCameraContent(isHelpDesk = true)
                                IconButton(
                                    onClick = { isSessionActive = false },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha=0.5f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TOP HEADER: INSTITUTION NAME (Only show if inactive)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1A1A1A),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = institutionName.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700) // High Contrast Gold
                        )
                        Text(
                            text = "PROFESSIONAL HELP DESK",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    
                    Button(
                        onClick = { isSessionActive = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("START SESSION", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Session is currently inactive.\nTap 'START SESSION' to begin communication.",
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun StandardHomeScreenContent() {
    val context = LocalContext.current
    val dataStore = remember { StoreUserProfile(context) }
    val userName by dataStore.userNameFlow.collectAsState(initial = "User")
    
    var selectedTab by remember { mutableStateOf(0) }
    Column(modifier = Modifier.fillMaxSize()) {
        // Personalized Greeting Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "Hello, ${userName ?: "User"}! \uD83D\uDC4B",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Start translating sign language in real-time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        // Daily Sign of the Day
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        val dailySigns = listOf(
            Triple("Hello 👋", "Wave your open hand side to side", "Open palm, fingers together"),
            Triple("Thank You 🙏", "Touch your chin with fingertips, move hand forward", "Flat hand from chin outward"),
            Triple("Help 🆘", "Place fist on open palm, lift both up", "Fist on flat palm, raise"),
            Triple("Sorry 😔", "Make a fist, rub in circles on chest", "Fist circles on chest"),
            Triple("Please 🙂", "Rub open palm in circles on chest", "Flat hand circles on chest"),
            Triple("Yes ✅", "Nod your fist up and down", "Fist nods like head"),
            Triple("No ❌", "Snap index and middle finger to thumb", "Two fingers snap to thumb"),
            Triple("Friend 🤝", "Hook index fingers together", "Linked index fingers"),
            Triple("Family 👨‍👩‍👧‍👦", "Both hands make 'F', circle outward", "F-hands circle outward"),
            Triple("Water 💧", "Tap chin with 'W' hand shape", "Three fingers tap chin"),
            Triple("Food 🍽️", "Bring flat O hand to mouth repeatedly", "Fingertips to mouth"),
            Triple("Good 👍", "Touch chin, move hand down to open palm", "Chin to flat palm below"),
            Triple("Bad 👎", "Touch chin, flip hand downward", "Chin then flip down"),
            Triple("Love ❤️", "Cross arms over chest in hug", "Self-hug gesture"),
            Triple("Learn 📚", "Take from open palm up to forehead", "Palm to forehead"),
            Triple("School 🏫", "Clap hands together twice", "Double clap"),
            Triple("Doctor 🩺", "Tap wrist with two fingers", "Pulse-taking motion"),
            Triple("Home 🏠", "Flat O to cheek, then move to jaw", "Cheek to jaw"),
            Triple("Name 📛", "H-fingers tap on opposite H-fingers", "Two H-shapes tap"),
            Triple("Beautiful ✨", "Circle face with open hand, close to flat O", "Hand circles face"),
            Triple("Happy 😊", "Brush chest upward repeatedly", "Upward brush on chest"),
            Triple("Sad 😢", "Hands slide down face", "Fingers slide down face"),
            Triple("Morning 🌅", "Arm rises like sun from flat hand", "Arm rises from flat base"),
            Triple("Night 🌙", "Bent hand drops over flat hand", "Bent hand descends"),
            Triple("Eat 🍴", "Bring flat O to mouth", "Fingertips to lips"),
            Triple("Drink 🥤", "C-hand tilts to mouth", "Cup shape to mouth"),
            Triple("Stop ✋", "Flat hand chops onto open palm", "Chop onto palm"),
            Triple("Go 🏃", "Both index fingers point and move forward", "Point and push forward"),
            Triple("Come 🫳", "Beckon with index finger", "Finger curls toward you"),
            Triple("Understand 💡", "Flick index finger up near forehead", "Finger flicks up at temple")
        )
        val todaySign = dailySigns[dayOfYear % dailySigns.size]
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "✨ Sign of the Day",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(java.util.Date()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    todaySign.first,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    todaySign.second,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Hand shape: ${todaySign.third}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                )
            }
        }
        
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Sign \u2192 Text") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Text \u2192 Sign") })
        }
        
        when (selectedTab) {
            0 -> SignToTextScreen()
            1 -> TextToSignScreen()
        }
    }
}

@Composable
fun KidModeHomeScreenContent(navController: NavController, onSubModeChange: (Boolean) -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    
    LaunchedEffect(selectedTab) {
        onSubModeChange(selectedTab != 0)
    }
    var showTalkChoice by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFF9DB)) // Soft playful yellow background
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Face,
                    contentDescription = "Kid Mode Box",
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFFFE7F2D)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Welcome to Kid Mode!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF215E61)
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Large playful buttons
                KidModeCard(
                    title = "Let's Talk!",
                    subtitle = "Translate your signs to words.",
                    icon = Icons.Default.PlayArrow,
                    color = Color(0xFF91C6BC),
                    onClick = { showTalkChoice = true }
                )

                if (showTalkChoice) {
                    AlertDialog(
                        onDismissRequest = { showTalkChoice = false },
                        title = { Text("How do you want to talk?", fontWeight = FontWeight.Bold) },
                        text = { Text("Choose if you want to sign for others or hear what others say!") },
                        confirmButton = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { 
                                        selectedTab = 1 // Sign to Text
                                        showTalkChoice = false 
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF91C6BC))
                                ) {
                                    Text("I want to Sign 🖐️", color = Color.White)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { 
                                        selectedTab = 3 // Text to Sign
                                        showTalkChoice = false 
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF215E61))
                                ) {
                                    Text("I want to Hear 👂", color = Color.White)
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTalkChoice = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                KidModeCard(
                    title = "Learn Signs",
                    subtitle = "Play games and learn new signs!",
                    icon = Icons.Default.School,
                    color = Color(0xFF215E61),
                    onClick = { selectedTab = 2 }
                )

                Spacer(modifier = Modifier.height(16.dp))

                KidModeCard(
                    title = "Sign Quiz",
                    subtitle = "Test your knowledge and earn stars!",
                    icon = Icons.Default.EmojiEvents,
                    color = Color(0xFFFE7F2D),
                    onClick = { navController.navigate(com.signtogether.navigation.Screen.KidQuiz.route) }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🔒 Safe Environment",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50), // Friendly Green
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Social features and nearby maps are hidden.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else if (selectedTab == 1) {
            Box(modifier = Modifier.fillMaxSize()) {
                SignToTextScreen()
                IconButton(
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(Color.Black.copy(alpha=0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        } else if (selectedTab == 2) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF9DB)).padding(8.dp)) {
                    IconButton(onClick = { selectedTab = 0 }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF215E61))
                    }
                    Text("Learn Signs", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterVertically), color = Color(0xFF215E61))
                }
                LearnScreen()
            }
        } else if (selectedTab == 3) {
            Box(modifier = Modifier.fillMaxSize()) {
                TextToSignScreen()
                IconButton(
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(Color.White.copy(alpha=0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
            }
        }
    }
}

@Composable
fun KidModeCard(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(color)
            .clickable { onClick() }
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha=0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha=0.8f))
            }
        }
    }
}

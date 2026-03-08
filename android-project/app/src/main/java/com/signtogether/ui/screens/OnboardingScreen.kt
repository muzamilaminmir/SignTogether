package com.signtogether.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.signtogether.navigation.Screen
import kotlinx.coroutines.launch

// Data class for each onboarding page
data class OnboardingPage(val title: String, val description: String, val emoji: String)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pages = listOf(
        OnboardingPage(
            title = "Communication Without Barriers",
            description = "Instantly translate sign language to speech and text using your mobile camera.",
            emoji = "🤟"
        ),
        OnboardingPage(
            title = "Text/Speech to Sign",
            description = "Speak or type to the app and generate dynamic 3D avatar sign language animations.",
            emoji = "🗣️"
        ),
        OnboardingPage(
            title = "Deaf Community & Emergency SOS",
            description = "Connect with nearby users, find NGOs, and trigger SOS alerts with a single tap.",
            emoji = "🌍"
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Skip Button at top right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { 
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }) {
                    Text("Skip", color = MaterialTheme.colorScheme.onBackground)
                }
            }
            
            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { position ->
                OnboardingPageContent(page = pages[position])
            }

            // Bottom indicators and Next/GetStarted button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    repeat(pages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }
                
                // Navigation button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            navController.navigate(Screen.ProfileSetup.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    val colors = listOf(
        androidx.compose.ui.graphics.Color(0xFF91C6BC),
        androidx.compose.ui.graphics.Color(0xFF215E61),
        androidx.compose.ui.graphics.Color(0xFFFE7F2D)
    )
    val colorIndex = when(page.emoji) {
        "🤟" -> 0
        "🗣️" -> 1
        else -> 2
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated emoji illustration
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    color = colors[colorIndex].copy(alpha = 0.15f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(
                        color = colors[colorIndex].copy(alpha = 0.25f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = page.emoji,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = androidx.compose.ui.unit.TextUnit(72f, androidx.compose.ui.unit.TextUnitType.Sp)
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(64.dp))
        
        Text(
            text = page.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

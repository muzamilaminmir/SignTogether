package com.signtogether.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.signtogether.R
import com.signtogether.navigation.Screen
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.signtogether.data.StoreUserProfile

@Composable
fun SplashScreen(navController: NavController) {
    val alphaAnim = remember { Animatable(0f) }
    val context = LocalContext.current
    val dataStore = remember { StoreUserProfile(context) }
    val userName by dataStore.userNameFlow.collectAsState(initial = null)
    
    LaunchedEffect(key1 = true) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500)
        )
        delay(1000)
        
        if (userName.isNullOrBlank()) {
            navController.navigate(Screen.ModeSelection.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        // App logo to be added next
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher),
            contentDescription = "SignTogether Logo",
            modifier = Modifier
                .alpha(alphaAnim.value)
                .size(200.dp)
        )
    }
}

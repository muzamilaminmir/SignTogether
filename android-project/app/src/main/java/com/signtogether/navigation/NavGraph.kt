package com.signtogether.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.signtogether.ui.screens.HomeScreen
import com.signtogether.ui.screens.NearbyScreen
import com.signtogether.ui.screens.SOSScreen
import com.signtogether.ui.screens.HistoryScreen
import com.signtogether.ui.screens.SettingsScreen
import com.signtogether.ui.screens.NgoLocatorScreen

import com.signtogether.ui.screens.ProfileSetupScreen
import com.signtogether.ui.screens.EmergencyContactsScreen
import com.signtogether.ui.screens.LearnScreen

// Shared transition specs
private val enterTransition = fadeIn(animationSpec = tween(300)) + slideInHorizontally(
    initialOffsetX = { 100 }, animationSpec = tween(300)
)
private val exitTransition = fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
    targetOffsetX = { -100 }, animationSpec = tween(300)
)
private val popEnterTransition = fadeIn(animationSpec = tween(300)) + slideInHorizontally(
    initialOffsetX = { -100 }, animationSpec = tween(300)
)
private val popExitTransition = fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
    targetOffsetX = { 100 }, animationSpec = tween(300)
)

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { popEnterTransition },
        popExitTransition = { popExitTransition }
    ) {
        composable(route = Screen.Splash.route) {
            com.signtogether.ui.screens.SplashScreen(navController = navController)
        }
        composable(route = Screen.ModeSelection.route) {
            com.signtogether.ui.screens.ModeSelectionScreen(navController = navController)
        }
        composable(route = Screen.InstitutionalRegistration.route) {
            com.signtogether.ui.screens.InstitutionalRegistrationScreen(navController = navController)
        }
        composable(route = Screen.KidQuiz.route) {
            com.signtogether.ui.screens.KidQuizScreen(navController = navController)
        }
        composable(route = Screen.Onboarding.route) {
            com.signtogether.ui.screens.OnboardingScreen(navController = navController)
        }
        // Profile Setup Flow
        composable(route = Screen.ProfileSetup.route) {
            ProfileSetupScreen(navController = navController)
        }
        composable(route = Screen.EmergencyContacts.route) {
            EmergencyContactsScreen(navController = navController)
        }

        // Main App Tabs
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(route = Screen.Learn.route) {
            LearnScreen()
        }
        composable(route = Screen.SOS.route) {
            SOSScreen(navController = navController)
        }
        composable(route = Screen.History.route) {
            HistoryScreen(navController = navController)
        }
        composable(route = Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        
        // Extra Features
        composable(route = Screen.NGOLocator.route) {
            NgoLocatorScreen(navController = navController)
        }
        
        composable("chat_session/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId")
            if (sessionId != null) {
                com.signtogether.ui.screens.ChatSessionScreen(
                    navController = navController,
                    sessionId = sessionId
                )
            }
        }
        
        composable(route = Screen.PhraseLibrary.route) {
            com.signtogether.ui.screens.PhraseLibraryScreen(navController = navController)
        }
        
        composable(route = Screen.AnalyticsDashboard.route) {
            com.signtogether.ui.screens.AnalyticsDashboardScreen(navController = navController)
        }
    }
}

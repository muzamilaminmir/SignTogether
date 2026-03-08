package com.signtogether.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Onboarding : Screen("onboarding_screen")
    object ProfileSetup : Screen("profile_setup_screen")
    object EmergencyContacts : Screen("emergency_contacts_screen")
    
    // Bottom Nav Screens
    object Home : Screen("home_screen")
    object Nearby : Screen("nearby_screen")
    object SOS : Screen("sos_screen")
    object History : Screen("history_screen")
    object Settings : Screen("settings_screen")
    
    // Extra Features
    object NGOLocator : Screen("ngo_locator_screen")
    object SignToText : Screen("sign_to_text_screen")
    object TextToSign : Screen("text_to_sign_screen")
    object PhraseLibrary : Screen("phrase_library_screen")
    object AnalyticsDashboard : Screen("analytics_dashboard_screen")
    object ModeSelection : Screen("mode_selection_screen")
    object InstitutionalRegistration : Screen("institutional_registration_screen")
    object KidQuiz : Screen("kid_quiz_screen")
    object Learn : Screen("learn_screen")
}

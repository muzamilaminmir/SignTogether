package com.signtogether.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "userProfile")

class StoreUserProfile(private val context: Context) {
    
    companion object {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_GENDER = stringPreferencesKey("user_gender")
        val USER_DOB = stringPreferencesKey("user_dob")
        val USER_BLOOD_GROUP = stringPreferencesKey("user_blood_group")
        val USER_ADDRESS = stringPreferencesKey("user_address")
        val USER_PROFILE_PIC_URI = stringPreferencesKey("user_profile_pic_uri")
        val APP_MODE = stringPreferencesKey("app_mode") // "STANDARD", "KID", "HELP_DESK"

        // Kid Mode Fields
        val USER_AGE = stringPreferencesKey("user_age")
        val USER_CLASS = stringPreferencesKey("user_class")
        val GUARDIAN_CONTACT = stringPreferencesKey("guardian_contact")

        // Institutional Fields
        val INST_NAME = stringPreferencesKey("inst_name")
        val INST_TYPE = stringPreferencesKey("inst_type")
        val INST_CONTACT = stringPreferencesKey("inst_contact")
        val INST_ADDRESS = stringPreferencesKey("inst_address")
        val INST_ADMIN = stringPreferencesKey("inst_admin")
        val INST_EMAIL = stringPreferencesKey("inst_email")
        val INST_SUBSCRIPTION = stringPreferencesKey("inst_subscription")
        
        // App Settings
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val PARENTAL_PIN = stringPreferencesKey("parental_pin")
    }
    
    val userNameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME]
    }
    
    val userGenderFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_GENDER]
    }
    
    val userDobFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_DOB]
    }

    val userBloodGroupFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_BLOOD_GROUP]
    }

    val userAddressFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ADDRESS]
    }
    
    val userProfilePicUriFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_PROFILE_PIC_URI]
    }

    val appModeFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[APP_MODE] ?: "STANDARD"
    }

    val userAgeFlow: Flow<String?> = context.dataStore.data.map { it[USER_AGE] }
    val userClassFlow: Flow<String?> = context.dataStore.data.map { it[USER_CLASS] }
    val guardianContactFlow: Flow<String?> = context.dataStore.data.map { it[GUARDIAN_CONTACT] }

    // Institutional Flows
    val instNameFlow: Flow<String?> = context.dataStore.data.map { it[INST_NAME] }
    val instTypeFlow: Flow<String?> = context.dataStore.data.map { it[INST_TYPE] }
    val instContactFlow: Flow<String?> = context.dataStore.data.map { it[INST_CONTACT] }
    val instAddressFlow: Flow<String?> = context.dataStore.data.map { it[INST_ADDRESS] }
    val instAdminFlow: Flow<String?> = context.dataStore.data.map { it[INST_ADMIN] }
    val instEmailFlow: Flow<String?> = context.dataStore.data.map { it[INST_EMAIL] }
    val instSubscriptionFlow: Flow<String?> = context.dataStore.data.map { it[INST_SUBSCRIPTION] }

    // App Settings Flows
    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: false }
    val parentalPinFlow: Flow<String?> = context.dataStore.data.map { it[PARENTAL_PIN] }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun setParentalPin(pin: String) {
        context.dataStore.edit { it[PARENTAL_PIN] = pin }
    }

    suspend fun saveUserProfile(
        name: String,
        gender: String,
        dob: String,
        bloodGroup: String,
        address: String,
        picUri: String,
        appMode: String = "STANDARD",
        age: String = "",
        userClass: String = "",
        guardianContact: String = ""
    ) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = name
            preferences[USER_GENDER] = gender
            preferences[USER_DOB] = dob
            preferences[USER_BLOOD_GROUP] = bloodGroup
            preferences[USER_ADDRESS] = address
            preferences[USER_PROFILE_PIC_URI] = picUri
            preferences[APP_MODE] = appMode
            preferences[USER_AGE] = age
            preferences[USER_CLASS] = userClass
            preferences[GUARDIAN_CONTACT] = guardianContact
        }
    }

    suspend fun setAppMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_MODE] = mode
        }
    }

    suspend fun saveInstitutionalProfile(
        name: String,
        type: String,
        contact: String,
        address: String,
        admin: String,
        email: String,
        subscription: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[INST_NAME] = name
            preferences[INST_TYPE] = type
            preferences[INST_CONTACT] = contact
            preferences[INST_ADDRESS] = address
            preferences[INST_ADMIN] = admin
            preferences[INST_EMAIL] = email
            preferences[INST_SUBSCRIPTION] = subscription
            preferences[APP_MODE] = "HELP_DESK"
            preferences[StoreUserProfile.USER_NAME] = name // Use institution name as display name
        }
    }

    suspend fun clearUserProfile() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

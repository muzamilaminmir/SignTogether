package com.signtogether

import android.app.Application
import com.google.firebase.FirebaseApp
import com.signtogether.data.room.AppDatabase

class SignTogetherApp : Application() {
    
    // Lazy instantation of the Room Database so it's tied to the global Application lifecycle
    val database by lazy { AppDatabase.getDatabase(this) }
    
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        
        // RevenueCat initialization will go here in Phase 11
        // Purchases.configure(PurchasesConfiguration.Builder(this, "public_sdk_key").build())
    }
}

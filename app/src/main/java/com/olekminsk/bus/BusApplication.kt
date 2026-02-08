package com.olekminsk.bus

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.olekminsk.bus.data.local.BusDatabase

/**
 * Application класс для инициализации
 */
class BusApplication : Application() {
    
    val database by lazy { BusDatabase.getDatabase(this) }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Инициализация Firebase
        try {
            FirebaseApp.initializeApp(this)
            
            // Получение FCM токена
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM", "Token: $token")
                } else {
                    Log.e("FCM", "Failed to get token", task.exception)
                }
            }
        } catch (e: Exception) {
            Log.e("FCM", "Firebase initialization failed: ${e.message}")
        }
    }
    
    companion object {
        lateinit var instance: BusApplication
            private set
    }
}

package com.example.olekminskbus

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class PushNotificationManager(private val context: Context) {

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
        
        fun hasPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context, 
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Permission not needed before Android 13
            }
        }
    }

    fun requestPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    suspend fun getToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun subscribeToRoute(routeId: Int): Boolean {
        return try {
            FirebaseMessaging.getInstance().subscribeToTopic("route_$routeId").await()
            // Also update server with token and subscribed routes
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun unsubscribeFromRoute(routeId: Int): Boolean {
        return try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("route_$routeId").await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun scheduleReminder(routeId: Int, direction: String, time: String, reminderMinutes: List<Int> = listOf(10, 5, 1)) {
        // Send request to backend to schedule push notifications
        // POST /api/scheduleBusReminder
    }
}

package com.olekminsk.bus.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.olekminsk.bus.MainActivity
import com.olekminsk.bus.R

/**
 * Сервис для обработки FCM push-уведомлений
 */
class BusFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "bus_fcm_notifications"
        const val CHANNEL_NAME = "Обновления расписания"
        const val TAG = "FCM"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Получено новое push-уведомление
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "From: ${remoteMessage.from}")
        
        // Обработка данных из пуша
        remoteMessage.data.let { data ->
            Log.d(TAG, "Message data payload: $data")
            
            val title = data["title"] ?: "Автобусы Олёкминска"
            val message = data["message"] ?: "Новое обновление"
            val routeId = data["route_id"]?.toIntOrNull()
            
            sendNotification(title, message, routeId)
        }
        
        // Обработка notification payload (если есть)
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "Автобусы Олёкминска"
            val message = notification.body ?: ""
            sendNotification(title, message, null)
        }
    }

    /**
     * Обновление токена FCM
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        
        // Отправить токен на сервер
        sendRegistrationToServer(token)
    }

    /**
     * Отправить токен на сервер для привязки к пользователю
     */
    private fun sendRegistrationToServer(token: String) {
        // TODO: Реализовать отправку токена на ваш сервер
        // Например, через Retrofit API
    }

    /**
     * Создание канала уведомлений (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления об изменениях в расписании"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Отправка локального уведомления
     */
    private fun sendNotification(title: String, message: String, routeId: Int?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            routeId?.let { putExtra("route_id", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bus)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}

package com.olekminsk.bus.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.olekminsk.bus.MainActivity
import com.olekminsk.bus.R
import com.olekminsk.bus.data.local.BusDatabase
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Worker для отправки уведомлений о прибытии автобуса
 */
class BusNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val routeId = inputData.getInt(KEY_ROUTE_ID, -1)
        val routeName = inputData.getString(KEY_ROUTE_NAME) ?: return Result.failure()
        val departureTime = inputData.getString(KEY_DEPARTURE_TIME) ?: return Result.failure()
        
        createNotificationChannel()
        sendNotification(routeName, departureTime)
        
        return Result.success()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Автобусы"
            val descriptionText = "Уведомления о времени отправления автобусов"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun sendNotification(routeName: String, departureTime: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bus) // Нужно добавить иконку
            .setContentTitle("🚌 Автобус скоро отправится")
            .setContentText("Маршрут: $routeName\nВремя: $departureTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    companion object {
        const val CHANNEL_ID = "bus_notifications"
        const val NOTIFICATION_ID = 1001
        const val WORK_TAG = "bus_notification"
        
        const val KEY_ROUTE_ID = "route_id"
        const val KEY_ROUTE_NAME = "route_name"
        const val KEY_DEPARTURE_TIME = "departure_time"
        
        /**
         * Запланировать уведомление перед отправлением автобуса
         * 
         * @param context Контекст
         * @param routeId ID маршрута
         * @param routeName Название маршрута
         * @param departureTime Время отправления (HH:mm)
         * @param minutesBefore За сколько минут уведомить (по умолчанию 15)
         */
        fun scheduleNotification(
            context: Context,
            routeId: Int,
            routeName: String,
            departureTime: String,
            minutesBefore: Int = 15
        ) {
            // Парсим время отправления
            val timeParts = departureTime.split(":")
            if (timeParts.size != 2) return
            
            val departureHour = timeParts[0].toIntOrNull() ?: return
            val departureMinute = timeParts[1].toIntOrNull() ?: return
            
            // Рассчитываем время уведомления
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, departureHour)
                set(Calendar.MINUTE, departureMinute)
                add(Calendar.MINUTE, -minutesBefore)
            }
            
            // Если время уже прошло — не устанавливаем
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                return
            }
            
            val delay = calendar.timeInMillis - System.currentTimeMillis()
            
            val inputData = workDataOf(
                KEY_ROUTE_ID to routeId,
                KEY_ROUTE_NAME to routeName,
                KEY_DEPARTURE_TIME to departureTime
            )
            
            val workRequest = OneTimeWorkRequestBuilder<BusNotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(WORK_TAG)
                .build()
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                "notification_$routeId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
        
        /**
         * Отменить уведомление
         */
        fun cancelNotification(context: Context, routeId: Int) {
            WorkManager.getInstance(context).cancelUniqueWork("notification_$routeId")
        }
        
        /**
         * Отменить все уведомления
         */
        fun cancelAllNotifications(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
        }
    }
}

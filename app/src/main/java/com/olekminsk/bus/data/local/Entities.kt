package com.olekminsk.bus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * Entity для хранения маршрута в локальной БД (Room)
 */
@Entity(tableName = "routes")
@TypeConverters(Converters::class)
data class RouteEntity(
    @PrimaryKey
    val id: Int,
    val number: String,
    val name: String,
    val type: String, // urban, suburban, intercity
    val stopsJson: String, // JSON список остановок
    val weekdayTimesJson: String, // JSON список времени
    val weekendTimesJson: String,
    val notes: String? = null,
    val isActive: Boolean = true,
    val isFavorite: Boolean = false, // Для избранного
    val cachedAt: Long = System.currentTimeMillis() // Когда закэшировано
)

/**
 * Entity для избранных маршрутов (отдельная таблица для гибкости)
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val routeId: Int,
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * Entity для настроек уведомлений
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routeId: Int,
    val routeName: String,
    val departureTime: String,
    val notificationTime: Long, // Время когда показать уведомление
    val isActive: Boolean = true
)

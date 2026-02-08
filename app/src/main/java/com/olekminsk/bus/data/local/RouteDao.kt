package com.olekminsk.bus.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с маршрутами
 */
@Dao
interface RouteDao {
    
    @Query("SELECT * FROM routes ORDER BY number")
    fun getAllRoutes(): Flow<List<RouteEntity>>
    
    @Query("SELECT * FROM routes WHERE type = :type ORDER BY number")
    fun getRoutesByType(type: String): Flow<List<RouteEntity>>
    
    @Query("SELECT * FROM routes WHERE id = :id")
    suspend fun getRouteById(id: Int): RouteEntity?
    
    @Query("SELECT * FROM routes WHERE isFavorite = 1 ORDER BY number")
    fun getFavoriteRoutes(): Flow<List<RouteEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<RouteEntity>)
    
    @Update
    suspend fun updateRoute(route: RouteEntity)
    
    @Query("UPDATE routes SET isFavorite = :isFavorite WHERE id = :routeId")
    suspend fun updateFavoriteStatus(routeId: Int, isFavorite: Boolean)
    
    @Delete
    suspend fun deleteRoute(route: RouteEntity)
    
    @Query("DELETE FROM routes")
    suspend fun deleteAllRoutes()
    
    @Query("SELECT * FROM routes WHERE name LIKE '%' || :query || '%' OR number LIKE '%' || :query || '%'")
    fun searchRoutes(query: String): Flow<List<RouteEntity>>
}

/**
 * DAO для избранного
 */
@Dao
interface FavoriteDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(favorite: FavoriteEntity)
    
    @Delete
    suspend fun removeFromFavorites(favorite: FavoriteEntity)
    
    @Query("DELETE FROM favorites WHERE routeId = :routeId")
    suspend fun removeByRouteId(routeId: Int)
    
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE routeId = :routeId)")
    suspend fun isFavorite(routeId: Int): Boolean
}

/**
 * DAO для уведомлений
 */
@Dao
interface NotificationDao {
    
    @Insert
    suspend fun insertNotification(notification: NotificationEntity): Long
    
    @Update
    suspend fun updateNotification(notification: NotificationEntity)
    
    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)
    
    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("SELECT * FROM notifications WHERE isActive = 1 ORDER BY notificationTime ASC")
    fun getActiveNotifications(): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notifications WHERE notificationTime <= :currentTime AND isActive = 1")
    suspend fun getDueNotifications(currentTime: Long): List<NotificationEntity>
    
    @Query("UPDATE notifications SET isActive = 0 WHERE id = :id")
    suspend fun markAsShown(id: Long)
}

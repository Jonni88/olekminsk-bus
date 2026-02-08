package com.olekminsk.bus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room Database для приложения
 */
@Database(
    entities = [
        RouteEntity::class,
        FavoriteEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BusDatabase : RoomDatabase() {
    
    abstract fun routeDao(): RouteDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun notificationDao(): NotificationDao
    
    companion object {
        @Volatile
        private var INSTANCE: BusDatabase? = null
        
        fun getDatabase(context: Context): BusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BusDatabase::class.java,
                    "bus_schedule_database"
                )
                    .fallbackToDestructiveMigration() // При изменении схемы пересоздаёт БД
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.olekminsk.bus.data.repository

import com.olekminsk.bus.data.local.*
import com.olekminsk.bus.data.remote.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Repository для работы с расписанием
 * Объединяет локальное хранилище (Room) и удалённый API
 */
class BusScheduleRepository(
    private val routeDao: RouteDao,
    private val favoriteDao: FavoriteDao,
    private val api: BusScheduleApi
) {
    private val gson = Gson()
    
    /**
     * Получить все маршруты (сначала из кэша, потом из сети)
     */
    fun getAllRoutes(): Flow<Result<List<RouteEntity>>> = flow {
        // Сначала отдаём кэш
        routeDao.getAllRoutes().collect { cachedRoutes ->
            emit(Result.success(cachedRoutes))
            
            // Пытаемся обновить из сети
            try {
                val response = api.getAllRoutes()
                if (response.isSuccessful) {
                    response.body()?.let { routesDto ->
                        val entities = routesDto.map { it.toEntity() }
                        routeDao.insertRoutes(entities)
                    }
                }
            } catch (e: Exception) {
                // Сеть недоступна — используем кэш
                emit(Result.failure(e))
            }
        }
    }
    
    /**
     * Получить маршруты по типу
     */
    fun getRoutesByType(type: RouteType): Flow<List<RouteEntity>> {
        return routeDao.getRoutesByType(type.name.lowercase())
    }
    
    /**
     * Поиск маршрутов
     */
    fun searchRoutes(query: String): Flow<List<RouteEntity>> {
        return routeDao.searchRoutes("%$query%")
    }
    
    /**
     * Получить избранные маршруты
     */
    fun getFavoriteRoutes(): Flow<List<RouteEntity>> {
        return routeDao.getFavoriteRoutes()
    }
    
    /**
     * Добавить/удалить из избранного
     */
    suspend fun toggleFavorite(routeId: Int) {
        val isFavorite = favoriteDao.isFavorite(routeId)
        if (isFavorite) {
            favoriteDao.removeByRouteId(routeId)
            routeDao.updateFavoriteStatus(routeId, false)
        } else {
            favoriteDao.addToFavorites(FavoriteEntity(routeId))
            routeDao.updateFavoriteStatus(routeId, true)
        }
    }
    
    /**
     * Принудительное обновление из сети
     */
    suspend fun refreshRoutes(): Result<Unit> {
        return try {
            val response = api.getAllRoutes()
            if (response.isSuccessful) {
                response.body()?.let { routes ->
                    routeDao.insertRoutes(routes.map { it.toEntity() })
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Конвертер DTO -> Entity
     */
    private fun BusRouteDto.toEntity(): RouteEntity {
        return RouteEntity(
            id = id,
            number = number,
            name = name,
            type = type,
            stopsJson = gson.toJson(stops),
            weekdayTimesJson = gson.toJson(schedule.weekday),
            weekendTimesJson = gson.toJson(schedule.weekend),
            notes = schedule.notes,
            isActive = isActive,
            cachedAt = System.currentTimeMillis()
        )
    }
}

/**
 * Типы маршрутов
 */
enum class RouteType {
    URBAN,      // Городской
    SUBURBAN,   // Пригородный
    INTERCITY   // Междугородний
}

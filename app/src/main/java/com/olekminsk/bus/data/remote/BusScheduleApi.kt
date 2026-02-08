package com.olekminsk.bus.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API интерфейс для работы с Google Sheets
 * 
 * Для настройки:
 * 1. Создайте Google Sheets с расписанием
 * 2. Опубликуйте как веб-страницу (File → Share → Publish to web)
 * 3. Используйте JSON endpoint или создайте Apps Script
 */
interface BusScheduleApi {
    
    /**
     * Получить все маршруты
     * URL должен возвращать JSON с расписанием
     */
    @GET("api/routes")
    suspend fun getAllRoutes(): Response<List<BusRouteDto>>
    
    /**
     * Получить маршрут по ID
     */
    @GET("api/routes/{id}")
    suspend fun getRouteById(@retrofit2.http.Path("id") id: Int): Response<BusRouteDto>
    
    /**
     * Получить маршруты по типу (urban, suburban, intercity)
     */
    @GET("api/routes")
    suspend fun getRoutesByType(@Query("type") type: String): Response<List<BusRouteDto>>
    
    companion object {
        /**
         * Базовый URL для API
         * ЗАМЕНИТЕ на реальный URL вашего Google Sheets / сервера
         * 
         * Примеры:
         * - Собственный сервер: "https://your-server.com/"
         * - Google Apps Script: "https://script.google.com/macros/s/YOUR_SCRIPT_ID/exec/"
         */
        const val BASE_URL = "https://your-api-server.com/"
    }
}

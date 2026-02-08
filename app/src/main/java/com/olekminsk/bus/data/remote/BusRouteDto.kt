package com.olekminsk.bus.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Data class для маршрута из Google Sheets
 */
data class BusRouteDto(
    @SerializedName("id") val id: Int,
    @SerializedName("number") val number: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String, // urban, suburban, intercity
    @SerializedName("stops") val stops: List<StopDto>,
    @SerializedName("schedule") val schedule: ScheduleDto,
    @SerializedName("isActive") val isActive: Boolean = true
)

data class StopDto(
    @SerializedName("name") val name: String,
    @SerializedName("time") val time: String? = null // Время прибытия (для конечных)
)

data class ScheduleDto(
    @SerializedName("weekday") val weekday: List<String>, // Время отправления по будням
    @SerializedName("weekend") val weekend: List<String>, // Время отправления по выходным
    @SerializedName("notes") val notes: String? = null
)

/**
 * Ответ от Google Sheets API
 */
data class SheetsResponse(
    @SerializedName("routes") val routes: List<BusRouteDto>,
    @SerializedName("lastUpdated") val lastUpdated: String
)

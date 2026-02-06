package com.olekminsk.bus

data class BusRoute(
    val id: Int,
    val number: String,
    val name: String,
    val interval: String,
    val weekday: ScheduleDay,
    val weekend: ScheduleDay,
    val stops: List<String>
)

data class ScheduleDay(
    val start: String,
    val end: String,
    val times: List<String>
)

data class ScheduleData(
    val routes: List<BusRoute>
)

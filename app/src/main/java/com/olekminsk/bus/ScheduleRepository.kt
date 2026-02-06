package com.olekminsk.bus

import android.content.Context
import com.google.gson.Gson
import java.io.InputStreamReader

object ScheduleRepository {
    private var scheduleData: ScheduleData? = null
    
    fun loadSchedule(context: Context): ScheduleData {
        if (scheduleData == null) {
            val inputStream = context.assets.open("schedule.json")
            val reader = InputStreamReader(inputStream)
            scheduleData = Gson().fromJson(reader, ScheduleData::class.java)
            reader.close()
        }
        return scheduleData!!
    }
    
    fun getAllRoutes(context: Context): List<BusRoute> {
        return loadSchedule(context).routes
    }
    
    fun getRouteById(context: Context, id: Int): BusRoute? {
        return loadSchedule(context).routes.find { it.id == id }
    }
}

package com.olekminsk.bus.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton для работы с API
 */
object RetrofitClient {
    
    private const val BASE_URL = BusScheduleApi.BASE_URL
    
    val instance: BusScheduleApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        retrofit.create(BusScheduleApi::class.java)
    }
}

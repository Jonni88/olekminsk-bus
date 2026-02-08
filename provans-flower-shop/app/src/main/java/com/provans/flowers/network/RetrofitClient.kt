package com.provans.flowers.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // ЗАМЕНИТЕ НА ВАШ URL СЕРВЕРА
    private const val BASE_URL = "https://your-server.com/api/"
    
    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        retrofit.create(ApiService::class.java)
    }
}

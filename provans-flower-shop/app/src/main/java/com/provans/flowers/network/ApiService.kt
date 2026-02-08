package com.provans.flowers.network

import com.provans.flowers.model.Flower
import com.provans.flowers.model.Order
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Получить список цветов
    @GET("flowers")
    suspend fun getFlowers(): Response<List<Flower>>
    
    // Получить цветок по ID
    @GET("flowers/{id}")
    suspend fun getFlower(@Path("id") id: Int): Response<Flower>
    
    // Получить категории
    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryResponse>>
    
    // Отправить заказ на сервер
    @POST("orders")
    suspend fun createOrder(@Body orderRequest: OrderRequest): Response<OrderResponse>
    
    // Получить заказы пользователя
    @GET("orders")
    suspend fun getOrders(@Query("phone") phone: String): Response<List<Order>>
    
    // Авторизация
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>
    
    // Регистрация
    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<AuthResponse>
}

// DTO для сервера
data class CategoryResponse(
    val id: Int,
    val name: String,
    val icon: String
)

data class OrderRequest(
    val items: List<OrderItemRequest>,
    val totalPrice: Int,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String?,
    val deliveryDate: String,
    val deliveryTime: String,
    val comment: String?,
    val paymentMethod: String
)

data class OrderItemRequest(
    val flowerId: Int,
    val quantity: Int,
    val price: Int
)

data class OrderResponse(
    val id: String,
    val status: String,
    val message: String
)

data class LoginRequest(
    val phone: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val phone: String,
    val email: String?,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val token: String?,
    val user: UserResponse?,
    val message: String?
)

data class UserResponse(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String?,
    val isAdmin: Boolean
)

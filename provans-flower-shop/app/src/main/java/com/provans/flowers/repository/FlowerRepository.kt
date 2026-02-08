package com.provans.flowers.repository

import com.provans.flowers.model.Flower
import com.provans.flowers.model.Order
import com.provans.flowers.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FlowerRepository {
    
    private val api = RetrofitClient.instance
    
    // Получить список цветов с сервера
    suspend fun getFlowers(): Result<List<Flower>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getFlowers()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Ошибка загрузки: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Отправить заказ на сервер
    suspend fun createOrder(order: Order): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = OrderRequest(
                    items = order.items.map { 
                        OrderItemRequest(
                            flowerId = it.flower.id,
                            quantity = it.quantity,
                            price = it.flower.price
                        )
                    },
                    totalPrice = order.totalPrice,
                    customerName = order.customerName,
                    customerPhone = order.customerPhone,
                    deliveryAddress = order.deliveryAddress,
                    deliveryDate = order.deliveryDate,
                    deliveryTime = order.deliveryTime,
                    comment = order.comment,
                    paymentMethod = order.paymentMethod.name
                )
                
                val response = api.createOrder(request)
                if (response.isSuccessful) {
                    Result.success(response.body()?.id ?: "")
                } else {
                    Result.failure(Exception("Ошибка создания заказа: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Получить заказы пользователя
    suspend fun getOrders(phone: String): Result<List<Order>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getOrders(phone)
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Ошибка загрузки заказов: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Авторизация
    suspend fun login(phone: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.login(LoginRequest(phone, password))
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Пустой ответ"))
                } else {
                    Result.failure(Exception("Ошибка авторизации"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Регистрация
    suspend fun register(name: String, phone: String, email: String?, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.register(RegisterRequest(name, phone, email, password))
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Пустой ответ"))
                } else {
                    Result.failure(Exception("Ошибка регистрации"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

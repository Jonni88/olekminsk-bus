package com.provans.flowers.data

import com.provans.flowers.model.User

object UserManager {
    private var currentUser: User? = null
    private val users = mutableListOf(
        // Тестовый админ
        User(
            id = 1,
            name = "Администратор",
            phone = "+79001234567",
            email = "admin@provans.ru",
            isAdmin = true
        ),
        // Тестовый пользователь
        User(
            id = 2,
            name = "Иван Петров",
            phone = "+79009876543",
            email = "ivan@mail.ru",
            isAdmin = false
        )
    )
    
    private var listeners = mutableListOf<() -> Unit>()
    
    fun login(phone: String, password: String): User? {
        // Простая проверка: телефон = пароль для демо
        val user = users.find { it.phone == phone }
        if (user != null) {
            currentUser = user
            notifyListeners()
            return user
        }
        return null
    }
    
    fun register(name: String, phone: String, email: String?): Boolean {
        if (users.any { it.phone == phone }) {
            return false // Пользователь уже существует
        }
        
        val newUser = User(
            id = users.size + 1,
            name = name,
            phone = phone,
            email = email,
            isAdmin = false
        )
        users.add(newUser)
        currentUser = newUser
        notifyListeners()
        return true
    }
    
    fun logout() {
        currentUser = null
        notifyListeners()
    }
    
    fun getCurrentUser(): User? = currentUser
    
    fun isLoggedIn(): Boolean = currentUser != null
    
    fun isAdmin(): Boolean = currentUser?.isAdmin ?: false
    
    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }
    
    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }
    
    private fun notifyListeners() {
        listeners.forEach { it() }
    }
}

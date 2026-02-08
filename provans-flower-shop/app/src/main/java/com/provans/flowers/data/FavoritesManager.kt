package com.provans.flowers.data

object FavoritesManager {
    private val favorites = mutableSetOf<Int>() // ID цветов
    private var listeners = mutableListOf<() -> Unit>()
    
    fun addToFavorites(flowerId: Int) {
        favorites.add(flowerId)
        notifyListeners()
    }
    
    fun removeFromFavorites(flowerId: Int) {
        favorites.remove(flowerId)
        notifyListeners()
    }
    
    fun toggleFavorite(flowerId: Int) {
        if (isFavorite(flowerId)) {
            removeFromFavorites(flowerId)
        } else {
            addToFavorites(flowerId)
        }
    }
    
    fun isFavorite(flowerId: Int): Boolean = favorites.contains(flowerId)
    
    fun getFavorites(): Set<Int> = favorites.toSet()
    
    fun clear() {
        favorites.clear()
        notifyListeners()
    }
    
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

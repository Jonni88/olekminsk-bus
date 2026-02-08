package com.olekminsk.bus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.olekminsk.bus.data.local.RouteEntity
import com.olekminsk.bus.data.repository.BusScheduleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel для главного экрана
 */
class BusViewModel(
    application: Application,
    private val repository: BusScheduleRepository
) : AndroidViewModel(application) {
    
    // Состояния
    private val _routes = MutableStateFlow<List<RouteEntity>>(emptyList())
    val routes: StateFlow<List<RouteEntity>> = _routes.asStateFlow()
    
    private val _favoriteRoutes = MutableStateFlow<List<RouteEntity>>(emptyList())
    val favoriteRoutes: StateFlow<List<RouteEntity>> = _favoriteRoutes.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<RouteEntity>>(emptyList())
    val searchResults: StateFlow<List<RouteEntity>> = _searchResults.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Недавние поиски
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()
    
    init {
        loadRoutes()
        loadFavorites()
    }
    
    private fun loadRoutes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllRoutes()
                    .catch { e ->
                        _error.value = e.message
                        _isLoading.value = false
                    }
                    .collect { result ->
                        result.onSuccess { routes ->
                            _routes.value = routes
                            _isLoading.value = false
                        }.onFailure { error ->
                            _error.value = error.message
                            _isLoading.value = false
                        }
                    }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                repository.getFavoriteRoutes()
                    .collect { favorites ->
                        _favoriteRoutes.value = favorites
                    }
            } catch (e: Exception) {
                // Игнорируем ошибку избранного
            }
        }
    }
    
    fun toggleFavorite(routeId: Int) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(routeId)
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            }
        }
    }
    
    fun searchRoutes(query: String) {
        viewModelScope.launch {
            try {
                repository.searchRoutes(query)
                    .collect { results ->
                        _searchResults.value = results
                    }
            } catch (e: Exception) {
                _error.value = "Ошибка поиска: ${e.message}"
            }
        }
    }
    
    fun addToRecentSearches(query: String) {
        if (query.isBlank()) return
        val current = _recentSearches.value.toMutableList()
        current.remove(query)
        current.add(0, query)
        _recentSearches.value = current.take(10)
    }
    
    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }
    
    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.refreshRoutes()
                    .onSuccess {
                        _isLoading.value = false
                    }
                    .onFailure { error ->
                        _error.value = error.message
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = "Ошибка обновления: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

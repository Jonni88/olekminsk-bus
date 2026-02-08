package com.olekminsk.bus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.olekminsk.bus.data.local.BusDatabase
import com.olekminsk.bus.data.remote.RetrofitClient
import com.olekminsk.bus.data.repository.BusScheduleRepository
import com.olekminsk.bus.viewmodel.BusViewModel

/**
 * Factory для создания ViewModel с зависимостями
 */
class BusViewModelFactory(
    private val application: BusApplication
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusViewModel::class.java)) {
            val database = application.database
            val repository = BusScheduleRepository(
                routeDao = database.routeDao(),
                favoriteDao = database.favoriteDao(),
                api = RetrofitClient.instance
            )
            return BusViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.olekminsk.bus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.olekminsk.bus.ui.screens.*
import com.olekminsk.bus.ui.theme.BusTheme
import com.olekminsk.bus.viewmodel.BusViewModel

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val factory = BusViewModelFactory(BusApplication.instance)
        
        setContent {
            BusTheme {
                val viewModel: BusViewModel = viewModel(factory = factory)
                BusApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusApp(viewModel: BusViewModel) {
    var selectedScreen by remember { mutableStateOf(Screen.Home) }
    var selectedRoute by remember { mutableStateOf<com.olekminsk.bus.data.local.RouteEntity?>(null) }
    var showSearch by remember { mutableStateOf(false) }
    
    if (selectedRoute != null) {
        RouteDetailScreen(
            route = selectedRoute!!,
            isFavorite = selectedRoute!!.isFavorite,
            onFavoriteClick = { viewModel.toggleFavorite(selectedRoute!!.id) },
            onBackClick = { selectedRoute = null }
        )
    } else if (showSearch) {
        SearchScreen(
            viewModel = viewModel,
            onRouteClick = { route ->
                selectedRoute = route
                showSearch = false
            },
            onBackClick = { showSearch = false }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Маршруты") },
                        selected = selectedScreen == Screen.Home,
                        onClick = { selectedScreen = Screen.Home }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.LocationOn, null) },
                        label = { Text("Пригород") },
                        selected = selectedScreen == Screen.Suburban,
                        onClick = { selectedScreen = Screen.Suburban }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, null) },
                        label = { Text("Избранное") },
                        selected = selectedScreen == Screen.Favorites,
                        onClick = { selectedScreen = Screen.Favorites }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = { Text("Настройки") },
                        selected = selectedScreen == Screen.Settings,
                        onClick = { selectedScreen = Screen.Settings }
                    )
                }
            }
        ) { padding ->
            when (selectedScreen) {
                Screen.Home -> HomeScreen(
                    viewModel = viewModel,
                    onRouteClick = { selectedRoute = it },
                    onSearchClick = { showSearch = true },
                    modifier = Modifier.padding(padding)
                )
                Screen.Suburban -> SuburbanScreen(
                    viewModel = viewModel,
                    onRouteClick = { selectedRoute = it },
                    modifier = Modifier.padding(padding)
                )
                Screen.Favorites -> FavoritesScreen(
                    viewModel = viewModel,
                    onRouteClick = { selectedRoute = it },
                    modifier = Modifier.padding(padding)
                )
                Screen.Settings -> SettingsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

enum class Screen {
    Home, Suburban, Favorites, Settings
}

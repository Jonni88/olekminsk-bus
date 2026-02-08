package com.olekminsk.bus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olekminsk.bus.data.local.RouteEntity
import com.olekminsk.bus.viewmodel.BusViewModel

/**
 * Экран пригородных маршрутов
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuburbanScreen(
    viewModel: BusViewModel,
    onRouteClick: (RouteEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val routes by viewModel.routes.collectAsState()
    val suburbanRoutes = routes.filter { it.type == "suburban" }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Пригородные маршруты") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (suburbanRoutes.isEmpty()) {
                // Пустое состояние
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Нет пригородных маршрутов",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Проверьте подключение к интернету\nили обновите расписание",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suburbanRoutes) { route ->
                        RouteCard(
                            route = route,
                            isFavorite = route.isFavorite,
                            onClick = { onRouteClick(route) },
                            onFavoriteClick = { viewModel.toggleFavorite(route.id) }
                        )
                    }
                }
            }
        }
    }
}

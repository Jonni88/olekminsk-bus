package com.olekminsk.bus.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olekminsk.bus.R
import com.olekminsk.bus.data.local.RouteEntity
import com.olekminsk.bus.viewmodel.BusViewModel

/**
 * Экран списка всех маршрутов с вкладками
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BusViewModel,
    onRouteClick: (RouteEntity) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val routes by viewModel.routes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Все", "Городские", "Пригород", "Межгород")
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.header_image),
                        contentDescription = "Автобусы Олёкминска",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, "Поиск")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Вкладки типов маршрутов
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, maxLines = 1) }
                    )
                }
            }
            
            // Контент
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading && routes.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    val filteredRoutes = when (selectedTab) {
                        1 -> routes.filter { it.type == "urban" }
                        2 -> routes.filter { it.type == "suburban" }
                        3 -> routes.filter { it.type == "intercity" }
                        else -> routes
                    }
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredRoutes) { route ->
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
}

/**
 * Карточка маршрута с кнопкой избранного
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteCard(
    route: RouteEntity,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val typeLabel = when (route.type) {
        "urban" -> "Городской"
        "suburban" -> "Пригород"
        "intercity" -> "Междугородний"
        else -> route.type
    }
    
    val typeColor = when (route.type) {
        "urban" -> MaterialTheme.colorScheme.primary
        "suburban" -> MaterialTheme.colorScheme.secondary
        "intercity" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Номер маршрута
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = typeColor.copy(alpha = 0.2f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = route.number,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Информация
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = typeLabel,
                    fontSize = 12.sp,
                    color = typeColor,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Кнопка избранного
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "В избранном" else "Добавить в избранное",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Экран избранных маршрутов
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: BusViewModel,
    onRouteClick: (RouteEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favoriteRoutes.collectAsState()
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Избранное") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (favorites.isEmpty()) {
                // Пустое состояние
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Нет избранных маршрутов",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Нажмите ♡ на маршруте,\nчтобы добавить сюда",
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
                    items(favorites) { route ->
                        RouteCard(
                            route = route,
                            isFavorite = true,
                            onClick = { onRouteClick(route) },
                            onFavoriteClick = { viewModel.toggleFavorite(route.id) }
                        )
                    }
                }
            }
        }
    }
}

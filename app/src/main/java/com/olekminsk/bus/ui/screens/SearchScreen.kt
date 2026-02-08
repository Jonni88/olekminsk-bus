package com.olekminsk.bus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.olekminsk.bus.data.local.RouteEntity
import com.olekminsk.bus.viewmodel.BusViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

/**
 * Экран поиска маршрутов
 */
@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun SearchScreen(
    viewModel: BusViewModel,
    onRouteClick: (RouteEntity) -> Unit,
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val focusManager = LocalFocusManager.current
    
    // Debounce для поиска
    LaunchedEffect(searchQuery) {
        kotlinx.coroutines.delay(300) // 300ms задержка
        if (searchQuery.isNotBlank()) {
            viewModel.searchRoutes(searchQuery)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Поиск маршрута или остановки...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Search, null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, "Очистить")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                                viewModel.searchRoutes(searchQuery)
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // Показываем результаты поиска
                searchQuery.isNotBlank() -> {
                    if (searchResults.isEmpty()) {
                        EmptySearchResult(query = searchQuery)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults) { route ->
                                RouteCard(
                                    route = route,
                                    isFavorite = route.isFavorite,
                                    onClick = {
                                        viewModel.addToRecentSearches(searchQuery)
                                        onRouteClick(route)
                                    },
                                    onFavoriteClick = { viewModel.toggleFavorite(route.id) }
                                )
                            }
                        }
                    }
                }
                // Показываем недавние поиски
                recentSearches.isNotEmpty() -> {
                    RecentSearches(
                        searches = recentSearches,
                        onSearchClick = { query ->
                            searchQuery = query
                            viewModel.searchRoutes(query)
                        },
                        onClearClick = { viewModel.clearRecentSearches() }
                    )
                }
                // Пустое состояние
                else -> {
                    EmptySearchState()
                }
            }
        }
    }
}

@Composable
fun EmptySearchResult(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "По запросу \"$query\"\nничего не найдено",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Попробуйте другие слова:\nномер маршрута, название остановки",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun RecentSearches(
    searches: List<String>,
    onSearchClick: (String) -> Unit,
    onClearClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Недавние поиски",
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = onClearClick) {
                Text("Очистить")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        searches.forEach { search ->
            ListItem(
                headlineContent = { Text(search) },
                leadingContent = {
                    Icon(Icons.Default.List, null)
                },
                modifier = Modifier.clickable { onSearchClick(search) }
            )
        }
    }
}

@Composable
fun EmptySearchState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Введите номер маршрута\nили название остановки",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

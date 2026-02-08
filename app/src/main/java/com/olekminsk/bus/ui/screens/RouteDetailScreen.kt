package com.olekminsk.bus.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olekminsk.bus.data.local.RouteEntity
import com.olekminsk.bus.worker.BusNotificationWorker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * Экран деталей маршрута
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailScreen(
    route: RouteEntity,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val gson = Gson()
    
    // Парсим JSON
    val stops: List<StopData> = try {
        val type = object : TypeToken<List<StopData>>() {}.type
        gson.fromJson(route.stopsJson, type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    
    val weekdayTimes: List<String> = try {
        val type = object : TypeToken<List<String>>() {}.type
        gson.fromJson(route.weekdayTimesJson, type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    
    val weekendTimes: List<String> = try {
        val type = object : TypeToken<List<String>>() {}.type
        gson.fromJson(route.weekendTimesJson, type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    
    // Определяем сегодня выходной или нет
    val calendar = Calendar.getInstance()
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
    
    var isWeekendSelected by remember { mutableStateOf(isWeekend) }
    val schedule = if (isWeekendSelected) weekendTimes else weekdayTimes
    
    // Для диалога уведомлений
    var showNotificationDialog by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Маршрут ${route.number}") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "В избранном" else "Добавить в избранное",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else LocalContentColor.current
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // Открыть карту с маршрутом
                    val uri = Uri.parse("geo:0,0?q=Олёкминск+${route.name.replace(" ", "+")}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("com.google.android.apps.maps")
                    context.startActivity(intent)
                },
                icon = { Icon(Icons.Default.LocationOn, null) },
                text = { Text("На карте") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Переключатель будни/выходные
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = !isWeekendSelected,
                        onClick = { isWeekendSelected = false },
                        label = { Text("Будни") },
                        leadingIcon = if (!isWeekendSelected) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = isWeekendSelected,
                        onClick = { isWeekendSelected = true },
                        label = { Text("Выходные") },
                        leadingIcon = if (isWeekendSelected) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Название маршрута
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                if (!route.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = route.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Остановки
                Text(
                    text = "Остановки",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Список остановок
            items(stops) { stop ->
                StopItem(stop = stop)
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Время отправления
                Text(
                    text = "Время отправления",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Таблица расписания (как на фото)
            item {
                ScheduleTable(
                    times = schedule,
                    onTimeClick = { time ->
                        selectedTime = time
                        showNotificationDialog = true
                    }
                )
            }
            
            // Ближайший автобус
            item {
                Spacer(modifier = Modifier.height(24.dp))
                
                val nextBus = findNextBus(schedule)
                if (nextBus != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Ближайший рейс",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = nextBus,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    selectedTime = nextBus
                                    showNotificationDialog = true
                                }
                            ) {
                                Icon(Icons.Default.Notifications, null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Напомнить")
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "На сегодня автобусов больше нет",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(80.dp)) // Для FAB
            }
        }
    }
    
    // Диалог уведомления
    if (showNotificationDialog && selectedTime != null) {
        NotificationDialog(
            routeName = route.name,
            departureTime = selectedTime!!,
            onDismiss = { showNotificationDialog = false },
            onConfirm = { minutesBefore ->
                BusNotificationWorker.scheduleNotification(
                    context = context,
                    routeId = route.id,
                    routeName = route.name,
                    departureTime = selectedTime!!,
                    minutesBefore = minutesBefore
                )
                showNotificationDialog = false
            }
        )
    }
}

@Composable
fun StopItem(stop: StopData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Точка остановки
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(8.dp)
        ) {}
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stop.name,
                style = MaterialTheme.typography.bodyLarge
            )
            if (!stop.time.isNullOrBlank()) {
                Text(
                    text = "Прибытие: ${stop.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDialog(
    routeName: String,
    departureTime: String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedMinutes by remember { mutableStateOf(15) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Напомнить об отправлении") },
        text = {
            Column {
                Text("Маршрут: $routeName")
                Text("Отправление: $departureTime")
                Spacer(modifier = Modifier.height(16.dp))
                Text("За сколько минут напомнить?")
                Spacer(modifier = Modifier.height(8.dp))
                
                // Выбор времени
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(5, 10, 15, 30).forEach { minutes ->
                        FilterChip(
                            selected = selectedMinutes == minutes,
                            onClick = { selectedMinutes = minutes },
                            label = { Text("$minutes мин") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedMinutes) }) {
                Text("Установить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

// Вспомогательные функции
data class StopData(
    val name: String,
    val time: String? = null
)

/**
 * Таблица расписания в стиле фото (сетка 5 колонок)
 */
@Composable
fun ScheduleTable(
    times: List<String>,
    onTimeClick: (String) -> Unit
) {
    val nextBus = findNextBus(times)
    var index = 0
    
    // Группируем по 5 колонок как на фото
    Column(modifier = Modifier.fillMaxWidth()) {
        while (index < times.size) {
            val rowTimes = times.drop(index).take(5)
            index += 5
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                rowTimes.forEach { time ->
                    val isPast = isTimePast(time)
                    val isNext = time == nextBus
                    
                    // Разбиваем время на часы и минуты
                    val timeParts = time.split(":")
                    val hour = timeParts.getOrNull(0) ?: ""
                    val minute = timeParts.getOrNull(1) ?: ""
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .aspectRatio(1.5f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    isNext -> MaterialTheme.colorScheme.primary
                                    isPast -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                            .clickable { onTimeClick(time) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = hour,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isNext -> MaterialTheme.colorScheme.onPrimary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = minute,
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    isNext -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
                
                // Заполняем пустые ячейки если ряд неполный
                val emptyCells = 5 - rowTimes.size
                repeat(emptyCells) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .aspectRatio(1.5f)
                    )
                }
            }
        }
    }
}

fun isTimePast(timeStr: String): Boolean {
    return try {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = sdf.parse(timeStr)
        val now = Calendar.getInstance()
        val nowTime = sdf.parse("${now.get(Calendar.HOUR_OF_DAY)}:${now.get(Calendar.MINUTE)}")
        time != null && nowTime != null && time.before(nowTime)
    } catch (e: Exception) {
        false
    }
}

fun findNextBus(times: List<String>): String? {
    return times.firstOrNull { !isTimePast(it) }
}

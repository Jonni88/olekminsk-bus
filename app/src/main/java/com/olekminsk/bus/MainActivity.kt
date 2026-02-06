package com.olekminsk.bus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BusApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusApp() {
    var selectedRoute by remember { mutableStateOf<BusRoute?>(null) }
    
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            if (selectedRoute == null) "Автобусы Олёкминска" 
                            else "Маршрут ${selectedRoute!!.number}"
                        ) 
                    },
                    navigationIcon = {
                        if (selectedRoute != null) {
                            IconButton(onClick = { selectedRoute = null }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                if (selectedRoute == null) {
                    RouteListScreen { route ->
                        selectedRoute = route
                    }
                } else {
                    RouteDetailScreen(route = selectedRoute!!)
                }
            }
        }
    }
}

@Composable
fun RouteListScreen(onRouteClick: (BusRoute) -> Unit) {
    val context = LocalContext.current
    val routes = remember { ScheduleRepository.getAllRoutes(context) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(routes) { route ->
            RouteCard(route = route, onClick = { onRouteClick(route) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteCard(route: BusRoute, onClick: () -> Unit) {
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
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = route.number,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Интервал: ${route.interval}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun RouteDetailScreen(route: BusRoute) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
    
    var isWeekendSelected by remember { mutableStateOf(isWeekend) }
    val schedule = if (isWeekendSelected) route.weekend else route.weekday
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            // Переключатель будни/выходные
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = !isWeekendSelected,
                        onClick = { isWeekendSelected = false },
                        shape = SegmentedButtonDefaults.itemShape(0, 2)
                    ) {
                        Text("Будни")
                    }
                    SegmentedButton(
                        selected = isWeekendSelected,
                        onClick = { isWeekendSelected = true },
                        shape = SegmentedButtonDefaults.itemShape(1, 2)
                    ) {
                        Text("Выходные")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Информация о маршруте
            Text(
                text = route.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Интервал движения: ${route.interval}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Работает: ${schedule.start} — ${schedule.end}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Остановки
            Text(
                text = "Остановки:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            route.stops.forEachIndexed { index, stop ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(24.dp)
                    )
                    Text(
                        text = stop,
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Время отправления
            Text(
                text = "Время отправления:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Список времени
        items(schedule.times.chunked(4)) { rowTimes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowTimes.forEach { time ->
                    val isPast = isTimePast(time)
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (isPast) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else 
                            MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = time,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontSize = 16.sp,
                            fontWeight = if (isPast) FontWeight.Normal else FontWeight.Medium,
                            color = if (isPast) 
                                MaterialTheme.colorScheme.onSurfaceVariant 
                            else 
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Ближайший автобус
            val nextBus = findNextBus(schedule.times)
            if (nextBus != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ближайший автобус",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = nextBus,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
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
            
            Spacer(modifier = Modifier.height(32.dp))
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
    return try {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val now = Calendar.getInstance()
        val nowTime = sdf.parse("${now.get(Calendar.HOUR_OF_DAY)}:${now.get(Calendar.MINUTE)}")
        
        times.firstOrNull { timeStr ->
            val time = sdf.parse(timeStr)
            time != null && nowTime != null && !time.before(nowTime)
        }
    } catch (e: Exception) {
        null
    }
}

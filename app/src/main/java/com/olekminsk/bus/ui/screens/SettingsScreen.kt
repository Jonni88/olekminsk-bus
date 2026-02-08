package com.olekminsk.bus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.olekminsk.bus.viewmodel.BusViewModel
import com.olekminsk.bus.worker.BusNotificationWorker

/**
 * Экран настроек
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BusViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Обновление данных
            ListItem(
                headlineContent = { Text("Обновить расписание") },
                supportingContent = { Text("Загрузить актуальные данные с сервера") },
                leadingContent = {
                    Icon(Icons.Default.Refresh, null)
                },
                modifier = Modifier.clickable { viewModel.refresh() }
            )
            
            Divider()
            
            // Уведомления
            ListItem(
                headlineContent = { Text("Уведомления") },
                supportingContent = { Text("Отменить все запланированные") },
                leadingContent = {
                    Icon(Icons.Default.Notifications, null)
                },
                modifier = Modifier.clickable {
                    BusNotificationWorker.cancelAllNotifications(context)
                }
            )
            
            Divider()
            
            // О приложении
            ListItem(
                headlineContent = { Text("О приложении") },
                supportingContent = { Text("Версия 2.0\nРасписание автобусов Олёкминска") },
                leadingContent = {
                    Icon(Icons.Default.Info, null)
                }
            )
            
            Divider()
            
            // Как пользоваться
            ListItem(
                headlineContent = { Text("Помощь") },
                supportingContent = { Text("Как пользоваться приложением") },
                leadingContent = {
                    Icon(Icons.Default.Info, null)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопка обновить
            Button(
                onClick = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Обновить расписание")
            }
        }
    }
}

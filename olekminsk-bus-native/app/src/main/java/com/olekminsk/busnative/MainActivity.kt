package com.olekminsk.busnative

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var currentRouteIndex = 0
    private lateinit var contentContainer: LinearLayout
    private lateinit var tabContainer: LinearLayout
    
    // Цвета маршрутов
    private val routeColors = listOf(
        0xFF2196F3.toInt(), // Синий
        0xFF4CAF50.toInt(), // Зелёный
        0xFFFF9800.toInt(), // Оранжевый
        0xFF9C27B0.toInt(), // Фиолетовый
        0xFFF44336.toInt()  // Красный
    )
    
    // Данные маршрутов с остановками
    private val routes = listOf(
        Route("1", "Автовокзал — Дача", listOf(
            Stop("Автовокзал", listOf("07:00", "07:42", "08:32", "09:05", "10:07", "11:18", "12:47", "13:52", "14:38", "15:19", "16:28", "17:11", "17:55", "18:51")),
            Stop("Центр", listOf("07:15", "07:57", "08:47", "09:20", "10:22", "11:33", "13:02", "14:07", "14:53", "15:34", "16:43", "17:26", "18:10", "19:06")),
            Stop("Площадь", listOf("07:30", "08:12", "09:02", "09:35", "10:37", "11:48", "13:17", "14:22", "15:08", "15:49", "16:58", "17:41", "18:25", "19:21")),
            Stop("Дача", listOf("07:45", "08:27", "09:17", "09:50", "10:52", "12:03", "13:32", "14:37", "15:23", "16:04", "17:13", "17:56", "18:40", "19:36"))
        )),
        Route("2", "Автовокзал — Авиапорт", listOf(
            Stop("Автовокзал", listOf("07:00", "07:40", "08:20", "09:30", "11:00", "12:20", "13:00", "13:40", "14:40", "16:40", "17:20", "18:00", "18:40", "19:00", "20:10")),
            Stop("Рынок", listOf("07:10", "07:50", "08:30", "09:40", "11:10", "12:30", "13:10", "13:50", "14:50", "16:50", "17:30", "18:10", "18:50", "19:10", "20:20")),
            Stop("Авиапорт", listOf("07:20", "08:00", "08:40", "09:50", "11:20", "12:40", "13:20", "14:00", "14:40", "17:00", "17:40", "18:20", "19:00", "20:30"))
        )),
        Route("5", "Автовокзал — ПНДИ", listOf(
            Stop("Автовокзал", listOf("07:25", "08:15", "09:05", "09:42", "11:22", "12:30", "13:50", "15:10", "16:30", "17:50")),
            Stop("Больница", listOf("07:35", "08:25", "09:15", "09:52", "11:32", "12:40", "14:00", "15:20", "16:40", "18:00")),
            Stop("ПНДИ", listOf("07:45", "08:35", "09:25", "10:02", "11:42", "12:50", "14:10", "15:30", "16:50", "18:10"))
        )),
        Route("6", "Автовокзал — Новостройки", listOf(
            Stop("Автовокзал", listOf("07:20", "08:15", "12:30", "13:30", "15:00", "16:30", "18:00")),
            Stop("Школа", listOf("07:35", "08:30", "12:45", "13:45", "15:15", "16:45", "18:15")),
            Stop("Новостройки", listOf("07:50", "08:45", "13:00", "14:00", "15:30", "17:00", "18:30"))
        )),
        Route("7", "Автовокзал — Нефтебаза", listOf(
            Stop("Автовокзал", listOf("07:05", "08:55", "12:30", "14:30", "16:20", "17:55")),
            Stop("Завод", listOf("07:25", "09:15", "12:50", "14:50", "16:40", "18:15")),
            Stop("Нефтебаза", listOf("07:45", "09:30", "13:00", "15:10", "17:00", "18:35"))
        ))
    )

    data class Stop(val name: String, val times: List<String>)
    data class Route(val number: String, val name: String, val stops: List<Stop>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF5F5F5.toInt())
        }
        
        // Шапка
        root.addView(createHeader())
        
        // Табы (простые кнопки)
        tabContainer = createTabs()
        root.addView(tabContainer)
        
        // Контент
        contentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        root.addView(contentContainer)
        
        // Нижняя навигация
        root.addView(createBottomNav())
        
        setContentView(root)
        
        // Запускаем обновление времени
        startTimeUpdates()
        
        // Показываем первый маршрут
        showRoute(0)
    }
    
    private var timeTextView: TextView? = null
    
    private fun createHeader(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF1976D2.toInt())
            setPadding(32, 64, 32, 24)
            
            addView(TextView(this@MainActivity).apply {
                text = "🚌 Автобусы Олёкминска"
                textSize = 26f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            })
            
            timeTextView = TextView(this@MainActivity).apply {
                text = getCurrentTime()
                textSize = 16f
                setTextColor(0xB3FFFFFF.toInt())
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 0)
            }
            addView(timeTextView)
        }
    }
    
    private fun startTimeUpdates() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                timeTextView?.text = getCurrentTime()
                handler.postDelayed(this, 60000)
            }
        }, 60000)
    }
    
    private fun createTabs(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(8, 16, 8, 16)
            
            routes.forEachIndexed { index, route ->
                val btn = TextView(this@MainActivity).apply {
                    text = "М${route.number}"
                    textSize = 16f
                    setPadding(20, 12, 20, 12)
                    gravity = Gravity.CENTER
                    
                    background = GradientDrawable().apply {
                        cornerRadius = 24f
                        setColor(if (index == 0) routeColors[index] else 0xFFEEEEEE.toInt())
                    }
                    
                    setTextColor(if (index == 0) Color.WHITE else 0xFF333333.toInt())
                    
                    setOnClickListener {
                        showRoute(index)
                        updateTabs(index)
                    }
                }
                
                addView(btn, LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(4, 0, 4, 0)
                })
            }
        }
    }
    
    private fun updateTabs(selectedIndex: Int) {
        for (i in 0 until tabContainer.childCount) {
            val btn = tabContainer.getChildAt(i) as TextView
            btn.background = GradientDrawable().apply {
                cornerRadius = 24f
                setColor(if (i == selectedIndex) routeColors[i] else 0xFFEEEEEE.toInt())
            }
            btn.setTextColor(if (i == selectedIndex) Color.WHITE else 0xFF333333.toInt())
        }
    }
    
    private fun createBottomNav(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(0, 16, 0, 16)
            elevation = 8f
            
            val items = listOf(
                "🚌" to "Маршруты",
                "⭐" to "Избранное", 
                "⚙️" to "Ещё"
            )
            
            items.forEach { (icon, label) ->
                addView(TextView(this@MainActivity).apply {
                    text = "$icon\n$label"
                    textSize = 12f
                    gravity = Gravity.CENTER
                    setPadding(0, 8, 0, 8)
                    setTextColor(0xFF666666.toInt())
                }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            }
        }
    }
    
    private fun showRoute(index: Int) {
        currentRouteIndex = index
        val route = routes[index]
        val color = routeColors[index]
        
        contentContainer.removeAllViews()
        
        val scroll = ScrollView(this).apply {
            setBackgroundColor(0xFFF5F5F5.toInt())
        }
        
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 32)
        }
        
        // Карточка маршрута
        content.addView(createRouteHeader(route, color))
        
        // Остановки
        route.stops.forEachIndexed { i, stop ->
            content.addView(createStopView(stop, color, i == 0 || i == route.stops.lastIndex))
        }
        
        scroll.addView(content)
        contentContainer.addView(scroll)
    }
    
    private fun createRouteHeader(route: Route, color: Int): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(24, 24, 24, 24)
            
            background = GradientDrawable().apply {
                cornerRadius = 20f
                setColor(Color.WHITE)
            }
            
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            
            // Номер в кружке
            val header = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            
            header.addView(TextView(this@MainActivity).apply {
                text = route.number
                textSize = 22f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(20, 12, 20, 12)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                }
            })
            
            header.addView(TextView(this@MainActivity).apply {
                text = route.name
                textSize = 18f
                setTextColor(0xFF333333.toInt())
                setPadding(16, 0, 0, 0)
            })
            
            addView(header)
            
            // Ближайшее время
            val nextTime = findNextTime(route.stops[0].times)
            if (nextTime != null) {
                addView(TextView(this@MainActivity).apply {
                    text = "⏰ Ближайший: $nextTime"
                    textSize = 16f
                    setTextColor(color)
                    setPadding(0, 16, 0, 0)
                })
            }
        }
    }
    
    private fun createStopView(stop: Stop, color: Int, isTerminal: Boolean): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(20, 16, 20, 16)
            
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8)
            }
            
            // Индикатор и название
            val header = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            
            header.addView(View(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(14, 14)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(if (isTerminal) color else 0xFFCCCCCC.toInt())
                }
            })
            
            header.addView(TextView(this@MainActivity).apply {
                text = stop.name + if (isTerminal) " ⭐" else ""
                textSize = 16f
                setTextColor(0xFF333333.toInt())
                setPadding(12, 0, 0, 0)
            })
            
            addView(header)
            
            // Времена
            val timesRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(26, 8, 0, 0)
            }
            
            val nextIndex = findNextTimeIndex(stop.times)
            
            stop.times.forEachIndexed { i, time ->
                val isNext = i == nextIndex
                val isPast = i < nextIndex
                
                timesRow.addView(TextView(this@MainActivity).apply {
                    text = if (isNext) "【$time】" else "$time "
                    textSize = if (isNext) 15f else 13f
                    setTextColor(when {
                        isNext -> color
                        isPast -> 0xFFAAAAAA.toInt()
                        else -> 0xFF666666.toInt()
                    })
                    setPadding(3, 3, 3, 3)
                    
                    if (isNext) {
                        setBackgroundColor(0xFFE8F5E9.toInt())
                    }
                })
            }
            
            addView(timesRow)
        }
    }
    
    private fun getCurrentTime(): String {
        val now = Calendar.getInstance()
        return String.format("%02d:%02d", now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))
    }
    
    private fun findNextTime(times: List<String>): String? {
        val index = findNextTimeIndex(times)
        return if (index != -1) times[index] else null
    }
    
    private fun findNextTimeIndex(times: List<String>): Int {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        
        times.forEachIndexed { index, timeStr ->
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull() ?: 0
                val minute = parts[1].toIntOrNull() ?: 0
                val busMinutes = hour * 60 + minute
                
                if (busMinutes > currentMinutes) {
                    return index
                }
            }
        }
        return -1
    }
}

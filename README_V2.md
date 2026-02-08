# Olekminsk Bus App v2.0

Приложение для просмотра расписания автобусов в Олёкминске.

## 🚀 Новые функции v2.0

### ✅ Добавлено:
- **Google Sheets интеграция** — загрузка актуального расписания с сервера
- **Офлайн-режим** — работа без интернета (кэш в Room Database)
- **Избранное** — сохраняйте часто используемые маршруты
- **Поиск** — быстрый поиск по номеру маршрута или остановке
- **Push-уведомления** — напоминания перед отправлением автобуса
- **Bottom Navigation** — удобное переключение между разделами
- **Вкладки типов** — фильтрация: Все / Городские / Пригород / Междугородние
- **Карта** — открытие маршрута в Google Maps (кнопка FAB)
- **Material You дизайн** — современный UI с адаптивными цветами

## 📁 Структура проекта

```
app/src/main/java/com/olekminsk/bus/
├── data/
│   ├── local/           # Room Database (кэш, избранное)
│   │   ├── BusDatabase.kt
│   │   ├── Entities.kt
│   │   ├── RouteDao.kt
│   │   └── Converters.kt
│   ├── remote/          # API, Retrofit
│   │   ├── BusScheduleApi.kt
│   │   ├── BusRouteDto.kt
│   │   └── RetrofitClient.kt
│   └── repository/      # Repository pattern
│       └── BusScheduleRepository.kt
├── ui/
│   ├── screens/         # Экраны приложения
│   │   ├── HomeScreen.kt
│   │   ├── SearchScreen.kt
│   │   ├── RouteDetailScreen.kt
│   │   ├── FavoritesScreen.kt
│   │   └── SettingsScreen.kt
│   └── theme/           # Тема приложения
│       ├── Theme.kt
│       └── Type.kt
├── viewmodel/           # ViewModel
│   └── BusViewModel.kt
├── worker/              # WorkManager
│   └── BusNotificationWorker.kt
└── MainActivity.kt
```

## 📱 Экраны

1. **Главная** — список маршрутов с вкладками
2. **Избранное** — сохранённые маршруты
3. **Поиск** — поиск по названию/номеру
4. **Детали маршрута** — остановки, время, уведомления
5. **Настройки** — обновление данных, уведомления

## 🔧 Настройка Google Sheets

См. файл `GOOGLE_SHEETS_SETUP.md` — подробная инструкция по подключению таблицы.

## 📋 Зависимости

- **Compose** — UI фреймворк
- **Room** — локальная БД
- **Retrofit** — сетевые запросы
- **WorkManager** — фоновые задачи
- **Coroutines** — асинхронность

## 🚀 Сборка

```bash
./gradlew assembleDebug
```

APK будет в `app/build/outputs/apk/debug/`

## ⚠️ Важно

1. **Перед использованием** настройте `BASE_URL` в `BusScheduleApi.kt`
2. **Токен бота** (если используете Telegram) храните в переменных окружения
3. **Google Maps** — требуется API ключ для полной функциональности карты

## 📝 Лицензия

Свободное использование.

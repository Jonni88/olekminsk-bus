# Push Notifications — Полная инструкция

## Что реализовано

### 📱 Android
- `BusFirebaseMessagingService.kt` — обработка входящих уведомлений
- `PushNotificationManager.kt` — управление подписками и разрешениями
- Каналы уведомлений (Android 8+):
  - `bus_reminders` — напоминания о ближайших автобусах
  - `schedule_updates` — обновления расписания
  - `general` — общие уведомления

### 🍎 iOS  
- `PushNotificationManager.swift` — полная интеграция FCM + APNs
- Обработка foreground/background уведомлений
- Deep links при нажатии на уведомление

### 🌐 Web/PWA
- `firebase-messaging-sw.js` — Service Worker для background уведомлений
- `push-notifications.js` — управление токенами и подписками
- In-app notifications когда вкладка активна

### ☁️ Backend
- `api/push.js` — Cloud Functions для:
  - Регистрация/удаление токенов
  - Подписка на маршруты
  - Планирование напоминаний
  - Массовые рассылки

---

## Настройка Firebase

### 1. Создать проект
```
https://console.firebase.google.com/
→ Создать проект "Olekminsk Bus"
→ Включить Cloud Messaging
```

### 2. Android
```
Firebase Console → Добавить приложение (Android)
→ Package name: com.example.olekminskbus
→ Скачать google-services.json
→ Поместить в: olekminsk-bus/app/google-services.json
```

**build.gradle (project):**
```gradle
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.4.0'
    }
}
```

**build.gradle (app):**
```gradle
plugins {
    id 'com.google.gms.google-services'
}

dependencies {
    implementation 'com.google.firebase:firebase-messaging:23.4.0'
}
```

### 3. iOS
```
Firebase Console → Добавить приложение (iOS)
→ Bundle ID: com.jonni88.olekminskbus
→ Скачать GoogleService-Info.plist
→ Добавить в Xcode проект
```

**Xcode → Signing & Capabilities:**
- Добавить "Push Notifications"
- Добавить "Background Modes" → "Remote notifications"

**Apple Developer Portal:**
- Certificates → Create APNs Auth Key
- Скачать .p8 файл
- Загрузить в Firebase Console → Project Settings → Cloud Messaging

### 4. Web
```
Firebase Console → Project Settings → General
→ Веб-приложение → Зарегистрировать
→ Скопировать конфигурацию
```

**Обновить файлы:**
- `firebase-messaging-sw.js` — заменить `YOUR_API_KEY` и другие значения
- `push-notifications.js` — та же конфигурация

**VAPID Key:**
```
Firebase Console → Project Settings → Cloud Messaging
→ Web Push certificates → Generate key pair
→ Скопировать публичный ключ
→ Вставить в push-notifications.js: vapidKey: 'YOUR_VAPID_KEY'
```

---

## Использование

### Android
```kotlin
// В MainActivity.kt
val pushManager = PushNotificationManager(this)

// Запросить разрешение (Android 13+)
if (!PushNotificationManager.hasPermission(this)) {
    pushManager.requestPermission(this)
}

// Подписаться на маршрут
lifecycleScope.launch {
    pushManager.subscribeToRoute(1)
}

// Запланировать напоминание
pushManager.scheduleReminder(1, "Центр → Автовокзал", "08:00")
```

### iOS
```swift
// В AppDelegate.swift
func application(_ application: UIApplication, 
                 didFinishLaunchingWithOptions launchOptions: ...) -> Bool {
    FirebaseApp.configure()
    PushNotificationManager.shared.registerForPushNotifications()
    return true
}

// Подписаться на маршрут
PushNotificationManager.shared.subscribeToRoute(1)

// Запланировать напоминание
PushNotificationManager.shared.scheduleReminder(
    routeId: 1, 
    direction: "Центр → Автовокзал", 
    time: "08:00"
)
```

### Web
```javascript
// Инициализация
await pushManager.init();

// Запросить разрешение
await pushManager.requestPermission();

// Подписаться на маршрут
await pushManager.subscribeToRoute(1);

// Запланировать напоминание
await pushManager.scheduleReminder(1, "Центр → Автовокзал", "08:00");
```

---

## Тестирование

### Отправить тестовое уведомление (Firebase Console)
```
Firebase Console → Cloud Messaging → Новая кампания
→ Заголовок: Тест
→ Текст: Привет из Олёкминска!
→ Отправить на тестовое устройство
→ Ввести FCM токен
```

### Получить FCM токен

**Android:**
```kotlin
FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
    Log.d("FCM", "Token: $token")
}
```

**iOS:**
```swift
// Token выводится в консоль при запуске через
// PushNotificationManager.shared.messaging(didReceiveRegistrationToken)
```

**Web:**
```javascript
// В консоли браузера
console.log(pushManager.token);
```

---

## Типы уведомлений

| Тип | Когда отправляется | Действие |
|-----|-------------------|----------|
| `bus_reminder` | За 10/5/1 минут до автобуса | Открыть маршрут |
| `schedule_update` | При изменении расписания в Google Sheets | Обновить данные |
| `welcome` | После включения уведомлений | — |
| `custom` | Ручная отправка из админки | Зависит от настройки |

---

## Что нужно сделать

1. ✅ Создать Firebase проект
2. ✅ Скачать конфигурационные файлы
3. ✅ Обновить `API_KEY` во всех файлах
4. ✅ Для iOS — создать APNs ключ в Apple Developer
5. ✅ Для Web — сгенерировать VAPID ключ
6. ✅ Задеплоить Cloud Functions
7. ✅ Протестировать на всех платформах

---

## Проблемы и решения

### Android: Не приходят уведомления
- Проверить `google-services.json`
- Проверить разрешение `POST_NOTIFICATIONS` (Android 13+)
- Проверить, что Service объявлен в `AndroidManifest.xml`

### iOS: Не приходят уведомления  
- Проверить Provisioning Profile (должен включать Push)
- Проверить APNs ключ в Firebase
- Проверить, что `Messaging.messaging().apnsToken` установлен

### Web: Не приходят уведомления
- Проверить VAPID ключ
- Проверить, что Service Worker зарегистрирован
- Проверить разрешение в настройках браузера

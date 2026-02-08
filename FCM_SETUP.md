# Настройка Firebase Cloud Messaging (FCM)

## Шаг 1: Создать проект Firebase

1. Перейдите на [console.firebase.google.com](https://console.firebase.google.com)
2. Нажмите "Создать проект"
3. Назовите проект: "Olekminsk Bus" (или как хотите)
4. Включите Google Analytics (опционально)
5. Дождитесь создания проекта

## Шаг 2: Добавить Android приложение

1. Нажмите на иконку Android (Добавить приложение)
2. **Имя пакета Android**: `com.olekminsk.bus`
3. **Псевдоним приложения**: Olekminsk Bus
4. **Отладочный сертификат SHA-1**: (опционально, для авторизации)

### Как получить SHA-1:
```bash
keytool -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore
# Пароль: android
```

5. Нажмите "Зарегистрировать приложение"
6. Скачайте файл `google-services.json`

## Шаг 3: Добавить файл в проект

1. Скопируйте скачанный `google-services.json`
2. Вставьте в папку: `app/google-services.json` (рядом с `build.gradle`)

```
olekminsk-bus/
├── app/
│   ├── build.gradle.kts
│   ├── google-services.json  <-- СЮДА
│   └── src/
```

## Шаг 4: Пересобрать приложение

```bash
./gradlew clean assembleDebug
```

## Шаг 5: Отправить тестовое уведомление

1. В Firebase Console выберите проект
2. Слева меню: "Cloud Messaging"
3. Нажмите "Отправить первое сообщение"
4. Введите:
   - **Заголовок уведомления**: "Тест"
   - **Текст уведомления**: "Привет из Firebase!"
5. Нажмите "Отправить тестовое сообщение"
6. Введите токен устройства (см. ниже как получить)

## Как получить FCM токен

В коде приложения логи покажут токен. Или добавьте в MainActivity:

```kotlin
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val token = task.result
        Log.d("FCM", "Token: $token")
        // Отправьте этот токен в Firebase Console для теста
    }
}
```

## Отправка пушей с сервера

### HTTP запрос:
```bash
curl -X POST https://fcm.googleapis.com/fcm/send \
  -H "Authorization: key=YOUR_SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "DEVICE_FCM_TOKEN",
    "data": {
      "title": "Изменение расписания",
      "message": "Маршрут 1: задержка 10 минут",
      "route_id": "1"
    }
  }'
```

### Получить Server Key:
1. Firebase Console → Project Settings → Cloud Messaging
2. Скопируйте "Server key"

## Типы пуш-уведомлений

Приложение поддерживает:

1. **Изменения в расписании**
   ```json
   {
     "title": "Изменение расписания",
     "message": "Маршрут 1 по выходным до 22:00",
     "route_id": "1"
   }
   ```

2. **Отмена рейсов**
   ```json
   {
     "title": "Внимание!",
     "message": "Рейс 18:30 отменен",
     "route_id": "1"
   }
   ```

3. **Общие новости**
   ```json
   {
     "title": "Новое в приложении",
     "message": "Добавлено 2 новых маршрута"
   }
   ```

## Важно!

- Не публикуйте `google-services.json` в открытый репозиторий
- Добавьте его в `.gitignore`
- Для production используйте ограничение по IP для Server Key

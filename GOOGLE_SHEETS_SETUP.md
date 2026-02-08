# Инструкция: Подключение Google Sheets к приложению

## Вариант 1: Google Apps Script (Простой)

### Шаг 1: Создайте Google Sheets

1. Откройте [sheets.google.com](https://sheets.google.com)
2. Создайте новую таблицу
3. Настройте структуру:

```
Лист "Routes" (Маршруты):
| A | B | C | D | E |
|---|---|---|---|---|
| ID | Number | Name | Type | Active |
| 1 | 1 | Автовокзал - Центр | urban | TRUE |
| 2 | 2 | Центр - Микрорайон | urban | TRUE |

Лист "Stops" (Остановки):
| A | B | C |
|---|---|---|
| RouteID | StopName | Time |
| 1 | Автовокзал | 06:00 |
| 1 | Центр | 06:15 |

Лист "Schedule" (Расписание):
| A | B | C | D |
|---|---|---|---|
| RouteID | Weekday | Weekend | Notes |
| 1 | 06:00,07:00,08:00 | 08:00,09:00,10:00 | По будням чаще |
```

### Шаг 2: Создайте Apps Script

1. В таблице: Расширения → Apps Script
2. Вставьте код:

```javascript
function doGet(e) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet();
  var routesSheet = sheet.getSheetByName("Routes");
  var stopsSheet = sheet.getSheetByName("Stops");
  var scheduleSheet = sheet.getSheetByName("Schedule");
  
  var routes = [];
  var routesData = routesSheet.getDataRange().getValues();
  
  // Пропускаем заголовок (i = 1)
  for (var i = 1; i < routesData.length; i++) {
    var row = routesData[i];
    var routeId = row[0];
    
    // Получаем остановки для этого маршрута
    var stops = [];
    var stopsData = stopsSheet.getDataRange().getValues();
    for (var j = 1; j < stopsData.length; j++) {
      if (stopsData[j][0] == routeId) {
        stops.push({
          name: stopsData[j][1],
          time: stopsData[j][2]
        });
      }
    }
    
    // Получаем расписание
    var schedule = { weekday: [], weekend: [], notes: "" };
    var scheduleData = scheduleSheet.getDataRange().getValues();
    for (var k = 1; k < scheduleData.length; k++) {
      if (scheduleData[k][0] == routeId) {
        schedule.weekday = scheduleData[k][1].toString().split(',');
        schedule.weekend = scheduleData[k][2].toString().split(',');
        schedule.notes = scheduleData[k][3];
        break;
      }
    }
    
    routes.push({
      id: routeId,
      number: row[1].toString(),
      name: row[2],
      type: row[3],
      stops: stops,
      schedule: schedule,
      isActive: row[4] == "TRUE"
    });
  }
  
  var output = JSON.stringify({
    routes: routes,
    lastUpdated: new Date().toISOString()
  });
  
  return ContentService.createTextOutput(output)
    .setMimeType(ContentService.MimeType.JSON);
}
```

3. Сохраните проект (Ctrl+S)
4. Разверните → Новое развертывание
5. Тип: Веб-приложение
6. Доступ: "Все"
7. Нажмите "Развернуть"
8. Скопируйте URL веб-приложения

### Шаг 3: Настройте приложение

Откройте файл `BusScheduleApi.kt` и замените:

```kotlin
const val BASE_URL = "https://script.google.com/macros/s/ВАШ_ID/exec/"
```

## Вариант 2: JSON файл на сервере

Если есть свой сервер (Beget):

1. Создайте файл `routes.json`:

```json
{
  "routes": [
    {
      "id": 1,
      "number": "1",
      "name": "Автовокзал - Центр",
      "type": "urban",
      "stops": [
        {"name": "Автовокзал", "time": "06:00"},
        {"name": "Центр", "time": "06:15"}
      ],
      "schedule": {
        "weekday": ["06:00", "07:00", "08:00"],
        "weekend": ["08:00", "09:00", "10:00"],
        "notes": "По будням чаще"
      },
      "isActive": true
    }
  ],
  "lastUpdated": "2025-02-07T10:00:00Z"
}
```

2. Залейте на сервер в папку `public_html/api/`
3. В `BusScheduleApi.kt` укажите:

```kotlin
const val BASE_URL = "https://your-site.ru/api/"
```

## Вариант 3: Статичный JSON в приложении

Если расписание редко меняется, можно встроить JSON в приложение:

1. Создайте файл `src/main/assets/routes.json`
2. Положите туда расписание
3. В `Repository` добавьте загрузку из assets если сеть недоступна

## Тестирование

Проверьте URL в браузере:
```
https://script.google.com/macros/s/ВАШ_ID/exec/
```

Должен вернуть JSON с расписанием.

## Проблемы и решения

### "Доступ запрещен"
В Apps Script проверьте права доступа при развертывании — должно быть "Все".

### Пустой ответ
Проверьте названия листов (должны быть "Routes", "Stops", "Schedule").

### Не обновляется
В Apps Script: Выполнить → Развернуть → Управление развертываниями → Отредактировать → Новая версия.

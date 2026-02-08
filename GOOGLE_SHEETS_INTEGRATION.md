# Google Sheets Integration for Bus Schedule

## Структура таблицы

Создай Google Sheet с такими колонками:

| id | number | name | type | forward_name | forward_stops | forward_times | backward_name | backward_stops | backward_times | is_active |
|----|--------|------|------|--------------|---------------|---------------|---------------|----------------|----------------|-----------|
| 1 | 1 | Автовокзал ↔ Центр | urban | Автовокзал → Центр | Автовокзал → Центр → Площадь | 06:30,07:15,08:00 | Центр → Автовокзал | Площадь → Центр → Автовокзал | 07:00,07:45,08:30 | TRUE |

## Шаги настройки

### 1. Создать Google Cloud Project
- https://console.cloud.google.com/
- Создать новый проект
- Включить Google Sheets API
- Создать Service Account
- Скачать JSON ключ

### 2. Настроить доступ к таблице
- Открыть Google Sheet
- Нажать "Share"
- Добавить email Service Account (из JSON ключа)
- Дать права "Viewer" или "Editor"

### 3. Deploy Cloud Function (рекомендуется)

Вариант A: **Google Cloud Functions** (бесплатный tier)
Вариант B: **Supabase Edge Function** (бесплатно)
Вариант C: **Vercel Serverless** (бесплатно)

### 4. Обновить приложения

Все приложения будут делать GET запрос:
```
GET https://your-api.com/api/routes
```

Ответ:
```json
{
  "routes": [
    {
      "id": 1,
      "number": "1",
      "name": "Автовокзал ↔ Центр",
      "type": "urban",
      "forward": {
        "name": "Автовокзал → Центр",
        "stops": "Автовокзал → Центр → Площадь",
        "times": ["06:30", "07:15", "08:00"]
      },
      "backward": {
        "name": "Центр → Автовокзал",
        "stops": "Площадь → Центр → Автовокзал",
        "times": ["07:00", "07:45", "08:00"]
      }
    }
  ],
  "last_updated": "2026-02-08T10:00:00Z"
}
```

## Варианты реализации

### Простой (прямой доступ) — НЕ рекомендуется
- API ключ в коде приложения
- Легко украсть
- Нет кэширования

### Рекомендуемый (Cloud Function)
- API ключ на сервере
- Кэширование (Redis/memory)
- Rate limiting
- Логирование

## Код Cloud Function (Node.js)

```javascript
const { google } = require('googleapis');

const SPREADSHEET_ID = 'YOUR_SHEET_ID';
const SHEET_NAME = 'Routes';

exports.getRoutes = async (req, res) => {
  const auth = new google.auth.GoogleAuth({
    credentials: JSON.parse(process.env.GOOGLE_CREDENTIALS),
    scopes: ['https://www.googleapis.com/auth/spreadsheets.readonly'],
  });

  const sheets = google.sheets({ version: 'v4', auth });
  
  const response = await sheets.spreadsheets.values.get({
    spreadsheetId: SPREADSHEET_ID,
    range: `${SHEET_NAME}!A:J`,
  });

  const rows = response.data.values;
  const headers = rows[0];
  const routes = rows.slice(1).map(row => ({
    id: parseInt(row[0]),
    number: row[1],
    name: row[2],
    type: row[3],
    forward: {
      name: row[4],
      stops: row[5],
      times: row[6].split(',')
    },
    backward: {
      name: row[7],
      stops: row[8],
      times: row[9].split(',')
    },
    is_active: row[10] === 'TRUE'
  })).filter(r => r.is_active);

  res.json({ routes, last_updated: new Date().toISOString() });
};
```

## Обновление приложений

### Android (Kotlin + Retrofit)
```kotlin
interface BusApi {
    @GET("api/routes")
    suspend fun getRoutes(): RoutesResponse
}
```

### iOS (Swift)
```swift
struct BusAPI {
    func fetchRoutes() async throws -> [BusRoute] {
        let (data, _) = try await URLSession.shared.data(from: url)
        let response = try JSONDecoder().decode(RoutesResponse.self, from: data)
        return response.routes
    }
}
```

### Web (JavaScript)
```javascript
async function loadRoutes() {
  const response = await fetch('/api/routes');
  const data = await response.json();
  return data.routes;
}
```

## Кэширование офлайн

Все приложения должны:
1. Загружать данные при старте
2. Сохранять в локальное хранилище
3. Показывать кэш если нет интернета
4. Обновлять при pull-to-refresh

## Следующие шаги

1. Создашь Google Sheet?
2. Выберешь хостинг для API (Google Cloud / Supabase / Vercel)?
3. Я настрою код для всех платформ

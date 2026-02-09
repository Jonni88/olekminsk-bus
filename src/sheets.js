const { google } = require('googleapis');
const fs = require('fs');
const path = require('path');

// ID твоей таблицы (из URL)
const SPREADSHEET_ID = '1jNSVkXTohNjy2Ukpb2-IZMUbu7OKGJQ_G-eel60c-IE';

// Путь к файлу сервисного аккаунта
const KEY_PATH = path.join(__dirname, '..', 'config', 'service-account.json');

/**
 * Создаёт клиент для работы с Google Sheets
 */
function createAuthClient() {
  if (!fs.existsSync(KEY_PATH)) {
    throw new Error(`Файл сервисного аккаунта не найден: ${KEY_PATH}`);
  }

  const credentials = JSON.parse(fs.readFileSync(KEY_PATH, 'utf8'));
  
  return new google.auth.GoogleAuth({
    credentials,
    scopes: ['https://www.googleapis.com/auth/spreadsheets.readonly'],
  });
}

/**
 * Читает данные из указанного листа
 * @param {string} sheetName - Название листа (например, "Маршруты")
 * @param {string} range - Диапазон ячеек (например, "A1:D100")
 */
async function readSheet(sheetName, range = 'A1:Z1000') {
  try {
    const auth = createAuthClient();
    const sheets = google.sheets({ version: 'v4', auth });

    const response = await sheets.spreadsheets.values.get({
      spreadsheetId: SPREADSHEET_ID,
      range: `${sheetName}!${range}`,
    });

    const rows = response.data.values;
    if (!rows || rows.length === 0) {
      console.log(`Лист "${sheetName}" пуст`);
      return [];
    }

    // Первая строка — заголовки
    const headers = rows[0];
    const data = rows.slice(1).map(row => {
      const obj = {};
      headers.forEach((header, index) => {
        obj[header] = row[index] || '';
      });
      return obj;
    });

    return data;
  } catch (error) {
    console.error(`Ошибка чтения листа "${sheetName}":`, error.message);
    throw error;
  }
}

/**
 * Загружает всё расписание из всех листов
 */
async function loadAllSchedules() {
  console.log('📊 Загрузка расписания из Google Sheets...\n');

  try {
    const [routes, stops, schedule, exceptions] = await Promise.all([
      readSheet('Маршруты'),
      readSheet('Остановки'),
      readSheet('Расписание'),
      readSheet('Исключения'),
    ]);

    const data = {
      routes,
      stops,
      schedule,
      exceptions,
      lastUpdated: new Date().toISOString(),
    };

    console.log('✅ Данные загружены:');
    console.log(`   • Маршрутов: ${routes.length}`);
    console.log(`   • Остановок: ${stops.length}`);
    console.log(`   • Рейсов: ${schedule.length}`);
    console.log(`   • Исключений: ${exceptions.length}`);

    return data;
  } catch (error) {
    console.error('❌ Ошибка загрузки:', error.message);
    throw error;
  }
}

/**
 * Сохраняет данные в JSON файл (для кэширования)
 */
function saveToCache(data, filename = 'schedule-cache.json') {
  const cachePath = path.join(__dirname, '..', filename);
  fs.writeFileSync(cachePath, JSON.stringify(data, null, 2), 'utf8');
  console.log(`💾 Кэш сохранён: ${filename}`);
}

/**
 * Загружает данные из кэша (если Google недоступен)
 */
function loadFromCache(filename = 'schedule-cache.json') {
  const cachePath = path.join(__dirname, '..', filename);
  
  if (!fs.existsSync(cachePath)) {
    throw new Error('Кэш не найден');
  }

  const data = JSON.parse(fs.readFileSync(cachePath, 'utf8'));
  console.log(`📂 Загружено из кэша (обновлено: ${data.lastUpdated})`);
  return data;
}

/**
 * Получает расписание для конкретного маршрута
 */
function getRouteSchedule(routeId, data) {
  const route = data.routes.find(r => r.ID === routeId || r.Номер === routeId);
  if (!route) return null;

  const schedule = data.schedule.filter(s => s.Маршрут_ID === routeId);
  
  return {
    ...route,
    schedule: schedule.map(s => ({
      time: s.Время,
      days: s.Дни,
      stop: data.stops.find(stop => stop.ID === s.Остановка_ID)?.Название || 'Неизвестно',
    })),
  };
}

/**
 * Получает ближайшие рейсы
 */
function getNextBuses(routeId, data, count = 3) {
  const now = new Date();
  const currentTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
  
  const routeSchedule = data.schedule.filter(s => s.Маршрут_ID === routeId);
  
  // Сортируем по времени
  const sorted = routeSchedule
    .filter(s => s.Время > currentTime)
    .sort((a, b) => a.Время.localeCompare(b.Время));

  return sorted.slice(0, count);
}

module.exports = {
  readSheet,
  loadAllSchedules,
  saveToCache,
  loadFromCache,
  getRouteSchedule,
  getNextBuses,
};

// Если запущено напрямую — тестируем
if (require.main === module) {
  (async () => {
    try {
      const data = await loadAllSchedules();
      saveToCache(data);
      
      // Пример: показать расписание первого маршрута
      if (data.routes.length > 0) {
        const firstRoute = getRouteSchedule(data.routes[0].ID, data);
        console.log('\n📋 Пример — первый маршрут:');
        console.log(JSON.stringify(firstRoute, null, 2));
      }
    } catch (error) {
      console.error('❌ Ошибка:', error.message);
      process.exit(1);
    }
  })();
}

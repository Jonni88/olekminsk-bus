const { google } = require('googleapis');
const fs = require('fs');
const path = require('path');
const sampleData = require('../data/sample-schedule');

// ID твоей таблицы
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
    scopes: ['https://www.googleapis.com/auth/spreadsheets'],
  });
}

/**
 * Записывает данные в лист Google Sheets
 */
async function writeSheet(sheetName, headers, data) {
  try {
    const auth = createAuthClient();
    const sheets = google.sheets({ version: 'v4', auth });

    // Формируем данные для записи
    const values = [headers, ...data.map(row => 
      headers.map(header => row[header] || '')
    )];

    // Очищаем лист и записываем новые данные
    await sheets.spreadsheets.values.clear({
      spreadsheetId: SPREADSHEET_ID,
      range: `${sheetName}!A1:Z1000`,
    });

    const response = await sheets.spreadsheets.values.update({
      spreadsheetId: SPREADSHEET_ID,
      range: `${sheetName}!A1`,
      valueInputOption: 'RAW',
      resource: { values },
    });

    console.log(`✅ Лист "${sheetName}" обновлён: ${response.data.updatedCells} ячеек`);
    return response.data;
  } catch (error) {
    console.error(`❌ Ошибка записи в лист "${sheetName}":`, error.message);
    throw error;
  }
}

/**
 * Загружает все данные в Google Sheets
 */
async function uploadAllData() {
  console.log('📤 Загрузка данных в Google Sheets...\n');
  console.log('=====================================\n');

  try {
    // Проверяем наличие файла
    if (!fs.existsSync(KEY_PATH)) {
      console.error('❌ ОШИБКА: Файл config/service-account.json не найден!');
      process.exit(1);
    }

    // Загружаем данные
    await writeSheet('Маршруты', 
      ['ID', 'Номер', 'Название', 'Описание'], 
      sampleData.routes
    );

    await writeSheet('Остановки', 
      ['ID', 'Название', 'Маршруты', 'Примечание'], 
      sampleData.stops
    );

    await writeSheet('Расписание', 
      ['Маршрут_ID', 'Остановка_ID', 'Время', 'Дни', 'Примечание'], 
      sampleData.schedule
    );

    await writeSheet('Исключения', 
      ['Дата', 'Тип', 'Описание'], 
      sampleData.exceptions
    );

    console.log('\n✅ Все данные успешно загружены!');
    console.log('\n📊 Итого:');
    console.log(`   • Маршрутов: ${sampleData.routes.length}`);
    console.log(`   • Остановок: ${sampleData.stops.length}`);
    console.log(`   • Рейсов: ${sampleData.schedule.length}`);
    console.log(`   • Исключений: ${sampleData.exceptions.length}`);
    console.log('\n🔗 Открой таблицу: https://docs.google.com/spreadsheets/d/' + SPREADSHEET_ID);

  } catch (error) {
    console.error('\n❌ Ошибка загрузки:', error.message);
    
    if (error.message.includes('Permission denied') || error.message.includes('403')) {
      console.log('\n💡 Возможные причины:');
      console.log('• Email сервисного аккаунта не добавлен в Google Sheet');
      console.log('• Сервисному аккаунту даны неправильные права (нужен "Редактор")');
      console.log('• Неправильный ID таблицы');
      console.log('\n📋 Email для добавления:');
      const credentials = JSON.parse(fs.readFileSync(KEY_PATH, 'utf8'));
      console.log(credentials.client_email);
    }
    
    process.exit(1);
  }
}

// Запускаем загрузку
uploadAllData();

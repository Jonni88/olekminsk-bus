const { google } = require('googleapis');
const fs = require('fs');
const path = require('path');

const SPREADSHEET_ID = '1jNSVkXTohNjy2Ukpb2-IZMUbu7OKGJQ_G-eel60c-IE';
const KEY_PATH = path.join(__dirname, 'config', 'service-account.json');

async function diagnose() {
  console.log('🔍 Диагностика подключения к Google Sheets\n');
  console.log('==========================================\n');

  // 1. Проверяем файл ключа
  if (!fs.existsSync(KEY_PATH)) {
    console.error('❌ Файл service-account.json не найден!');
    return;
  }
  console.log('✅ Файл service-account.json найден\n');

  // 2. Читаем email сервисного аккаунта
  const credentials = JSON.parse(fs.readFileSync(KEY_PATH, 'utf8'));
  console.log('📧 Email сервисного аккаунта:');
  console.log('   ' + credentials.client_email);
  console.log('\n   ⚠️  Этот email должен быть добавлен в таблицу с правами "Редактор"!\n');

  // 3. Пытаемся подключиться
  try {
    const auth = new google.auth.GoogleAuth({
      credentials,
      scopes: ['https://www.googleapis.com/auth/spreadsheets.readonly'],
    });

    const sheets = google.sheets({ version: 'v4', auth });

    // 4. Пробуем получить информацию о таблице
    console.log('📊 Проверка доступа к таблице...');
    const spreadsheet = await sheets.spreadsheets.get({
      spreadsheetId: SPREADSHEET_ID,
    });

    console.log('✅ Таблица доступна!');
    console.log('   Название: ' + spreadsheet.data.properties.title);
    console.log('\n📋 Доступные листы:');
    
    spreadsheet.data.sheets.forEach((sheet, index) => {
      console.log(`   ${index + 1}. "${sheet.properties.title}"`);
    });

    console.log('\n💡 Проверь, что названия листов совпадают с:');
    console.log('   - Маршруты');
    console.log('   - Остановки');
    console.log('   - Расписание');
    console.log('   - Исключения');

  } catch (error) {
    console.error('\n❌ Ошибка:', error.message);
    
    if (error.message.includes('403') || error.message.includes('Forbidden')) {
      console.log('\n🔴 ПРОБЛЕМА: Нет доступа к таблице!');
      console.log('\n📋 Что нужно сделать:');
      console.log('1. Открой таблицу: https://docs.google.com/spreadsheets/d/' + SPREADSHEET_ID);
      console.log('2. Нажми кнопку "Настройки доступа" (🔒 справа вверху)');
      console.log('3. Нажми "Добавить людей"');
      console.log('4. Введи email: ' + credentials.client_email);
      console.log('5. Выбери роль: "Редактор"');
      console.log('6. Нажми "Готово"');
    }
    
    if (error.message.includes('404') || error.message.includes('Not Found')) {
      console.log('\n🔴 ПРОБЛЕМА: Таблица не найдена!');
      console.log('Проверь правильность ID таблицы.');
    }
  }
}

diagnose();

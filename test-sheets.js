const { loadAllSchedules, saveToCache } = require('./src/sheets');

/**
 * Тест подключения к Google Sheets
 * Запуск: node test-sheets.js
 */

console.log('🚌 Тест подключения к Google Sheets\n');
console.log('=====================================\n');

(async () => {
  try {
    // Проверяем наличие файла сервисного аккаунта
    const fs = require('fs');
    const path = require('path');
    const keyPath = path.join(__dirname, 'config', 'service-account.json');
    
    if (!fs.existsSync(keyPath)) {
      console.error('❌ ОШИБКА: Файл config/service-account.json не найден!');
      console.log('\n📋 Что нужно сделать:');
      console.log('1. Положи свой JSON-файл сервисного аккаунта в папку config/');
      console.log('2. Переименуй его в service-account.json');
      console.log('3. Запусти скрипт снова: node test-sheets.js');
      process.exit(1);
    }

    console.log('✅ Файл сервисного аккаунта найден\n');

    // Загружаем данные
    const data = await loadAllSchedules();
    
    // Сохраняем в кэш
    saveToCache(data);
    
    console.log('\n✅ Тест пройден успешно!');
    console.log('\n📁 Данные сохранены в schedule-cache.json');
    console.log('🚀 Можно использовать в приложении!');
    
  } catch (error) {
    console.error('\n❌ Ошибка:', error.message);
    
    if (error.message.includes('invalid_grant')) {
      console.log('\n💡 Возможные причины:');
      console.log('• Email сервисного аккаунта не добавлен в Google Sheet');
      console.log('• Неправильный файл ключа');
      console.log('• Сервисный аккаунт удалён в Google Cloud');
    }
    
    process.exit(1);
  }
})();

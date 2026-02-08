#!/usr/bin/env python3
"""
Telegram бот для поиска информации на сайте мояолекма.рф
Сайт работает на Joomla
"""

import logging
import requests
from bs4 import BeautifulSoup
from telegram import Update
from telegram.ext import Application, CommandHandler, MessageHandler, filters, ContextTypes

# Настройка логирования
logging.basicConfig(
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    level=logging.INFO
)
logger = logging.getLogger(__name__)

# Конфигурация
BOT_TOKEN = 'YOUR_BOT_TOKEN_HERE'  # Замените на токен от @BotFather
SITE_URL = 'https://xn--80ajpcedka6m.xn--p1ai'  # мояолекма.рф


async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Обработчик команды /start"""
    welcome_message = """
👋 Привет! Я бот для поиска информации на сайте Моя Олёкма.

🔍 Отправь мне слово или фразу, и я найду информацию на сайте.

📌 Примеры:
• "библиотека"
• "школа"
• "детский сад"

💡 Используй /help для справки.
    """
    await update.message.reply_text(welcome_message)


async def help_command(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Обработчик команды /help"""
    help_text = """
🔍 Как пользоваться ботом:

1️⃣ Просто отправь слово или фразу
2️⃣ Бот найдёт информацию на сайте мояолекма.рф
3️⃣ Получишь результаты с названиями и ссылками

⚡️ Сайт работает на Joomla, поиск идёт по всему контенту.

❓ Проблемы? Проверь:
• Правильность написания
• Попробуй синонимы
• Используй более общие слова
    """
    await update.message.reply_text(help_text)


def search_on_site(query: str) -> list:
    """
    Поиск на сайте мояолекма.рф
    Возвращает список найденных результатов
    """
    try:
        # Формируем URL для поиска Joomla
        search_url = f"{SITE_URL}/index.php"
        params = {
            'option': 'com_search',
            'searchword': query,
            'Itemid': '1'
        }
        
        # Заголовки для имитации браузера
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }
        
        # Делаем запрос
        response = requests.get(search_url, params=params, headers=headers, timeout=30)
        response.encoding = 'utf-8'
        
        if response.status_code != 200:
            logger.error(f"Ошибка запроса: {response.status_code}")
            return []
        
        # Парсим HTML
        soup = BeautifulSoup(response.text, 'html.parser')
        results = []
        
        # Ищем результаты поиска (типичная структура Joomla)
        # Пробуем разные селекторы
        result_blocks = soup.find_all('div', class_='search-results') or \
                       soup.find_all('div', class_='result') or \
                       soup.find_all('article') or \
                       soup.find_all('div', class_='item')
        
        for block in result_blocks[:10]:  # Максимум 10 результатов
            # Ищем заголовок и ссылку
            title_elem = block.find('a') or block.find('h2') or block.find('h3')
            desc_elem = block.find('p') or block.find('div', class_='description')
            
            if title_elem:
                title = title_elem.get_text(strip=True)
                link = title_elem.get('href', '')
                
                # Формируем полный URL
                if link and not link.startswith('http'):
                    link = f"{SITE_URL}{link if link.startswith('/') else '/' + link}"
                
                description = ''
                if desc_elem:
                    description = desc_elem.get_text(strip=True)[:200] + '...' if len(desc_elem.get_text(strip=True)) > 200 else desc_elem.get_text(strip=True)
                
                results.append({
                    'title': title,
                    'link': link or SITE_URL,
                    'description': description
                })
        
        return results
        
    except Exception as e:
        logger.error(f"Ошибка при поиске: {e}")
        return []


async def handle_message(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Обработчик текстовых сообщений"""
    query = update.message.text.strip()
    
    if len(query) < 2:
        await update.message.reply_text("⚠️ Слишком короткий запрос. Введите минимум 2 символа.")
        return
    
    # Отправляем сообщение о поиске
    search_msg = await update.message.reply_text(f"🔍 Ищу «{query}» на сайте Моя Олёкма...")
    
    # Выполняем поиск
    results = search_on_site(query)
    
    # Удаляем сообщение о поиске
    await search_msg.delete()
    
    if not results:
        await update.message.reply_text(
            f"😕 По запросу «{query}» ничего не найдено.\n\n"
            f"💡 Попробуй:\n"
            f"• Другие слова\n"
            f"• Синонимы\n"
            f"• Более общие термины"
        )
        return
    
    # Формируем ответ
    response_text = f"🔍 Результаты поиска «{query}» ({len(results)} найдено):\n\n"
    
    for i, result in enumerate(results[:5], 1):  # Показываем первые 5
        response_text += f"{i}. 📄 *{result['title']}*\n"
        if result['description']:
            response_text += f"   {result['description']}\n"
        response_text += f"   🔗 {result['link']}\n\n"
    
    if len(results) > 5:
        response_text += f"_...и ещё {len(results) - 5} результатов_"
    
    # Отправляем результаты
    await update.message.reply_text(response_text, parse_mode='Markdown', disable_web_page_preview=True)


async def error_handler(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Обработчик ошибок"""
    logger.error(f"Update {update} caused error {context.error}")
    if update and update.message:
        await update.message.reply_text("❌ Произошла ошибка. Попробуйте позже.")


def main():
    """Главная функция"""
    # Проверяем токен
    if BOT_TOKEN == 'YOUR_BOT_TOKEN_HERE':
        print("❌ ОШИБКА: Замените YOUR_BOT_TOKEN_HERE на реальный токен от @BotFather")
        print("1. Напишите @BotFather в Telegram")
        print("2. Отправьте /newbot")
        print("3. Скопируйте полученный токен")
        print("4. Вставьте в файл bot.py в строку BOT_TOKEN = 'ваш_токен'")
        return
    
    # Создаём приложение
    application = Application.builder().token(BOT_TOKEN).build()
    
    # Добавляем обработчики
    application.add_handler(CommandHandler("start", start))
    application.add_handler(CommandHandler("help", help_command))
    application.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, handle_message))
    application.add_error_handler(error_handler)
    
    # Запускаем бота
    print("🤖 Бот запущен!")
    print("Отправьте /start в Telegram для начала работы")
    application.run_polling(allowed_updates=Update.ALL_TYPES)


if __name__ == '__main__':
    main()

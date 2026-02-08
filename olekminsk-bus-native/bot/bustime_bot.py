# BusTime Telegram Bot для водителей
# Простой бот для отметки положения автобуса

import asyncio
import json
import os
from datetime import datetime
from telegram import Update, InlineKeyboardButton, InlineKeyboardMarkup
from telegram.ext import Application, CommandHandler, CallbackQueryHandler, ContextTypes

# Хранение данных (в реальном проекте — база данных)
BUS_LOCATIONS = {}

# Маршруты
ROUTES = {
    "1": "Автовокзал — Дача",
    "2": "Автовокзал — Авиапорт", 
    "5": "Автовокзал — ПНДИ",
    "6": "Автовокзал — Новостройки",
    "7": "Автовокзал — Нефтебаза"
}

# Остановки для каждого маршрута (упрощённо)
STOPS = {
    "1": ["Автовокзал", "Центр", "Площадь", "Дача"],
    "2": ["Автовокзал", "Рынок", "Авиапорт"],
    "5": ["Автовокзал", "Больница", "ПНДИ"],
    "6": ["Автовокзал", "Школа", "Новостройки"],
    "7": ["Автовокзал", "Завод", "Нефтебаза"]
}

async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Начало работы с ботом"""
    keyboard = []
    for route_id, route_name in ROUTES.items():
        keyboard.append([InlineKeyboardButton(f"{route_id}: {route_name}", callback_data=f"route_{route_id}")])
    
    reply_markup = InlineKeyboardMarkup(keyboard)
    await update.message.reply_text(
        "🚌 BusTime — Выбор маршрута\n\nВыберите ваш маршрут:",
        reply_markup=reply_markup
    )

async def button_handler(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Обработка кнопок"""
    query = update.callback_query
    await query.answer()
    
    data = query.data
    
    if data.startswith("route_"):
        route_id = data.split("_")[1]
        context.user_data["route"] = route_id
        
        # Показываем остановки
        stops = STOPS.get(route_id, [])
        keyboard = []
        
        for i, stop in enumerate(stops):
            keyboard.append([InlineKeyboardButton(f"📍 {stop}", callback_data=f"stop_{i}_{stop}")])
        
        keyboard.append([InlineKeyboardButton("🚀 Я здесь (GPS)", callback_data="gps_location")])
        
        reply_markup = InlineKeyboardMarkup(keyboard)
        await query.edit_message_text(
            f"🚌 Маршрут {route_id}: {ROUTES[route_id]}\n\nВыберите остановку:",
            reply_markup=reply_markup
        )
    
    elif data.startswith("stop_"):
        parts = data.split("_")
        stop_index = int(parts[1])
        stop_name = parts[2]
        route_id = context.user_data.get("route", "unknown")
        
        # Сохраняем положение
        driver_id = update.effective_user.id
        BUS_LOCATIONS[driver_id] = {
            "route": route_id,
            "stop": stop_name,
            "stop_index": stop_index,
            "timestamp": datetime.now().isoformat(),
            "status": "at_stop"
        }
        
        # Отправляем подтверждение
        keyboard = [
            [InlineKeyboardButton("🚗 Отправился дальше", callback_data="departed")],
            [InlineKeyboardButton("🔙 Выбрать другой маршрут", callback_data="back_to_routes")]
        ]
        reply_markup = InlineKeyboardMarkup(keyboard)
        
        await query.edit_message_text(
            f"✅ Отмечено!\n\n🚌 Маршрут {route_id}\n📍 Остановка: {stop_name}\n⏰ {datetime.now().strftime('%H:%M')}\n\nСтатус: На остановке",
            reply_markup=reply_markup
        )
    
    elif data == "departed":
        driver_id = update.effective_user.id
        if driver_id in BUS_LOCATIONS:
            BUS_LOCATIONS[driver_id]["status"] = "moving"
            BUS_LOCATIONS[driver_id]["departure_time"] = datetime.now().isoformat()
        
        keyboard = [
            [InlineKeyboardButton("📍 Прибыл на следующую", callback_data="next_stop")],
            [InlineKeyboardButton("🔙 Выбрать другой маршрут", callback_data="back_to_routes")]
        ]
        reply_markup = InlineKeyboardMarkup(keyboard)
        
        await query.edit_message_text(
            "🚗 Отправились!\n\nКогда приедете на следующую остановку — нажмите кнопку ниже.",
            reply_markup=reply_markup
        )
    
    elif data == "next_stop":
        # Логика перехода к следующей остановке
        driver_id = update.effective_user.id
        if driver_id in BUS_LOCATIONS:
            current_index = BUS_LOCATIONS[driver_id].get("stop_index", 0)
            route_id = BUS_LOCATIONS[driver_id].get("route", "1")
            stops = STOPS.get(route_id, [])
            
            if current_index + 1 < len(stops):
                next_stop = stops[current_index + 1]
                BUS_LOCATIONS[driver_id]["stop"] = next_stop
                BUS_LOCATIONS[driver_id]["stop_index"] = current_index + 1
                BUS_LOCATIONS[driver_id]["status"] = "at_stop"
                BUS_LOCATIONS[driver_id]["timestamp"] = datetime.now().isoformat()
                
                keyboard = [
                    [InlineKeyboardButton("🚗 Отправился дальше", callback_data="departed")],
                    [InlineKeyboardButton("🔙 Выбрать другой маршрут", callback_data="back_to_routes")]
                ]
                reply_markup = InlineKeyboardMarkup(keyboard)
                
                await query.edit_message_text(
                    f"✅ Прибытие отмечено!\n\n🚌 Маршрут {route_id}\n📍 Остановка: {next_stop}\n⏰ {datetime.now().strftime('%H:%M')}\n\nСтатус: На остановке",
                    reply_markup=reply_markup
                )
            else:
                await query.edit_message_text("🏁 Конечная остановка! Маршрут завершён.")
    
    elif data == "back_to_routes":
        # Возврат к выбору маршрута
        keyboard = []
        for route_id, route_name in ROUTES.items():
            keyboard.append([InlineKeyboardButton(f"{route_id}: {route_name}", callback_data=f"route_{route_id}")])
        
        reply_markup = InlineKeyboardMarkup(keyboard)
        await query.edit_message_text(
            "🚌 BusTime — Выбор маршрута\n\nВыберите ваш маршрут:",
            reply_markup=reply_markup
        )

async def status(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Показать текущий статус"""
    driver_id = update.effective_user.id
    
    if driver_id in BUS_LOCATIONS:
        info = BUS_LOCATIONS[driver_id]
        await update.message.reply_text(
            f"📊 Ваш статус:\n\n"
            f"🚌 Маршрут: {info['route']}\n"
            f"📍 Остановка: {info['stop']}\n"
            f"⏰ Время: {info['timestamp'][:16].replace('T', ' ')}\n"
            f"📌 Статус: {'На остановке' if info['status'] == 'at_stop' else 'В пути'}"
        )
    else:
        await update.message.reply_text("❌ Вы ещё не отмечали положение. Нажмите /start")

async def get_locations(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """API endpoint - получить все положения (для Android приложения)"""
    # В реальном проекте — проверка API ключа
    locations = []
    for driver_id, info in BUS_LOCATIONS.items():
        locations.append({
            "driver_id": driver_id,
            "route": info["route"],
            "stop": info["stop"],
            "status": info["status"],
            "timestamp": info["timestamp"]
        })
    
    await update.message.reply_text(
        f"📍 Активные автобусы: {len(locations)}\n\n" +
        "\n".join([f"🚌 М{loc['route']} — {loc['stop']} ({loc['status']})" for loc in locations])
    )

def main():
    """Запуск бота"""
    # В реальном проекте — загрузить из переменных окружения
    TOKEN = "YOUR_BOT_TOKEN_HERE"
    
    if TOKEN == "YOUR_BOT_TOKEN_HERE":
        print("⚠️  Укажите токен бота в переменной TOKEN!")
        print("1. Напишите @BotFather в Telegram")
        print("2. Создайте нового бота: /newbot")
        print("3. Скопируйте токен и вставьте в код")
        return
    
    application = Application.builder().token(TOKEN).build()
    
    # Обработчики
    application.add_handler(CommandHandler("start", start))
    application.add_handler(CommandHandler("status", status))
    application.add_handler(CommandHandler("locations", get_locations))
    application.add_handler(CallbackQueryHandler(button_handler))
    
    print("🚀 BusTime бот запущен!")
    print("Водители могут начать с команды /start")
    
    application.run_polling()

if __name__ == "__main__":
    main()

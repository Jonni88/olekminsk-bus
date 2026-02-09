#!/bin/bash

# build-apk.sh — скрипт сборки APK через Bubblewrap

echo "🚌 Сборка Android APK для Олёкминск Bus"
echo "========================================"
echo ""

# Проверяем Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js не установлен!"
    echo "Установите: https://nodejs.org/"
    exit 1
fi

# Устанавливаем Bubblewrap
echo "📦 Установка Bubblewrap..."
npm install -g @bubblewrap/cli

# Создаём проект
echo ""
echo "🔨 Создание Android проекта..."
mkdir -p bubblewrap-project
cd bubblewrap-project

# Инициализация (отвечаем на вопросы автоматически где возможно)
echo "Инициализация с manifest..."
bubblewrap init --manifest https://jonni88.github.io/olekminsk-bus/manifest.json \
    --package com.olekminsk.bus \
    --name "Автобусы Олёкминск"

# Сборка APK
echo ""
echo "📱 Сборка APK..."
bubblewrap build

echo ""
echo "✅ Готово!"
echo "APK находится в: bubblewrap-project/app-release-signed.apk"
echo ""
echo "Для установки на телефон:"
echo "  adb install app-release-signed.apk"

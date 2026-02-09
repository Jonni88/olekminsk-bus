#!/bin/bash
# deploy.sh — скрипт развёртывания на GitHub Pages

echo "🚌 Развёртывание Расписания автобусов Олёкминск"
echo "================================================"
echo ""

# Проверяем, что мы в правильной папке
if [ ! -f "index.html" ]; then
    echo "❌ Ошибка: index.html не найден"
    echo "Запусти скрипт из папки web/"
    exit 1
fi

# Создаём .nojekyll (для GitHub Pages)
touch .nojekyll

# Инициализируем git (если нужно)
if [ ! -d ".git" ]; then
    echo "📦 Инициализация git..."
    git init
    git checkout -b gh-pages
fi

# Добавляем файлы
echo "📁 Добавление файлов..."
git add .
git commit -m "Deploy bus schedule web app"

echo ""
echo "✅ Готово!"
echo ""
echo "Для публикации на GitHub Pages:"
echo "1. Создай репозиторий на GitHub"
echo "2. Выполни:"
echo "   git remote add origin https://github.com/USERNAME/REPO.git"
echo "   git push -u origin gh-pages"
echo ""
echo "3. В настройках репозитория включи GitHub Pages"
echo "   Source: Deploy from a branch → gh-pages"
echo ""
echo "🌐 После публикации сайт будет доступен по адресу:"
echo "   https://USERNAME.github.io/REPO/"

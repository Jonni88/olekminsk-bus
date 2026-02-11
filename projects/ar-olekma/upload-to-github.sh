#!/bin/bash
# Скрипт для публикации AR Олёкминск на GitHub Pages
# Запусти: bash upload-to-github.sh

echo "🚀 Публикация AR Олёкминск на GitHub"
echo ""

# Проверка git
if ! command -v git &> /dev/null; then
    echo "❌ Git не установлен. Установи: sudo apt install git"
    exit 1
fi

# Запрос данных
read -p "Твой GitHub username: " USERNAME
read -p "Название репозитория (ar-olekma): " REPO
REPO=${REPO:-ar-olekma}

cd /home/jonni88/.openclaw/workspace/projects/ar-olekma

# Инициализация
git init

# Настройка пользователя (если не настроено)
git config user.email "${USERNAME}@users.noreply.github.com" 2>/dev/null || true
git config user.name "$USERNAME" 2>/dev/null || true

# Добавление файлов
git add .

# Коммит
git commit -m "AR Олёкминск - исторические экскурсии в дополненной реальности

Функции:
- WebAR без установки приложения
- GPS-навигация по локациям
- Исторические фото и факты
- Интерактивные квесты
- Система баллов"

# Привязка к GitHub
git remote add origin "https://github.com/$USERNAME/$REPO.git" 2>/dev/null || git remote set-url origin "https://github.com/$USERNAME/$REPO.git"

# Пуш
echo ""
echo "📤 Загрузка на GitHub..."
git push -u origin main || git push -u origin master

echo ""
echo "✅ Готово!"
echo ""
echo "Теперь:"
echo "1. Зайди на https://github.com/$USERNAME/$REPO"
echo "2. Settings → Pages"
echo "3. Source: main → Save"
echo "4. Жди 2-5 минут"
echo ""
echo "🔗 Ссылка будет: https://$USERNAME.github.io/$REPO/demo.html"
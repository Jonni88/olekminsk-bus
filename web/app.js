/**
 * Расписание автобусов Олёкминск — Web App
 * Загружает данные из Google Sheets API
 */

// Конфигурация
const CONFIG = {
    // ID таблицы Google Sheets
    SPREADSHEET_ID: '1jNSVkXTohNjy2Ukpb2-IZMUbu7OKGJQ_G-eel60c-IE',
    // API Key для чтения (ограниченный доступ, только для этой таблицы)
    API_KEY: '', // Пользователь должен добавить свой API ключ
    // URL для загрузки данных
    SHEETS_URL: 'https://docs.google.com/spreadsheets/d/e/2PACX-1vQ/pubhtml',
    // Кэш в localStorage
    CACHE_KEY: 'bus_schedule_cache',
    CACHE_TIME_KEY: 'bus_schedule_time',
    // Время жизни кэша (5 минут)
    CACHE_TTL: 5 * 60 * 1000,
};

// Глобальные переменные
let appData = {
    routes: [],
    stops: [],
    schedule: [],
    exceptions: [],
    lastUpdate: null
};

// Текущее время
function updateCurrentTime() {
    const now = new Date();
    document.getElementById('currentTime').textContent = 
        now.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
    document.getElementById('currentDate').textContent = 
        now.toLocaleDateString('ru-RU', { weekday: 'short', day: 'numeric', month: 'short' });
}

// Определение типа дня
function getDayType() {
    const now = new Date();
    const day = now.getDay();
    const hour = now.getHours();
    const minute = now.getMinutes();
    
    // Проверяем исключения (праздники)
    const dateStr = now.toISOString().split('T')[0];
    const exception = appData.exceptions.find(e => e.Дата === dateStr);
    if (exception) {
        return { type: 'exception', label: exception.Тип, description: exception.Описание };
    }
    
    // Будни/выходные
    if (day === 0 || day === 6) {
        return { type: 'weekend', label: 'Выходной' };
    }
    return { type: 'weekday', label: 'Будний день' };
}

// Загрузка данных из кэша
function loadFromCache() {
    try {
        const cached = localStorage.getItem(CONFIG.CACHE_KEY);
        const cachedTime = localStorage.getItem(CONFIG.CACHE_TIME_KEY);
        
        if (!cached || !cachedTime) return null;
        
        const age = Date.now() - parseInt(cachedTime);
        if (age > CONFIG.CACHE_TTL) return null;
        
        return JSON.parse(cached);
    } catch (e) {
        return null;
    }
}

// Сохранение в кэш
function saveToCache(data) {
    try {
        localStorage.setItem(CONFIG.CACHE_KEY, JSON.stringify(data));
        localStorage.setItem(CONFIG.CACHE_TIME_KEY, Date.now().toString());
    } catch (e) {
        console.error('Ошибка сохранения кэша:', e);
    }
}

// Загрузка данных (основная функция)
async function loadData(force = false) {
    // Показываем загрузку
    document.getElementById('loading').classList.remove('hidden');
    document.getElementById('error').classList.add('hidden');
    
    // Пробуем загрузить из кэша
    if (!force) {
        const cached = loadFromCache();
        if (cached) {
            appData = cached;
            renderAll();
            document.getElementById('loading').classList.add('hidden');
            return;
        }
    }
    
    try {
        // Загружаем CSV данные (публичный экспорт)
        const [routesCsv, stopsCsv, scheduleCsv, exceptionsCsv] = await Promise.all([
            fetchCsv('Маршруты'),
            fetchCsv('Остановки'),
            fetchCsv('Расписание'),
            fetchCsv('Исключения')
        ]);
        
        // Парсим CSV
        appData.routes = parseCsv(routesCsv);
        appData.stops = parseCsv(stopsCsv);
        appData.schedule = parseCsv(scheduleCsv);
        appData.exceptions = parseCsv(exceptionsCsv);
        appData.lastUpdate = new Date();
        
        // Сохраняем в кэш
        saveToCache(appData);
        
        // Отображаем
        renderAll();
        document.getElementById('loading').classList.add('hidden');
        
    } catch (error) {
        console.error('Ошибка загрузки:', error);
        document.getElementById('loading').classList.add('hidden');
        document.getElementById('error').classList.remove('hidden');
    }
}

// Загрузка CSV из Google Sheets
async function fetchCsv(sheetName) {
    const url = `https://docs.google.com/spreadsheets/d/${CONFIG.SPREADSHEET_ID}/gviz/tq?tqx=out:csv&sheet=${encodeURIComponent(sheetName)}`;
    
    const response = await fetch(url);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    
    return await response.text();
}

// Парсинг CSV
function parseCsv(csv) {
    const lines = csv.trim().split('\n');
    if (lines.length < 2) return [];
    
    // Заголовки
    const headers = parseCsvLine(lines[0]);
    
    // Данные
    return lines.slice(1).map(line => {
        const values = parseCsvLine(line);
        const obj = {};
        headers.forEach((header, i) => {
            obj[header] = values[i] || '';
        });
        return obj;
    });
}

// Парсинг строки CSV (учёт кавычек)
function parseCsvLine(line) {
    const result = [];
    let current = '';
    let inQuotes = false;
    
    for (let i = 0; i < line.length; i++) {
        const char = line[i];
        
        if (char === '"') {
            if (inQuotes && line[i + 1] === '"') {
                current += '"';
                i++;
            } else {
                inQuotes = !inQuotes;
            }
        } else if (char === ',' && !inQuotes) {
            result.push(current.trim());
            current = '';
        } else {
            current += char;
        }
    }
    
    result.push(current.trim());
    return result;
}

// Отрисовка всего
function renderAll() {
    renderRoutes();
    renderStops();
    renderExceptions();
    updateLastUpdate();
}

// Отрисовка маршрутов
function renderRoutes() {
    const container = document.getElementById('routes-list');
    const dayType = getDayType();
    
    container.innerHTML = appData.routes.map(route => {
        const nextBuses = getNextBuses(route.ID, 3);
        
        return `
            <div class="route-card" onclick="showRouteSchedule('${route.ID}')">
                <div class="route-header">
                    <div class="route-number">${route.Номер}</div>
                    <div class="route-info">
                        <h3>${route.Название}</h3>
                        <p>${route.Описание || ''}</p>
                    </div>
                </div>
                <div class="route-next">
                    <span>Ближайшие:${dayType.type === 'exception' ? ' ⚠️ ' + dayType.label : ''}</span>
                    <div class="next-buses">
                        ${nextBuses.map((bus, i) => `
                            <span class="time-badge ${i === 0 ? 'soon' : ''}">${bus.Время}</span>
                        `).join('')}
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

// Получение ближайших автобусов
function getNextBuses(routeId, count = 3) {
    const now = new Date();
    const currentTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    
    const dayType = getDayType();
    let schedule = appData.schedule.filter(s => s.Маршрут_ID === routeId);
    
    // Фильтруем по дням
    if (dayType.type === 'exception') {
        // В праздники — выходное расписание
        schedule = schedule.filter(s => s.Дни.includes('Сб-Вс') || s.Дни.includes('выход'));
    } else if (dayType.type === 'weekend') {
        schedule = schedule.filter(s => s.Дни.includes('Сб-Вс') || s.Дни.includes('Ежедневно'));
    } else {
        schedule = schedule.filter(s => s.Дни.includes('Пн-Пт') || s.Дни.includes('Ежедневно'));
    }
    
    // Сортируем по времени и берём ближайшие
    return schedule
        .filter(s => s.Время > currentTime)
        .sort((a, b) => a.Время.localeCompare(b.Время))
        .slice(0, count);
}

// Показать расписание маршрута
function showRouteSchedule(routeId) {
    const route = appData.routes.find(r => r.ID === routeId);
    if (!route) return;
    
    document.getElementById('routes-list').classList.add('hidden');
    document.getElementById('route-schedule').classList.remove('hidden');
    document.getElementById('schedule-title').textContent = `Маршрут ${route.Номер}: ${route.Название}`;
    
    // Группируем расписание по остановкам
    const schedule = appData.schedule.filter(s => s.Маршрут_ID === routeId);
    const stopsInRoute = [...new Set(schedule.map(s => s.Остановка_ID))];
    
    const content = document.getElementById('schedule-content');
    const now = new Date();
    const currentTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    
    content.innerHTML = stopsInRoute.map(stopId => {
        const stop = appData.stops.find(s => s.ID === stopId);
        if (!stop) return '';
        
        const times = schedule
            .filter(s => s.Остановка_ID === stopId)
            .sort((a, b) => a.Время.localeCompare(b.Время));
        
        return `
            <div class="stop-section">
                <div class="stop-title">📍 ${stop.Название}</div>
                <div class="time-grid">
                    ${times.map(t => {
                        const isPast = t.Время < currentTime;
                        const isNext = !isPast && times.find(nt => !isPast && nt.Время === t.Время) === t;
                        return `
                            <div class="time-item ${isPast ? 'past' : ''} ${isNext ? 'next' : ''}">
                                ${t.Время}
                            </div>
                        `;
                    }).join('')}
                </div>
            </div>
        `;
    }).join('');
}

// Вернуться к списку маршрутов
function showRoutes() {
    document.getElementById('routes-list').classList.remove('hidden');
    document.getElementById('route-schedule').classList.add('hidden');
}

// Отрисовка остановок
function renderStops() {
    const container = document.getElementById('stops-list');
    
    container.innerHTML = appData.stops.map(stop => {
        const routeIds = stop.Маршруты.split(/[,;]/).map(id => id.trim());
        const routeNumbers = routeIds.map(id => {
            const route = appData.routes.find(r => r.ID === id || r.Номер === id);
            return route ? route.Номер : id;
        });
        
        return `
            <div class="stop-card">
                <h4>📍 ${stop.Название}</h4>
                <p style="color: var(--text-light); font-size: 0.875rem; margin-bottom: 8px;">${stop.Примечание || ''}</p>
                <div class="stop-routes">
                    ${routeNumbers.map(num => `
                        <span class="route-tag">Маршрут ${num}</span>
                    `).join('')}
                </div>
            </div>
        `;
    }).join('');
}

// Отрисовка исключений
function renderExceptions() {
    const container = document.getElementById('exceptions-list');
    
    if (appData.exceptions.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: var(--text-light); padding: 40px;">Нет запланированных изменений</p>';
        return;
    }
    
    // Сортируем по дате (будущие сначала)
    const sorted = [...appData.exceptions].sort((a, b) => 
        new Date(a.Дата) - new Date(b.Дата)
    );
    
    container.innerHTML = sorted.map(exc => {
        const date = new Date(exc.Дата);
        const dateStr = date.toLocaleDateString('ru-RU', { 
            weekday: 'long', 
            day: 'numeric', 
            month: 'long' 
        });
        
        return `
            <div class="exception-card">
                <div class="exception-date">${dateStr}</div>
                <p style="font-weight: 600; margin-top: 4px;">${exc.Тип}</p>
                <p style="color: var(--text-light); font-size: 0.875rem; margin-top: 4px;">${exc.Описание}</p>
            </div>
        `;
    }).join('');
}

// Обновление времени последнего обновления
function updateLastUpdate() {
    const el = document.getElementById('lastUpdate');
    if (appData.lastUpdate) {
        const date = new Date(appData.lastUpdate);
        el.textContent = `Обновлено: ${date.toLocaleString('ru-RU')}`;
    }
}

// Переключение табов
document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => {
        // Активный таб
        document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        
        // Показываем соответствующий контент
        const tabName = tab.dataset.tab;
        document.querySelectorAll('.tab-content').forEach(content => {
            content.classList.add('hidden');
        });
        document.getElementById(`${tabName}-tab`).classList.remove('hidden');
        
        // Если возвращаемся к маршрутам — показываем список
        if (tabName === 'routes') {
            showRoutes();
        }
    });
});

// PWA установка
let deferredPrompt;

window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault();
    deferredPrompt = e;
    document.getElementById('installPrompt').classList.add('show');
});

function installApp() {
    if (deferredPrompt) {
        deferredPrompt.prompt();
        deferredPrompt.userChoice.then((choiceResult) => {
            if (choiceResult.outcome === 'accepted') {
                console.log('Пользователь установил приложение');
            }
            document.getElementById('installPrompt').classList.remove('show');
            deferredPrompt = null;
        });
    }
}

// Service Worker для PWA
if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('sw.js').catch(err => {
        console.log('SW registration failed:', err);
    });
}

// Инициализация
updateCurrentTime();
setInterval(updateCurrentTime, 1000);
loadData();

// Обновление при возвращении на страницу
document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
        updateCurrentTime();
    }
});

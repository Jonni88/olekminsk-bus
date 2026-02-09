/**
 * 🚌 Расписание автобусов Олёкминск — Metro Style App
 * Дизайн в стиле Яндекс.Метро
 */

// === КОНФИГУРАЦИЯ ===
const CONFIG = {
    SPREADSHEET_ID: '1jNSVkXTohNjy2Ukpb2-IZMUbu7OKGJQ_G-eel60c-IE',
    CACHE_KEY: 'bus_metro_cache_v2',
    CACHE_TIME_KEY: 'bus_metro_time_v2',
    CACHE_TTL: 5 * 60 * 1000, // 5 минут
    AUTO_REFRESH: 60 * 60 * 1000, // 1 час
    ROUTE_COLORS: ['#ef4444', '#3b82f6', '#22c55e', '#a855f7', '#f97316', '#ec4899', '#14b8a6', '#f59e0b'],
};

// === СОСТОЯНИЕ ===
let appData = { routes: [], stops: [], schedule: [], exceptions: [], lastUpdate: null };
let currentTab = 'routes';
let favorites = JSON.parse(localStorage.getItem('bus_favorites') || '[]');

// === ИНИЦИАЛИЗАЦИЯ ===
document.addEventListener('DOMContentLoaded', () => {
    updateTime();
    setInterval(updateTime, 1000);
    setupNavigation();
    setupSearch();
    setupFilters();
    loadData();
    
    // Автообновление
    setInterval(() => loadData(true), CONFIG.AUTO_REFRESH);
});

// === ВРЕМЯ ===
function updateTime() {
    const now = new Date();
    document.getElementById('currentTime').textContent = 
        now.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
    document.getElementById('currentDate').textContent = 
        now.toLocaleDateString('ru-RU', { weekday: 'long', day: 'numeric', month: 'long' });
}

// === ЗАГРУЗКА ДАННЫХ ===
async function loadData(force = false) {
    const loadingScreen = document.getElementById('loadingScreen');
    
    // Проверяем кэш
    if (!force) {
        const cached = localStorage.getItem(CONFIG.CACHE_KEY);
        const cachedTime = localStorage.getItem(CONFIG.CACHE_TIME_KEY);
        
        if (cached && cachedTime) {
            const age = Date.now() - parseInt(cachedTime);
            if (age < CONFIG.CACHE_TTL) {
                appData = JSON.parse(cached);
                renderAll();
                loadingScreen.classList.add('hidden');
                return;
            }
        }
    }
    
    loadingScreen.classList.remove('hidden');
    
    try {
        const [routes, stops, schedule, exceptions] = await Promise.all([
            fetchSheet('Маршруты'),
            fetchSheet('Остановки'),
            fetchSheet('Расписание'),
            fetchSheet('Исключения')
        ]);
        
        appData = { routes, stops, schedule, exceptions, lastUpdate: new Date() };
        
        // Сохраняем в кэш
        localStorage.setItem(CONFIG.CACHE_KEY, JSON.stringify(appData));
        localStorage.setItem(CONFIG.CACHE_TIME_KEY, Date.now().toString());
        
        renderAll();
        showUpdateToast();
        
    } catch (error) {
        console.error('Ошибка загрузки:', error);
        // Пробуем загрузить из кэша даже если устарел
        const cached = localStorage.getItem(CONFIG.CACHE_KEY);
        if (cached) {
            appData = JSON.parse(cached);
            renderAll();
        }
    } finally {
        loadingScreen.classList.add('hidden');
    }
}

async function fetchSheet(sheetName) {
    const url = `https://docs.google.com/spreadsheets/d/${CONFIG.SPREADSHEET_ID}/gviz/tq?tqx=out:csv&sheet=${encodeURIComponent(sheetName)}`;
    const response = await fetch(url);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return parseCSV(await response.text());
}

function parseCSV(csv) {
    const lines = csv.trim().split('\n');
    if (lines.length < 2) return [];
    
    const headers = parseCSVLine(lines[0]);
    return lines.slice(1).map(line => {
        const values = parseCSVLine(line);
        const obj = {};
        headers.forEach((h, i) => obj[h] = values[i] || '');
        return obj;
    });
}

function parseCSVLine(line) {
    const result = [];
    let current = '', inQuotes = false;
    
    for (let i = 0; i < line.length; i++) {
        const char = line[i];
        if (char === '"') {
            if (inQuotes && line[i + 1] === '"') {
                current += '"'; i++;
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

// === ОТРИСОВКА ===
function renderAll() {
    renderRoutes();
    renderStops();
    updateLastUpdate();
}

function renderRoutes() {
    const container = document.getElementById('routesList');
    const now = new Date();
    const currentTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    
    container.innerHTML = appData.routes.map((route, index) => {
        const color = CONFIG.ROUTE_COLORS[index % CONFIG.ROUTE_COLORS.length];
        const nextBuses = getNextBuses(route.ID, 3);
        const nextTime = nextBuses[0]?.Время;
        const timeDiff = nextTime ? getMinutesDiff(currentTime, nextTime) : null;
        
        let timeClass = '';
        let timeText = nextTime || '--:--';
        
        if (timeDiff !== null) {
            if (timeDiff <= 5) {
                timeClass = 'urgent';
                timeText = `${timeDiff} мин`;
            } else if (timeDiff <= 15) {
                timeClass = 'soon';
                timeText = `${timeDiff} мин`;
            }
        }
        
        return `
            <div class="route-metro-card" onclick="openRouteDetail('${route.ID}')"
                 style="border-left: 4px solid ${color}">
                <div class="route-metro-header">
                    <div class="route-line" style="background: ${color}">${route.Номер}</div>
                    <div class="route-metro-info">
                        <h3>${route.Название}</h3>
                        <p>${route.Описание || 'Обычный маршрут'}</p>
                    </div>
                    <div class="route-status">
                        <div class="next-time ${timeClass}">${timeText}</div>
                        <div class="status-text">${nextBuses.length > 0 ? 'до прибытия' : 'нет рейсов'}</div>
                    </div>
                </div>
                <div class="route-timeline">
                    ${nextBuses.slice(1).map((b, i) => `
                        <span class="time-pill ${i === 0 ? 'next' : ''}">${b.Время}</span>
                    `).join('')}
                </div>
            </div>
        `;
    }).join('');
}

function getNextBuses(routeId, count) {
    const now = new Date();
    const currentTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    
    const isWeekend = now.getDay() === 0 || now.getDay() === 6;
    const dateStr = now.toISOString().split('T')[0];
    const exception = appData.exceptions.find(e => e.Дата === dateStr);
    
    let schedule = appData.schedule.filter(s => s.Маршрут_ID === routeId);
    
    // Фильтруем по дням
    if (exception) {
        schedule = schedule.filter(s => s.Дни.includes('Сб-Вс') || s.Дни.includes('выход') || s.Дни.includes('Ежедневно'));
    } else if (isWeekend) {
        schedule = schedule.filter(s => s.Дни.includes('Сб-Вс') || s.Дни.includes('Ежедневно'));
    } else {
        schedule = schedule.filter(s => s.Дни.includes('Пн-Пт') || s.Дни.includes('Ежедневно'));
    }
    
    return schedule
        .filter(s => s.Время > currentTime)
        .sort((a, b) => a.Время.localeCompare(b.Время))
        .slice(0, count);
}

function getMinutesDiff(time1, time2) {
    const [h1, m1] = time1.split(':').map(Number);
    const [h2, m2] = time2.split(':').map(Number);
    return (h2 * 60 + m2) - (h1 * 60 + m1);
}

function renderStops() {
    const container = document.getElementById('stopsGrid');
    
    container.innerHTML = appData.stops.map(stop => {
        const routeIds = stop.Маршруты.split(/[,;]/).map(id => id.trim());
        
        return `
            <div class="stop-card-metro" onclick="openStopDetail('${stop.ID}')">
                <h4>📍 ${stop.Название}</h4>
                <div class="routes-dots">
                    ${routeIds.map((id, i) => {
                        const color = CONFIG.ROUTE_COLORS[(parseInt(id) - 1) % CONFIG.ROUTE_COLORS.length];
                        return `<span class="route-dot" style="background: ${color}"></span>`;
                    }).join('')}
                </div>
            </div>
        `;
    }).join('');
}

// === ДЕТАЛИ ===
function openRouteDetail(routeId) {
    const route = appData.routes.find(r => r.ID === routeId);
    if (!route) return;
    
    document.getElementById('detailTitle').textContent = `Маршрут ${route.Номер}`;
    
    const stops = [...new Set(appData.schedule.filter(s => s.Маршрут_ID === routeId).map(s => s.Остановка_ID))];
    
    document.getElementById('detailContent').innerHTML = stops.map(stopId => {
        const stop = appData.stops.find(s => s.ID === stopId);
        if (!stop) return '';
        
        const times = appData.schedule
            .filter(s => s.Маршрут_ID === routeId && s.Остановка_ID === stopId)
            .sort((a, b) => a.Время.localeCompare(b.Время));
        
        return `
            <div class="stop-item">
                <div class="stop-marker"></div>
                <div class="stop-info">
                    <h4>${stop.Название}</h4>
                    <div class="stop-times">
                        ${times.map(t => `
                            <span class="time-pill">${t.Время}</span>
                        `).join('')}
                    </div>
                </div>
            </div>
        `;
    }).join('');
    
    document.getElementById('detailView').classList.add('open');
}

function openStopDetail(stopId) {
    const stop = appData.stops.find(s => s.ID === stopId);
    if (!stop) return;
    
    document.getElementById('detailTitle').textContent = stop.Название;
    
    const routeIds = stop.Маршруты.split(/[,;]/).map(id => id.trim());
    
    document.getElementById('detailContent').innerHTML = routeIds.map(id => {
        const route = appData.routes.find(r => r.ID === id || r.Номер === id);
        if (!route) return '';
        
        const nextBuses = getNextBuses(route.ID, 5);
        
        return `
            <div class="route-metro-card" style="margin-bottom: 12px;">
                <div class="route-metro-header">
                    <div class="route-line">${route.Номер}</div>
                    <div class="route-metro-info">
                        <h3>${route.Название}</h3>
                    </div>
                </div>
                <div class="route-timeline">
                    ${nextBuses.map(b => `
                        <span class="time-pill">${b.Время}</span>
                    `).join('')}
                </div>
            </div>
        `;
    }).join('');
    
    document.getElementById('detailView').classList.add('open');
}

function closeDetail() {
    document.getElementById('detailView').classList.remove('open');
}

// === НАВИГАЦИЯ ===
function setupNavigation() {
    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', () => {
            const nav = item.dataset.nav;
            switchTab(nav);
            
            document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
            item.classList.add('active');
        });
    });
}

function switchTab(tab) {
    currentTab = tab;
    document.querySelectorAll('.tab-content').forEach(t => t.classList.add('hidden'));
    document.getElementById(`${tab}Tab`).classList.remove('hidden');
}

// === ПОИСК ===
function setupSearch() {
    const searchInput = document.getElementById('searchInput');
    
    searchInput.addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase();
        
        if (query.length < 2) {
            renderAll();
            return;
        }
        
        // Фильтруем маршруты
        const filteredRoutes = appData.routes.filter(r => 
            r.Номер.toLowerCase().includes(query) ||
            r.Название.toLowerCase().includes(query) ||
            r.Описание?.toLowerCase().includes(query)
        );
        
        renderFilteredRoutes(filteredRoutes);
    });
}

function renderFilteredRoutes(routes) {
    const container = document.getElementById('routesList');
    // ... (та же логика что и в renderRoutes, но с filtered массивом)
    container.innerHTML = routes.length === 0 
        ? '<div style="text-align: center; padding: 40px; color: var(--text-secondary)">Ничего не найдено</div>'
        : routes.map((route, index) => {
            const color = CONFIG.ROUTE_COLORS[index % CONFIG.ROUTE_COLORS.length];
            return `
                <div class="route-metro-card" onclick="openRouteDetail('${route.ID}')"
                     style="border-left: 4px solid ${color}">
                    <div class="route-metro-header">
                        <div class="route-line" style="background: ${color}">${route.Номер}</div>
                        <div class="route-metro-info">
                            <h3>${route.Название}</h3>
                            <p>${route.Описание || ''}</p>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
}

// === ФИЛЬТРЫ ===
function setupFilters() {
    document.querySelectorAll('.filter-chip').forEach(chip => {
        chip.addEventListener('click', () => {
            document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            
            const filter = chip.dataset.filter;
            applyFilter(filter);
        });
    });
}

function applyFilter(filter) {
    switch(filter) {
        case 'now':
            // Показываем только маршруты с ближайшими рейсами
            const routesWithBuses = appData.routes.filter(r => getNextBuses(r.ID, 1).length > 0);
            renderFilteredRoutes(routesWithBuses);
            break;
        case 'favorites':
            const favRoutes = appData.routes.filter(r => favorites.includes(r.ID));
            renderFilteredRoutes(favRoutes);
            break;
        default:
            renderRoutes();
    }
}

// === УТИЛИТЫ ===
function updateLastUpdate() {
    const el = document.getElementById('lastUpdateInfo');
    if (appData.lastUpdate) {
        const date = new Date(appData.lastUpdate);
        el.textContent = `Обновлено: ${date.toLocaleString('ru-RU')}`;
    }
}

function showUpdateToast() {
    const toast = document.getElementById('updateToast');
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 3000);
}

// === PWA ===
if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('sw.js').catch(console.error);
}

// Закрытие детали по свайпу
let touchStartY = 0;
document.getElementById('detailView').addEventListener('touchstart', e => {
    touchStartY = e.touches[0].clientY;
});

document.getElementById('detailView').addEventListener('touchend', e => {
    const touchEndY = e.changedTouches[0].clientY;
    if (touchEndY - touchStartY > 100) {
        closeDetail();
    }
});

/**
 * Расписание автобусов Олёкминск — С Google Sheets
 */

// === КОНФИГУРАЦИЯ ===
const CONFIG = {
    SPREADSHEET_ID: '1jNSVkXTohNjy2Ukpb2-IZMUbu7OKGJQ_G-eel60c-IE',
    CACHE_KEY: 'bus_schedule_cache_v3',
    CACHE_TIME_KEY: 'bus_schedule_time_v3',
    CACHE_TTL: 5 * 60 * 1000, // 5 минут
};

// === ДАННЫЕ ===
let scheduleData = {
    route1: { weekday: {}, saturday: {}, sunday: {} },
    route5: { weekday: {}, saturday: {}, sunday: {} },
    suburban: {},
    stops: []
};

// === ИНИЦИАЛИЗАЦИЯ ===
document.addEventListener('DOMContentLoaded', () => {
    updateTime();
    setInterval(updateTime, 1000);
    setupTabs();
    setupDayTabs();
    loadData();
    setInterval(() => loadData(true), 60 * 60 * 1000); // Обновление каждый час
});

// === ВРЕМЯ ===
function updateTime() {
    const now = new Date();
    document.getElementById('currentTime').textContent = 
        now.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
}

// === ЗАГРУЗКА ДАННЫХ ИЗ GOOGLE SHEETS ===
async function loadData(force = false) {
    // Проверяем кэш
    if (!force) {
        const cached = localStorage.getItem(CONFIG.CACHE_KEY);
        const cachedTime = localStorage.getItem(CONFIG.CACHE_TIME_KEY);
        
        if (cached && cachedTime) {
            const age = Date.now() - parseInt(cachedTime);
            if (age < CONFIG.CACHE_TTL) {
                scheduleData = JSON.parse(cached);
                updateAllTimes();
                renderStops();
                return;
            }
        }
    }
    
    try {
        // Загружаем все листы
        const [route1Weekday, route1Saturday, route1Sunday,
               route5Weekday, route5Saturday, route5Sunday,
               suburban, stops] = await Promise.all([
            fetchSheet('Маршрут1_Будни'),
            fetchSheet('Маршрут1_Суббота'),
            fetchSheet('Маршрут1_Воскресенье'),
            fetchSheet('Маршрут5_Будни'),
            fetchSheet('Маршрут5_Суббота'),
            fetchSheet('Маршрут5_Воскресенье'),
            fetchSheet('Пригород'),
            fetchSheet('Остановки')
        ]);
        
        // Преобразуем данные
        scheduleData.route1.weekday = parseRouteData(route1Weekday, 'Маршрут №1 — С автовокзала (Пн-Пт)', 'Маршрут №1 — С дачи (Пн-Пт)');
        scheduleData.route1.saturday = parseRouteData(route1Saturday, 'Маршрут №1 — С автовокзала (Суббота)', 'Маршрут №1 — С дачи (Суббота)');
        scheduleData.route1.sunday = parseRouteData(route1Sunday, 'Маршрут №1 — С автовокзала (Вск/Праздники)', 'Маршрут №1 — С дачи (Вск/Праздники)');
        
        scheduleData.route5.weekday = parseRouteData(route5Weekday, 'Маршрут №5 — С автовокзала (Пн-Пт)', 'Маршрут №5 — С ПНДИ (Пн-Пт)');
        scheduleData.route5.saturday = parseRouteData(route5Saturday, 'Маршрут №5 — С автовокзала (Суббота)', 'Маршрут №5 — С ПНДИ (Суббота)');
        scheduleData.route5.sunday = parseRouteData(route5Sunday, 'Маршрут №5 — С автовокзала (Вск/Праздники)', 'Маршрут №5 — С ПНДИ (Вск/Праздники)');
        
        scheduleData.suburban = parseSuburbanData(suburban);
        scheduleData.stops = parseStopsData(stops);
        
        // Сохраняем в кэш
        localStorage.setItem(CONFIG.CACHE_KEY, JSON.stringify(scheduleData));
        localStorage.setItem(CONFIG.CACHE_TIME_KEY, Date.now().toString());
        
        updateAllTimes();
        renderStops();
        
    } catch (error) {
        console.error('Ошибка загрузки:', error);
        // Используем кэш если есть
        const cached = localStorage.getItem(CONFIG.CACHE_KEY);
        if (cached) {
            scheduleData = JSON.parse(cached);
            updateAllTimes();
            renderStops();
        }
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

function parseRouteData(data, forwardName, backName) {
    const forward = data.filter(r => r.Направление === 'forward' || r.Направление === 'туда').map(r => r.Время).filter(Boolean);
    const back = data.filter(r => r.Направление === 'back' || r.Направление === 'обратно').map(r => r.Время).filter(Boolean);
    
    return {
        forward: { name: forwardName, times: forward.sort() },
        back: { name: backName, times: back.sort() }
    };
}

function parseSuburbanData(data) {
    const result = {};
    data.forEach(row => {
        if (row.Направление === 'yakutsk' || row.Направление === 'Якутск') {
            result.yakutsk = {
                name: row.Название || 'Олёкминск → Якутск',
                times: row.Время ? row.Время.split(',').map(t => t.trim()) : [],
                price: row.Цена || '1200₽'
            };
        } else if (row.Направление === 'olekminsk' || row.Направление === 'Олёкминск') {
            result.olekminsk = {
                name: row.Название || 'Якутск → Олёкминск',
                times: row.Время ? row.Время.split(',').map(t => t.trim()) : [],
                price: row.Цена || '1200₽'
            };
        }
    });
    return result;
}

function parseStopsData(data) {
    return data.map(row => ({
        name: row.Название || row.Остановка,
        routes: (row.Маршруты || '').split(/[,;]/).map(r => r.trim()).filter(Boolean)
    }));
}

// === ТАБЫ ===
function setupTabs() {
    document.querySelectorAll('.tab').forEach(tab => {
        tab.addEventListener('click', () => {
            document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
            
            tab.classList.add('active');
            document.getElementById(tab.dataset.tab + 'Tab').classList.add('active');
        });
    });
}

function setupDayTabs() {
    document.querySelectorAll('.day-tab').forEach(tab => {
        tab.addEventListener('click', () => {
            const day = tab.dataset.day;
            
            document.querySelectorAll('.day-tab').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.day-content').forEach(c => c.classList.remove('active'));
            
            tab.classList.add('active');
            document.getElementById(day + 'Content').classList.add('active');
            
            updateAllTimes();
        });
    });
}

// === ОБНОВЛЕНИЕ ВРЕМЕНИ ===
function updateAllTimes() {
    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();
    const dayOfWeek = now.getDay();
    
    let currentDayType = 'weekday';
    if (dayOfWeek === 6) currentDayType = 'saturday';
    if (dayOfWeek === 0) currentDayType = 'sunday';
    
    ['weekday', 'saturday', 'sunday'].forEach(dayType => {
        ['1', '5'].forEach(routeNum => {
            ['forward', 'back'].forEach(direction => {
                const elementId = `time-${routeNum}-${dayType === 'weekday' ? 'weekday' : dayType === 'saturday' ? 'sat' : 'sun'}-${direction}`;
                const route = scheduleData[`route${routeNum}`][dayType];
                if (route && route[direction]) {
                    updateTimeDisplay(elementId, route[direction].times, currentMinutes, dayType === currentDayType);
                }
            });
        });
    });
}

function updateTimeDisplay(elementId, times, currentMinutes, isCurrentDay) {
    const el = document.getElementById(elementId);
    if (!el || !times) return;
    
    const nextBus = times.find(time => {
        if (!time) return false;
        const [h, m] = time.split(':').map(Number);
        return h * 60 + m > currentMinutes;
    });
    
    if (nextBus) {
        const [h, m] = nextBus.split(':').map(Number);
        const busMinutes = h * 60 + m;
        const diff = busMinutes - currentMinutes;
        
        if (isCurrentDay) {
            el.textContent = diff;
            el.className = 'time-main' + (diff <= 5 ? ' urgent' : '');
        } else {
            el.textContent = nextBus;
            el.className = 'time-main';
        }
    } else {
        el.textContent = isCurrentDay ? '—' : (times[0] || '—');
        el.className = 'time-main';
    }
}

// === ОТРИСОВКА ОСТАНОВОК ===
function renderStops() {
    const container = document.getElementById('stopsList');
    if (!container || !scheduleData.stops.length) return;
    
    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();
    const dayOfWeek = now.getDay();
    
    let dayType = 'weekday';
    if (dayOfWeek === 6) dayType = 'saturday';
    if (dayOfWeek === 0) dayType = 'sunday';
    
    container.innerHTML = scheduleData.stops.map(stop => {
        let nearestBus = null;
        let nearestTime = Infinity;
        let nearestRoute = '';
        
        stop.routes.forEach(routeNum => {
            const route = scheduleData[`route${routeNum}`][dayType];
            if (route) {
                ['forward', 'back'].forEach(dir => {
                    if (route[dir] && route[dir].times) {
                        route[dir].times.forEach(time => {
                            if (!time) return;
                            const [h, m] = time.split(':').map(Number);
                            const busMinutes = h * 60 + m;
                            if (busMinutes > currentMinutes && busMinutes < nearestTime) {
                                nearestTime = busMinutes;
                                nearestBus = time;
                                nearestRoute = routeNum;
                            }
                        });
                    }
                });
            }
        });
        
        const diff = nearestBus ? nearestTime - currentMinutes : null;
        
        return `
            <div class="stop-card" onclick="showStopDetail('${stop.name}')">
                <div class="stop-header">
                    <span class="stop-name">${stop.name}</span>
                    <div class="stop-next">
                        ${nearestBus ? `
                            <div class="stop-next-time">${nearestBus}</div>
                            <div class="stop-next-route">Маршрут ${nearestRoute} · ${diff} мин</div>
                        ` : `
                            <div class="stop-next-time">—</div>
                            <div class="stop-next-route">Нет рейсов</div>
                        `}
                    </div>
                </div>
                <div class="stop-routes">
                    ${stop.routes.map(r => `<span class="route-pill route-${r}">${r}</span>`).join('')}
                </div>
            </div>
        `;
    }).join('');
}

// === ПОКАЗАТЬ РАСПИСАНИЕ ===
function showSchedule(routeNum, dayType, direction) {
    const route = scheduleData[`route${routeNum}`][dayType];
    if (!route || !route[direction]) return;
    
    const data = route[direction];
    document.getElementById('detailTitle').textContent = data.name;
    
    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();
    
    document.getElementById('detailContent').innerHTML = data.times.map(time => {
        if (!time) return '';
        const [h, m] = time.split(':').map(Number);
        const busMinutes = h * 60 + m;
        const isPast = busMinutes < currentMinutes;
        const diff = busMinutes - currentMinutes;
        
        return `
            <div class="schedule-item" style="opacity: ${isPast ? 0.4 : 1}">
                <span class="schedule-time">${time}</span>
                ${!isPast && diff <= 60 ? `<span style="color: var(--accent)">${diff} мин</span>` : ''}
            </div>
        `;
    }).join('');
    
    document.getElementById('detailView').classList.add('open');
}

// === ПРИГОРОД ===
function showSuburban(direction) {
    const data = scheduleData.suburban[direction];
    if (!data) return;
    
    document.getElementById('detailTitle').textContent = data.name;
    
    document.getElementById('detailContent').innerHTML = `
        <div style="padding: 16px; background: var(--bg-card); border-radius: 12px; margin-bottom: 16px;">
            <div style="font-size: 15px; color: var(--text-secondary); margin-bottom: 4px;">Стоимость билета</div>
            <div style="font-size: 28px; font-weight: 600;">${data.price}</div>
        </div>
        <div style="font-size: 13px; color: var(--text-secondary); margin-bottom: 12px; text-transform: uppercase;">Расписание</div>
        ${data.times.map(time => `
            <div class="schedule-item">
                <span class="schedule-time">${time}</span>
            </div>
        `).join('')}
    `;
    
    document.getElementById('detailView').classList.add('open');
}

// === ОСТАНОВКА ===
function showStopDetail(stopName) {
    const stop = scheduleData.stops.find(s => s.name === stopName);
    if (!stop) return;
    
    document.getElementById('detailTitle').textContent = stopName;
    
    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();
    const dayOfWeek = now.getDay();
    
    let dayType = 'weekday';
    if (dayOfWeek === 6) dayType = 'saturday';
    if (dayOfWeek === 0) dayType = 'sunday';
    
    let html = `<div style="margin-bottom: 16px;">
        <span style="font-size: 13px; color: var(--text-secondary);">${dayType === 'weekday' ? 'Понедельник — Пятница' : dayType === 'saturday' ? 'Суббота' : 'Воскресенье / Праздники'}</span>
    </div>`;
    
    stop.routes.forEach(routeNum => {
        const route = scheduleData[`route${routeNum}`][dayType];
        if (!route) return;
        
        ['forward', 'back'].forEach(dir => {
            if (!route[dir]) return;
            const direction = route[dir];
            const dirName = dir === 'forward' ? 'С автовокзала' : routeNum === '1' ? 'С дачи' : 'С ПНДИ';
            
            html += `<div style="margin-bottom: 20px;">`;
            html += `<div style="font-size: 13px; color: var(--text-secondary); margin-bottom: 8px;">Маршрут ${routeNum} — ${dirName}</div>`;
            
            html += `<div style="display: flex; flex-wrap: wrap; gap: 8px;">`;
            direction.times.forEach(time => {
                if (!time) return;
                const [h, m] = time.split(':').map(Number);
                const busMinutes = h * 60 + m;
                const isPast = busMinutes < currentMinutes;
                const diff = busMinutes - currentMinutes;
                
                html += `
                    <div style="
                        padding: 8px 12px; 
                        background: var(--bg-card); 
                        border-radius: 8px;
                        opacity: ${isPast ? 0.4 : 1};
                        ${!isPast && diff <= 30 ? 'border: 1px solid var(--accent);' : ''}
                    ">
                        <div style="font-weight: 500;">${time}</div>
                        ${!isPast && diff <= 30 ? `<div style="font-size: 11px; color: var(--accent);">${diff} мин</div>` : ''}
                    </div>
                `;
            });
            html += `</div></div>`;
        });
    });
    
    document.getElementById('detailContent').innerHTML = html;
    document.getElementById('detailView').classList.add('open');
}

// === ЗАКРЫТИЕ ===
function closeDetail() {
    document.getElementById('detailView').classList.remove('open');
}

// === PWA ===
if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('sw.js').catch(console.error);
}

// Свайп для закрытия
let touchStartX = 0;
document.getElementById('detailView').addEventListener('touchstart', e => {
    touchStartX = e.touches[0].clientX;
});

document.getElementById('detailView').addEventListener('touchend', e => {
    const touchEndX = e.changedTouches[0].clientX;
    if (touchEndX - touchStartX > 100) {
        closeDetail();
    }
});

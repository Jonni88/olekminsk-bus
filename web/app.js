/**
 * Расписание автобусов Олёкминск — Упрощённая версия
 */

// === ДАННЫЕ РАСПИСАНИЯ ===
const SCHEDULE_DATA = {
    // Маршрут 1: Автовокзал ⇄ Дача
    route1: {
        forward: {  // Автовокзал → Дача
            name: "Автовокзал → Дача",
            stops: ["Автовокзал", "Центр", "Школа №1", "Дача"],
            times: ["06:00", "06:30", "07:00", "07:30", "08:00", "08:30", "09:00", "10:00", 
                    "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00"]
        },
        back: {  // Дача → Автовокзал
            name: "Дача → Автовокзал",
            stops: ["Дача", "Школа №1", "Центр", "Автовокзал"],
            times: ["06:15", "06:45", "07:15", "07:45", "08:15", "08:45", "09:15", "10:15",
                    "11:15", "12:15", "13:15", "14:15", "15:15", "16:15", "17:15", "18:15", "19:15", "20:15"]
        }
    },
    
    // Маршрут 5: Автовокзал ⇄ ПНДИ
    route5: {
        forward: {  // Автовокзал → ПНДИ
            name: "Автовокзал → ПНДИ",
            stops: ["Автовокзал", "Центральная площадь", "Больница", "ПНДИ"],
            times: ["06:20", "07:00", "07:40", "08:20", "09:00", "10:00", "11:00", "12:00",
                    "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00"]
        },
        back: {  // ПНДИ → Автовокзал
            name: "ПНДИ → Автовокзал",
            stops: ["ПНДИ", "Больница", "Центральная площадь", "Автовокзал"],
            times: ["06:35", "07:15", "07:55", "08:35", "09:15", "10:15", "11:15", "12:15",
                    "13:15", "14:15", "15:15", "16:15", "17:15", "18:15", "19:15"]
        }
    },
    
    // Пригород
    suburban: {
        yakutsk: {
            name: "Олёкминск → Якутск",
            times: ["07:00", "12:00", "18:00"],
            price: "1200₽"
        },
        olekminsk: {
            name: "Якутск → Олёкминск",
            times: ["08:00", "14:00", "19:00"],
            price: "1200₽"
        }
    },
    
    // Остановки
    stops: [
        { name: "Автовокзал", routes: ["1", "5"], times: { "1": ["06:00", "06:30"], "5": ["06:20", "07:00"] } },
        { name: "Центр", routes: ["1"], times: { "1": ["06:05", "06:35"] } },
        { name: "Центральная площадь", routes: ["5"], times: { "5": ["06:25", "07:05"] } },
        { name: "Школа №1", routes: ["1"], times: { "1": ["06:10", "06:40"] } },
        { name: "Больница", routes: ["5"], times: { "5": ["06:30", "07:10"] } },
        { name: "Дача", routes: ["1"], times: { "1": ["06:15", "06:45"] } },
        { name: "ПНДИ", routes: ["5"], times: { "5": ["06:35", "07:15"] } }
    ]
};

// === ИНИЦИАЛИЗАЦИЯ ===
document.addEventListener('DOMContentLoaded', () => {
    updateTime();
    setInterval(updateTime, 1000);
    setupTabs();
    updateRouteTimes();
    renderStops();
    setInterval(updateRouteTimes, 60000); // Обновляем каждую минуту
});

// === ВРЕМЯ ===
function updateTime() {
    const now = new Date();
    document.getElementById('currentTime').textContent = 
        now.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
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

// === ОБНОВЛЕНИЕ ВРЕМЕНИ МАРШРУТОВ ===
function updateRouteTimes() {
    const now = new Date();
    const currentTime = now.getHours() * 60 + now.getMinutes();
    
    // Маршрут 1
    updateDirectionTime('1-forward', SCHEDULE_DATA.route1.forward.times, currentTime);
    updateDirectionTime('1-back', SCHEDULE_DATA.route1.back.times, currentTime);
    
    // Маршрут 5
    updateDirectionTime('5-forward', SCHEDULE_DATA.route5.forward.times, currentTime);
    updateDirectionTime('5-back', SCHEDULE_DATA.route5.back.times, currentTime);
}

function updateDirectionTime(elementId, times, currentMinutes) {
    const el = document.getElementById('time-' + elementId);
    if (!el) return;
    
    const nextBus = times.find(time => {
        const [h, m] = time.split(':').map(Number);
        return h * 60 + m > currentMinutes;
    });
    
    if (nextBus) {
        const [h, m] = nextBus.split(':').map(Number);
        const busMinutes = h * 60 + m;
        const diff = busMinutes - currentMinutes;
        
        el.textContent = diff;
        el.className = 'time-main' + (diff <= 5 ? ' urgent' : '');
        
        // Добавляем время отправления в data-атрибут
        el.dataset.time = nextBus;
    } else {
        el.textContent = '—';
        el.className = 'time-main';
    }
}

// === ОТРИСОВКА ОСТАНОВОК ===
function renderStops() {
    const container = document.getElementById('stopsList');
    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();
    
    container.innerHTML = SCHEDULE_DATA.stops.map(stop => {
        // Находим ближайший автобус
        let nearestBus = null;
        let nearestTime = Infinity;
        let nearestRoute = '';
        
        stop.routes.forEach(routeNum => {
            const route = routeNum === '1' ? SCHEDULE_DATA.route1 : SCHEDULE_DATA.route5;
            
            // Проверяем оба направления
            [route.forward, route.back].forEach(direction => {
                if (direction.stops.includes(stop.name)) {
                    const stopIndex = direction.stops.indexOf(stop.name);
                    direction.times.forEach(time => {
                        const [h, m] = time.split(':').map(Number);
                        // Прибавляем время до остановки (примерно 5 мин на остановку)
                        const busMinutes = h * 60 + m + (stopIndex * 5);
                        if (busMinutes > currentMinutes && busMinutes < nearestTime) {
                            nearestTime = busMinutes;
                            nearestBus = time;
                            nearestRoute = routeNum;
                        }
                    });
                }
            });
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

// === ДЕТАЛИ МАРШРУТА ===
function showRouteDetail(routeNum, direction) {
    const route = routeNum === '1' ? SCHEDULE_DATA.route1 : SCHEDULE_DATA.route5;
    const data = direction === 'forward' ? route.forward : route.back;
    
    document.getElementById('detailTitle').textContent = data.name;
    
    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();
    
    document.getElementById('detailContent').innerHTML = data.times.map(time => {
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

// === ДЕТАЛИ ПРИГОРОДА ===
function showSuburbanDetail(direction) {
    const data = direction === 'yakutsk' ? SCHEDULE_DATA.suburban.yakutsk : SCHEDULE_DATA.suburban.olekminsk;
    
    document.getElementById('detailTitle').textContent = data.name;
    
    document.getElementById('detailContent').innerHTML = `
        <div style="padding: 16px; background: var(--bg-card); border-radius: 12px; margin-bottom: 16px;">
            <div style="font-size: 15px; color: var(--text-secondary); margin-bottom: 4px;">Стоимость</div>
            <div style="font-size: 24px; font-weight: 600;">${data.price}</div>
        </div>
        ${data.times.map(time => `
            <div class="schedule-item">
                <span class="schedule-time">${time}</span>
            </div>
        `).join('')}
    `;
    
    document.getElementById('detailView').classList.add('open');
}

// === ДЕТАЛИ ОСТАНОВКИ ===
function showStopDetail(stopName) {
    const stop = SCHEDULE_DATA.stops.find(s => s.name === stopName);
    if (!stop) return;
    
    document.getElementById('detailTitle').textContent = stop.name;
    
    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();
    
    let html = '';
    
    stop.routes.forEach(routeNum => {
        const route = routeNum === '1' ? SCHEDULE_DATA.route1 : SCHEDULE_DATA.route5;
        
        [route.forward, route.back].forEach(direction => {
            if (direction.stops.includes(stopName)) {
                const stopIndex = direction.stops.indexOf(stopName);
                
                html += `<div style="margin-bottom: 20px;">`;
                html += `<div style="font-size: 13px; color: var(--text-secondary); margin-bottom: 8px; text-transform: uppercase;">${direction.name}</div>`;
                
                html += `<div style="display: flex; flex-wrap: wrap; gap: 8px;">`;
                direction.times.forEach(time => {
                    const [h, m] = time.split(':').map(Number);
                    const busMinutes = h * 60 + m + (stopIndex * 5);
                    const isPast = busMinutes < currentMinutes;
                    const diff = busMinutes - currentMinutes;
                    
                    const actualTime = new Date();
                    actualTime.setHours(h, m + (stopIndex * 5));
                    const timeStr = actualTime.toLocaleTimeString('ru-RU', {hour: '2-digit', minute:'2-digit'});
                    
                    html += `
                        <div style="
                            padding: 8px 12px; 
                            background: ${isPast ? 'var(--bg-hover)' : 'var(--bg-hover)'}; 
                            border-radius: 8px;
                            opacity: ${isPast ? 0.4 : 1};
                            ${!isPast && diff <= 30 ? 'border: 1px solid var(--accent);' : ''}
                        ">
                            <div style="font-weight: 500;">${timeStr}</div>
                            ${!isPast ? `<div style="font-size: 11px; color: var(--accent);">${diff} мин</div>` : ''}
                        </div>
                    `;
                });
                html += `</div></div>`;
            }
        });
    });
    
    document.getElementById('detailContent').innerHTML = html;
    document.getElementById('detailView').classList.add('open');
}

// === ЗАКРЫТИЕ ДЕТАЛЕЙ ===
function closeDetail() {
    document.getElementById('detailView').classList.remove('open');
}

// === PWA ===
if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('sw.js').catch(console.error);
}

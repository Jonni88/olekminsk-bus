/**
 * Расписание автобусов Олёкминск — С разделением по дням недели
 */

// === РАСПИСАНИЕ ===
const SCHEDULE = {
    route1: {
        weekday: {
            forward: {  // С автовокзала
                name: "Маршрут №1 — С автовокзала (Пн-Пт)",
                times: ["06:00", "06:30", "07:00", "07:30", "08:00", "08:30", "09:00", "10:00", 
                        "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00"]
            },
            back: {  // С дачи
                name: "Маршрут №1 — С дачи (Пн-Пт)",
                times: ["06:15", "06:45", "07:15", "07:45", "08:15", "08:45", "09:15", "10:15",
                        "11:15", "12:15", "13:15", "14:15", "15:15", "16:15", "17:15", "18:15", "19:15", "20:15"]
            }
        },
        saturday: {
            forward: {
                name: "Маршрут №1 — С автовокзала (Суббота)",
                times: ["07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00"]
            },
            back: {
                name: "Маршрут №1 — С дачи (Суббота)",
                times: ["07:15", "08:15", "09:15", "10:15", "11:15", "12:15", "13:15", "14:15", "15:15", "16:15", "17:15", "18:15", "19:15", "20:15"]
            }
        },
        sunday: {
            forward: {
                name: "Маршрут №1 — С автовокзала (Вск/Праздники)",
                times: ["08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00"]
            },
            back: {
                name: "Маршрут №1 — С дачи (Вск/Праздники)",
                times: ["08:15", "09:15", "10:15", "11:15", "12:15", "13:15", "14:15", "15:15", "16:15", "17:15", "18:15", "19:15"]
            }
        }
    },
    route5: {
        weekday: {
            forward: {  // С автовокзала
                name: "Маршрут №5 — С автовокзала (Пн-Пт)",
                times: ["06:20", "07:00", "07:40", "08:20", "09:00", "10:00", "11:00", "12:00",
                        "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00"]
            },
            back: {  // С ПНДИ
                name: "Маршрут №5 — С ПНДИ (Пн-Пт)",
                times: ["06:35", "07:15", "07:55", "08:35", "09:15", "10:15", "11:15", "12:15",
                        "13:15", "14:15", "15:15", "16:15", "17:15", "18:15", "19:15"]
            }
        },
        saturday: {
            forward: {
                name: "Маршрут №5 — С автовокзала (Суббота)",
                times: ["07:30", "08:30", "09:30", "10:30", "11:30", "12:30", "13:30", "14:30", "15:30", "16:30", "17:30", "18:30"]
            },
            back: {
                name: "Маршрут №5 — С ПНДИ (Суббота)",
                times: ["07:45", "08:45", "09:45", "10:45", "11:45", "12:45", "13:45", "14:45", "15:45", "16:45", "17:45", "18:45"]
            }
        },
        sunday: {
            forward: {
                name: "Маршрут №5 — С автовокзала (Вск/Праздники)",
                times: ["09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"]
            },
            back: {
                name: "Маршрут №5 — С ПНДИ (Вск/Праздники)",
                times: ["09:15", "10:15", "11:15", "12:15", "13:15", "14:15", "15:15", "16:15", "17:15", "18:15"]
            }
        }
    },
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
    stops: [
        { name: "Автовокзал", routes: ["1", "5"] },
        { name: "Центр", routes: ["1"] },
        { name: "Центральная площадь", routes: ["5"] },
        { name: "Школа №1", routes: ["1"] },
        { name: "Больница", routes: ["5"] },
        { name: "Дача", routes: ["1"] },
        { name: "ПНДИ", routes: ["5"] }
    ]
};

// === ИНИЦИАЛИЗАЦИЯ ===
document.addEventListener('DOMContentLoaded', () => {
    updateTime();
    setInterval(updateTime, 1000);
    setupTabs();
    setupDayTabs();
    updateAllTimes();
    renderStops();
    setInterval(updateAllTimes, 30000);
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

function setupDayTabs() {
    document.querySelectorAll('.day-tab').forEach(tab => {
        tab.addEventListener('click', () => {
            const day = tab.dataset.day;
            
            // Переключаем табы
            document.querySelectorAll('.day-tab').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.day-content').forEach(c => c.classList.remove('active'));
            
            tab.classList.add('active');
            document.getElementById(day + 'Content').classList.add('active');
            
            // Обновляем времена
            updateAllTimes();
        });
    });
}

// === ОБНОВЛЕНИЕ ВРЕМЕНИ ===
function updateAllTimes() {
    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();
    const dayOfWeek = now.getDay(); // 0-вс, 1-пн, ..., 6-сб
    
    // Определяем текущий тип дня для отображения
    let currentDayType = 'weekday';
    if (dayOfWeek === 6) currentDayType = 'saturday';
    if (dayOfWeek === 0) currentDayType = 'sunday';
    
    // Обновляем все времена для всех дней
    ['weekday', 'saturday', 'sunday'].forEach(dayType => {
        ['1', '5'].forEach(routeNum => {
            ['forward', 'back'].forEach(direction => {
                const elementId = `time-${routeNum}-${dayType === 'weekday' ? 'weekday' : dayType === 'saturday' ? 'sat' : 'sun'}-${direction}`;
                const schedule = SCHEDULE[`route${routeNum}`][dayType][direction];
                updateTimeDisplay(elementId, schedule.times, currentMinutes, dayType === currentDayType);
            });
        });
    });
}

function updateTimeDisplay(elementId, times, currentMinutes, isCurrentDay) {
    const el = document.getElementById(elementId);
    if (!el) return;
    
    const nextBus = times.find(time => {
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
        el.textContent = isCurrentDay ? '—' : times[0];
        el.className = 'time-main';
    }
}

// === ОТРИСОВКА ОСТАНОВОК ===
function renderStops() {
    const container = document.getElementById('stopsList');
    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();
    const dayOfWeek = now.getDay();
    
    // Текущий тип дня
    let dayType = 'weekday';
    if (dayOfWeek === 6) dayType = 'saturday';
    if (dayOfWeek === 0) dayType = 'sunday';
    
    container.innerHTML = SCHEDULE.stops.map(stop => {
        // Находим ближайший автобус
        let nearestBus = null;
        let nearestTime = Infinity;
        let nearestRoute = '';
        
        stop.routes.forEach(routeNum => {
            const route = SCHEDULE[`route${routeNum}`][dayType];
            
            ['forward', 'back'].forEach(dir => {
                route[dir].times.forEach(time => {
                    const [h, m] = time.split(':').map(Number);
                    const busMinutes = h * 60 + m;
                    if (busMinutes > currentMinutes && busMinutes < nearestTime) {
                        nearestTime = busMinutes;
                        nearestBus = time;
                        nearestRoute = routeNum;
                    }
                });
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

// === ПОКАЗАТЬ РАСПИСАНИЕ ===
function showSchedule(routeNum, dayType, direction) {
    const route = SCHEDULE[`route${routeNum}`][dayType][direction];
    
    document.getElementById('detailTitle').textContent = route.name;
    
    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();
    
    document.getElementById('detailContent').innerHTML = route.times.map(time => {
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
    const data = SCHEDULE.suburban[direction];
    
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
    const stop = SCHEDULE.stops.find(s => s.name === stopName);
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
        const route = SCHEDULE[`route${routeNum}`][dayType];
        
        ['forward', 'back'].forEach(dir => {
            const direction = route[dir];
            html += `<div style="margin-bottom: 20px;">`;
            html += `<div style="font-size: 13px; color: var(--text-secondary); margin-bottom: 8px;">Маршрут ${routeNum} — ${dir === 'forward' ? 'С автовокзала' : routeNum === '1' ? 'С дачи' : 'С ПНДИ'}</div>`;
            
            html += `<div style="display: flex; flex-wrap: wrap; gap: 8px;">`;
            direction.times.forEach(time => {
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

const admin = require('firebase-admin');

// Initialize Firebase Admin (в реальном деплое использовать environment variables)
// admin.initializeApp({
//   credential: admin.credential.cert(JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT))
// });

// Store FCM tokens (в продакшене использовать Firestore/Database)
let userTokens = new Map(); // userId -> { token, platform, subscribedRoutes: [] }

// === API Endpoints ===

// Регистрация токена устройства
exports.registerToken = async (req, res) => {
  res.set('Access-Control-Allow-Origin', '*');
  res.set('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.set('Access-Control-Allow-Headers', 'Content-Type');
  
  if (req.method === 'OPTIONS') {
    res.status(204).send('');
    return;
  }

  try {
    const { userId, token, platform, routes = [] } = req.body;
    
    if (!token || !platform) {
      return res.status(400).json({ error: 'Missing token or platform' });
    }
    
    userTokens.set(userId || token.slice(-10), {
      token,
      platform, // 'android', 'ios', 'web'
      routes,   // [1, 2, 3] - ID маршрутов
      registeredAt: new Date().toISOString()
    });
    
    console.log(`Registered ${platform} token: ${token.slice(-20)}`);
    
    // Send welcome notification
    await sendNotification(token, {
      title: 'Автобусы Олёкминск',
      body: 'Уведомления включены!',
      data: { type: 'welcome' }
    });
    
    res.json({ success: true, message: 'Token registered' });
    
  } catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: error.message });
  }
};

// Удаление токена (logout/uninstall)
exports.unregisterToken = async (req, res) => {
  res.set('Access-Control-Allow-Origin', '*');
  
  try {
    const { token } = req.body;
    
    // Find and remove token
    for (const [userId, data] of userTokens.entries()) {
      if (data.token === token) {
        userTokens.delete(userId);
        break;
      }
    }
    
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

// Обновление подписок на маршруты
exports.updateSubscriptions = async (req, res) => {
  res.set('Access-Control-Allow-Origin', '*');
  
  try {
    const { token, routes } = req.body;
    
    for (const [userId, data] of userTokens.entries()) {
      if (data.token === token) {
        data.routes = routes;
        break;
      }
    }
    
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

// Отправка уведомления о ближайшем автобусе
exports.scheduleBusReminder = async (req, res) => {
  try {
    const { token, routeId, direction, departureTime, reminderMinutes = [10, 5, 1] } = req.body;
    
    const [hours, minutes] = departureTime.split(':').map(Number);
    const departureDate = new Date();
    departureDate.setHours(hours, minutes, 0, 0);
    
    // Schedule notifications
    for (const mins of reminderMinutes) {
      const reminderTime = new Date(departureDate.getTime() - mins * 60000);
      
      if (reminderTime > new Date()) {
        // В реальном приложении использовать Cloud Tasks / scheduled functions
        setTimeout(async () => {
          await sendNotification(token, {
            title: '🚌 Автобус через ' + mins + ' мин',
            body: `Маршрут ${routeId}, ${direction}`,
            data: { 
              type: 'bus_reminder',
              routeId: routeId.toString(),
              direction,
              time: departureTime
            }
          });
        }, reminderTime - new Date());
      }
    }
    
    res.json({ scheduled: true });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

// Массовая рассылка (для админки)
exports.broadcast = async (req, res) => {
  try {
    const { title, body, data = {}, filter = {} } = req.body;
    
    const results = { success: 0, failed: 0 };
    
    for (const [userId, userData] of userTokens.entries()) {
      try {
        // Apply filters if specified
        if (filter.platform && userData.platform !== filter.platform) continue;
        if (filter.routes && !userData.routes.some(r => filter.routes.includes(r))) continue;
        
        await sendNotification(userData.token, { title, body, data });
        results.success++;
      } catch (e) {
        results.failed++;
        console.error(`Failed to send to ${userId}:`, e.message);
      }
    }
    
    res.json(results);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

// === Helper Functions ===

async function sendNotification(token, { title, body, data = {} }) {
  if (!admin.apps.length) {
    console.log('Firebase not initialized, skipping notification');
    console.log('Would send:', { token: token.slice(-20), title, body });
    return;
  }
  
  const message = {
    notification: { title, body },
    data,
    token
  };
  
  // Platform-specific options
  if (data.platform === 'android') {
    message.android = {
      notification: {
        channelId: 'bus_reminders',
        priority: 'high',
        sound: 'default'
      }
    };
  }
  
  if (data.platform === 'ios') {
    message.apns = {
      payload: {
        aps: {
          sound: 'default',
          badge: 1
        }
      }
    };
  }
  
  return admin.messaging().send(message);
}

// Cron: Проверка ближайших автобусов и отправка напоминаний
exports.checkUpcomingBuses = async (req, res) => {
  // Вызвать из cron каждую минуту
  const now = new Date();
  const currentHour = now.getHours();
  const currentMinute = now.getMinutes();
  
  // Загрузить расписание из Google Sheets
  // Для каждого пользователя проверить подписанные маршруты
  // Отправить уведомление если автобус через 10/5/1 минут
  
  res.json({ checked: true, time: now.toISOString() });
};

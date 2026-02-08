const { google } = require('googleapis');

// Конфигурация
const SPREADSHEET_ID = process.env.SPREADSHEET_ID;
const SHEET_NAME = process.env.SHEET_NAME || 'Routes';

// Кэш (в памяти, 5 минут)
let cache = null;
let cacheTime = 0;
const CACHE_TTL = 5 * 60 * 1000; // 5 минут

exports.getRoutes = async (req, res) => {
  // CORS headers
  res.set('Access-Control-Allow-Origin', '*');
  res.set('Access-Control-Allow-Methods', 'GET');
  res.set('Access-Control-Allow-Headers', 'Content-Type');
  
  if (req.method === 'OPTIONS') {
    res.status(204).send('');
    return;
  }

  try {
    // Проверяем кэш
    if (cache && Date.now() - cacheTime < CACHE_TTL) {
      return res.json({
        routes: cache,
        last_updated: new Date(cacheTime).toISOString(),
        cached: true
      });
    }

    // Авторизация
    const auth = new google.auth.GoogleAuth({
      credentials: JSON.parse(process.env.GOOGLE_CREDENTIALS),
      scopes: ['https://www.googleapis.com/auth/spreadsheets.readonly'],
    });

    const sheets = google.sheets({ version: 'v4', auth });
    
    // Читаем данные
    const response = await sheets.spreadsheets.values.get({
      spreadsheetId: SPREADSHEET_ID,
      range: `${SHEET_NAME}!A:K`,
    });

    const rows = response.data.values;
    if (!rows || rows.length < 2) {
      throw new Error('No data found in spreadsheet');
    }

    // Парсим данные
    const routes = rows.slice(1)
      .filter(row => row[10] !== 'FALSE') // is_active
      .map(row => ({
        id: parseInt(row[0]) || 0,
        number: row[1] || '',
        name: row[2] || '',
        type: row[3] || 'urban',
        forward: {
          name: row[4] || '',
          stops: row[5] || '',
          times: (row[6] || '').split(',').map(t => t.trim()).filter(Boolean)
        },
        backward: {
          name: row[7] || '',
          stops: row[8] || '',
          times: (row[9] || '').split(',').map(t => t.trim()).filter(Boolean)
        },
        is_active: row[10] !== 'FALSE'
      }));

    // Сохраняем в кэш
    cache = routes;
    cacheTime = Date.now();

    res.json({
      routes,
      last_updated: new Date().toISOString(),
      cached: false
    });

  } catch (error) {
    console.error('Error:', error);
    res.status(500).json({
      error: 'Failed to fetch routes',
      message: error.message
    });
  }
};

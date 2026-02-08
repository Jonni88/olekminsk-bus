# Инструкция по подключению сервера

## 1. Настройка сервера (Beget)

### Требования к серверу:
- PHP 7.4+ или Node.js 14+
- MySQL 5.7+ или PostgreSQL
- SSL-сертификат (HTTPS)
- Дисковое пространство: минимум 1 ГБ

### Структура API:

```
/api/
  ├── flowers/          GET    - Список цветов
  ├── flowers/{id}      GET    - Один цветок
  ├── categories/       GET    - Категории
  ├── orders/           POST   - Создать заказ
  ├── orders/           GET    - Список заказов (по телефону)
  ├── auth/login        POST   - Авторизация
  └── auth/register     POST   - Регистрация
```

## 2. Пример API на PHP

### Получение списка цветов (flowers.php):
```php
<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

// Подключение к БД
$db = new PDO('mysql:host=localhost;dbname=provans', 'user', 'password');

$stmt = $db->query("SELECT * FROM flowers WHERE in_stock = 1");
$flowers = $stmt->fetchAll(PDO::FETCH_ASSOC);

echo json_encode($flowers);
?>
```

### Создание заказа (orders.php):
```php
<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

$data = json_decode(file_get_contents('php://input'), true);

// Сохранение заказа в БД
$orderId = generateOrderId();
// ... код сохранения

$response = [
    'id' => $orderId,
    'status' => 'success',
    'message' => 'Заказ создан'
];

echo json_encode($response);
?>
```

## 3. Настройка приложения

### В файле `RetrofitClient.kt` замените:
```kotlin
private const val BASE_URL = "https://your-server.com/api/"
```

На ваш реальный URL, например:
```kotlin
private const val BASE_URL = "https://provans-flowers.beget.tech/api/"
```

## 4. Структура базы данных

### Таблица flowers:
```sql
CREATE TABLE flowers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    description TEXT,
    price INT,
    old_price INT NULL,
    image_url VARCHAR(500),
    category_id INT,
    occasion_id INT,
    colors JSON,
    in_stock BOOLEAN DEFAULT TRUE,
    rating FLOAT DEFAULT 4.5,
    review_count INT DEFAULT 0,
    is_bestseller BOOLEAN DEFAULT FALSE,
    is_new BOOLEAN DEFAULT FALSE
);
```

### Таблица orders:
```sql
CREATE TABLE orders (
    id VARCHAR(20) PRIMARY KEY,
    customer_name VARCHAR(255),
    customer_phone VARCHAR(20),
    delivery_address TEXT,
    delivery_date DATE,
    delivery_time TIME,
    total_price INT,
    payment_method VARCHAR(50),
    status VARCHAR(50) DEFAULT 'NEW',
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 5. Проверка подключения

1. Установите приложение
2. Откройте любой экран с товарами
3. Если сервер настроен правильно - товары загрузятся с сервера
4. Если нет - приложение покажет ошибку

## 6. Важно!

- Всегда используйте HTTPS (SSL)
- Храните пароли в хешированном виде
- Валидируйте все входные данные
- Делайте бэкапы базы данных
- Ограничивайте доступ к API (CORS)

## Контакты для помощи

Если нужна помощь с настройкой сервера - обратитесь к поддержке Beget или найдите фрилансера на Kwork/Kwork.ru

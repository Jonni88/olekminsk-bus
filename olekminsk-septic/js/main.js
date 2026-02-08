// Основной JavaScript для лендинга

document.addEventListener('DOMContentLoaded', function() {
    
    // Кнопка наверх
    const scrollTopBtn = document.getElementById('scrollTop');
    
    window.addEventListener('scroll', function() {
        if (window.pageYOffset > 300) {
            scrollTopBtn.classList.add('visible');
        } else {
            scrollTopBtn.classList.remove('visible');
        }
    });
    
    scrollTopBtn.addEventListener('click', function() {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
    
    // Обработка формы заказа
    const orderForm = document.getElementById('orderForm');
    
    orderForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        // Получаем данные формы
        const formData = new FormData(orderForm);
        const data = {
            name: formData.get('name'),
            phone: formData.get('phone'),
            service: formData.get('service'),
            address: formData.get('address'),
            comment: formData.get('comment')
        };
        
        // Валидация телефона
        if (!data.phone || data.phone.length < 10) {
            alert('Пожалуйста, введите корректный номер телефона');
            return;
        }
        
        // Здесь можно отправить данные на сервер
        // Пример отправки на Telegram или email
        sendOrder(data);
        
        // Показываем сообщение об успехе
        showSuccessMessage();
        
        // Очищаем форму
        orderForm.reset();
    });
    
    // Функция отправки заказа
    function sendOrder(data) {
        // Вариант 1: Отправка в Telegram (через бота)
        // const telegramUrl = `https://api.telegram.org/botYOUR_BOT_TOKEN/sendMessage?chat_id=YOUR_CHAT_ID&text=${encodeURIComponent(createMessage(data))}`;
        // fetch(telegramUrl);
        
        // Вариант 2: Отправка на email (через PHP)
        // fetch('sendmail.php', {
        //     method: 'POST',
        //     headers: {'Content-Type': 'application/json'},
        //     body: JSON.stringify(data)
        // });
        
        // Вариант 3: Сохранение в Google Sheets
        // fetch('https://script.google.com/macros/s/YOUR_SCRIPT_ID/exec', {
        //     method: 'POST',
        //     body: JSON.stringify(data)
        // });
        
        console.log('Заказ отправлен:', data);
    }
    
    // Создание сообщения для отправки
    function createMessage(data) {
        const services = {
            'septic': 'Откачка септика',
            'pit': 'Выгребная яма',
            'sewer': 'Прочистка канализации',
            'grease': 'Жироуловитель',
            'other': 'Другое'
        };
        
        return `
🚛 Новая заявка с сайта!

👤 Имя: ${data.name || 'Не указано'}
📞 Телефон: ${data.phone}
🔧 Услуга: ${services[data.service] || data.service}
📍 Адрес: ${data.address || 'Не указан'}
💬 Комментарий: ${data.comment || 'Нет'}

⏰ Время заявки: ${new Date().toLocaleString()}
        `.trim();
    }
    
    // Показ сообщения об успехе
    function showSuccessMessage() {
        const modal = document.createElement('div');
        modal.className = 'success-modal';
        modal.innerHTML = `
            <div class="modal-content">
                <span class="close-btn">&times;</span>
                <div class="modal-icon">✓</div>
                <h3>Заявка отправлена!</h3>
                <p>Мы перезвоним вам в течение 5 минут.</p>
            </div>
        `;
        
        // Стили для модального окна
        modal.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.7);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 9999;
        `;
        
        modal.querySelector('.modal-content').style.cssText = `
            background: white;
            padding: 40px;
            border-radius: 15px;
            text-align: center;
            max-width: 400px;
            position: relative;
            animation: slideIn 0.3s ease;
        `;
        
        modal.querySelector('.modal-icon').style.cssText = `
            font-size: 60px;
            color: #4CAF50;
            margin-bottom: 20px;
        `;
        
        modal.querySelector('.close-btn').style.cssText = `
            position: absolute;
            top: 15px;
            right: 20px;
            font-size: 30px;
            cursor: pointer;
            color: #999;
        `;
        
        document.body.appendChild(modal);
        
        // Закрытие по клику
        modal.querySelector('.close-btn').addEventListener('click', function() {
            modal.remove();
        });
        
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                modal.remove();
            }
        });
        
        // Автоматическое закрытие через 5 секунд
        setTimeout(function() {
            modal.remove();
        }, 5000);
    }
    
    // Плавная прокрутка к якорям
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
    
    // Маска для телефона
    const phoneInput = document.querySelector('input[name="phone"]');
    if (phoneInput) {
        phoneInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            
            if (value.length > 0) {
                if (value[0] === '7' || value[0] === '8') {
                    value = value.substring(1);
                }
                
                let formattedValue = '+7';
                
                if (value.length > 0) {
                    formattedValue += ' (' + value.substring(0, 3);
                }
                if (value.length >= 3) {
                    formattedValue += ') ' + value.substring(3, 6);
                }
                if (value.length >= 6) {
                    formattedValue += '-' + value.substring(6, 8);
                }
                if (value.length >= 8) {
                    formattedValue += '-' + value.substring(8, 10);
                }
                
                e.target.value = formattedValue;
            }
        });
    }
    
    // Анимация появления элементов при скролле
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);
    
    // Наблюдаем за карточками
    document.querySelectorAll('.service-card, .advantage-item, .step').forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(20px)';
        el.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
        observer.observe(el);
    });
});

// Добавляем стиль для анимации
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            opacity: 0;
            transform: translateY(-50px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }
`;
document.head.appendChild(style);

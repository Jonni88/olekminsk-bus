# 🚌 Автобусы Олёкминск — Android App

Trusted Web Activity обёртка для Web PWA приложения.

## 📱 Что это

Приложение открывает https://jonni88.github.io/olekminsk-bus/ в нативном Chrome WebView с полной интеграцией в Android.

## ✅ Возможности

- 🎨 Тёмная тема нативного Android
- 🔔 Push-уведомления (через Web Push)
- 💾 Offline-режим (Service Worker)
- 📲 Установка как нативное приложение
- 🔄 Автообновление контента

## 🚀 Сборка APK

### 1. Установите Android Studio

### 2. Откройте проект
```bash
File → Open → olekminsk-bus/android-wrapper/
```

### 3. Создайте Keystore (для подписи)
```bash
cd android-wrapper
keytool -genkey -v -keystore bus.keystore -alias bus -keyalg RSA -keysize 2048 -validity 10000
```

### 4. Соберите Release APK
```bash
./gradlew assembleRelease
```

APK будет в: `app/build/outputs/apk/release/`

## 📦 Установка

### На телефон:
1. Включите "Установка из неизвестных источников"
2. Скопируйте APK на телефон
3. Установите

### В Google Play:
1. Создайте аккаунт Google Play Developer ($25)
2. Загрузите AAB (App Bundle)
3. Пройдите проверку

## 🔧 Настройка

### Для Trusted Web Activity нужен Digital Asset Links файл на сервере:

Создайте файл `/.well-known/assetlinks.json` на GitHub Pages:
```json
[{
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
        "namespace": "android_app",
        "package_name": "com.olekminsk.bus",
        "sha256_cert_fingerprints": ["ВАШ_SHA256_ОТПЕЧАТОК"]
    }
}]
```

Получить отпечаток:
```bash
keytool -list -v -keystore bus.keystore -alias bus
```

## 📝 Лицензия

MIT

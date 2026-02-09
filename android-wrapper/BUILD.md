# Android App Wrapper

## Быстрая сборка APK через GitHub Actions

Создайте файл `.github/workflows/build-apk.yml` в основном репозитории:

```yaml
name: Build APK

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup JDK
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Setup Android SDK
      uses: android-actions/setup-android@v2
    
    - name: Build APK
      run: |
        cd android-wrapper
        ./gradlew assembleDebug
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: android-wrapper/app/build/outputs/apk/debug/app-debug.apk
```

## Ручная сборка

1. Установите Android Studio
2. Откройте папку `android-wrapper/`
3. Build → Build Bundle(s) / APK(s) → Build APK(s)

## Альтернатива: Bubblewrap

Установите Node.js и выполните:

```bash
npm install -g @bubblewrap/cli
bubblewrap init --manifest https://jonni88.github.io/olekminsk-bus/manifest.json
bubblewrap build
```

Это создаст APK автоматически!

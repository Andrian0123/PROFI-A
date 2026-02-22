@echo off
chcp 65001 >nul
cd /d "%~dp0"

if not exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo APK не найден. Сначала соберите приложение:
    echo   - В Android Studio: Build -^> Build APK(s)
    echo   - Или запустите СОБРАТЬ_И_ПОЛОЖИТЬ_APK_НА_САЙТ.bat
    pause
    exit /b 1
)

echo Копирую APK в site...
copy /Y "app\build\outputs\apk\debug\app-debug.apk" "site\profi-a.apk"
if errorlevel 1 (
    echo Ошибка копирования.
    pause
    exit /b 1
)

echo Добавляю в git и отправляю на GitHub...
git add site/profi-a.apk site/index.html
git commit -m "Add APK for download"
git push origin main

echo.
echo Готово. Через 1-2 мин ссылка https://andrian0123.github.io/PROFI-A/profi-a.apk заработает.
pause

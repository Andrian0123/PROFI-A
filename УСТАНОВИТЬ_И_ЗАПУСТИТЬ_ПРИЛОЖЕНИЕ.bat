@echo off
chcp 65001 >nul
cd /d E:\PROFI-A
set ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
if not exist "%ADB%" (
    echo ADB не найден. Укажите путь к Android SDK в переменной LOCALAPPDATA.
    pause
    exit /b 1
)
echo Установка app-debug.apk...
"%ADB%" install -r "app\build\outputs\apk\debug\app-debug.apk"
if errorlevel 1 (
    echo Ошибка установки. Запущен ли эмулятор / подключено ли устройство?
    pause
    exit /b 1
)
echo Запуск приложения ПРОФИ-А...
"%ADB%" shell am start -n ru.profia.app/.MainActivity
echo Готово.
pause

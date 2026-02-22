@echo off
chcp 65001 >nul
echo Сборка APK и копирование в site/ для раздачи друзьям...
echo.
cd /d "%~dp0"
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo Ошибка сборки. Проверьте, что в проекте есть gradlew.bat и настроен Android SDK.
    pause
    exit /b 1
)
if not exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo APK не найден.
    pause
    exit /b 1
)
copy /Y "app\build\outputs\apk\debug\app-debug.apk" "site\profi-a.apk"
echo.
echo Готово: site\profi-a.apk
echo.
echo Дальше: закоммитьте и отправьте на GitHub (git add site/profi-a.apk, commit, push).
echo Тогда ссылка https://andrian0123.github.io/PROFI-A/profi-a.apk заработает.
echo Подробно: site\ВРЕМЕННО_ДЛЯ_ДРУЗЕЙ.md
pause

@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ========== Шаг 1: Сборка APK ==========
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo JAVA не найден по пути Android Studio. Соберите APK в Android Studio: Build -^> Build APK(s)
    echo Затем запустите ДОПИСАТЬ_APK_И_ОТПРАВИТЬ.bat
    pause
    exit /b 1
)
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo Сборка не удалась. Закройте другие окна с Gradle/Android Studio и попробуйте снова.
    pause
    exit /b 1
)

echo.
echo ========== Шаг 2: Копирование в site/ ==========
copy /Y "app\build\outputs\apk\debug\app-debug.apk" "site\profi-a.apk"
if errorlevel 1 (
    echo Ошибка копирования.
    pause
    exit /b 1
)

echo.
echo ========== Шаг 3: Отправка на GitHub ==========
git add site/profi-a.apk site/index.html
git commit -m "Add APK for download"
git push origin main

echo.
echo ========== Готово ==========
echo Через 1-2 мин ссылка https://andrian0123.github.io/PROFI-A/profi-a.apk заработает.
pause

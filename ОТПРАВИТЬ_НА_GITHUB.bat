@echo off
chcp 65001 >nul
set "PATH=%PATH%;C:\Program Files\Git\bin;C:\Program Files (x86)\Git\bin"
echo ============================================
echo   Отправка проекта ПРОФЙ-А на GitHub
echo ============================================
echo.

cd /d E:\PROFI-A

git remote remove origin 2>nul

echo.
echo Сначала создайте пустой репозиторий на github.com (см. ШАГИ_ДО_GITHUB_ПРОСТО.txt)
echo.
set /p GITHUB_URL="Вставьте ссылку на репозиторий (Ctrl+V) и нажмите Enter: "

if "%GITHUB_URL%"=="" (
    echo Ссылка не введена. Запустите файл снова и вставьте ссылку.
    pause
    exit /b 1
)

echo.
echo Добавляю удалённый репозиторий...
git remote add origin %GITHUB_URL%

echo.
echo Отправляю код на GitHub (может попросить войти)...
git push -u origin main

echo.
if %ERRORLEVEL% EQU 0 (
    echo Готово! Проект загружен на GitHub.
) else (
    echo Ошибка при отправке. Убедитесь, что:
    echo 1. Репозиторий создан на github.com
    echo 2. Ссылка скопирована правильно
    echo 3. Вы вошли в GitHub при запросе
)
echo.
pause

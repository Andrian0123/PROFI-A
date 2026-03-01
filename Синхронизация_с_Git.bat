@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"

echo ============================================
echo   Синхронизация проекта PROFI-A с Git
echo ============================================
echo.

:: Проверка, что мы в репозитории
git rev-parse --git-dir >nul 2>&1
if errorlevel 1 (
    echo Ошибка: папка не является Git-репозиторием.
    pause
    exit /b 1
)

:: Подтянуть изменения с сервера (если кто-то пушил с другого ПК)
echo [1/3] Получаем изменения с origin...
git pull origin main --rebase 2>nul || git pull origin main 2>nul
if errorlevel 1 (
    echo Предупреждение: не удалось выполнить pull. Продолжаем...
)
echo.

:: Добавить все изменения
echo [2/3] Добавляем изменения...
git add -A
git status --short
echo.

:: Коммит и пуш
set MSG=Sync %date% %time%
echo [3/3] Коммит и отправка на origin main...
git commit -m "%MSG%" 2>nul
if errorlevel 1 (
    echo Нет изменений для коммита или коммит отменён.
) else (
    git push origin main
    if errorlevel 1 (
        echo Ошибка push. Проверьте доступ в интернет и права на репозиторий.
    ) else (
        echo.
        echo Синхронизация завершена успешно.
    )
)

echo.
pause

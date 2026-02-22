@echo off
chcp 65001 >nul
cd /d E:\PROFI-A
echo Проверка локального backend (Auth, Support, Scan)...
echo.
powershell -ExecutionPolicy Bypass -File ".\ПРОВЕРИТЬ_СЕРВЕР.ps1"
echo.
pause

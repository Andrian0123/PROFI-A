@echo off
chcp 65001 >nul
cd /d E:\PROFI-A\server
echo ============================================
echo   Локальный backend ПРОФЙ-А (Auth, Support, Scan)
echo ============================================
echo.
echo Для эмулятора в приложении уже указаны:
echo   AUTH    = http://10.0.2.2:3001
echo   SUPPORT = http://10.0.2.2:3002
echo   SCAN    = http://10.0.2.2:3003
echo.
echo Не закрывайте это окно пока тестируете приложение.
echo Для остановки нажмите Ctrl+C или закройте окно.
echo ============================================
echo.
node server.js
pause

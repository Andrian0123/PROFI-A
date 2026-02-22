@echo off
chcp 65001 >nul
echo Открываю папку сайта и инструкцию...
start "" "%~dp0site"
start "" "%~dp0site\ПОШАГОВО.md"
start "" "%~dp0site\index.html"
echo.
echo Готово. Открыты: папка site, инструкция ПОШАГОВО.md и главная страница в браузере.
pause

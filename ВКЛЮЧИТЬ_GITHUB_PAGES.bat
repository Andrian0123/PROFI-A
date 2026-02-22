@echo off
chcp 65001 >nul
echo Открываю настройки GitHub Pages...
echo.
echo ВАЖНО: без этого шага ссылка andrian0123.github.io/PROFI-A даёт 404.
echo.
echo После входа в GitHub:
echo 1. В блоке "Build and deployment" выберите Source: GitHub Actions
echo 2. Затем откройте вкладку Actions и нажмите Run workflow для "Deploy site to GitHub Pages"
echo 3. Через 1-2 минуты сайт откроется: https://andrian0123.github.io/PROFI-A/
echo.
start "" "https://github.com/Andrian0123/PROFI-A/settings/pages"
echo Открываю также вкладку Actions для ручного запуска деплоя...
start "" "https://github.com/Andrian0123/PROFI-A/actions/workflows/deploy-site.yml"
pause

# PROFI-A Reference Backend

Эталонная реализация API по контрактам [docs/BACKEND_API_CONTRACTS.md](../docs/BACKEND_API_CONTRACTS.md) для разработки и тестов.

## Запуск

```bash
cd server
node server.js
```

- **Auth:**    http://localhost:3001  
- **Support:** http://localhost:3002  
- **Scan:**    http://localhost:3003  

Для эмулятора Android: `http://10.0.2.2:3001` (и 3002, 3003). В `app/build.gradle.kts` задайте:

- `AUTH_SERVER_URL` = `"http://10.0.2.2:3001"`
- `SUPPORT_SERVER_URL` = `"http://10.0.2.2:3002"`
- `SCAN_SERVER_URL` = `"http://10.0.2.2:3003"`

## Реализованные эндпоинты

| Сервис | Метод | Путь | Описание |
|--------|--------|------|----------|
| Auth | POST | /auth/login | Логин (login, password) → userId, accessToken, refreshToken |
| Auth | POST | /auth/register | Регистрация |
| Auth | POST | /account/change-password | Смена пароля (oldPassword, newPassword) |
| Auth | POST | /account/2fa | Вкл/выкл 2FA (enabled) |
| Auth | POST | /account/delete | Удаление аккаунта |
| Support | POST | /support/tickets | Отправка заявки (phone, email, description) |
| Support | GET | /support/tickets | Список заявок (для проверки очереди) |
| Scan | POST | /api/v1/scan/process | multipart: project_id, room_id, scan_id, frames → dimensions |
| Scan | POST | /api/v1/scan/finish | scan_id, project_id, room_id → dimensions |

Данные хранятся в памяти (перезапуск очищает). Production-реализация должна использовать БД и полноценную аутентификацию по Bearer.

## Один порт для облака (Render, Railway)

Для деплоя в облако используйте **server-single.js** — один HTTP-сервер на порту `process.env.PORT` с путями `/auth/*`, `/support/*`, `/api/v1/scan/*`. В приложении задайте один и тот же URL для `AUTH_SERVER_URL`, `SUPPORT_SERVER_URL` и `SCAN_SERVER_URL`. Подробно: [docs/ПОШАГОВОЕ_ПОДКЛЮЧЕНИЕ_СЕРВИСОВ.md](../docs/ПОШАГОВОЕ_ПОДКЛЮЧЕНИЕ_СЕРВИСОВ.md).

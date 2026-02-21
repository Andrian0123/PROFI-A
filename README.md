# PROFI-A — Сметный калькулятор

Мобильное приложение для строительных и ремонтных работ на Android (Kotlin, Jetpack Compose).

## Требования

- Android Studio Hedgehog или новее
- JDK 17
- Android SDK 34

## Установка

1. Клонируйте репозиторий и откройте проект в Android Studio.

2. Сгенерируйте Gradle Wrapper (если папки `gradle/wrapper` нет или отсутствует `gradle-wrapper.jar`):
   ```bash
   gradle wrapper
   ```

3. Синхронизируйте проект и соберите:
   ```bash
   ./gradlew assembleDebug
   ```
   Или на Windows:
   ```bash
   gradlew.bat assembleDebug
   ```

4. Установите APK на устройство или эмулятор.

## Структура проекта

- `app/src/main/java/ru/profia/app/`
  - `data/` — модели, репозитории
  - `di/` — Hilt-модули
  - `ui/components/` — переиспользуемые UI-компоненты
  - `ui/screens/` — экраны приложения
  - `ui/navigation/` — навигация
  - `ui/theme/` — тема и типографика

## Реализованные экраны

- **SplashScreen** — стартовый экран; при первом запуске ведёт в онбординг
- **SpecialtySelectionScreen** — выбор специальности (строительство, отделка, электрика, сантехника и др.)
- **BusinessTypeScreen** — выбор формы: ИП или ООО
- **AuthScreen** — авторизация: поля email/телефон, пароль, кнопки «Войти» и «Зарегистрироваться»; после перехода в приложение
- **HomeScreen** — проекты, Bottom Navigation, Drawer
- **CreateProjectScreen** — создание проекта с валидацией
- **AddRoomScreen** — добавление/редактирование комнаты, расчёты площадей (с учётом проёмов), откосы/короба, проёмы (окна/двери), калькулятор
- **ProjectDetailScreen** — просмотр проекта, список комнат, переход к редактированию
- **CalculatorScreen** — калькулятор площади/стоимости
- **ProfileScreen** — профиль, компания, реквизиты, редактирование
- **SettingsScreen** — выбор города, валюты, языка, единиц измерения
- **SubscriptionScreen** — подписка, пробный период, планы, промокоды
- **SupportScreen** — контакты, форма обращения (отправка через mailto, прикрепление файлов)
- **AboutScreen** — информация о приложении
- Справочники: MaterialsScreen, WorksScreen, RoomTypesScreen, StagesScreen

## Функциональность

- **Room Database** — проекты, комнаты, проёмы
- **DataStore** — настройки, профиль, подписка
- **Подписка** — пробный период 10 дней, планы 1/6/12 мес., промокоды PROMO3/6/12
- **Навигация** — Drawer, анимации переходов
- **Валидация** — формы, UnsavedChangesDialog, ConfirmExitDialog
- **Локализация** — strings.xml

## Интеграция сканера помещений

Документация по сканеру для автоматического определения размеров помещений:

- **[Интеграция сканера](docs/ИНТЕГРАЦИЯ_СКАНЕРА.md)** — архитектура, API, модель данных, передача размеров в форму комнаты.
- **[Как подключить сервер сканирования](docs/КАК_ПОДКЛЮЧИТЬ_СЕРВЕР_СКАНИРОВАНИЯ.md)** — запуск Python-сервера, настройка `SCAN_SERVER_URL` для эмулятора и устройства.

## Оплата подписки

Используется **Google Play Billing** (библиотека billing-ktx). При наличии Google Play Services показывается нативный диалог оплаты. Если биллинг недоступен (эмулятор без Play, устройство без GMS) — включается демо-режим (подписка активируется сразу).

**Настройка:** в Google Play Console создайте подписки с Product ID:
- `profi_a_sub_1` — 1 месяц
- `profi_a_sub_6` — 6 месяцев  
- `profi_a_sub_12` — 1 год

## Верификация покупок на сервере

После успешной оплаты через Google Play приложение может отправить данные покупки на ваш сервер для проверки (рекомендуется для продакшена). Если сервер не настроен (`VERIFICATION_SERVER_URL` пустой), подписка активируется только по факту успешного ответа от Google Play на устройстве.

**Включение:** в `app/build.gradle.kts` в `defaultConfig` задайте URL вашего API:
```kotlin
buildConfigField("String", "VERIFICATION_SERVER_URL", "\"https://your-api.example.com\"")
buildConfigField("String", "AUTH_SERVER_URL", "\"https://your-auth.example.com\"")
buildConfigField("String", "SUPPORT_SERVER_URL", "\"https://your-support.example.com\"")
buildConfigField("String", "SCAN_SERVER_URL", "\"https://your-scan.example.com\"")
```

**Контракт API:**

- **Метод:** `POST`
- **Путь:** `{VERIFICATION_SERVER_URL}/verify`
- **Заголовок:** `Content-Type: application/json`
- **Тело запроса (JSON):**
  - `purchaseToken` (string) — токен покупки от Google Play
  - `productId` (string) — ID продукта (`profi_a_sub_1` / `profi_a_sub_6` / `profi_a_sub_12`)
  - `orderId` (string) — ID заказа

- **Ответ 200 (JSON):**
  - `valid` (boolean) — признана ли покупка действительной после проверки через Google Play Developer API (subscriptions)
  - `endDateMillis` (number, необязательно) — дата окончания подписки в миллисекундах; если не указана, приложение вычислит дату по длительности плана

Сервер должен проверить покупку через [Google Play Developer API](https://developers.google.com/android-publisher/api-ref/rest/v3/purchases.subscriptions) (например, `purchases.subscriptions.get`) и вернуть `valid: true` только при успешной проверке.

## Дальнейшая разработка

- Реализация бэкенда для верификации (эндпоинт `/verify` по контракту выше)

## Future-Generation Docs

- `docs/FUTURE_GENERATION_MANIFESTO_V1.md` — продуктовый манифест v1.
- `docs/BRAND_COLOR_SYSTEM.md` — стандарты брендовых цветов.
- `docs/P0_BACKEND_CLOSURE.md` — статус закрытия P0 backend-интеграций.
- `docs/QUALITY_GATES.md` — обязательные quality gates.
- `docs/GOVERNANCE_ROADMAP_2026.md` — канонический governance и квартальный roadmap.

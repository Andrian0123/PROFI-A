# Подписка: BillingManager и верификация на сервере

## Текущая интеграция

- **BillingManager** (`app/.../billing/BillingManager.kt`): подключение к Google Play Billing (BillingClient), запрос продукта по `productId`, запуск покупки через `launchPurchase(activity, productId, onResult)`. После успешной покупки вызывается `acknowledgePurchaseIfNeeded`.
- **PurchaseVerificationApi** (`app/.../data/remote/PurchaseVerificationApi.kt`): при непустом `BuildConfig.VERIFICATION_SERVER_URL` отправляет на сервер POST `{baseUrl}/verify` с телом `{ "purchaseToken", "productId", "orderId" }`. Ожидает ответ с `valid` и опционально `endDateMillis`. Если URL пустой — верификация не вызывается, подписка считается активной по факту покупки в клиенте.
- **SubscriptionViewModel** использует BillingManager и (при наличии) верификацию; результат сохраняется в DataStore (тип подписки, даты).

## Что нужно для production

1. **Google Play Console**
   - Создать подписки (Product ID) в Monetization → Products → Subscriptions.
   - Указать те же `productId` в коде (например, в `SubscriptionScreen` / ViewModel при вызове `launchPurchase`).

2. **Сервер верификации** (опционально, но рекомендуется)
   - Реализовать POST `{VERIFICATION_SERVER_URL}/verify` по контракту выше.
   - Сервер должен проверять `purchaseToken` через Google Play Developer API и возвращать `{ "valid": true, "endDateMillis": ... }` или `valid: false`.
   - В приложении задать `VERIFICATION_SERVER_URL` в `build.gradle.kts` (release/buildType).

3. **Тестирование**
   - Лицензии тестирования в Play Console для тестовых покупок.
   - При пустом `VERIFICATION_SERVER_URL` покупка в клиенте сразу даёт активную подписку без проверки на бэкенде.

Текущая связка BillingManager + PurchaseVerificationApi готова к подключению магазина и сервера; для полного цикла достаточно задать product IDs и при необходимости URL верификации.

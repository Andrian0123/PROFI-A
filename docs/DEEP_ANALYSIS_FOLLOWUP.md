# Доработки по результатам глубокого анализа

По результатам интеллектуального анализа кодовой базы внедрены следующие недостающие элементы.

## 1. Навигация и экраны

- **Итоговая смета:** На экране проекта (ProjectDetailScreen) добавлена кнопка «Итоговая смета»; переход по маршруту `NavRoutes.finalEstimate(projectId)` реализован.
- **Строки вместо хардкода:** В ProjectDetailScreen используются ресурсы: `edit_project`, `project_total_cost`, `add_floor_same_params`. Снэкбары «Проект создан» и «Комната сохранена» переведены на `project_created` и `room_saved` в ProfiANavHost.

## 2. Профиль (ProfileScreen)

- Все видимые пользователю тексты вынесены в строковые ресурсы: `profile_cabinet_type`, `profile_single_choice`, `profile_profi`, `profile_ip_ooo`, `profile_block_profi`, `profile_block_business`, `profile_edit_btn`, `profile_inn_kpp`, `profile_bank_rs`, `profile_bank_ks`, `profile_bik`, `profile_ks2_ks3_desc`, `profile_form_ks2_ks3_btn`.
- Для иконок заданы `contentDescription`: `content_desc_person`, `content_desc_business`, `content_desc_account_balance`, `content_desc_settings` (доступность).

## 3. Переиспользуемые UI-компоненты

- **ErrorView** (`ui/components/ErrorView.kt`): блок с сообщением об ошибке и кнопкой «Повторить»; строки `error_generic`, `error_try_again`.
- **EmptyState** (`ui/components/EmptyState.kt`): блок для пустых списков (иконка, заголовок, подзаголовок, опциональное действие); строки `empty_state_title`, `empty_state_subtitle`. Можно подключать на экранах с пустыми списками (проекты, комнаты, акты).

## 4. Обработка ошибок

- **SupportViewModel:** добавлены `errorMessage: StateFlow<String?>`, `clearError()`. При неуспешной отправке заявки выставляется сообщение об ошибке.
- **SupportScreen:** отображается сообщение из `errorMessage` над формой; при изменении полей вызывается `viewModel.clearError()`. Используется строка `support_error_submit` при пустом сообщении от сервера.

## 5. Строковые ресурсы и i18n

- В `values/strings.xml` и `values-en/strings.xml` добавлены все перечисленные выше ключи (в т.ч. для ошибок, пустого состояния, описаний иконок). Проверка паритета: **232 ключа**, пройдена.
- Остальные локали (de, ro, uz, tg, kk) можно дополнить этими ключами при необходимости по тому же списку.

## 6. Что остаётся на будущее (из анализа)

- Вынести оставшиеся хардкод-тексты в AddRoomScreen, AddWorkTypesScreen, EditProfileSectionScreen, RoomScanScreen, AddOpeningDialog, SettingsScreen (города/единицы), WorkCategoryScreen, ActsScreen (НДС).
- Использовать ErrorView/EmptyState на экранах загрузки проектов, списка актов и т.п.
- Обработка ошибок в ProjectViewModel/CreateProjectScreen (снэкбар при неудачном создании проекта), контракт ProjectRepository при отсутствии проекта (Result вместо пустой строки).
- Unit-тесты для ViewModels и репозиториев; дополнительные testTag для E2E.
- Опционально: retry для API, deep links, backup rules в манифесте, комментарии к BuildConfig.

Текущие изменения закрывают приоритетные пользовательские и стабильностные пункты из отчёта анализа.

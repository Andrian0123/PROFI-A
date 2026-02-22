# PROFI-A для Apple (iOS)

Дубликат проекта PROFI-A под платформу Apple. Все решения и функциональность Android-версии переносятся в iOS (Swift / SwiftUI).

## Требования

- Xcode 15+
- iOS 16+
- Swift 5.9+

## Структура (дублирование Android)

```
apple/
├── PROFI-A/                 # Xcode app target
│   ├── PROFI-AApp.swift     # Точка входа (@main)
│   ├── ContentView.swift
│   ├── Models/              # data/model
│   ├── ViewModels/          # ui/viewmodel
│   ├── Views/               # ui/screens + ui/components
│   ├── Navigation/          # ui/navigation
│   ├── Data/                # data/repository, data/local
│   ├── Resources/           # Localizable.strings, Assets
│   └── Export/              # ui/export (PDF/CSV)
├── PROFI-A.xcodeproj
└── README.md
```

## Решения для дублирования (полный перечень)

### Навигация (NavRoutes)
- Splash, Specialty, BusinessType, Auth, Home, Projects
- CreateProject, AddRoom, RoomScan
- ProjectDetail, EditProject
- GeneralEstimate, FinalEstimate, Acts, KS2, KS3, FormKs2Ks3
- Profile, EditProfileSection, Settings, ChangePassword, TwoFa
- Materials, Works, WorkSection, AddWorkTypes, WorkCategory
- RoomTypes, Stages, Subscription, Support, About, Foreman

### Модели данных (data/model)
- UserProfile (ФИО, email, phone, companyName, inn, kpp, legalAddress, bankName, accountNumber, correspondentAccount, bic)
- AppSettings (city, currency, language, unitSystem)
- ProjectData, Project, Room, RoomWorkItemEntity
- WorkTemplate, WorkCategory
- IntermediateEstimateActEntity, IntermediateEstimateActItemEntity
- KS2Act, KS2Item, KS3Certificate
- ContractorInfo, ConstructionInfo

### Экраны (ui/screens)
- SplashScreen, SpecialtySelectionScreen, BusinessTypeScreen
- AuthScreen, HomeScreen, CreateProjectScreen, EditProjectScreen
- ProjectDetailScreen, AddRoomScreen, RoomScanScreen
- GeneralEstimateScreen, FinalEstimateScreen
- ActsScreen (промежуточные акты: скидка, НДС, язык сметы Ру/En/De/Ro)
- FormKs2Ks3Screen, KS2Screen, KS3Screen
- ProfileScreen (Профи vs ИП/ООО: профиль, о компании, реквизиты)
- EditProfileSectionScreen (profile, company, requisites)
- SettingsScreen, ChangePasswordScreen, TwoFaSettingsScreen
- MaterialsScreen, WorksScreen, WorksSectionScreen (Внутренние/Наружные работы)
- AddWorkTypesScreen, WorkCategoryScreen
- RoomTypesScreen, StagesScreen
- SubscriptionScreen, SupportScreen, AboutScreen, ForemanInviteScreen

### Бизнес-логика при экспорте актов
- **Профи (PROFI):** в акте/смете только профиль — ФИО, тел., email.
- **ИП/ООО (BUSINESS):** профиль + о компании (ИНН, КПП, адрес) + реквизиты (банк, р/с, к/с, БИК).

### Справочники (data/reference)
- WorksReference: секции «Внутренние работы» и «Наружные работы»; группы «Внутренняя отделка», «Уличные работы», Потолок, Стены, Пол, Двери, Окна, Сантехника, Электрика, Вентиляция, Фасад, Кровля, Водосток, Отмостка и др.
- RoomTypesReference
- StagesReference
- MaterialsReference

### Хранение данных
- Room Database → Core Data / SwiftData (проекты, комнаты, проёмы, работы, акты).
- DataStore → UserDefaults / SwiftData / Keychain (профиль, настройки, тип кабинета PROFI/BUSINESS, подписка).

### Локализация
- values/strings.xml → Localizable.strings (ru, en, de, ro, uz, tg, kk).
- Язык сметы в актах: Ру, En, De, Ro (двухбуквенные коды).

### Экспорт
- EstimateExport: PDF/CSV предварительная и итоговая смета; exportActToPdf/exportActToCsv (промежуточный акт).
- Ks2Ks3Export: КС-2 и КС-3 в PDF/CSV с учётом типа профиля (Профи vs ИП/ООО).

### Подписка
- Пробный период (Профи 14 дней, ИП/ООО 30 дней), планы 1/6/12 мес., промокоды.
- Apple: StoreKit 2 (In-App Purchase для подписок).

### Прочее
- Валидация форм, диалоги «Несохранённые изменения», подтверждение выхода.
- Калькулятор площади/стоимости, расчёты по комнатам (площадь, периметр, откосы).
- Drawer-меню, анимации переходов.

---

## Добавлено под Android

- **Drawer (боковое меню):** `Views/Components/DrawerMenuView.swift` — пункты как в Android (Профиль, Проекты, Калькулятор, Скачать приложение, Добавить прораба, Справочники, Настройки и поддержка, Документы, Выход).
- **Ссылка без аккаунта:** `Helpers/AppUrls.swift` — `AppUrls.download` открывает страницу приложения; для iOS после публикации можно подставить ссылку на App Store в `appStore`.
- **Кнопка «Скачать»** в тулбаре (рядом с бургер-меню) открывает ту же ссылку.

## Сборка

1. Откройте `PROFI-A.xcodeproj` в Xcode.
2. При необходимости добавьте в проект новые файлы: `Helpers/AppUrls.swift`, `Views/Components/DrawerMenuView.swift`.
3. Выберите целевое устройство или симулятор.
4. Run (⌘R).

## Примечание

Исходный Android-проект: `../` (PROFI-A на Kotlin/Compose). Все изменения в логике и экранах Android-версии следует дублировать в этом iOS-проекте.

# Экраны для дублирования (Android → iOS)

Полный список экранов и решений из Android-проекта для переноса в iOS.

## Онбординг и авторизация
| Android (Kotlin)        | iOS (Swift)        | Описание |
|------------------------|--------------------|----------|
| SplashScreen           | SplashView         | Старт, переход в онбординг или Home |
| SpecialtySelectionScreen | SpecialtySelectionView | Выбор специальности |
| BusinessTypeScreen     | BusinessTypeView   | Профи / ИП / ООО (PROFI / BUSINESS) |
| AuthScreen             | AuthView           | Вход / регистрация |

## Главное приложение
| Android                 | iOS                 |
|------------------------|---------------------|
| HomeScreen             | HomeView            |
| ProfiANavigationDrawer | Sidebar / NavigationSplitView |
| CreateProjectScreen    | CreateProjectView   |
| EditProjectScreen      | EditProjectView     |
| ProjectDetailScreen    | ProjectDetailView   |
| AddRoomScreen          | AddRoomView         |
| RoomScanScreen         | RoomScanView        |

## Сметы и акты
| Android                 | iOS                 | Решения |
|------------------------|---------------------|---------|
| GeneralEstimateScreen  | GeneralEstimateView | buildExecutorLines(profile, accountType) |
| FinalEstimateScreen    | FinalEstimateView   | то же |
| ActsScreen             | ActsView            | Скидка, НДС, язык сметы (Ру/En/De/Ro); экспорт с учётом Профи/ИП |
| FormKs2Ks3Screen       | FormKs2Ks3View      | КС-2/КС-3 PDF/CSV по accountType |
| KS2Screen, KS3Screen   | KS2View, KS3View    | Формы актов |

## Профиль и настройки
| Android                 | iOS                 |
|------------------------|---------------------|
| ProfileScreen          | ProfileView         | Блоки: Профиль, О компании, Реквизиты (для ИП/ООО) |
| EditProfileSectionScreen | EditProfileSectionView | profile / company / requisites |
| SettingsScreen         | SettingsView        |
| ChangePasswordScreen   | ChangePasswordView  |
| TwoFaSettingsScreen   | TwoFaSettingsView   |

## Справочники
| Android                 | iOS                 |
|------------------------|---------------------|
| MaterialsScreen        | MaterialsView       |
| WorksScreen            | WorksView           | Папки: Внутренние работы, Наружные работы |
| WorksSectionScreen     | WorksSectionView    | Группы работ по секции |
| AddWorkTypesScreen     | AddWorkTypesView    |
| WorkCategoryScreen     | WorkCategoryView    |
| RoomTypesScreen        | RoomTypesView       |
| StagesScreen           | StagesView          |

## Остальное
| Android                 | iOS                 |
|------------------------|---------------------|
| CalculatorScreen       | CalculatorView      |
| SubscriptionScreen    | SubscriptionView   |
| SupportScreen         | SupportView         |
| AboutScreen           | AboutView           |
| ForemanInviteScreen   | ForemanInviteView   |

## Ключевые решения при дублировании
1. **Тип кабинета** (UserDefaults/Keychain): PROFI → в актах только ФИО, тел., email; BUSINESS → + компания + реквизиты.
2. **Язык сметы** в актах: двухбуквенные коды (Ру, En, De, Ro).
3. **Справочник работ**: секции «Внутренние» и «Наружные»; группы «Внутренняя отделка», «Уличные работы» и остальные из WorksReference.
4. **Экспорт PDF/CSV**: блок исполнителя формируется по accountType (см. buildExecutorLines / Ks2Ks3Export).

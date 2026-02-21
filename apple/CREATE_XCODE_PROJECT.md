# Создание Xcode-проекта для PROFI-A (iOS)

1. Откройте Xcode → File → New → Project.
2. Выберите **App** (iOS), Next.
3. Product Name: **PROFI-A**, Team: ваш аккаунт, Organization Identifier: например `ru.profia`, Interface: **SwiftUI**, Language: **Swift**, Storage: при необходимости **SwiftData** или Core Data. Создайте проект в папке `apple` (рядом с папкой `PROFI-A`).
4. Удалите сгенерированные Xcode файлы `ContentView.swift` и `PROFI_AApp.swift` из целевого таргета, если они созданы в корне.
5. Добавьте в проект папку **PROFI-A** (Add Files to "PROFI-A" → выбрать папку `PROFI-A`, Create groups, Copy items if needed).
6. Убедитесь, что в Target → Build Phases → Compile Sources включены все `.swift` файлы из папок Models, ViewModels, Views, Navigation, Data.
7. Минимальная версия iOS: 16.0.
8. Запуск: ⌘R.

После этого можно дублировать остальные экраны и логику по списку из `DUPLICATE_SCREENS.md` и `README.md`.

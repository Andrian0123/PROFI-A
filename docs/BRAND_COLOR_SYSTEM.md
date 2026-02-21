# PROFI-A Brand Color System

## Canonical Colors

- `DarkOlive` (`#2F3B27`) — main brand color.
- `DarkGreen` (`#1F2A1B`) — secondary brand reinforcement color.

These two colors represent trust, stability, and engineering discipline in product surfaces.

## Source of Truth

- Compose tokens: `app/src/main/java/ru/profia/app/ui/theme/Color.kt`
- XML compatibility: `app/src/main/res/values/colors.xml`, `app/src/main/res/values/themes.xml`

## Usage Rules

1. New UI must use `MaterialTheme.colorScheme.*` values.
2. Direct hex usage in feature screens is not allowed.
3. Legacy aliases (`Khaki`, `Pistachio`, etc.) are compatibility-only and should not be used in new code.
4. `Primary` is always `DarkOlive`; `PrimaryVariant` is always `DarkGreen`.

## Migration Rule

When touching old screens/components, replace direct aliases and custom colors with semantic theme roles:

- CTA / main interactive elements -> `colorScheme.primary`
- pressed/selected/strong emphasis -> `colorScheme.primaryContainer`
- neutral surfaces -> `colorScheme.surface` and `colorScheme.background`

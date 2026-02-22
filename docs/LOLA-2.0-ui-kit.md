# LOLA 2.0 — UI Kit v1

## 1) Color tokens

- Primary: `#FFD700` (Chaplin Gold)
- Secondary: `#4169E1` (AI Blue)
- Accent Positive: `#20B2AA` (Prana Teal)
- Accent Warning: `#FF6347` (Chef Tomato)
- Surface: `#121212` (Dark)
- Surface Variant: `#1E1E1E`
- Text Primary: `#F5F5F5`
- Text Secondary: `#B0B0B0`
- Error: `#B00020`

## 2) Typography

- H1: 28 / SemiBold
- H2: 22 / SemiBold
- Body: 16 / Regular
- Caption: 13 / Regular
- Button: 16 / Medium

## 3) Core components

- Button/Primary: filled, radius 12, min height 48
- Button/Secondary: outlined, radius 12
- Input/Text: radius 12, label + helper text
- Card/Character: avatar, name, subtitle, color stripe
- Toggle: `Текст | Аудио`
- IconButton/Mic: round 48, active state with glow

## 4) Chat-specific UI

- Message bubble user: right-aligned, primary tint
- Message bubble assistant: left-aligned, surface variant
- Composer:
  - text input
  - mic button
  - send button
- Audio response bubble:
  - play/pause
  - progress bar
  - duration

## 5) Spacing and layout

- Base spacing unit: 8
- Screen horizontal padding: 16
- Component vertical gap: 12
- Section spacing: 24

## 6) Accessibility baseline

- Contrast ratio >= 4.5:1 for text
- Tap target >= 44x44
- Voice controls reachable with one hand
- Dynamic type support (font scaling)

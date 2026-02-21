# Анализ видео для 3D-сканера

Скрипт `analyze_scan_video.py` разбирает видеофайл (метаданные и при необходимости кадры) для пайплайна сканирования.

## Использование

```bash
# Из корня проекта E:\PROFI-A
python tools/analyze_scan_video.py "видео\document_5242313571123105378.mp4"
# или полный путь:
python tools/analyze_scan_video.py "E:\PROFI-A\видео\document_5242313571123105378.mp4"
```

### Параметры

| Параметр | Описание |
|----------|----------|
| `video` | Путь к файлу .mp4 (или другому формату, поддерживаемому OpenCV/ffprobe). |
| `--json` | Вывести результат в JSON. |
| `--extract ПАПКА` | Извлечь кадры в указанную папку (JPEG). |
| `--every N` | Брать каждый N-й кадр (по умолчанию 5). |
| `--max-frames N` | Не более N кадров (по умолчанию 30). |

### Примеры

```bash
# Только метаданные
python tools/analyze_scan_video.py "видео\document_5242313571123105378.mp4"

# Метаданные в JSON
python tools/analyze_scan_video.py "видео\document_5242313571123105378.mp4" --json

# Извлечь до 30 кадров (каждый 5-й) в папку frames/
python tools/analyze_scan_video.py "видео\document_5242313571123105378.mp4" --extract frames
```

## Полный анализ (разрешение, FPS, кадры, извлечение)

Нужен один из вариантов:

- **OpenCV:**  
  `pip install opencv-python-headless`  
  (в `requirements.txt` указан как опциональный.)

- **ffmpeg** в PATH (программы `ffprobe`/`ffmpeg`).

Без них скрипт выведет только путь к файлу и размер.

## Ваш файл

- **Путь:** `E:\PROFI-A\видео\document_5242313571123105378.mp4` (или копия в корне: `E:\PROFI-A\document_5242313571123105378.mp4`).
- **Размер:** ~4.81 МБ.

После установки `opencv-python-headless` или ffmpeg перезапустите скрипт — появятся разрешение, FPS, число кадров и длительность.

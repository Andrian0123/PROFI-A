# Scan Service (FastAPI) — PROFI-A

Локальный сервис обработки 3D-сканов комнат.

## 1) Установка

```bash
pip install -r requirements.txt
```

## 2) Запуск

```bash
uvicorn app.main:app --reload
```

Документация Swagger:

- `http://127.0.0.1:8000/docs`

### Запуск в Docker

```bash
docker compose up --build
```

После старта:

- API: `http://127.0.0.1:8000`
- Swagger: `http://127.0.0.1:8000/docs`

## 3) API

### POST `/api/v1/scan/process`

`multipart/form-data`

Поля:

- `project_id` (string, required)
- `room_id` (string, required)
- `scan_id` (string, required)
- `frames[]` (file[], required, `.jpg/.jpeg`)
- `trajectory` (JSON-string, optional)
- `depth[]` (file[], optional; при включенной валидации количество должно совпадать с `frames[]`)

Ограничения:

- максимум `30` кадров за батч (настраивается в `app/core/config.py`)

Пример:

```bash
curl -X POST "http://127.0.0.1:8000/api/v1/scan/process" \
  -F "project_id=proj-123" \
  -F "room_id=room-001" \
  -F "scan_id=scan-abc" \
  -F "frames=@frame_001.jpg" \
  -F "trajectory=[{\"t\":0,\"position\":[0,0,0],\"rotation\":[0,0,0,1]}]"
```

### POST `/api/v1/scan/finish`

`application/json`

Тело:

```json
{
  "scan_id": "scan-abc",
  "project_id": "proj-123",
  "room_id": "room-001"
}
```

Пример:

```bash
curl -X POST "http://127.0.0.1:8000/api/v1/scan/finish" \
  -H "Content-Type: application/json" \
  -d "{\"scan_id\":\"scan-abc\",\"project_id\":\"proj-123\",\"room_id\":\"room-001\"}"
```

## 4) Текущий пайплайн обработки

`process_scan`:

1. Сохраняет входные кадры/depth во временную папку
2. Формирует `trajectory.json`
3. `load_frames_to_pointcloud(...)`
4. `detect_planes(...)` (RANSAC)
5. `find_junctions(...)`
6. Считает `dimensions`, `coverage`, `quality_metrics`
7. Сохраняет результат в in-memory сессию по `scan_id`

`finish_scan`:

- Возвращает сохраненную сессию + `artifacts`
- Если сессии нет — отдает fallback-ответ

## 5) Тесты

```bash
pytest -q
```

Покрыто:

- `tests/test_scan_api.py` — валидации `/process` и smoke-success
- `tests/test_finish_api.py` — `/finish` c сессией и без нее (fallback)

## 6) Удобные команды (Makefile)

```bash
make install   # установка зависимостей
make run       # локальный запуск uvicorn
make up        # запуск в Docker (build + up)
make down      # остановка Docker-сервиса
make logs      # логи контейнера
make test      # запуск тестов
make lint      # ruff + black --check
make format    # автоисправление ruff + black
```

## 7) CI

Добавлен workflow:

- `.github/workflows/python-scan-service.yml`

Что проверяется на `push`/`pull_request`:

1. установка зависимостей (`requirements.txt`)
2. `make lint`
3. `make test`


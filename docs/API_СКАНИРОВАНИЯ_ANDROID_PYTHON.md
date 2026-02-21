# API сканирования: Android ↔ Python backend

Контракт обмена данными между приложением PROFI-A (Android) и сервером обработки 3D-сканирования (Python).

---

## 1. Отправка данных с Android на сервер

### Endpoint

```
POST /api/v1/scan/process
Content-Type: multipart/form-data
```

### Тело запроса (form-data)

| Поле | Тип | Обязательно | Описание |
|------|-----|-------------|----------|
| `project_id` | string | да | ID проекта |
| `room_id` | string | да | ID комнаты или `"new"` |
| `scan_id` | string | да | ID сессии скана (UUID) |
| `frames[]` | file[] | да | Кадры JPEG (батч, например до 30 за запрос) |
| `trajectory` | JSON string | нет | Массив pose камеры для паутины по траектории |
| `depth[]` | file[] | нет | Карты глубины (если есть), порядок как у `frames[]` |

### Формат `trajectory` (JSON-строка)

```json
[
  {
    "t": 0.0,
    "position": [x, y, z],
    "rotation": [qx, qy, qz, qw]
  }
]
```

- `t` — время или индекс кадра (сек или int).
- `position` — метры, СК комнаты (например, пол = 0 по Y).
- `rotation` — кватернион камеры (опционально; если нет — сервер строит паутину только по позициям).

### Пример запроса (curl)

```bash
curl -X POST "https://api.example.com/api/v1/scan/process" \
  -F "project_id=proj-123" \
  -F "room_id=new" \
  -F "scan_id=550e8400-e29b-41d4-a716-446655440000" \
  -F "frames=@frame_001.jpg" \
  -F "frames=@frame_002.jpg" \
  -F "trajectory=[{\"t\":0,\"position\":[0,0,0],\"rotation\":[0,0,0,1]}]"
```

---

## 2. Ответ сервера (результат обработки)

### Успех: `200 OK`

```json
{
  "scan_id": "550e8400-e29b-41d4-a716-446655440000",
  "coverage": {
    "percentage": 92.5,
    "web_lines": [
      {
        "start": [x1, z1],
        "end": [x2, z2],
        "alpha": 0.3
      }
    ],
    "missing_zones": [
      {
        "boundary": [[x1,z1], [x2,z2], [x3,z3], [x4,z4]],
        "label": "unscanned"
      }
    ]
  },
  "junctions": [
    {
      "type": "floor_wall_internal",
      "position_3d": [x, y, z],
      "direction": [dx, dy, dz],
      "confidence": 0.95,
      "icon": "↘️"
    },
    {
      "type": "ceiling_wall_internal",
      "position_3d": [x, y, z],
      "direction": [dx, dy, dz],
      "confidence": 0.88,
      "icon": "⬆️"
    },
    {
      "type": "wall_wall_internal",
      "position_3d": [x, y, z],
      "vertical_line": { "bottom": [x,y,z], "top": [x,y,z] },
      "confidence": 0.91,
      "icon": "◀️▶️"
    }
  ],
  "dimensions": {
    "length_m": 4.2,
    "width_m": 3.5,
    "wall_height_m": 2.75,
    "perimeter_m": 15.4,
    "floor_area_m2": 14.7,
    "ceiling_area_m2": 14.7,
    "wall_area_m2": 42.35
  },
  "quality_metrics": {
    "scan_quality": 0.88,
    "junction_count": 12,
    "missing_corners": 0
  },
  "reveals": [
    {
      "opening_type": "door",
      "width_m": 0.9,
      "height_m": 2.1,
      "depth_m": 0.12,
      "position_3d": [1.2, 1.05, 0.5],
      "confidence": 0.85
    }
  ],
  "frame_planes": [
    {
      "width_m": 0.08,
      "height_m": 2.1,
      "linear_m": 2.1,
      "position_3d": [1.15, 1.05, 0.5],
      "direction": null,
      "plane_index": 0,
      "confidence": 0.8
    }
  ],
  "frame_linear_m_total": 4.2
}
```

### Описание полей ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `coverage.percentage` | number | Процент покрытия плана (0–100). |
| `coverage.web_lines` | array | Линии «паутины» в плоскости пола: `[x, z]` в метрах. |
| `coverage.missing_zones` | array | Полигоны пропущенных зон: `boundary` — массив точек `[x, z]`. |
| `junctions[].type` | string | Ключ из таблицы типов (см. ниже). |
| `junctions[].position_3d` | [x,y,z] | Точка в метрах, СК комнаты. |
| `junctions[].direction` | [dx,dy,dz] | Направление линии примыкания (единичный вектор). |
| `junctions[].vertical_line` | object | Только для wall_wall: нижняя и верхняя точки вертикального ребра. |
| `junctions[].confidence` | number | 0–1. |
| `junctions[].icon` | string | Эмодзи для легенды (опционально). |
| `dimensions` | object | Параметры помещения (по комнате): `length_m`, `width_m`, `wall_height_m`, `perimeter_m`, `floor_area_m2`, `ceiling_area_m2`, `wall_area_m2`. |
| `quality_metrics` | object | Сводка для UI (подсказки, прогресс). |
| `reveals` | array | **Откосы** (дверь/окно): размеры для раздела «Откосы». Элемент: `opening_type` ("door" \| "window"), `width_m`, `height_m`, `depth_m`, `position_3d`, `confidence`. |
| `frame_planes` | array | **Плоскости короба** по вертикали. Элемент: `width_m`, `height_m`, **`linear_m`** (погонные метры, м.п.), `position_3d`, `plane_index`, `confidence`. |
| `frame_linear_m_total` | number | Сумма погонных метров по всем плоскостям короба (м.п.) — для подстановки в раздел «Короба». |

### Типы углов (`junctions[].type`)

- `floor_wall_internal`, `floor_wall_external`
- `ceiling_wall_internal`, `ceiling_wall_external`
- `wall_wall_internal`, `wall_wall_external`
- `floor_ceiling_edge`
- `window_opening`, `door_opening`
- `niche_recess`

### Ошибки

- `400 Bad Request` — неверный формат (например, нет `frames` или невалидный `trajectory`). Тело: `{ "error": "code", "message": "..." }`.
- `413 Payload Too Large` — слишком много кадров в одном запросе.
- `500 Internal Server Error` — сбой обработки. Тело: `{ "error": "processing_failed", "message": "..." }`.

---

## 3. Финализация скана (сохранение результата)

### Endpoint

```
POST /api/v1/scan/finish
Content-Type: application/json
```

### Тело запроса

```json
{
  "scan_id": "550e8400-e29b-41d4-a716-446655440000",
  "project_id": "proj-123",
  "room_id": "new"
}
```

### Ответ `200 OK`

Повторно возвращается полный результат (как в п. 2), включая `reveals`, `frame_planes`, `frame_linear_m_total`, плюс при необходимости ссылки на сохранённые файлы:

```json
{
  "scan_id": "550e8400-e29b-41d4-a716-446655440000",
  "coverage": { ... },
  "junctions": [ ... ],
  "dimensions": { ... },
  "quality_metrics": { ... },
  "artifacts": {
    "mesh_url": "https://cdn.../scan_xxx.ply",
    "preview_url": "https://cdn.../scan_xxx.jpg",
    "json_url": "https://cdn.../scan_xxx.json"
  }
}
```

Android может сохранить в комнату/скан только `dimensions` и при необходимости `artifacts.json_url` для последующей загрузки полного JSON.

---

## 4. Проекция 3D → 2D на Android

Чтобы рисовать `junctions` и `web_lines` поверх превью камеры, нужно перевести 3D-точки в экранные координаты.

- **Входные данные с сервера:** `position_3d` в метрах в СК комнаты; на устройстве есть текущая pose камеры (из ARCore/CameraX или переданная с последним кадром).
- **Формула:** `screen = K @ (R | t) @ point_3d` (матрица камеры K, extrinsic [R|t]), затем перевод из однородных в 2D и обрезка по границам экрана.
- **Поле `coverage.web_lines` и `missing_zones.boundary`** заданы в плоскости пола `[x, z]`; для оверлея «сверху» их можно рисовать как 2D-график поверх уменьшенной карты; для вида «камера» — перевести точки пола в 3D (y = 0), затем спроецировать через ту же камеру.

Эти правила достаточно задать в коде один раз (функция `projectToScreen`), после чего все ответы сервера отображаются единообразно.

---

## 5. Сканирование документа (путь фото3д)

Режим **«Сканировать документ»**: один снимок документа → параметры (ширина, длина) и распознавание содержимого (картинка, инженерные коммуникации и т.д.).

### Endpoint

```
POST /api/v1/scan/document
Content-Type: multipart/form-data
```

### Тело запроса

| Поле | Тип | Обязательно | Описание |
|------|-----|-------------|----------|
| `scan_id` | string | да | ID сессии скана документа |
| `document` | file | да | Один файл изображения (JPEG или PNG) |

### Ответ 200 OK

```json
{
  "scan_id": "uuid",
  "width_mm": 210,
  "length_mm": 297,
  "content": [
    { "label": "инженерные_коммуникации", "confidence": 0.85 },
    { "label": "схема", "confidence": 0.7 }
  ],
  "has_engineering_communications": true
}
```

### Поля ответа

| Поле | Тип | Описание |
|------|-----|----------|
| `scan_id` | string | ID скана |
| `width_mm` | number | Ширина документа (мм) |
| `length_mm` | number | Длина документа (мм) |
| `content` | array | Распознанное содержимое: список `{ "label", "confidence" }`, опционально `bbox` [x1,y1,x2,y2] в долях 0..1 |
| `has_engineering_communications` | boolean | Признак наличия инженерных коммуникаций на изображении |

### Метки содержимого (content[].label)

- `инженерные_коммуникации` — общий класс
- `трубы`, `кабели`, `вентиляция`, `электропроводка`
- `схема`, `чертёж`, `текст`, `таблица`, `печать`, `подпись`, `другое`

Встраиваемая ИИ-модель для сканера документа реализована в `app/ml/document_analyzer.py`; при необходимости туда подключают свою модель распознавания (OCR, детекция объектов, классификация).

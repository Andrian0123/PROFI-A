"""
Анализ документа для пути фото3д/сканировать: параметры (ширина, длина) и распознавание содержимого.
В т.ч. инженерные коммуникации (трубы, кабели, вентиляция, электропроводка).
Встраиваемая ИИ-модель для сканера документа.
"""
from __future__ import annotations

from pathlib import Path
from typing import List, Optional, Tuple

from app.models.schemas import (
    ContentLabel,
    DocumentScanResult,
    RecognizedContentItem,
)


def _analyze_image_stub(
    image_path: Path,
) -> Tuple[float, float, List[Tuple[str, float]]]:
    """
    Заглушка анализа изображения. Возвращает (width_mm, length_mm, [(label, confidence), ...]).
    Реальная реализация: подставить вызов ИИ-модели (классификация + детекция + OCR/размеры).
    """
    # По размеру файла или разрешению можно грубо оценить «документ»; для точных размеров нужна эталонная линейка в кадре или модель.
    width_mm = 210.0  # A4 по умолчанию
    length_mm = 297.0
    # Заглушка распознавания: предполагаем возможное наличие инженерских коммуникаций
    content: List[Tuple[str, float]] = [
        ("инженерные_коммуникации", 0.5),
        ("схема", 0.4),
    ]
    return width_mm, length_mm, content


def _ensure_content_label(s: str) -> ContentLabel:
    """Привести строку к допустимому ContentLabel."""
    allowed = {
        "инженерные_коммуникации",
        "трубы",
        "кабели",
        "вентиляция",
        "электропроводка",
        "схема",
        "чертёж",
        "текст",
        "таблица",
        "печать",
        "подпись",
        "другое",
    }
    return s if s in allowed else "другое"


def analyze_document(
    image_path: str | Path,
    scan_id: str = "",
) -> DocumentScanResult:
    """
    Анализ одного изображения документа: ширина, длина, распознавание содержимого
    (инженерные коммуникации, трубы, кабели, схема и т.д.).

    Вход: путь к файлу изображения (JPEG/PNG).
    Выход: DocumentScanResult с width_mm, length_mm и списком content.
    """
    path = Path(image_path)
    if not path.is_file():
        return DocumentScanResult(
            scan_id=scan_id,
            width_mm=0.0,
            length_mm=0.0,
            content=[],
            has_engineering_communications=False,
        )

    width_mm, length_mm, raw_content = _analyze_image_stub(path)
    content_list: List[RecognizedContentItem] = [
        RecognizedContentItem(
            label=_ensure_content_label(label),
            confidence=conf,
        )
        for label, conf in raw_content
    ]
    engineering_labels = {"инженерные_коммуникации", "трубы", "кабели", "вентиляция", "электропроводка"}
    has_eng = any(
        item.label in engineering_labels and item.confidence >= 0.3
        for item in content_list
    )

    return DocumentScanResult(
        scan_id=scan_id,
        width_mm=width_mm,
        length_mm=length_mm,
        content=content_list,
        has_engineering_communications=has_eng,
    )

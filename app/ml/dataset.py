"""
Датасет для обучения модели классификации плоскостей.
Формат: директория с JSON-аннотациями и (опционально) сохранёнными признаками.
"""
from __future__ import annotations

import json
from pathlib import Path
from typing import List, Optional, Tuple

import numpy as np

from app.ml.features import extract_plane_features
from app.ml.model import PLANE_LABELS


def load_annotation_file(path: Path) -> List[Tuple[np.ndarray, str]]:
    """
    Загрузить один файл аннотаций.
    Ожидаемый формат JSON:
    {
      "planes": [
        { "features": [ ... ], "label": "wall" },
        ...
      ]
    }
    """
    raw = json.loads(path.read_text(encoding="utf-8"))
    planes = raw.get("planes", raw) if isinstance(raw, dict) else []
    out = []
    for p in planes:
        if isinstance(p, dict):
            feat = p.get("features")
            label = p.get("label", "wall")
            if feat is not None and label in PLANE_LABELS:
                out.append((np.array(feat, dtype=np.float64), label))
        elif isinstance(p, (list, tuple)) and len(p) >= 2:
            out.append((np.array(p[0], dtype=np.float64), str(p[1])))
    return out


def save_annotation_file(samples: List[Tuple[np.ndarray, str]], path: Path) -> None:
    """Сохранить аннотации в JSON."""
    planes = [
        {"features": f.tolist(), "label": label}
        for f, label in samples
    ]
    path.write_text(json.dumps({"planes": planes}, ensure_ascii=False, indent=2), encoding="utf-8")


def build_dataset_from_scan_dirs(
    scan_dirs: List[Path],
    annotation_suffix: str = "_planes.json",
) -> Tuple[np.ndarray, List[str]]:
    """
    Собрать X, y из нескольких директорий сканов.
    В каждой директории ожидается файл *_planes.json с аннотациями плоскостей.
    """
    X_list: List[np.ndarray] = []
    y_list: List[str] = []

    for d in scan_dirs:
        if not d.is_dir():
            continue
        for f in d.glob("*" + annotation_suffix):
            for feat, label in load_annotation_file(f):
                X_list.append(feat)
                y_list.append(label)

    if not X_list:
        return np.empty((0, 15)), []

    X = np.vstack(X_list)
    return X, y_list


def build_dataset_from_point_clouds(
    scan_list: List[Tuple[object, List[List[object]], List[str]]],
    distance_threshold: float = 0.05,
) -> Tuple[np.ndarray, List[str]]:
    """
    Собрать датасет из списка (point_cloud, planes, labels_per_plane).
    labels_per_plane: метка для каждой плоскости в том же порядке, что и planes.
    """
    X_list: List[np.ndarray] = []
    y_list: List[str] = []

    for point_cloud, planes, labels in scan_list:
        if len(labels) != len(planes):
            continue
        extracted = extract_plane_features(point_cloud, planes, distance_threshold)
        for (feat, _, _), label in zip(extracted, labels):
            if label in PLANE_LABELS:
                X_list.append(feat)
                y_list.append(label)

    if not X_list:
        return np.empty((0, 15)), []
    return np.vstack(X_list), y_list

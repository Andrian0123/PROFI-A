#!/usr/bin/env python3
"""
Скрипт обучения модели классификации плоскостей для 3D-сканера.
Использование:
  python -m app.ml.train --data_dir ./data/annotations [--model_dir ./app/ml/models]
  или передача датасета через JSON/файлы аннотаций.
"""
from __future__ import annotations

import argparse
from pathlib import Path

import numpy as np

from app.ml.dataset import build_dataset_from_scan_dirs, load_annotation_file
from app.ml.model import PlaneClassifier, PLANE_LABELS


def main() -> None:
    parser = argparse.ArgumentParser(description="Обучение модели классификации плоскостей (путь фото 3D сканер)")
    parser.add_argument("--data_dir", type=str, nargs="+", help="Директории с *_planes.json аннотациями")
    parser.add_argument("--model_dir", type=str, default=None, help="Директория для сохранения модели")
    parser.add_argument("--heuristic_only", action="store_true", help="Не обучать ML, только сохранить конфиг эвристики")
    args = parser.parse_args()

    model_dir = Path(args.model_dir) if args.model_dir else Path(__file__).resolve().parent / "models"
    data_dirs = [Path(d) for d in (args.data_dir or [])]

    if args.heuristic_only:
        clf = PlaneClassifier(use_heuristic_only=True)
        model_dir.mkdir(parents=True, exist_ok=True)
        clf.save(str(model_dir))
        print("Сохранена конфигурация эвристики в", model_dir)
        return

    if not data_dirs:
        print("Укажите --data_dir с аннотациями или --heuristic_only для эвристики без обучения.")
        return

    X, y = build_dataset_from_scan_dirs(data_dirs)
    if X.size == 0:
        print("Нет данных для обучения. Добавьте JSON-файлы с полем 'planes': [ { 'features': [...], 'label': 'wall' } ]")
        return

    print(f"Примеров: {X.shape[0]}, признаков: {X.shape[1]}")
    for lbl in PLANE_LABELS:
        n = sum(1 for a in y if a == lbl)
        if n:
            print(f"  {lbl}: {n}")

    clf = PlaneClassifier(use_heuristic_only=False)
    clf.fit(X, y)
    model_dir.mkdir(parents=True, exist_ok=True)
    clf.save(str(model_dir))
    print("Модель сохранена в", model_dir)


if __name__ == "__main__":
    main()

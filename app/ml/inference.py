"""
Вывод модели: по плоскостям и облаку точек — откосы (дверь/окно) и плоскости короба.
Размеры помещения уже считаются в scan_processor; здесь только раскладка по разделам.
"""
from __future__ import annotations

from typing import List, Optional, Tuple

import numpy as np

from pathlib import Path

from app.ml.features import extract_plane_features
from app.ml.model import PlaneClassifier
from app.models.schemas import Dimensions, FramePlane, Reveal

_DEFAULT_MODEL_DIR = Path(__file__).resolve().parent / "models"


def _load_classifier_if_exists() -> Optional[PlaneClassifier]:
    """Загружает обученную модель из app/ml/models при наличии."""
    clf = PlaneClassifier(use_heuristic_only=True)
    if (_DEFAULT_MODEL_DIR / "meta.json").exists() and clf.load(str(_DEFAULT_MODEL_DIR)):
        return clf
    return None


def _plane_extent_meters(inlier_points: Optional[np.ndarray]) -> Tuple[float, float, float]:
    """Ширина (X), высота (Y), глубина (Z) в метрах по inlier-точкам."""
    if inlier_points is None or inlier_points.size == 0:
        return 0.0, 0.0, 0.0
    p = inlier_points
    return float(np.ptp(p[:, 0])), float(np.ptp(p[:, 1])), float(np.ptp(p[:, 2]))


def run_scan_inference(
    point_cloud: object,
    planes: List[List[object]],
    dimensions: Dimensions,
    classifier: Optional[PlaneClassifier] = None,
    distance_threshold: float = 0.05,
    reveal_min_confidence: float = 0.6,
    frame_plane_min_confidence: float = 0.6,
    model_dir: Optional[str] = None,
) -> Tuple[List[Reveal], List[FramePlane]]:
    """
    По облаку точек и списку плоскостей определяет откосы (дверь/окно) и плоскости короба.
    Элементы с confidence ниже порогов не включаются в результат.

    Returns:
        (reveals, frame_planes)
    """
    extracted = extract_plane_features(point_cloud, planes, distance_threshold)
    if not extracted:
        return [], []

    if classifier is not None:
        clf = classifier
    elif model_dir:
        clf = PlaneClassifier(use_heuristic_only=True)
        if not clf.load(model_dir):
            clf = PlaneClassifier(use_heuristic_only=True)
    else:
        clf = _load_classifier_if_exists() or PlaneClassifier(use_heuristic_only=True)
    features = np.vstack([e[0] for e in extracted])
    labels = clf.predict(features)

    reveals: List[Reveal] = []
    frame_planes: List[FramePlane] = []

    for (feat, inlier_pts, _), label in zip(extracted, labels):
        w, h, d = _plane_extent_meters(inlier_pts)
        # Размеры в разумных пределах (метры)
        width_m = max(0.1, min(5.0, w + 0.05))
        height_m = max(0.1, min(4.0, h + 0.05))
        depth_m = max(0.0, min(1.0, d))

        centroid = inlier_pts.mean(axis=0) if inlier_pts is not None and inlier_pts.size else np.zeros(3)
        pos = [float(centroid[0]), float(centroid[1]), float(centroid[2])]

        conf_reveal = 0.85 if label == "door" or label == "window" else 0.7
        conf_frame = 0.8

        if label == "door":
            if conf_reveal >= reveal_min_confidence:
                reveals.append(Reveal(
                    opening_type="door",
                    width_m=width_m,
                    height_m=height_m,
                    depth_m=depth_m,
                    position_3d=pos,
                    confidence=conf_reveal,
                ))
        elif label == "window":
            if conf_reveal >= reveal_min_confidence:
                reveals.append(Reveal(
                    opening_type="window",
                    width_m=width_m,
                    height_m=height_m,
                    depth_m=depth_m,
                    position_3d=pos,
                    confidence=conf_reveal,
                ))
        elif label == "reveal":
            if conf_reveal >= reveal_min_confidence:
                reveals.append(Reveal(
                    opening_type="window",
                    width_m=width_m,
                    height_m=height_m,
                    depth_m=depth_m,
                    position_3d=pos,
                    confidence=conf_reveal,
                ))
        elif label == "frame":
            if conf_frame >= frame_plane_min_confidence:
                linear_m = height_m
                frame_planes.append(FramePlane(
                    width_m=width_m,
                    height_m=height_m,
                    linear_m=linear_m,
                    position_3d=pos,
                    direction=None,
                    plane_index=len(frame_planes),
                    confidence=conf_frame,
                ))

    return reveals, frame_planes

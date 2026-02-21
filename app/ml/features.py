"""
Извлечение признаков из облака точек и плоскостей для ML.
Используется для классификации: стена / пол / потолок / дверь / окно / откос / короб.
"""
from __future__ import annotations

from typing import List, Optional, Tuple

import numpy as np


def _plane_distance(points: np.ndarray, normal: np.ndarray, d: float) -> np.ndarray:
    """Расстояние от точек до плоскости n·x + d = 0."""
    return np.abs(points @ np.asarray(normal, dtype=np.float64) + d)


def _inliers_for_plane(
    points: np.ndarray,
    normal: np.ndarray,
    d: float,
    distance_threshold: float = 0.05,
) -> np.ndarray:
    """Индексы точек, принадлежащих плоскости."""
    dist = _plane_distance(points, normal, d)
    return np.flatnonzero(dist <= distance_threshold)


def _vertical_extent(inlier_points: np.ndarray) -> float:
    """Высота по Y (вертикаль)."""
    if inlier_points.size == 0:
        return 0.0
    return float(np.ptp(inlier_points[:, 1]))


def _horizontal_extents(inlier_points: np.ndarray) -> Tuple[float, float]:
    """Размах по X и Z (горизонталь)."""
    if inlier_points.size == 0:
        return 0.0, 0.0
    return float(np.ptp(inlier_points[:, 0])), float(np.ptp(inlier_points[:, 2]))


def extract_plane_features(
    point_cloud: object,
    planes: List[List[object]],
    distance_threshold: float = 0.05,
) -> List[Tuple[np.ndarray, Optional[np.ndarray], int]]:
    """
    Для каждой плоскости из списка (формат [normal, d]) выделяет inlier-точки
    и считает вектор признаков.

    Args:
        point_cloud: Open3D PointCloud или объект с .points
        planes: список [normal, d], normal = [nx, ny, nz]
        distance_threshold: порог расстояния до плоскости (м)

    Returns:
        Список (feature_vector, inlier_points, inlier_count) для каждой плоскости.
        inlier_points может быть None при отсутствии точек.
    """
    try:
        points = np.asarray(point_cloud.points, dtype=np.float64)
    except Exception:
        points = np.empty((0, 3), dtype=np.float64)

    if points.size == 0:
        return []

    result: List[Tuple[np.ndarray, Optional[np.ndarray], int]] = []

    for plane_item in planes:
        if not isinstance(plane_item, list) or len(plane_item) != 2:
            continue
        normal_raw, d_raw = plane_item[0], plane_item[1]
        try:
            normal = np.asarray(normal_raw, dtype=np.float64)
            if normal.shape != (3,):
                continue
            nnorm = np.linalg.norm(normal)
            if nnorm < 1e-10:
                continue
            normal = normal / nnorm
            d = float(d_raw) / nnorm
        except (TypeError, ValueError):
            continue

        idx = _inliers_for_plane(points, normal, d, distance_threshold)
        inlier_points = points[idx] if len(idx) > 0 else np.empty((0, 3))
        n_inliers = len(idx)

        # Признаки
        ny = float(normal[1])
        is_horizontal = abs(ny) >= 0.8
        is_vertical = abs(ny) < 0.35

        centroid = inlier_points.mean(axis=0) if inlier_points.size > 0 else np.zeros(3)
        height_y = _vertical_extent(inlier_points)
        ext_x, ext_z = _horizontal_extents(inlier_points)

        # Площадь (приближение): для вертикальной плоскости = height * width (max of ext_x, ext_z)
        if is_vertical:
            area_approx = height_y * max(ext_x, ext_z, 1e-6)
        else:
            area_approx = ext_x * ext_z if (ext_x > 1e-6 and ext_z > 1e-6) else 0.0

        aspect = height_y / max(ext_x, ext_z, 1e-6) if is_vertical else max(ext_x, ext_z) / max(height_y, 1e-6)

        # Вектор признаков для классификатора
        feature = np.array([
            normal[0], normal[1], normal[2],
            centroid[0], centroid[1], centroid[2],
            height_y, ext_x, ext_z,
            area_approx,
            aspect,
            1.0 if is_horizontal else 0.0,
            1.0 if is_vertical else 0.0,
            np.log1p(n_inliers),
        ], dtype=np.float64)

        result.append((feature, inlier_points if inlier_points.size > 0 else None, n_inliers))

    return result


def plane_features_to_vector(feature: np.ndarray) -> np.ndarray:
    """Нормализация/масштабирование признаков (опционально)."""
    return np.asarray(feature, dtype=np.float64)

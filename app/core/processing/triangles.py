"""
Треугольники для точных размеров помещения.

В строительстве используют правило 3–4–5: если два катета 3 м и 4 м, диагональ 5 м,
то угол прямой (3² + 4² = 5²). По длине, ширине и диагонали можно проверять и уточнять размеры.
Триангуляция (разбиение на треугольники) позволяет вычислять мелкие детали.
"""
from __future__ import annotations

from typing import List, Optional, Tuple

import numpy as np

# Допуск для проверки прямоугольного треугольника (в относительных единицах)
_RIGHT_ANGLE_TOLERANCE = 0.02  # ~2%


def is_right_triangle_345(a: float, b: float, c: float) -> bool:
    """
    Проверка: является ли треугольник прямоугольным (правило 3–4–5).
    Катеты a, b, гипотенуза c. Возвращает True, если c² ≈ a² + b².
    """
    if a <= 0 or b <= 0 or c <= 0:
        return False
    # c должна быть наибольшей стороной (гипотенуза)
    if c < a or c < b:
        return False
    expected_c_sq = a * a + b * b
    actual_c_sq = c * c
    rel = abs(actual_c_sq - expected_c_sq) / max(expected_c_sq, 1e-10)
    return rel <= _RIGHT_ANGLE_TOLERANCE


def scale_to_345(a: float, b: float, c: float) -> Optional[Tuple[float, float, float]]:
    """
    Если треугольник близок к 3–4–5, возвращает масштабированные стороны
    так, чтобы получилось 3 : 4 : 5 (гипотенуза — третья).
    Иначе None.
    """
    if a <= 0 or b <= 0 or c <= 0:
        return None
    # Гипотенуза — наибольшая
    if c < a or c < b:
        return None
    if not is_right_triangle_345(a, b, c):
        return None
    # Масштаб: k так, что k*c = 5 => k = 5/c; тогда k*a ≈ 3, k*b ≈ 4
    k = 5.0 / c
    return (float(k * a), float(k * b), 5.0)


def refine_length_width_by_diagonal(
    length_m: float,
    width_m: float,
    diagonal_m: float,
) -> Tuple[float, float]:
    """
    Уточнение длины и ширины по измеренной диагонали (комната как прямоугольник).
    Если diagonal² ≈ length² + width², возвращаем (length, width).
    Иначе подбираем масштаб так, чтобы выполнялось равенство диагонали,
    сохраняя отношение length/width.
    """
    if length_m <= 0 or width_m <= 0 or diagonal_m <= 0:
        return length_m, width_m
    current_diag_sq = length_m * length_m + width_m * width_m
    target_diag_sq = diagonal_m * diagonal_m
    if abs(current_diag_sq - target_diag_sq) < 1e-6 * max(current_diag_sq, target_diag_sq):
        return length_m, width_m
    # Масштаб: k так, что (k*L)² + (k*W)² = diag² => k = diag / sqrt(L²+W²)
    scale = diagonal_m / np.sqrt(current_diag_sq)
    return (float(length_m * scale), float(width_m * scale))


def triangle_sides_from_corners_2d(points: np.ndarray) -> List[Tuple[float, float, float]]:
    """
    По трём точкам в плоскости (N×2 или N×3, берём XZ) возвращает длины сторон (a, b, c).
    points: массив из 3 точек.
    """
    if points is None or points.shape[0] < 3:
        return []
    p = np.asarray(points, dtype=np.float64)
    if p.shape[1] == 3:
        p = p[:, [0, 2]]
    if p.shape[1] != 2 or p.shape[0] != 3:
        return []
    a = float(np.linalg.norm(p[1] - p[0]))
    b = float(np.linalg.norm(p[2] - p[1]))
    c = float(np.linalg.norm(p[0] - p[2]))
    return [(a, b, c)]


def room_rect_from_diagonals_and_sides(
    side_a_m: float,
    side_b_m: float,
    diagonal_m: float,
) -> Optional[Tuple[float, float]]:
    """
    Комната — прямоугольник. Известны две смежные стороны и диагональ.
    Проверка: diagonal² = side_a² + side_b². Если выполняется (с допуском),
    возвращаем (length, width) — большую и меньшую сторону.
    Иначе уточняем по диагонали (refine_length_width_by_diagonal).
    """
    if side_a_m <= 0 or side_b_m <= 0 or diagonal_m <= 0:
        return None
    length_m = max(side_a_m, side_b_m)
    width_m = min(side_a_m, side_b_m)
    refined_l, refined_w = refine_length_width_by_diagonal(length_m, width_m, diagonal_m)
    return (max(refined_l, refined_w), min(refined_l, refined_w))


def triangulate_corners_floor_plan(corners_xz: np.ndarray) -> List[Tuple[int, int, int]]:
    """
    Разбиение контура пола (углы комнаты в XZ) на треугольники.
    corners_xz: N×2, порядок обхода по контуру.
    Возвращает список индексов (i, j, k) — треугольников для мелких деталей.
    Простая триангуляция: веер из первой вершины (для выпуклого многоугольника).
    """
    corners_xz = np.asarray(corners_xz, dtype=np.float64)
    if corners_xz.shape[0] < 3:
        return []
    n = corners_xz.shape[0]
    return [(0, i, (i + 1) % n) for i in range(1, n - 1)]


def triangle_area_heron(a: float, b: float, c: float) -> float:
    """Площадь треугольника по трём сторонам (формула Герона)."""
    if a <= 0 or b <= 0 or c <= 0:
        return 0.0
    p = (a + b + c) / 2.0
    if p <= a or p <= b or p <= c:
        return 0.0
    return float(np.sqrt(p * (p - a) * (p - b) * (p - c)))

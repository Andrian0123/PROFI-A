"""Схемы для обучения и аннотаций ML-модели сканера."""
from __future__ import annotations

from typing import List, Literal, Optional, Tuple

import numpy as np

# Метки плоскостей для обучения
PlaneLabel = Literal[
    "wall",
    "floor",
    "ceiling",
    "door",
    "window",
    "reveal",   # откос
    "frame",    # плоскость короба
]

# Метки для junction (углы)
JunctionLabel = Literal[
    "wall_wall_internal",
    "wall_wall_external",
    "floor_wall_internal",
    "ceiling_wall_internal",
    "window_opening",
    "door_opening",
    "frame_corner",  # угол короба
]

# Один пример плоскости: признаки + метка (для датасета)
PlaneSample = Tuple[np.ndarray, str]  # (feature_vector, PlaneLabel)

# Аннотация кадра/скана: список плоскостей с метками
ScanAnnotation = List[Tuple[np.ndarray, str]]  # list of (features, label)

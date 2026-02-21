from __future__ import annotations

from typing import Dict, List, Optional, Tuple

import numpy as np


def _as_plane_tuple(plane_item: List[object]) -> Optional[Tuple[np.ndarray, float]]:
    """
    Convert plane item [normal, d] into (n, d), where n is np.ndarray(3,).
    """
    if not isinstance(plane_item, list) or len(plane_item) != 2:
        return None
    normal_raw, d_raw = plane_item
    if not isinstance(normal_raw, list) or len(normal_raw) != 3:
        return None
    try:
        n = np.asarray(normal_raw, dtype=np.float64)
        d = float(d_raw)
    except (TypeError, ValueError):
        return None
    norm = np.linalg.norm(n)
    if norm == 0:
        return None
    return n / norm, d / norm


def _line_from_two_planes(
    n1: np.ndarray,
    d1: float,
    n2: np.ndarray,
    d2: float,
) -> Optional[Tuple[np.ndarray, np.ndarray]]:
    """
    Return intersection line of two planes as (point_on_line, direction).
    Plane: n.x + d = 0
    """
    direction = np.cross(n1, n2)
    dir_norm = np.linalg.norm(direction)
    if dir_norm < 1e-8:
        return None
    direction = direction / dir_norm

    # Solve minimal-norm point on intersection:
    # [n1^T; n2^T; direction^T] * x = [-d1; -d2; 0]
    a = np.vstack([n1, n2, direction])
    b = np.array([-d1, -d2, 0.0], dtype=np.float64)
    try:
        point = np.linalg.solve(a, b)
    except np.linalg.LinAlgError:
        point, *_ = np.linalg.lstsq(a, b, rcond=None)

    return point, direction


def _is_horizontal(normal: np.ndarray) -> bool:
    return abs(float(normal[1])) >= 0.8


def _is_vertical(normal: np.ndarray) -> bool:
    return abs(float(normal[1])) < 0.35


def _junction_type(n1: np.ndarray, n2: np.ndarray, idx1: int, idx2: int) -> str:
    """
    Simple heuristic:
    - first horizontal plane in list is floor
    - second horizontal plane in list is ceiling
    - vertical+vertical -> wall_wall_internal
    - otherwise -> generic floor/ceiling-wall based on horizontal index
    """
    h1, h2 = _is_horizontal(n1), _is_horizontal(n2)
    v1, v2 = _is_vertical(n1), _is_vertical(n2)

    if v1 and v2:
        return "wall_wall_internal"

    # One horizontal + one vertical
    if h1 and v2:
        return "floor_wall_internal" if idx1 == 0 else "ceiling_wall_internal"
    if h2 and v1:
        return "floor_wall_internal" if idx2 == 0 else "ceiling_wall_internal"

    # Fallback for less strict normals.
    if h1 or h2:
        horizontal_idx = idx1 if h1 else idx2
        return "floor_wall_internal" if horizontal_idx == 0 else "ceiling_wall_internal"

    return "wall_wall_internal"


def _junction_confidence(n1: np.ndarray, n2: np.ndarray) -> float:
    """
    Confidence heuristic based on plane orthogonality:
    - 90deg intersection -> high confidence
    - near-parallel planes -> low confidence
    """
    cos = float(abs(np.dot(n1, n2)))
    # cos ~0 => orthogonal => confidence high
    confidence = 1.0 - cos
    return float(np.clip(confidence, 0.0, 1.0))


def find_junctions(planes: List[List[object]]) -> List[Dict[str, object]]:
    """
    Find room junctions from detected planes.

    Args:
        planes: list in format [normal, d], e.g.
            [
              [[nx, ny, nz], d],  # floor (optional)
              [[nx, ny, nz], d],  # ceiling (optional)
              [[nx, ny, nz], d],  # wall...
            ]

    Returns:
        List of dicts:
            {
              "type": "<junction_type>",
              "position_3d": [x, y, z],
              "direction": [dx, dy, dz],
              "confidence": 0..1
            }
    """
    parsed: List[Tuple[np.ndarray, float, int]] = []
    for idx, p in enumerate(planes):
        plane = _as_plane_tuple(p)
        if plane is None:
            continue
        n, d = plane
        parsed.append((n, d, idx))

    if len(parsed) < 2:
        return []

    junctions: List[Dict[str, object]] = []
    seen = set()

    for i in range(len(parsed)):
        n1, d1, idx1 = parsed[i]
        for j in range(i + 1, len(parsed)):
            n2, d2, idx2 = parsed[j]

            line = _line_from_two_planes(n1, d1, n2, d2)
            if line is None:
                continue
            point_on_line, direction = line

            # Deduplicate very close points.
            key = tuple(np.round(point_on_line, 3))
            if key in seen:
                continue
            seen.add(key)

            j_type = _junction_type(n1, n2, idx1, idx2)
            conf = _junction_confidence(n1, n2)
            junctions.append(
                {
                    "type": j_type,
                    "position_3d": [float(point_on_line[0]), float(point_on_line[1]), float(point_on_line[2])],
                    "direction": [float(direction[0]), float(direction[1]), float(direction[2])],
                    "confidence": conf,
                }
            )

    return junctions


from __future__ import annotations

from typing import List

import numpy as np
import open3d as o3d


def _normalize_plane(plane_model: np.ndarray) -> np.ndarray:
    """
    Normalize plane coefficients [a, b, c, d] so that ||[a,b,c]|| == 1.
    """
    normal = plane_model[:3]
    norm = np.linalg.norm(normal)
    if norm == 0:
        return plane_model
    return plane_model / norm


def detect_planes(
    point_cloud: o3d.geometry.PointCloud,
    distance_threshold: float = 0.03,
    ransac_n: int = 3,
    num_iterations: int = 1000,
    max_planes: int = 8,
    min_inliers: int = 500,
) -> List[List[object]]:
    """
    Detect floor, ceiling and wall planes from a point cloud using RANSAC.

    Args:
        point_cloud: Input Open3D point cloud.
        distance_threshold: Max point-to-plane distance for inliers.
        ransac_n: Number of points used to estimate one plane.
        num_iterations: Number of RANSAC iterations.
        max_planes: Maximum number of planes to extract.
        min_inliers: Minimum inliers required to accept a plane.

    Returns:
        List of planes in format [normal, d], where:
          - normal: [nx, ny, nz] (unit vector)
          - d: float from plane equation nx*x + ny*y + nz*z + d = 0

        Order of output:
          1) floor (if detected)
          2) ceiling (if detected)
          3) walls (0..N)
    """
    if not isinstance(point_cloud, o3d.geometry.PointCloud) or len(point_cloud.points) == 0:
        return []

    remaining = point_cloud
    candidates = []

    for _ in range(max_planes):
        if len(remaining.points) < max(min_inliers, ransac_n):
            break

        plane_model, inliers = remaining.segment_plane(
            distance_threshold=distance_threshold,
            ransac_n=ransac_n,
            num_iterations=num_iterations,
        )

        if len(inliers) < min_inliers:
            break

        plane = _normalize_plane(np.asarray(plane_model, dtype=np.float64))
        inlier_cloud = remaining.select_by_index(inliers)
        inlier_points = np.asarray(inlier_cloud.points)
        centroid = inlier_points.mean(axis=0) if inlier_points.size else np.zeros(3)

        candidates.append(
            {
                "plane": plane,           # [a, b, c, d], normal is normalized
                "normal": plane[:3],
                "d": float(plane[3]),
                "centroid": centroid,     # used to split floor/ceiling
                "inliers_count": len(inliers),
            }
        )

        remaining = remaining.select_by_index(inliers, invert=True)

    if not candidates:
        return []

    # Horizontal planes: normal mostly aligned with Y axis => |ny| close to 1.
    # Vertical planes (walls): normal mostly in XZ plane => |ny| close to 0.
    horizontal = [c for c in candidates if abs(float(c["normal"][1])) >= 0.8]
    walls = [c for c in candidates if abs(float(c["normal"][1])) < 0.8]

    floor_plane = None
    ceiling_plane = None

    if horizontal:
        # Split by centroid height. Lowest horizontal plane -> floor.
        horizontal_sorted = sorted(horizontal, key=lambda c: float(c["centroid"][1]))
        floor_plane = horizontal_sorted[0]
        if len(horizontal_sorted) > 1:
            ceiling_plane = horizontal_sorted[-1]

    result: List[List[object]] = []

    if floor_plane is not None:
        n = floor_plane["normal"].tolist()
        result.append([n, float(floor_plane["d"])])

    if ceiling_plane is not None and ceiling_plane is not floor_plane:
        n = ceiling_plane["normal"].tolist()
        result.append([n, float(ceiling_plane["d"])])

    # Keep biggest wall planes first.
    walls_sorted = sorted(walls, key=lambda c: int(c["inliers_count"]), reverse=True)
    for wall in walls_sorted:
        n = wall["normal"].tolist()
        result.append([n, float(wall["d"])])

    return result


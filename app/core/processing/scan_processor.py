from __future__ import annotations

import json
import tempfile
import time
from pathlib import Path
from typing import Dict, List, Optional, Tuple

import numpy as np

from fastapi import UploadFile

from app.core.config import settings
from app.core.processing.junctions import find_junctions
from app.core.processing.point_cloud import load_frames_to_pointcloud
from app.core.processing.ransac import detect_planes
from app.ml.inference import run_scan_inference
from app.models.schemas import (
    Artifacts,
    CoverageData,
    CoverageWebLine,
    Dimensions,
    Junction,
    MissingZone,
    QualityMetrics,
    Reveal,
    FramePlane,
    ScanFinishRequest,
    ScanFinishResponse,
    ScanProcessResponse,
    TrajectoryPoint,
)


class ScanProcessor:
    """
    Заглушка под будущую интеграцию Open3D/ML.
    В реальной реализации здесь:
    - декодирование кадров/depth;
    - реконструкция/поиск плоскостей;
    - вычисление junctions и dimensions.
    """
    def __init__(self) -> None:
        self._sessions: Dict[str, ScanProcessResponse] = {}

    @staticmethod
    def _build_coverage_from_trajectory(
        point_cloud: object,
        trajectory: Optional[List[TrajectoryPoint]],
        frames_count: int,
    ) -> CoverageData:
        web_lines: List[CoverageWebLine] = []
        if trajectory and len(trajectory) > 1:
            for i in range(len(trajectory) - 1):
                p1 = trajectory[i].position
                p2 = trajectory[i + 1].position
                web_lines.append(
                    CoverageWebLine(
                        start=[float(p1[0]), float(p1[2])],
                        end=[float(p2[0]), float(p2[2])],
                        alpha=0.25,
                    )
                )

        missing_zones, cloud_coverage = ScanProcessor._compute_missing_zones(
            point_cloud,
            cell_size_m=settings.processing.occupancy_cell_size_m,
        )
        # Blend point-cloud coverage with frame progress so early scans are not 0%.
        percentage = 0.7 * cloud_coverage + 0.3 * min(100.0, 10.0 + frames_count * 2.5)
        return CoverageData(
            percentage=float(np.clip(percentage, 0.0, 100.0)),
            web_lines=web_lines,
            missing_zones=missing_zones,
        )

    @staticmethod
    def _compute_dimensions(
        point_cloud: object,
        ceiling_height_fraction: float = 0.55,
    ) -> Dimensions:
        try:
            points = np.asarray(point_cloud.points)
        except Exception:
            points = np.empty((0, 3))

        if points.size == 0:
            return Dimensions(
                length_m=0.0,
                width_m=0.0,
                wall_height_m=2.7,
                perimeter_m=0.0,
                floor_area_m2=0.0,
                ceiling_area_m2=0.0,
                wall_area_m2=0.0,
                diagonal_m=None,
            )

        min_xyz = points.min(axis=0)
        max_xyz = points.max(axis=0)
        extent = np.maximum(max_xyz - min_xyz, 0.0)
        height = float(extent[1])

        # Длина и ширина — по верхней части (потолок), чтобы не учитывать фоновый шум на полу
        y_min, y_max = float(min_xyz[1]), float(max_xyz[1])
        threshold = y_min + (y_max - y_min) * ceiling_height_fraction
        upper = points[points[:, 1] >= threshold]
        if upper.size > 0:
            min_xz = upper[:, [0, 2]].min(axis=0)
            max_xz = upper[:, [0, 2]].max(axis=0)
            dim_x = float(np.maximum(max_xz[0] - min_xz[0], 0.0))
            dim_z = float(np.maximum(max_xz[1] - min_xz[1], 0.0))
        else:
            dim_x = float(extent[0])
            dim_z = float(extent[2])

        length_m = max(dim_x, dim_z)
        width_m = min(dim_x, dim_z)
        diagonal_m = float(np.sqrt(length_m * length_m + width_m * width_m))
        perimeter_m = 2.0 * (length_m + width_m)
        floor_area_m2 = length_m * width_m
        ceiling_area_m2 = floor_area_m2
        wall_area_m2 = perimeter_m * height

        return Dimensions(
            length_m=length_m,
            width_m=width_m,
            wall_height_m=height,
            perimeter_m=perimeter_m,
            floor_area_m2=floor_area_m2,
            ceiling_area_m2=ceiling_area_m2,
            wall_area_m2=wall_area_m2,
            diagonal_m=diagonal_m,
        )

    @staticmethod
    def _compute_missing_zones(
        point_cloud: object,
        cell_size_m: float,
    ) -> Tuple[List[MissingZone], float]:
        """
        Build a simple occupancy grid on XZ plane and return:
        - missing zones as coarse rectangular polygons
        - coverage percentage
        """
        try:
            points = np.asarray(point_cloud.points, dtype=np.float64)
        except Exception:
            points = np.empty((0, 3), dtype=np.float64)

        if points.size == 0:
            return [], 0.0

        x = points[:, 0]
        z = points[:, 2]
        min_x, max_x = float(x.min()), float(x.max())
        min_z, max_z = float(z.min()), float(z.max())

        span_x = max_x - min_x
        span_z = max_z - min_z
        if span_x <= 1e-6 or span_z <= 1e-6:
            return [], 0.0

        nx = max(1, int(np.ceil(span_x / cell_size_m)))
        nz = max(1, int(np.ceil(span_z / cell_size_m)))
        grid = np.zeros((nx, nz), dtype=bool)

        ix = np.clip(((x - min_x) / cell_size_m).astype(int), 0, nx - 1)
        iz = np.clip(((z - min_z) / cell_size_m).astype(int), 0, nz - 1)
        grid[ix, iz] = True

        occupied = int(grid.sum())
        total = int(grid.size)
        coverage_percent = 100.0 * occupied / max(1, total)

        empty = ~grid
        visited = np.zeros_like(empty, dtype=bool)
        missing_zones: List[MissingZone] = []

        # Flood fill connected empty regions; keep only meaningful ones.
        for sx in range(nx):
            for sz in range(nz):
                if not empty[sx, sz] or visited[sx, sz]:
                    continue

                stack = [(sx, sz)]
                visited[sx, sz] = True
                cells: List[Tuple[int, int]] = []

                while stack:
                    cx, cz = stack.pop()
                    cells.append((cx, cz))
                    for nxn, nzn in ((cx - 1, cz), (cx + 1, cz), (cx, cz - 1), (cx, cz + 1)):
                        if 0 <= nxn < nx and 0 <= nzn < nz and empty[nxn, nzn] and not visited[nxn, nzn]:
                            visited[nxn, nzn] = True
                            stack.append((nxn, nzn))

                # Ignore tiny holes.
                if len(cells) < settings.processing.tiny_hole_cells_threshold:
                    continue

                xs = [c[0] for c in cells]
                zs = [c[1] for c in cells]
                min_cx, max_cx = min(xs), max(xs)
                min_cz, max_cz = min(zs), max(zs)

                x1 = min_x + min_cx * cell_size_m
                x2 = min_x + (max_cx + 1) * cell_size_m
                z1 = min_z + min_cz * cell_size_m
                z2 = min_z + (max_cz + 1) * cell_size_m

                missing_zones.append(
                    MissingZone(
                        boundary=[[x1, z1], [x2, z1], [x2, z2], [x1, z2]],
                        label="unscanned",
                    )
                )

        # Keep only top few largest zones (by bbox area).
        def zone_area(zone: MissingZone) -> float:
            (ax, az), (bx, _), (_, dz), _ = zone.boundary
            return abs((bx - ax) * (dz - az))

        missing_zones.sort(key=zone_area, reverse=True)
        missing_zones = missing_zones[: settings.processing.max_missing_zones]

        return missing_zones, float(np.clip(coverage_percent, 0.0, 100.0))

    async def process_scan(
        self,
        project_id: str,
        room_id: str,
        scan_id: str,
        frames: List[UploadFile],
        trajectory: Optional[List[TrajectoryPoint]] = None,
        depth: Optional[List[UploadFile]] = None,
    ) -> ScanProcessResponse:
        _ = (project_id, room_id, depth)
        started_at = time.perf_counter()

        frame_paths: List[str] = []
        depth_paths: List[str] = []
        with tempfile.TemporaryDirectory(prefix="scan_processor_") as tmpdir:
            tmp = Path(tmpdir)
            for idx, frame in enumerate(frames):
                ext = Path(frame.filename or "").suffix or ".jpg"
                frame_path = tmp / f"frame_{idx:04d}{ext}"
                frame_bytes = await frame.read()
                frame_path.write_bytes(frame_bytes)
                frame_paths.append(str(frame_path))

            for idx, depth_item in enumerate(depth or []):
                depth_ext = Path(depth_item.filename or "").suffix or ".png"
                depth_path = tmp / f"depth_{idx:04d}{depth_ext}"
                depth_bytes = await depth_item.read()
                depth_path.write_bytes(depth_bytes)
                depth_paths.append(str(depth_path))

            trajectory_path = tmp / "trajectory.json"
            trajectory_payload = [tp.model_dump() for tp in (trajectory or [])]
            trajectory_path.write_text(
                json.dumps(trajectory_payload, ensure_ascii=False),
                encoding="utf-8",
            )

            point_cloud = load_frames_to_pointcloud(
                frame_paths=frame_paths,
                trajectory_json_path=str(trajectory_path),
                depth_paths=depth_paths if depth_paths else None,
            )
            planes = detect_planes(
                point_cloud=point_cloud,
                distance_threshold=settings.processing.ransac_distance_threshold,
                ransac_n=settings.processing.ransac_n,
                num_iterations=settings.processing.ransac_iterations,
                max_planes=settings.processing.ransac_max_planes,
                min_inliers=settings.processing.ransac_min_inliers,
            )
            raw_junctions = find_junctions(planes)

        junctions: List[Junction] = [
            Junction(
                type=item["type"],
                position_3d=item["position_3d"],
                direction=item.get("direction"),
                confidence=float(item.get("confidence", 0.75)),
                icon=None,
            )
            for item in raw_junctions
        ]

        dimensions = self._compute_dimensions(
            point_cloud,
            ceiling_height_fraction=settings.processing.ceiling_height_fraction,
        )
        coverage = self._build_coverage_from_trajectory(point_cloud, trajectory, len(frames))
        reveals: List[Reveal] = []
        frame_planes: List[FramePlane] = []
        try:
            reveals, frame_planes = run_scan_inference(
                point_cloud,
                planes,
                dimensions,
                reveal_min_confidence=settings.processing.reveal_min_confidence,
                frame_plane_min_confidence=settings.processing.frame_plane_min_confidence,
                model_dir=settings.processing.ml_model_dir or None,
            )
        except Exception:
            pass
        frame_linear_m_total = sum(fp.linear_m for fp in frame_planes)

        wall_wall_count = sum(1 for j in junctions if j.type == "wall_wall_internal")
        avg_junction_conf = (
            float(np.mean([j.confidence for j in junctions])) if junctions else 0.0
        )
        points_count = 0
        try:
            points_count = len(point_cloud.points)
        except Exception:
            points_count = 0
        density_score = min(1.0, points_count / float(settings.processing.density_points_norm))

        quality_score = (
            settings.processing.quality_weight_coverage * (coverage.percentage / 100.0)
            + settings.processing.quality_weight_junction_conf * avg_junction_conf
            + settings.processing.quality_weight_density * density_score
        )
        processing_time_ms = int((time.perf_counter() - started_at) * 1000)
        quality = QualityMetrics(
            scan_quality=float(np.clip(quality_score, 0.0, 1.0)),
            junction_count=len(junctions),
            missing_corners=max(0, 4 - min(4, wall_wall_count)),
            processing_time_ms=processing_time_ms,
            points_count=points_count,
            planes_count=len(planes),
        )

        response = ScanProcessResponse(
            scan_id=scan_id,
            coverage=coverage,
            junctions=junctions,
            dimensions=dimensions,
            quality_metrics=quality,
            reveals=reveals,
            frame_planes=frame_planes,
            frame_linear_m_total=frame_linear_m_total,
        )
        self._sessions[scan_id] = response
        return response

    async def finish_scan(self, payload: ScanFinishRequest) -> ScanFinishResponse:
        base = self._sessions.get(payload.scan_id)
        if base is None:
            base = ScanProcessResponse(
                scan_id=payload.scan_id,
                coverage=CoverageData(
                    percentage=0.0,
                    web_lines=[],
                    missing_zones=[MissingZone(boundary=[[0.0, 0.0]], label="no_data")],
                ),
                junctions=[],
                dimensions=Dimensions(
                    length_m=0.0,
                    width_m=0.0,
                    wall_height_m=0.0,
                    perimeter_m=0.0,
                    floor_area_m2=0.0,
                    ceiling_area_m2=0.0,
                    wall_area_m2=0.0,
                    diagonal_m=None,
                ),
                quality_metrics=QualityMetrics(scan_quality=0.0, junction_count=0, missing_corners=4),
            )

        return ScanFinishResponse(
            **base.model_dump(),
            artifacts=Artifacts(
                mesh_url=f"https://cdn.example.com/scans/{payload.scan_id}.ply",
                preview_url=f"https://cdn.example.com/scans/{payload.scan_id}.jpg",
                json_url=f"https://cdn.example.com/scans/{payload.scan_id}.json",
            ),
        )


from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class ApiLimits:
    max_frames_per_batch: int = 30
    require_depth_count_match: bool = True


@dataclass(frozen=True)
class ProcessingConfig:
    # RANSAC
    ransac_distance_threshold: float = 0.03
    ransac_n: int = 3
    ransac_iterations: int = 1000
    ransac_max_planes: int = 8
    ransac_min_inliers: int = 500

    # Coverage / missing zones
    occupancy_cell_size_m: float = 0.4
    max_missing_zones: int = 5
    tiny_hole_cells_threshold: int = 4

    # Dimensions: доля высоты, с которой считаем длину/ширину по потолку (0..1)
    ceiling_height_fraction: float = 0.55

    # ML: пороги уверенности для откосов и коробов (ниже — не добавляем в ответ)
    reveal_min_confidence: float = 0.6
    frame_plane_min_confidence: float = 0.6

    # ML: путь к директории с обученной моделью (пусто — использовать встроенную по умолчанию)
    ml_model_dir: str = ""

    # Quality weights
    quality_weight_coverage: float = 0.45
    quality_weight_junction_conf: float = 0.35
    quality_weight_density: float = 0.20
    density_points_norm: int = 80000


@dataclass(frozen=True)
class Settings:
    api: ApiLimits = ApiLimits()
    processing: ProcessingConfig = ProcessingConfig()


settings = Settings()


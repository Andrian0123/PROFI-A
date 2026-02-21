from __future__ import annotations

import json
from pathlib import Path
from typing import Any, Dict, List, Optional

import numpy as np
import open3d as o3d


def _quaternion_to_rotation_matrix(quat: List[float]) -> np.ndarray:
    """
    Convert quaternion [qx, qy, qz, qw] to 3x3 rotation matrix.
    """
    if len(quat) != 4:
        return np.eye(3, dtype=np.float64)

    qx, qy, qz, qw = quat
    norm = np.sqrt(qx * qx + qy * qy + qz * qz + qw * qw)
    if norm == 0:
        return np.eye(3, dtype=np.float64)
    qx, qy, qz, qw = qx / norm, qy / norm, qz / norm, qw / norm

    xx, yy, zz = qx * qx, qy * qy, qz * qz
    xy, xz, yz = qx * qy, qx * qz, qy * qz
    wx, wy, wz = qw * qx, qw * qy, qw * qz

    return np.array(
        [
            [1 - 2 * (yy + zz), 2 * (xy - wz), 2 * (xz + wy)],
            [2 * (xy + wz), 1 - 2 * (xx + zz), 2 * (yz - wx)],
            [2 * (xz - wy), 2 * (yz + wx), 1 - 2 * (xx + yy)],
        ],
        dtype=np.float64,
    )


def _load_trajectory(trajectory_json_path: str) -> List[Dict[str, Any]]:
    path = Path(trajectory_json_path)
    if not path.exists():
        return []

    raw = json.loads(path.read_text(encoding="utf-8"))
    if isinstance(raw, dict):
        # Support both {"trajectory": [...]} and plain list payloads.
        if "trajectory" in raw and isinstance(raw["trajectory"], list):
            return raw["trajectory"]
        return []
    if isinstance(raw, list):
        return raw
    return []


def _frame_transform(frame_pose: Dict[str, Any]) -> np.ndarray:
    transform = np.eye(4, dtype=np.float64)
    position = frame_pose.get("position") or [0.0, 0.0, 0.0]
    rotation = frame_pose.get("rotation") or [0.0, 0.0, 0.0, 1.0]

    if isinstance(position, list) and len(position) == 3:
        transform[:3, 3] = np.array(position, dtype=np.float64)
    transform[:3, :3] = _quaternion_to_rotation_matrix(rotation if isinstance(rotation, list) else [])
    return transform


def _depth_image_from_path(depth_path: Path) -> Optional[o3d.geometry.Image]:
    if not depth_path.exists():
        return None

    depth_o3d = o3d.io.read_image(str(depth_path))
    depth_np = np.asarray(depth_o3d)
    if depth_np.size == 0:
        return None

    # Open3D expects depth in uint16 (or float), here we normalize to uint16 millimeters.
    if depth_np.ndim == 3:
        depth_np = depth_np[:, :, 0]

    if depth_np.dtype == np.uint16:
        return o3d.geometry.Image(depth_np)

    if np.issubdtype(depth_np.dtype, np.floating):
        # Assume meters in float depth maps.
        depth_mm = np.clip(depth_np, 0.0, 10.0) * 1000.0
        return o3d.geometry.Image(depth_mm.astype(np.uint16))

    if depth_np.dtype == np.uint8:
        # Heuristic mapping 0..255 => 0.5..3.0m for grayscale depth-like maps.
        depth_m = 0.5 + (depth_np.astype(np.float32) / 255.0) * 2.5
        depth_mm = depth_m * 1000.0
        return o3d.geometry.Image(depth_mm.astype(np.uint16))

    # Fallback conversion.
    return o3d.geometry.Image(depth_np.astype(np.uint16))


def load_frames_to_pointcloud(
    frame_paths: List[str],
    trajectory_json_path: str,
    depth_paths: Optional[List[str]] = None,
) -> o3d.geometry.PointCloud:
    """
    Build a single Open3D point cloud from a list of JPEG frames and trajectory.

    Notes:
    - If depth_paths are provided, they are used as true depth input.
    - If no depth is provided, this function creates synthetic depth from luminance
      as a temporary approximation.
    - Trajectory poses are applied to each frame cloud as rigid transforms.
    """
    if not frame_paths:
        return o3d.geometry.PointCloud()

    trajectory = _load_trajectory(trajectory_json_path)
    merged = o3d.geometry.PointCloud()

    for idx, frame_path in enumerate(frame_paths):
        image_path = Path(frame_path)
        if not image_path.exists():
            continue

        color_o3d = o3d.io.read_image(str(image_path))
        color_np = np.asarray(color_o3d)
        if color_np.size == 0:
            continue

        # Ensure 3-channel uint8 color image.
        if color_np.ndim == 2:
            color_np = np.stack([color_np, color_np, color_np], axis=-1)
        elif color_np.shape[2] == 4:
            color_np = color_np[:, :, :3]
        color_np = color_np.astype(np.uint8)
        color_o3d = o3d.geometry.Image(color_np)

        depth_o3d: Optional[o3d.geometry.Image] = None
        if depth_paths and idx < len(depth_paths):
            depth_o3d = _depth_image_from_path(Path(depth_paths[idx]))

        if depth_o3d is None:
            # Synthetic depth from luminance: range ~0.5m..3.0m, then convert to mm.
            gray = (
                0.299 * color_np[:, :, 0]
                + 0.587 * color_np[:, :, 1]
                + 0.114 * color_np[:, :, 2]
            ) / 255.0
            depth_m = 0.5 + (1.0 - gray) * 2.5
            depth_mm = (depth_m * 1000.0).astype(np.uint16)
            depth_o3d = o3d.geometry.Image(depth_mm)

        rgbd = o3d.geometry.RGBDImage.create_from_color_and_depth(
            color=color_o3d,
            depth=depth_o3d,
            depth_scale=1000.0,
            depth_trunc=5.0,
            convert_rgb_to_intensity=False,
        )

        height, width = color_np.shape[0], color_np.shape[1]
        fx = float(max(width, height))
        fy = float(max(width, height))
        cx = width / 2.0
        cy = height / 2.0
        intrinsics = o3d.camera.PinholeCameraIntrinsic(width, height, fx, fy, cx, cy)

        frame_cloud = o3d.geometry.PointCloud.create_from_rgbd_image(rgbd, intrinsics)
        pose = trajectory[idx] if idx < len(trajectory) else {}
        frame_cloud.transform(_frame_transform(pose))
        merged += frame_cloud

    if len(merged.points) == 0:
        return merged

    merged = merged.voxel_down_sample(voxel_size=0.03)
    if len(merged.points) > 0:
        merged.estimate_normals()
    return merged


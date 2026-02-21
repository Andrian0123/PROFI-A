#!/usr/bin/env python3
"""
Анализ видео для пайплайна 3D-сканера PROFI-A.
Извлекает метаданные и при необходимости кадры для обработки сервером.
"""
from __future__ import annotations

import argparse
import json
import subprocess
import sys
from pathlib import Path

# Добавляем корень проекта в path
PROJECT_ROOT = Path(__file__).resolve().parent.parent
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))


def analyze_with_opencv(video_path: Path) -> dict | None:
    """Метаданные и подсчёт кадров через OpenCV (если установлен)."""
    try:
        import cv2
    except ImportError:
        return None

    cap = cv2.VideoCapture(str(video_path))
    if not cap.isOpened():
        return None

    out = {
        "width": int(cap.get(cv2.CAP_PROP_FRAME_WIDTH)),
        "height": int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT)),
        "fps": cap.get(cv2.CAP_PROP_FPS) or 0,
        "frame_count": int(cap.get(cv2.CAP_PROP_FRAME_COUNT)),
        "duration_sec": 0.0,
        "codec": "unknown",
    }
    if out["fps"] > 0 and out["frame_count"] > 0:
        out["duration_sec"] = out["frame_count"] / out["fps"]
    cap.release()
    return out


def analyze_with_ffprobe(video_path: Path) -> dict | None:
    """Метаданные через ffprobe (если установлен ffmpeg)."""
    try:
        result = subprocess.run(
            [
                "ffprobe",
                "-v", "quiet",
                "-print_format", "json",
                "-show_format",
                "-show_streams",
                str(video_path),
            ],
            capture_output=True,
            text=True,
            timeout=30,
        )
    except (FileNotFoundError, subprocess.TimeoutExpired):
        return None

    if result.returncode != 0 or not result.stdout:
        return None

    data = json.loads(result.stdout)
    out = {"width": 0, "height": 0, "fps": 0.0, "frame_count": 0, "duration_sec": 0.0, "codec": "unknown"}

    for stream in data.get("streams", []):
        if stream.get("codec_type") == "video":
            out["width"] = int(stream.get("width", 0))
            out["height"] = int(stream.get("height", 0))
            out["codec"] = stream.get("codec_name", "unknown")
            fps_str = stream.get("r_frame_rate", "0/1")
            if "/" in fps_str:
                a, b = fps_str.split("/", 1)
                try:
                    out["fps"] = float(a) / float(b) if float(b) else 0
                except (ValueError, ZeroDivisionError):
                    pass
            nb = stream.get("nb_frames")
            if nb is not None:
                try:
                    out["frame_count"] = int(nb)
                except ValueError:
                    pass
            break

    fmt = data.get("format", {})
    if "duration" in fmt:
        try:
            out["duration_sec"] = float(fmt["duration"])
        except (ValueError, TypeError):
            pass
    if out["frame_count"] == 0 and out["fps"] > 0 and out["duration_sec"] > 0:
        out["frame_count"] = int(out["duration_sec"] * out["fps"])

    return out


def extract_frames(video_path: Path, out_dir: Path, every_n: int = 1, max_frames: int = 30) -> list[Path]:
    """Извлечь кадры в out_dir (JPEG). every_n — каждый N-й кадр, max_frames — лимит."""
    try:
        import cv2
    except ImportError:
        return []

    out_dir.mkdir(parents=True, exist_ok=True)
    cap = cv2.VideoCapture(str(video_path))
    if not cap.isOpened():
        return []

    paths: list[Path] = []
    idx = 0
    saved = 0
    while saved < max_frames:
        ret, frame = cap.read()
        if not ret:
            break
        if idx % every_n == 0:
            p = out_dir / f"frame_{saved:04d}.jpg"
            if cv2.imwrite(str(p), frame):
                paths.append(p)
                saved += 1
        idx += 1
    cap.release()
    return paths


def main() -> None:
    parser = argparse.ArgumentParser(description="Анализ видео для 3D-сканера PROFI-A")
    parser.add_argument("video", type=Path, help="Путь к видеофайлу (.mp4 и т.д.)")
    parser.add_argument("--extract", type=Path, default=None, help="Папка для извлечённых кадров (JPEG)")
    parser.add_argument("--every", type=int, default=5, help="Брать каждый N-й кадр (по умолчанию 5)")
    parser.add_argument("--max-frames", type=int, default=30, help="Максимум кадров (по умолчанию 30)")
    parser.add_argument("--json", action="store_true", help="Вывести результат в JSON")
    args = parser.parse_args()

    video_path = args.video.resolve()
    if not video_path.is_file():
        print(f"Ошибка: файл не найден: {video_path}", file=sys.stderr)
        sys.exit(1)

    # Метаданные
    meta = analyze_with_opencv(video_path) or analyze_with_ffprobe(video_path)
    if not meta:
        # Минимум: путь и размер файла
        meta = {
            "video_path": str(video_path),
            "file_size_mb": round(video_path.stat().st_size / (1024 * 1024), 2),
            "width": 0,
            "height": 0,
            "fps": 0.0,
            "frame_count": 0,
            "duration_sec": 0.0,
            "codec": "unknown",
            "note": "Установите opencv-python или ffmpeg для полного анализа.",
        }
        if not args.json:
            print("Внимание: для метаданных (разрешение, FPS, кадры) установите opencv-python или ffmpeg.", file=sys.stderr)

    meta["video_path"] = str(video_path)
    meta["file_size_mb"] = round(video_path.stat().st_size / (1024 * 1024), 2)

    if "file_size_mb" not in meta:
        meta["file_size_mb"] = round(video_path.stat().st_size / (1024 * 1024), 2)
    if args.json:
        print(json.dumps(meta, ensure_ascii=False, indent=2))
    else:
        print("Метаданные видео:")
        print(f"  Путь: {meta['video_path']}")
        print(f"  Размер файла: {meta['file_size_mb']} МБ")
        if meta.get("width") and meta.get("height"):
            print(f"  Разрешение: {meta['width']}×{meta['height']}")
            print(f"  FPS: {meta['fps']:.2f}")
            print(f"  Кадров: {meta['frame_count']}")
            print(f"  Длительность: {meta['duration_sec']:.2f} с")
            print(f"  Кодек: {meta['codec']}")
        if meta.get("note"):
            print(f"  Примечание: {meta['note']}")

    # Извлечение кадров
    if args.extract is not None:
        paths = extract_frames(video_path, args.extract.resolve(), every_n=args.every, max_frames=args.max_frames)
        if args.json:
            print(json.dumps({"extracted_frames": [str(p) for p in paths]}, ensure_ascii=False))
        else:
            print(f"\nИзвлечено кадров: {len(paths)} в {args.extract}")
        if not paths and meta.get("frame_count", 0) > 0:
            print("Установите opencv-python для извлечения кадров.", file=sys.stderr)


if __name__ == "__main__":
    main()

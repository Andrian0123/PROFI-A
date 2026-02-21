# Модуль ML для анализа видео 3D-сканера:
# - классификация плоскостей (стена, пол, потолок, дверь, окно, откос, короб);
# - углы и примыкания;
# - размеры помещения, откосы (дверь/окно), плоскости короба по вертикали.

from app.ml.features import extract_plane_features
from app.ml.inference import run_scan_inference

__all__ = ["extract_plane_features", "run_scan_inference"]

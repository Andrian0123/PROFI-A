"""
Классификатор плоскостей: стена, пол, потолок, дверь, окно, откос, короб.
Обучаемая модель на признаках из features.py.
"""
from __future__ import annotations

import json
from pathlib import Path
from typing import List, Optional, Tuple

import numpy as np

# Метки, используемые в модели
PLANE_LABELS = ["wall", "floor", "ceiling", "door", "window", "reveal", "frame"]
DEFAULT_LABEL = "wall"


def _get_sklearn_forest():
    try:
        from sklearn.ensemble import RandomForestClassifier
        return RandomForestClassifier(n_estimators=50, max_depth=10, random_state=42)
    except ImportError:
        return None


class PlaneClassifier:
    """
    Классификатор типа плоскости по вектору признаков.
    По умолчанию — эвристика (без sklearn); при наличии sklearn — RandomForest.
    """

    def __init__(self, use_heuristic_only: bool = False):
        self._clf = None if use_heuristic_only else _get_sklearn_forest()
        self._label_to_idx = {lbl: i for i, lbl in enumerate(PLANE_LABELS)}
        self._idx_to_label = PLANE_LABELS

    def fit(self, X: np.ndarray, y: List[str]) -> None:
        """Обучить по массиву признаков X (n_samples, n_features) и меткам y."""
        if self._clf is None:
            return
        idx = np.array([self._label_to_idx.get(lbl, 0) for lbl in y])
        self._clf.fit(X, idx)

    def predict(self, X: np.ndarray) -> List[str]:
        """Предсказать метки для X (n_samples, n_features)."""
        if self._clf is not None:
            idx = self._clf.predict(X)
            return [self._idx_to_label[i] for i in idx]
        return self._predict_heuristic(X)

    def _predict_heuristic(self, X: np.ndarray) -> List[str]:
        """Эвристика без ML: по нормали и размерам."""
        out = []
        for i in range(X.shape[0]):
            row = X[i]
            if row.size < 10:
                out.append(DEFAULT_LABEL)
                continue
            ny, centroid_y, height_y, ext_x, ext_z, area, aspect, is_h, is_v = (
                row[1], row[4], row[6], row[7], row[8], row[9], row[10], row[11], row[12]
            )
            if is_h >= 0.9:
                out.append("ceiling" if centroid_y > 1.5 else "floor")
            elif is_v >= 0.9:
                if height_y > 2.0 and area > 4.0:
                    out.append("wall")
                elif 0.5 < height_y < 2.5 and 0.3 < max(ext_x, ext_z) < 1.5:
                    if aspect > 1.2:
                        out.append("door")
                    else:
                        out.append("window")
                elif height_y < 0.5 or area < 0.5:
                    out.append("reveal")
                elif 0.2 < height_y < 2.8 and area < 3.0:
                    out.append("frame")
                else:
                    out.append("wall")
            else:
                out.append(DEFAULT_LABEL)
        return out

    def save(self, path: str) -> None:
        """Сохранить модель в директорию (sklearn joblib + meta.json)."""
        path = Path(path)
        path.mkdir(parents=True, exist_ok=True)
        if self._clf is not None:
            try:
                import joblib
                joblib.dump(self._clf, path / "classifier.joblib")
            except ImportError:
                pass
        meta = {"labels": PLANE_LABELS, "has_clf": self._clf is not None}
        (path / "meta.json").write_text(json.dumps(meta, ensure_ascii=False), encoding="utf-8")

    def load(self, path: str) -> bool:
        """Загрузить модель из директории."""
        path = Path(path)
        if not path.exists():
            return False
        meta_path = path / "meta.json"
        if meta_path.exists():
            meta = json.loads(meta_path.read_text(encoding="utf-8"))
            if meta.get("has_clf"):
                try:
                    import joblib
                    self._clf = joblib.load(path / "classifier.joblib")
                except Exception:
                    self._clf = None
        return True

from __future__ import annotations

import json
import tempfile
from pathlib import Path
from typing import List, Optional

from fastapi import APIRouter, File, Form, HTTPException, UploadFile
from pydantic import ValidationError

from app.core.config import settings
from app.core.processing.scan_processor import ScanProcessor
from app.ml.document_analyzer import analyze_document
from app.models.schemas import (
    DocumentScanResult,
    ScanFinishRequest,
    ScanFinishResponse,
    ScanProcessResponse,
    TrajectoryPoint,
)

router = APIRouter()
processor = ScanProcessor()


def parse_trajectory(trajectory_raw: Optional[str]) -> Optional[List[TrajectoryPoint]]:
    if trajectory_raw is None or not trajectory_raw.strip():
        return None
    try:
        payload = json.loads(trajectory_raw)
    except json.JSONDecodeError as exc:
        raise HTTPException(status_code=400, detail=f"Invalid trajectory JSON: {exc.msg}") from exc

    if not isinstance(payload, list):
        raise HTTPException(status_code=400, detail="Trajectory must be a JSON array")

    try:
        return [TrajectoryPoint.model_validate(item) for item in payload]
    except ValidationError as exc:
        raise HTTPException(status_code=400, detail=f"Invalid trajectory structure: {exc.errors()}") from exc


@router.post("/process", response_model=ScanProcessResponse)
async def process_scan(
    project_id: str = Form(...),
    room_id: str = Form(...),
    scan_id: str = Form(...),
    frames: List[UploadFile] = File(...),
    trajectory: Optional[str] = Form(None),
    depth: Optional[List[UploadFile]] = File(None),
) -> ScanProcessResponse:
    if not frames:
        raise HTTPException(status_code=400, detail="frames is required")

    if len(frames) > settings.api.max_frames_per_batch:
        raise HTTPException(
            status_code=413,
            detail=f"Too many frames in batch. Max allowed: {settings.api.max_frames_per_batch}",
        )

    if depth and settings.api.require_depth_count_match and len(depth) != len(frames):
        raise HTTPException(
            status_code=400,
            detail="depth[] count must match frames[] count",
        )

    invalid_frames = [
        f.filename for f in frames
        if f.filename and not f.filename.lower().endswith((".jpg", ".jpeg"))
    ]
    if invalid_frames:
        raise HTTPException(
            status_code=400,
            detail=f"frames[] must be JPEG files (.jpg/.jpeg). Invalid: {invalid_frames}",
        )

    trajectory_points = parse_trajectory(trajectory)

    return await processor.process_scan(
        project_id=project_id,
        room_id=room_id,
        scan_id=scan_id,
        frames=frames,
        trajectory=trajectory_points,
        depth=depth,
    )


@router.post("/finish", response_model=ScanFinishResponse)
async def finish_scan(payload: ScanFinishRequest) -> ScanFinishResponse:
    return await processor.finish_scan(payload)


@router.post("/document", response_model=DocumentScanResult)
async def process_document(
    scan_id: str = Form(...),
    document: UploadFile = File(..., description="Изображение документа (JPEG/PNG)"),
) -> DocumentScanResult:
    """
    Сканирование документа (путь фото3д): параметры ширина/длина и распознавание содержимого.
    В т.ч. инженерные коммуникации (трубы, кабели, вентиляция, электропроводка).
    """
    if not document.filename or not document.filename.lower().endswith((".jpg", ".jpeg", ".png")):
        raise HTTPException(
            status_code=400,
            detail="document must be JPEG or PNG",
        )
    with tempfile.NamedTemporaryFile(delete=False, suffix=Path(document.filename or "").suffix or ".jpg") as tmp:
        content = await document.read()
        tmp.write(content)
        tmp_path = tmp.name
    try:
        result = analyze_document(tmp_path, scan_id=scan_id)
        return result
    finally:
        Path(tmp_path).unlink(missing_ok=True)


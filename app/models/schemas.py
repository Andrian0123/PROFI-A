from __future__ import annotations

from typing import List, Literal, Optional

from pydantic import BaseModel, Field, conlist


Vec2 = conlist(float, min_length=2, max_length=2)
Vec3 = conlist(float, min_length=3, max_length=3)
Vec4 = conlist(float, min_length=4, max_length=4)


class TrajectoryPoint(BaseModel):
    t: float = Field(..., description="Time/index in seconds or frame index")
    position: Vec3
    rotation: Optional[Vec4] = None


class CoverageWebLine(BaseModel):
    start: Vec2
    end: Vec2
    alpha: float = Field(..., ge=0.0, le=1.0)


class MissingZone(BaseModel):
    boundary: List[Vec2]
    label: str


class CoverageData(BaseModel):
    percentage: float = Field(..., ge=0.0, le=100.0)
    web_lines: List[CoverageWebLine] = Field(default_factory=list)
    missing_zones: List[MissingZone] = Field(default_factory=list)


class VerticalLine(BaseModel):
    bottom: Vec3
    top: Vec3


class Junction(BaseModel):
    type: Literal[
        "floor_wall_internal",
        "floor_wall_external",
        "ceiling_wall_internal",
        "ceiling_wall_external",
        "wall_wall_internal",
        "wall_wall_external",
        "floor_ceiling_edge",
        "window_opening",
        "door_opening",
        "niche_recess",
    ]
    position_3d: Vec3
    direction: Optional[Vec3] = None
    vertical_line: Optional[VerticalLine] = None
    confidence: float = Field(..., ge=0.0, le=1.0)
    icon: Optional[str] = None


class Dimensions(BaseModel):
    """Параметры помещения (индивидуально по комнате).
    Диагональ используется для проверки по правилу 3–4–5 и уточнения длины/ширины."""
    length_m: float = Field(..., ge=0.0, description="Длина помещения (м)")
    width_m: float = Field(..., ge=0.0, description="Ширина помещения (м)")
    wall_height_m: float = Field(..., ge=0.0, description="Высота стен (м)")
    perimeter_m: float = Field(..., ge=0.0, description="Периметр (м)")
    floor_area_m2: float = Field(..., ge=0.0, description="Площадь пола (м²)")
    ceiling_area_m2: float = Field(..., ge=0.0, description="Площадь потолка (м²)")
    wall_area_m2: float = Field(..., ge=0.0, description="Площадь стен (м²)")
    diagonal_m: Optional[float] = Field(None, ge=0.0, description="Диагональ пола (м), для проверки 3–4–5")


class Reveal(BaseModel):
    """Откос: привязка к проёму (дверь/окно), размеры для раздела «Откосы»."""
    opening_type: Literal["door", "window"]
    width_m: float = Field(..., ge=0.0)
    height_m: float = Field(..., ge=0.0)
    depth_m: float = Field(0.0, ge=0.0, description="Глубина откоса (м)")
    position_3d: Optional[Vec3] = None
    confidence: float = Field(0.8, ge=0.0, le=1.0)


class FramePlane(BaseModel):
    """Плоскость короба: вертикальная грань, размеры для раздела «Короба».
    Вертикаль учитывается как погонный метр (м.п.)."""
    width_m: float = Field(..., ge=0.0)
    height_m: float = Field(..., ge=0.0)
    linear_m: float = Field(..., ge=0.0, description="Погонные метры по вертикали (м.п.)")
    position_3d: Vec3
    direction: Optional[Vec3] = None
    plane_index: int = Field(0, ge=0)
    confidence: float = Field(0.8, ge=0.0, le=1.0)


class QualityMetrics(BaseModel):
    scan_quality: float = Field(..., ge=0.0, le=1.0)
    junction_count: int = Field(..., ge=0)
    missing_corners: int = Field(..., ge=0)
    processing_time_ms: Optional[int] = Field(default=None, ge=0)
    points_count: Optional[int] = Field(default=None, ge=0)
    planes_count: Optional[int] = Field(default=None, ge=0)


class ScanProcessResponse(BaseModel):
    scan_id: str
    coverage: CoverageData
    junctions: List[Junction] = Field(default_factory=list)
    dimensions: Dimensions
    quality_metrics: QualityMetrics
    reveals: List[Reveal] = Field(default_factory=list, description="Откосы (дверь/окно)")
    frame_planes: List[FramePlane] = Field(default_factory=list, description="Плоскости короба по вертикали")
    frame_linear_m_total: float = Field(0.0, ge=0.0, description="Сумма погонных метров по коробам (м.п.)")


class Artifacts(BaseModel):
    mesh_url: Optional[str] = None
    preview_url: Optional[str] = None
    json_url: Optional[str] = None


class ScanFinishRequest(BaseModel):
    scan_id: str
    project_id: str
    room_id: str


class ScanFinishResponse(ScanProcessResponse):
    artifacts: Optional[Artifacts] = None


# --- Сканирование документа (путь фото3д / документ) ---

ContentLabel = Literal[
    "инженерные_коммуникации",
    "трубы",
    "кабели",
    "вентиляция",
    "электропроводка",
    "схема",
    "чертёж",
    "текст",
    "таблица",
    "печать",
    "подпись",
    "другое",
]


class RecognizedContentItem(BaseModel):
    """Распознанный элемент содержимого (картинка, объект на изображении)."""
    label: ContentLabel
    confidence: float = Field(..., ge=0.0, le=1.0)
    bbox: Optional[List[float]] = Field(None, description="[x1, y1, x2, y2] в долях 0..1")


class DocumentScanResult(BaseModel):
    """Результат сканирования документа: параметры и распознанное содержимое."""
    scan_id: str
    width_mm: float = Field(..., ge=0.0, description="Ширина документа (мм)")
    length_mm: float = Field(..., ge=0.0, description="Длина документа (мм)")
    content: List[RecognizedContentItem] = Field(
        default_factory=list,
        description="Распознанное содержимое: инженерные коммуникации, трубы, кабели и т.д.",
    )
    has_engineering_communications: bool = Field(
        False,
        description="Признак наличия инженерных коммуникаций на изображении",
    )


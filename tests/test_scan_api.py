import base64
import json

from fastapi.testclient import TestClient

from app.main import app


client = TestClient(app)


def _jpeg_bytes() -> bytes:
    # Valid 1x1 JPEG.
    return base64.b64decode(
        b"/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBxAQEBAQEBAVEBUVFRUVFRUVFRUVFRUVFRUWFhUV"
        b"FRUYHSggGBolGxUVITEhJSkrLi4uFx8zODMsNygtLisBCgoKDg0OGhAQGy0lICUtLS0tLS0tLS0tLS0t"
        b"LS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLf/AABEIAAEAAQMBIgACEQEDEQH/xAAXAAEBAQE"
        b"AAAAAAAAAAAAAAAABAgME/8QAFhEBAQEAAAAAAAAAAAAAAAAAAAER/9oADAMBAAIQAxAAAAG7Qf/EAB"
        b"gQAQEAAwAAAAAAAAAAAAAAAAEAEQIS/9oACAEBAAEFAoXo1//EABYRAQEBAAAAAAAAAAAAAAAAAAARAf"
        b"/aAAgBAwEBPwGn/8QAFhEBAQEAAAAAAAAAAAAAAAAAABEh/9oACAECAQE/AYf/xAAbEAACAQUAAAAAAAA"
        b"AAAAAAAABEQAhMUFRcf/aAAgBAQAGPwJRTY2dY//EABoQAQEAAwEBAAAAAAAAAAAAAAERACExQWH/2gAIAQ"
        b"EAAT8h2S8xkqLzx7ZQmX//2gAMAwEAAgADAAAAEMf/xAAXEQADAQAAAAAAAAAAAAAAAAAAAREx/9oACAEDAQ"
        b"E/EA5f/8QAFxEBAAMAAAAAAAAAAAAAAAAAAAERMf/aAAgBAgEBPxAzf//EABsQAQACAgMAAAAAAAAAAAAAAA"
        b"EAEQAhMUFh/9oACAEBAAE/EDg8V5zaP8wWvDFQvO3Uo6f/2Q=="
    )


def _frame_file(name: str = "frame.jpg"):
    return (name, _jpeg_bytes(), "image/jpeg")


def test_process_rejects_too_many_frames():
    files = [("frames", _frame_file(f"f{i}.jpg")) for i in range(31)]
    response = client.post(
        "/api/v1/scan/process",
        data={"project_id": "p1", "room_id": "r1", "scan_id": "s1"},
        files=files,
    )
    assert response.status_code == 413
    assert "Too many frames" in response.json()["detail"]


def test_process_rejects_non_jpeg_frame():
    files = [("frames", ("frame.png", b"png", "image/png"))]
    response = client.post(
        "/api/v1/scan/process",
        data={"project_id": "p1", "room_id": "r1", "scan_id": "s1"},
        files=files,
    )
    assert response.status_code == 400
    assert "JPEG" in response.json()["detail"]


def test_process_rejects_depth_count_mismatch():
    files = [
        ("frames", _frame_file("f1.jpg")),
        ("frames", _frame_file("f2.jpg")),
        ("depth", ("d1.png", b"\x89PNG\r\n\x1a\n", "image/png")),
    ]
    response = client.post(
        "/api/v1/scan/process",
        data={"project_id": "p1", "room_id": "r1", "scan_id": "s1"},
        files=files,
    )
    assert response.status_code == 400
    assert "depth[] count must match frames[] count" in response.json()["detail"]


def test_process_smoke_success():
    trajectory = json.dumps(
        [
            {"t": 0.0, "position": [0.0, 0.0, 0.0], "rotation": [0.0, 0.0, 0.0, 1.0]},
            {"t": 1.0, "position": [0.2, 0.0, 0.1], "rotation": [0.0, 0.0, 0.0, 1.0]},
        ]
    )
    files = [("frames", _frame_file("f1.jpg"))]
    response = client.post(
        "/api/v1/scan/process",
        data={
            "project_id": "p1",
            "room_id": "r1",
            "scan_id": "scan-smoke",
            "trajectory": trajectory,
        },
        files=files,
    )
    assert response.status_code == 200
    payload = response.json()
    assert payload["scan_id"] == "scan-smoke"
    assert "coverage" in payload
    assert "dimensions" in payload
    assert "quality_metrics" in payload
    assert "processing_time_ms" in payload["quality_metrics"]

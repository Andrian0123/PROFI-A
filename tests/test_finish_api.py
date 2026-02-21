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


def test_finish_with_existing_session():
    scan_id = "scan-finish-existing"
    trajectory = json.dumps(
        [
            {"t": 0.0, "position": [0.0, 0.0, 0.0], "rotation": [0.0, 0.0, 0.0, 1.0]},
            {"t": 1.0, "position": [0.1, 0.0, 0.1], "rotation": [0.0, 0.0, 0.0, 1.0]},
        ]
    )

    process_resp = client.post(
        "/api/v1/scan/process",
        data={
            "project_id": "proj-1",
            "room_id": "room-1",
            "scan_id": scan_id,
            "trajectory": trajectory,
        },
        files=[("frames", _frame_file())],
    )
    assert process_resp.status_code == 200

    finish_resp = client.post(
        "/api/v1/scan/finish",
        json={
            "scan_id": scan_id,
            "project_id": "proj-1",
            "room_id": "room-1",
        },
    )
    assert finish_resp.status_code == 200
    payload = finish_resp.json()

    assert payload["scan_id"] == scan_id
    assert "artifacts" in payload
    assert payload["artifacts"]["mesh_url"].endswith(f"{scan_id}.ply")
    assert payload["quality_metrics"]["junction_count"] >= 0


def test_finish_without_session_returns_fallback():
    scan_id = "scan-finish-missing"
    finish_resp = client.post(
        "/api/v1/scan/finish",
        json={
            "scan_id": scan_id,
            "project_id": "proj-2",
            "room_id": "room-2",
        },
    )
    assert finish_resp.status_code == 200
    payload = finish_resp.json()

    assert payload["scan_id"] == scan_id
    assert payload["coverage"]["percentage"] == 0.0
    assert payload["quality_metrics"]["scan_quality"] == 0.0
    assert payload["quality_metrics"]["missing_corners"] == 4
    assert payload["artifacts"]["json_url"].endswith(f"{scan_id}.json")

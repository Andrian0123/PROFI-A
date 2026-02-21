.PHONY: up down logs test run install lint format

up:
	docker compose up --build

down:
	docker compose down

logs:
	docker compose logs -f scan-service

test:
	pytest -q

lint:
	ruff check app tests
	black --check app tests

format:
	ruff check --fix app tests
	black app tests

run:
	uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

install:
	pip install -r requirements.txt


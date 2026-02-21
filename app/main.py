from fastapi import FastAPI

from app.api.endpoints.scan import router as scan_router


def create_app() -> FastAPI:
    app = FastAPI(
        title="PROFI-A Scan Service",
        version="1.0.0",
        docs_url="/docs",
        redoc_url="/redoc",
    )
    app.include_router(scan_router, prefix="/api/v1/scan", tags=["scan"])
    return app


app = create_app()


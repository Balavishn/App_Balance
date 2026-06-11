from fastapi import FastAPI

from app.api.routes import router
from app.core.db import create_db_and_tables

app = FastAPI(title="AI Budget Planner API", version="1.0.0")


@app.on_event("startup")
def on_startup() -> None:
    create_db_and_tables()


app.include_router(router)

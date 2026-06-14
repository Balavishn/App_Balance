import os
from sqlmodel import Session, SQLModel, create_engine

database_url = os.environ.get("DATABASE_URL", "sqlite:///./budget_planner.db")

if database_url.startswith("postgres://"):
    database_url = database_url.replace("postgres://", "postgresql://", 1)

engine = create_engine(database_url, echo=False)


def create_db_and_tables() -> None:
    SQLModel.metadata.create_all(engine)


def get_session():
    with Session(engine) as session:
        yield session

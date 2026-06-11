from sqlmodel import Session, SQLModel, create_engine

engine = create_engine("sqlite:///./budget_planner.db", echo=False)


def create_db_and_tables() -> None:
    SQLModel.metadata.create_all(engine)


def get_session():
    with Session(engine) as session:
        yield session

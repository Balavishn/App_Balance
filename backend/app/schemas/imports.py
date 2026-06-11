from pydantic import BaseModel


class StatementImportResponse(BaseModel):
    statement_type: str
    imported_count: int
    duplicate_count: int
    total_parsed_count: int
    auto_categorized_count: int

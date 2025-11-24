from datetime import datetime

from pydantic import BaseModel


class ProgressUpdate(BaseModel):
    chapter: str
    offset: int


class ProgressRead(BaseModel):
    novel_id: int
    chapter: str
    offset: int
    last_read_at: datetime

    class Config:
        from_attributes = True

from datetime import datetime

from pydantic import BaseModel


class NovelRead(BaseModel):
    id: int
    title: str
    author: str
    format: str
    size: int
    description: str | None = None
    uploaded_at: datetime

    class Config:
        from_attributes = True

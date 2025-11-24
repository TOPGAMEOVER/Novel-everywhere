from datetime import datetime
from typing import Optional

from pydantic import BaseModel


class NovelRead(BaseModel):
    id: int
    title: str
    author: str
    format: str
    size: int
    description: Optional[str] = None
    uploaded_at: datetime

    class Config:
        from_attributes = True

from typing import Optional

from pydantic import BaseModel


class ReadingSettingsUpdate(BaseModel):
    font_family: Optional[str] = None
    font_size: Optional[int] = None
    line_height: Optional[int] = None
    theme: Optional[str] = None
    bg_color: Optional[str] = None
    tts_voice: Optional[str] = None


class ReadingSettingsRead(BaseModel):
    font_family: str
    font_size: int
    line_height: int
    theme: str
    bg_color: str
    tts_voice: str

    class Config:
        from_attributes = True

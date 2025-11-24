from pydantic import BaseModel


class ReadingSettingsUpdate(BaseModel):
    font_family: str | None = None
    font_size: int | None = None
    line_height: int | None = None
    theme: str | None = None
    bg_color: str | None = None
    tts_voice: str | None = None


class ReadingSettingsRead(BaseModel):
    font_family: str
    font_size: int
    line_height: int
    theme: str
    bg_color: str
    tts_voice: str

    class Config:
        from_attributes = True

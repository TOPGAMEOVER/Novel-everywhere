from pydantic import BaseModel


class TTSRequest(BaseModel):
    text: str
    voice: str | None = "default"
    rate: int | None = 200


class TTSResponse(BaseModel):
    audio_url: str
*** End Patch

from typing import Optional

from pydantic import BaseModel


class TTSRequest(BaseModel):
    text: str
    voice: Optional[str] = "default"
    rate: Optional[int] = 200


class TTSResponse(BaseModel):
    audio_url: str
*** End Patch

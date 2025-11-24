from app.schemas.novel import NovelRead
from app.schemas.progress import ProgressRead, ProgressUpdate
from app.schemas.settings import ReadingSettingsRead, ReadingSettingsUpdate
from app.schemas.token import Token
from app.schemas.tts import TTSRequest, TTSResponse
from app.schemas.user import UserCreate, UserLogin, UserRead

__all__ = [
    "NovelRead",
    "ProgressRead",
    "ProgressUpdate",
    "ReadingSettingsRead",
    "ReadingSettingsUpdate",
    "Token",
    "TTSRequest",
    "TTSResponse",
    "UserCreate",
    "UserLogin",
    "UserRead",
]

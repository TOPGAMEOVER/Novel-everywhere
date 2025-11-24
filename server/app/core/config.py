from functools import lru_cache
from pathlib import Path

from pydantic import Field
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_env: str = Field("development", alias="APP_ENV")
    database_url: str = Field("sqlite:///app/data/app.db", alias="DATABASE_URL")
    jwt_secret: str = Field("change-me", alias="JWT_SECRET")
    jwt_expire_minutes: int = Field(60 * 24 * 7, alias="JWT_EXPIRE_MINUTES")
    storage_root: Path = Field(Path("storage"), alias="STORAGE_ROOT")
    tts_audio_root: Path = Field(Path("storage/audio"), alias="TTS_AUDIO_ROOT")
    log_level: str = Field("INFO", alias="LOG_LEVEL")

    model_config = {"env_file": ".env", "case_sensitive": False}


@lru_cache
def get_settings() -> Settings:
    settings = Settings()
    settings.storage_root.mkdir(parents=True, exist_ok=True)
    settings.tts_audio_root.mkdir(parents=True, exist_ok=True)
    return settings

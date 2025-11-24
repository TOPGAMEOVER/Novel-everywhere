from __future__ import annotations

import hashlib
from pathlib import Path

import pyttsx3

from app.core.config import get_settings


def synthesize_tts(text: str, voice: str | None = None, rate: int | None = None) -> Path:
    settings = get_settings()
    digest = hashlib.sha256(f"{voice}-{rate}-{text}".encode("utf-8")).hexdigest()
    audio_path = settings.tts_audio_root / f"{digest}.mp3"
    if audio_path.exists():
        return audio_path

    audio_path.parent.mkdir(parents=True, exist_ok=True)
    engine = pyttsx3.init()
    if voice:
        for v in engine.getProperty("voices"):
            if voice in v.id:
                engine.setProperty("voice", v.id)
                break
    if rate:
        engine.setProperty("rate", rate)
    engine.save_to_file(text, str(audio_path))
    engine.runAndWait()
    engine.stop()
    return audio_path

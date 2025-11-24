from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from loguru import logger

from app.api.api_v1 import api_router
from app.core.config import get_settings
from app.db.session import Base, engine

settings = get_settings()

app = FastAPI(title="Novel Everywhere API", version="0.1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
def startup():
    Base.metadata.create_all(bind=engine)
    logger.info("Novel Everywhere API started in %s mode", settings.app_env)


app.include_router(api_router)

app.mount("/tts-audio", StaticFiles(directory=settings.tts_audio_root), name="tts-audio")

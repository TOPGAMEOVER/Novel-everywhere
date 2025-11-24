from fastapi import APIRouter

from app.api.routes import auth, novels, profile, settings, tts

api_router = APIRouter()
api_router.include_router(auth.router)
api_router.include_router(profile.router)
api_router.include_router(novels.router)
api_router.include_router(settings.router)
api_router.include_router(tts.router)

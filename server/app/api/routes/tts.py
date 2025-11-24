from fastapi import APIRouter, Depends
from fastapi.concurrency import run_in_threadpool

from app.api.deps import get_current_user
from app.models import User
from app.schemas import TTSRequest, TTSResponse
from app.services.tts_service import synthesize_tts

router = APIRouter(prefix="/tts", tags=["tts"])


@router.post("", response_model=TTSResponse)
async def create_tts(
    payload: TTSRequest,
    _: User = Depends(get_current_user),
):
    path = await run_in_threadpool(synthesize_tts, payload.text, payload.voice, payload.rate)
    return TTSResponse(audio_url=f"/tts-audio/{path.name}")

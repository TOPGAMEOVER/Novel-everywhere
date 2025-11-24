from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, get_db
from app.models import ReadingSettings, User
from app.schemas import ReadingSettingsRead, ReadingSettingsUpdate

router = APIRouter(prefix="/settings", tags=["settings"])


@router.get("/reading", response_model=ReadingSettingsRead)
def read_settings(user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    settings = (
        db.query(ReadingSettings)
        .filter(ReadingSettings.user_id == user.id)
        .first()
    )
    if not settings:
        settings = ReadingSettings(user_id=user.id)
        db.add(settings)
        db.commit()
        db.refresh(settings)
    return settings


@router.put("/reading", response_model=ReadingSettingsRead)
def update_settings(
    payload: ReadingSettingsUpdate,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    settings = (
        db.query(ReadingSettings)
        .filter(ReadingSettings.user_id == user.id)
        .first()
    )
    if not settings:
        settings = ReadingSettings(user_id=user.id)
        db.add(settings)
    update_data = payload.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(settings, field, value)
    db.commit()
    db.refresh(settings)
    return settings

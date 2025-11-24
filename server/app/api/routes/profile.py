from fastapi import APIRouter, Depends

from app.api.deps import get_current_user
from app.models.user import User
from app.schemas import UserRead

router = APIRouter(prefix="/profile", tags=["profile"])


@router.get("/me", response_model=UserRead)
def read_me(user: User = Depends(get_current_user)):
    return user

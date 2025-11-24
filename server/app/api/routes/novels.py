from pathlib import Path
from typing import Optional

from fastapi import APIRouter, Depends, File, HTTPException, UploadFile, status
from fastapi.responses import FileResponse
from fastapi.param_functions import Form
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.api.deps import get_db
from app.core.config import get_settings
from app.models import Novel, ReadingProgress, User
from app.schemas import NovelRead, ProgressRead, ProgressUpdate
from app.services.file_service import delete_file, save_novel_file

router = APIRouter(prefix="/novels", tags=["novels"])


@router.post("", response_model=NovelRead, status_code=status.HTTP_201_CREATED)
async def upload_novel(
    file: UploadFile = File(...),
    title: Optional[str] = Form(default=None),
    author: Optional[str] = Form(default=None),
    description: Optional[str] = Form(default=None),
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    saved_path, size, file_hash = save_novel_file(user.id, file)
    existing = db.query(Novel).filter(Novel.user_id == user.id, Novel.file_hash == file_hash).first()
    if existing:
        delete_file(str(saved_path))
        return existing

    final_title = title or Path(file.filename or "未命名小说").stem
    novel = Novel(
        user_id=user.id,
        title=final_title,
        author=author or "未知作者",
        format=(Path(file.filename or "txt").suffix or ".txt").replace(".", "").lower(),
        file_path=str(saved_path),
        description=description,
        file_hash=file_hash,
        size=size,
    )
    db.add(novel)
    db.commit()
    db.refresh(novel)
    return novel


@router.get("", response_model=list[NovelRead])
def list_novels(user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    novels = db.query(Novel).filter(Novel.user_id == user.id).order_by(Novel.uploaded_at.desc()).all()
    return novels


@router.get("/{novel_id}", response_model=NovelRead)
def get_novel(novel_id: int, user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    novel = (
        db.query(Novel)
        .filter(Novel.id == novel_id, Novel.user_id == user.id)
        .first()
    )
    if not novel:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="小说不存在")
    return novel


@router.get("/{novel_id}/file")
def download_file(novel_id: int, user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    novel = (
        db.query(Novel)
        .filter(Novel.id == novel_id, Novel.user_id == user.id)
        .first()
    )
    if not novel:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="小说不存在")
    file_path = Path(novel.file_path)
    if not file_path.exists():
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="文件缺失")
    return FileResponse(file_path, filename=file_path.name)


@router.delete("/{novel_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_novel(novel_id: int, user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    novel = (
        db.query(Novel)
        .filter(Novel.id == novel_id, Novel.user_id == user.id)
        .first()
    )
    if not novel:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="小说不存在")
    delete_file(novel.file_path)
    db.delete(novel)
    db.commit()
    return None


@router.post("/{novel_id}/progress", response_model=ProgressRead)
def update_progress(
    novel_id: int,
    payload: ProgressUpdate,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    novel = (
        db.query(Novel)
        .filter(Novel.id == novel_id, Novel.user_id == user.id)
        .first()
    )
    if not novel:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="小说不存在")
    progress = (
        db.query(ReadingProgress)
        .filter(ReadingProgress.novel_id == novel_id, ReadingProgress.user_id == user.id)
        .first()
    )
    if progress:
        progress.chapter = payload.chapter
        progress.offset = payload.offset
    else:
        progress = ReadingProgress(
            novel_id=novel_id,
            user_id=user.id,
            chapter=payload.chapter,
            offset=payload.offset,
        )
        db.add(progress)
    db.commit()
    db.refresh(progress)
    return progress


@router.get("/{novel_id}/progress", response_model=ProgressRead)
def get_progress(novel_id: int, user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    progress = (
        db.query(ReadingProgress)
        .filter(ReadingProgress.novel_id == novel_id, ReadingProgress.user_id == user.id)
        .first()
    )
    if not progress:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="进度不存在")
    return progress

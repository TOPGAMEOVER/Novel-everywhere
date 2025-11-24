from __future__ import annotations

import hashlib
import shutil
from pathlib import Path
from typing import IO

from fastapi import UploadFile

from app.core.config import get_settings


def _copy_to_path(source: IO[bytes], destination: Path) -> int:
    destination.parent.mkdir(parents=True, exist_ok=True)
    with destination.open("wb") as buffer:
        total = 0
        while True:
            chunk = source.read(1024 * 1024)
            if not chunk:
                break
            buffer.write(chunk)
            total += len(chunk)
        return total


def save_novel_file(user_id: int, upload_file: UploadFile) -> tuple[Path, int, str]:
    """保存上传文件，返回 (路径, 大小, 哈希)。"""
    settings = get_settings()
    ext = Path(upload_file.filename or "novel").suffix.lower() or ".txt"
    digest = hashlib.sha256()

    tmp_path = settings.storage_root / "tmp"
    tmp_path.mkdir(parents=True, exist_ok=True)
    temp_file = tmp_path / f"{user_id}_{upload_file.filename}"
    with temp_file.open("wb") as buffer:
        while True:
            chunk = upload_file.file.read(1024 * 1024)
            if not chunk:
                break
            buffer.write(chunk)
            digest.update(chunk)
    size = temp_file.stat().st_size

    final_dir = settings.storage_root / "novels" / str(user_id)
    final_dir.mkdir(parents=True, exist_ok=True)
    final_path = final_dir / f"{digest.hexdigest()}{ext}"
    shutil.move(temp_file, final_path)
    return final_path, size, digest.hexdigest()


def delete_file(path: str) -> None:
    file_path = Path(path)
    if file_path.exists():
        file_path.unlink()

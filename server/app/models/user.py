from datetime import datetime
from typing import TYPE_CHECKING

from sqlalchemy import DateTime, Integer, String, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.session import Base

if TYPE_CHECKING:
    from app.models.novel import Novel
    from app.models.reading_progress import ReadingProgress
    from app.models.reading_settings import ReadingSettings


class User(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    email: Mapped[str] = mapped_column(String(255), unique=True, index=True, nullable=False)
    password_hash: Mapped[str] = mapped_column(String(512), nullable=False)
    display_name: Mapped[str] = mapped_column(String(255), nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())

    novels: Mapped[list["Novel"]] = relationship(back_populates="owner", cascade="all, delete-orphan")
    progress_records: Mapped[list["ReadingProgress"]] = relationship(back_populates="user", cascade="all, delete-orphan")
    settings: Mapped["ReadingSettings"] = relationship(back_populates="user", uselist=False, cascade="all, delete-orphan")

from datetime import datetime
from typing import TYPE_CHECKING

from sqlalchemy import DateTime, ForeignKey, Integer, String, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.session import Base

if TYPE_CHECKING:
    from app.models.novel import Novel
    from app.models.user import User


class ReadingProgress(Base):
    __tablename__ = "reading_progress"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"))
    novel_id: Mapped[int] = mapped_column(ForeignKey("novels.id", ondelete="CASCADE"))
    chapter: Mapped[str] = mapped_column(String(255), nullable=False)
    offset: Mapped[int] = mapped_column(Integer, default=0)
    last_read_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

    user: Mapped["User"] = relationship(back_populates="progress_records")
    novel: Mapped["Novel"] = relationship(back_populates="progress_records")

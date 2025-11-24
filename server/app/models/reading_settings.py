from sqlalchemy import ForeignKey, Integer, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.session import Base


class ReadingSettings(Base):
    __tablename__ = "reading_settings"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"), unique=True)
    font_family: Mapped[str] = mapped_column(String(128), default="NotoSerif")
    font_size: Mapped[int] = mapped_column(Integer, default=16)
    line_height: Mapped[int] = mapped_column(Integer, default=150)
    theme: Mapped[str] = mapped_column(String(64), default="light")
    bg_color: Mapped[str] = mapped_column(String(16), default="#ffffff")
    tts_voice: Mapped[str] = mapped_column(String(128), default="default")

    user: Mapped["app.models.user.User"] = relationship(back_populates="settings")

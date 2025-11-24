"""initial schema

Revision ID: 20241124_0001
Revises:
Create Date: 2025-11-24 13:56:00
"""

from collections.abc import Sequence
from typing import Union

import sqlalchemy as sa
from alembic import op

revision: str = "20241124_0001"
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        "users",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("email", sa.String(length=255), nullable=False, unique=True),
        sa.Column("password_hash", sa.String(length=512), nullable=False),
        sa.Column("display_name", sa.String(length=255), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )
    op.create_index("ix_users_email", "users", ["email"])

    op.create_table(
        "novels",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("user_id", sa.Integer(), sa.ForeignKey("users.id", ondelete="CASCADE")),
        sa.Column("title", sa.String(length=255), nullable=False),
        sa.Column("author", sa.String(length=255), default="未知作者"),
        sa.Column("format", sa.String(length=32), nullable=False),
        sa.Column("file_path", sa.String(length=512), nullable=False),
        sa.Column("cover_path", sa.String(length=512)),
        sa.Column("file_hash", sa.String(length=128), nullable=False),
        sa.Column("size", sa.Integer(), nullable=False),
        sa.Column("description", sa.Text()),
        sa.Column("uploaded_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )
    op.create_index("ix_novels_file_hash", "novels", ["file_hash"])

    op.create_table(
        "reading_settings",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("user_id", sa.Integer(), sa.ForeignKey("users.id", ondelete="CASCADE"), unique=True),
        sa.Column("font_family", sa.String(length=128), default="NotoSerif"),
        sa.Column("font_size", sa.Integer(), default=16),
        sa.Column("line_height", sa.Integer(), default=150),
        sa.Column("theme", sa.String(length=64), default="light"),
        sa.Column("bg_color", sa.String(length=16), default="#ffffff"),
        sa.Column("tts_voice", sa.String(length=128), default="default"),
    )

    op.create_table(
        "reading_progress",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("user_id", sa.Integer(), sa.ForeignKey("users.id", ondelete="CASCADE")),
        sa.Column("novel_id", sa.Integer(), sa.ForeignKey("novels.id", ondelete="CASCADE")),
        sa.Column("chapter", sa.String(length=255), nullable=False),
        sa.Column("offset", sa.Integer(), default=0),
        sa.Column("last_read_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )


def downgrade() -> None:
    op.drop_table("reading_progress")
    op.drop_table("reading_settings")
    op.drop_index("ix_novels_file_hash", table_name="novels")
    op.drop_table("novels")
    op.drop_index("ix_users_email", table_name="users")
    op.drop_table("users")

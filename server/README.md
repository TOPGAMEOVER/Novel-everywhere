# Novel Everywhere 服务端

基于 FastAPI + SQLite 的 RESTful 后端，实现账号、小说上传、阅读进度及设置同步、TTS 等功能。部署方式详见 `docs/DEPLOYMENT.md`。

## 快速开始
```bash
cd server
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
cp example.env .env
alembic upgrade head
uvicorn app.main:app --reload
```

## 目录结构
```
server/
├── app/
│   ├── api/          # 路由
│   ├── core/         # 配置、安全
│   ├── db/           # 数据库会话
│   ├── models/       # SQLAlchemy 模型
│   ├── schemas/      # Pydantic 模型
│   └── services/     # 文件/TTS 等服务
├── migrations/       # Alembic 迁移
├── storage/          # 小说与音频文件
├── requirements.txt
└── README.md
```

## API 文档
启动后访问 `http://localhost:8000/docs` (Swagger) 或 `/redoc`。

## 测试
```bash
pytest
```

> 若需连接 PostgreSQL 等其他数据库，请修改 `.env` 中的 `DATABASE_URL` 并运行 Alembic 迁移。

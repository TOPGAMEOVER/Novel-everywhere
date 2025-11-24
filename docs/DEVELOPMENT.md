# Novel Everywhere 开发文档

## 1. 项目概述
- **目标**：提供一款跨终端（服务端 + 安卓手机 + 安卓平板 + Windows 桌面）的小说阅读解决方案，实现本地小说聚合、云端同步、阅读偏好设置、离线朗读等核心体验。
- **核心卖点**：
  - 支持多种常见小说文件格式（`.txt`、`.epub`、`.mobi`、`.pdf` 等）。
  - 云端备份小说、阅读进度和偏好设置，任意终端可无缝续读。
  - 自定义字体、字号、行距、夜间/护眼背景色，内置朗读引擎。
  - 统一账号体系，登录后跨设备同步。

## 2. 系统架构
```
┌───────────────────────────────┐
│           客户端层            │
│ ┌────────────┐ ┌────────────┐ │
│ │Android 手机│ │Android 平板│ │
│ └────────────┘ └────────────┘ │
│ ┌────────────┐               │
│ │Windows 桌面│               │
│ └────────────┘               │
└──────────┬────────────────────┘
           │REST / WebSocket
┌──────────▼────────────────────┐
│           服务端层            │
│ FastAPI + SQLite + 本地文件   │
│ 提供认证/小说/进度/TTS 等 API │
└──────────┬────────────────────┘
           │
┌──────────▼───────────────┐
│ SQLite（文件级）         │
│ - users                   │
│ - novels（含文件 BLOB）   │
│ - reading_progress        │
│ - settings                │
└───────────────────────────┘
```

## 3. 技术栈
| 层级           | 技术                           | 说明 |
|----------------|--------------------------------|------|
| 服务端         | Python 3.12 + FastAPI + Uvicorn | 快速构建 REST API，自动文档 |
| 数据存储       | SQLite + SQLAlchemy + Alembic  | 轻量、易部署、可迁移 |
| 文件/音频存储  | 本地 `storage/` 目录，可扩展 MinIO/S3 | 存小说原文件与 TTS 缓存 |
| 身份认证       | JWT（PyJWT）                   | 访问控制 |
| TTS            | pyttsx3（离线）、客户端原生 TTS | 朗读功能 |
| Android 客户端 | Flutter 3 + Dart 3             | 单代码基，按终端入口区分 |
| Windows 客户端 | Flutter Windows runner         | 桌面交互 |
| 网络层 (客户端)| Dio + Retrofit Generator       | REST 调用、拦截器、重试 |
| 状态管理       | Riverpod 2 + StateNotifier     | 管理小说/设置/朗读状态 |
| 本地缓存       | Hive/SharedPreferences         | 最新进度、设置离线可用 |

## 4. 功能拆解
1. **账户管理**
   - 注册、登录、Token 刷新
   - 设备记录、最后同步时间
2. **小说管理**
   - 本地导入（解析 txt/epub/mobi/pdf）
   - 元数据抽取（标题/作者/章节）
   - 上传云端（文本或压缩）
   - 文件哈希校验避免重复
3. **阅读体验**
   - 多主题背景与字体
   - 章节导航、分页/滚动模式切换
   - 标注/收藏/字典（留接口）
4. **朗读**
   - 客户端发起朗读，默认调本地 TTS
   - 若本地不可用，则调用服务端 `/tts` 返回音频 URL
5. **同步**
   - 小说元数据 + 进度 + 阅读设置
   - 乐观更新，冲突时以更新时间为准
   - WebSocket 推送进度（可选）

## 5. 数据库设计
| 表名 | 字段 | 说明 |
|------|------|------|
| users | id, email, password_hash, display_name, created_at | Argon2 hash |
| devices | id, user_id, platform, device_name, last_seen | 设备登陆记录 |
| novels | id, user_id, title, author, format, file_path, file_hash, size, uploaded_at | 文件在 `storage/novels` |
| reading_progress | id, user_id, novel_id, chapter, offset, last_read_at | 阅读位置 |
| reading_settings | id, user_id, font_family, font_size, line_height, theme, bg_color, tts_voice | 同步偏好 |
| sync_events | id, user_id, event_type, payload_json, created_at | 追踪操作 |

## 6. API 设计
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/auth/register` | 注册 |
| POST | `/auth/login` | 登录，返回 JWT |
| GET  | `/profile/me` | 当前用户信息 |
| POST | `/novels` (multipart) | 上传小说文件 |
| GET  | `/novels` | 小说列表 |
| GET  | `/novels/{id}` | 详情 |
| GET  | `/novels/{id}/file` | 下载文件 |
| DELETE | `/novels/{id}` | 删除小说 |
| POST | `/novels/{id}/progress` | 更新阅读进度 |
| GET  | `/novels/{id}/progress` | 查询进度 |
| PUT  | `/settings/reading` | 更新设置 |
| GET  | `/settings/reading` | 获取设置 |
| POST | `/tts` | 文本转音频 |

## 7. 客户端交互
1. 登录/注册 -> 同步小说列表 -> 显示本地与云端合并的书架。
2. 导入小说时允许多选；解析完毕生成章节索引。
3. 阅读器包含：
   - 手势翻页、长按唤出菜单
   - 阅读工具栏可设置字体、主题、朗读
   - 右上角同步按钮手动触发
4. 朗读面板：播放/暂停、语速、音量，展示当前句子。

## 8. UI 适配策略
- **Android 手机**：底部导航（书架/朗读/设置），阅读器单列布局。
- **Android 平板**：双栏自适应，左侧章节，右侧内容；支持分屏。
- **Windows 桌面**：左侧导航、顶部菜单（导入、同步、设置），支持快捷键 (Ctrl+O, Ctrl+L)。

## 9. 代码结构规划
```
Novel-everywhere/
├── docs/
│   ├── DEVELOPMENT.md
│   └── DEPLOYMENT.md
├── server/
│   ├── app/
│   │   ├── main.py
│   │   ├── api/
│   │   ├── core/
│   │   ├── models/
│   │   ├── schemas/
│   │   └── services/
│   ├── migrations/
│   ├── requirements.txt
│   └── README.md
├── clients/
│   ├── android-phone/
│   │   └── lib/main_phone.dart
│   ├── android-tablet/
│   │   └── lib/main_tablet.dart
│   └── windows-desktop/
│       └── lib/main_windows.dart
└── scripts/
    └── deploy/
```

## 10. 研发流程
1. **阶段 0 - 基础设施**：完成文档、初始化仓库、CI、依赖管理。
2. **阶段 1 - 服务端 MVP**：实现认证、小说上传/下载、进度同步。
3. **阶段 2 - 客户端 MVP**：完成书架/阅读器/朗读基础功能，接入 API。
4. **阶段 3 - 优化**：UI 细节、缓存、离线、TTS 缓存、WebSocket。
5. **阶段 4 - 部署与交付**：Ubuntu 20.04、Docker 镜像、自动备份脚本、GitHub Release。

## 11. 质量与安全
- 统一日志格式（loguru + JSON），分级写入文件。
- 大文件上传限制（默认 100MB），校验 MIME。
- JWT 过期后刷新，强制 HTTPS 访问。
- SQLite 文件与 `storage/` 目录定期备份（cron + rclone 或 rsync）。
- 单元/集成测试确保服务端 API 可靠，Flutter 集成测试确保同步/朗读流程。

## 12. 后续扩展
- 接入第三方云存储（S3/OSS）
- 上线增值功能（云端备份空间、语音包）
- 推送提醒（Firebase、Windows 通知）
- 与电子墨水屏终端联动

> 本文档指导后续服务端与三端客户端开发，若实施中出现差异，应在 `docs/` 下新增补充文档并同步至版本控制。

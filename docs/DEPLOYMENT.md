# 服务端部署文档（Ubuntu 20.04）

## 1. 前提条件
- Ubuntu 20.04 LTS，具备 sudo 权限。
- Python 3.12（建议使用 `pyenv` 或 `deadsnakes` 仓库安装）。
- Git、curl、systemd、Nginx（反向代理 HTTPS）。
- 可选：Docker 24+（如需容器化部署）。

## 2. 获取代码
```bash
sudo apt update
sudo apt install -y git python3.12 python3.12-venv python3.12-dev build-essential
git clone https://github.com/<your-org>/Novel-everywhere.git
cd Novel-everywhere/server
python3.12 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## 3. 初始化 SQLite 与存储目录
```bash
mkdir -p storage/novels storage/audio logs
chmod 750 storage logs
alembic upgrade head  # 生成数据库 schema
```

默认数据库文件位于 `server/app/data/app.db`（可通过环境变量覆盖），务必将该文件与 `storage/` 一并备份。

## 4. 环境变量
创建 `.env`（或 systemd `EnvironmentFile`）：
```
APP_ENV=production
DATABASE_URL=sqlite:///app/data/app.db
JWT_SECRET=<强随机值>
JWT_EXPIRE_MINUTES=10080
STORAGE_ROOT=/opt/novel-everywhere/storage
TTS_AUDIO_ROOT=/opt/novel-everywhere/storage/audio
```

## 5. 启动服务
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
```

若需常驻运行，建议使用 systemd：
```
/etc/systemd/system/novel.service
-----------------------------------
[Unit]
Description=Novel Everywhere API
After=network.target

[Service]
User=novel
Group=novel
WorkingDirectory=/opt/novel-everywhere/server
EnvironmentFile=/opt/novel-everywhere/server/.env
ExecStart=/opt/novel-everywhere/server/.venv/bin/uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
Restart=on-failure

[Install]
WantedBy=multi-user.target
```
```bash
sudo systemctl daemon-reload
sudo systemctl enable --now novel
```

## 6. 配置 Nginx + HTTPS
1. 安装 Nginx：`sudo apt install -y nginx`.
2. 使用 Certbot 申请证书：
   ```bash
   sudo snap install core && sudo snap refresh core
   sudo snap install --classic certbot
   sudo certbot certonly --nginx -d api.example.com
   ```
3. 创建虚拟主机 `/etc/nginx/sites-available/novel`：
   ```
   server {
       listen 80;
       server_name api.example.com;
       return 301 https://$host$request_uri;
   }

   server {
       listen 443 ssl;
       server_name api.example.com;

       ssl_certificate /etc/letsencrypt/live/api.example.com/fullchain.pem;
       ssl_certificate_key /etc/letsencrypt/live/api.example.com/privkey.pem;

       client_max_body_size 200M;

       location / {
           proxy_pass http://127.0.0.1:8000;
           proxy_set_header Host $host;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto https;
       }
   }
   ```
4. 启用站点：
   ```bash
   sudo ln -s /etc/nginx/sites-available/novel /etc/nginx/sites-enabled/novel
   sudo nginx -t
   sudo systemctl reload nginx
   ```

## 7. 备份策略
- 编写 `scripts/backup.sh`（cron 每日）：
  ```bash
  #!/usr/bin/env bash
  set -euo pipefail
  TARGET=/var/backups/novel-$(date +%F).tar.gz
  tar -czf "$TARGET" app/data/app.db storage/
  find /var/backups -type f -mtime +14 -delete
  ```
- 如需远程备份可结合 rclone/S3。

## 8. 日志与监控
- Uvicorn/应用日志写入 `logs/app.log`，通过 logrotate 控制大小。
- 监控：
  - systemd `journalctl -u novel`
  - Nginx 访问日志
  - 可选启 Prometheus exporter（Uvicorn/gunicorn metrics）。

## 9. 升级流程
```bash
cd /opt/novel-everywhere
git pull origin main
source server/.venv/bin/activate
pip install -r server/requirements.txt
alembic upgrade head
sudo systemctl restart novel
```

## 10. GitHub 上传说明
1. 创建仓库并推送：
   ```bash
   git init
   git remote add origin git@github.com:<you>/Novel-everywhere.git
   git add .
   git commit -m "Initial release"
   git push -u origin main
   ```
2. 建议配置 GitHub Actions：
   - 服务端：`python-app.yml` 运行 lint/test。
   - Flutter 客户端：`flutter.yml` 打包 APK / Windows。
3. 发布 Release：
   - 上传打包好的 APK、Windows 安装包、部署文档链接。

## 11. 故障排查
| 症状 | 排查方向 |
|------|----------|
| API 502/504 | 检查 systemd 小记、Uvicorn 是否启动 |
| 上传失败 413 | `client_max_body_size` 是否放大 |
| TTS 无法生成 | 安装 `espeak`, `ffmpeg`，确认 `pyttsx3` 引擎 |
| SQLite 锁 | 避免并发写（默认单实例无锁），必要时迁移至 Postgres |

> 确保部署过程符合公司安全规范，任何机密配置（JWT Secret、证书等）请存储在安全的秘密管理系统中。

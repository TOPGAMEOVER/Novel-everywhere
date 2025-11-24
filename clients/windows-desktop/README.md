# Novel Everywhere Windows 客户端

WPF (.NET 8) 实现的桌面阅读器，提供：
- 账号登录与书架同步
- 列表/阅读器双栏布局
- System.Speech 朗读
- 进度上传

## 运行
```bash
cd clients/windows-desktop
dotnet restore
dotnet run
```

默认后端地址为 `http://localhost:8000/`，可在 `Services/NovelClient.cs` 中修改。

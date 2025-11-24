# Novel Everywhere Android

该目录包含两个独立的 Android 应用：

1. `app-phone`：面向手机的竖屏体验，包含底部导航、文件导入、朗读等。
2. `app-tablet`：针对平板的双栏界面，支持并排阅读和章节快速切换。

两款应用共用 `core` 模块，里面封装了：
- Retrofit + Kotlin 序列化 API 客户端
- Datastore 会话管理
- ViewModel（登录、书架、阅读器、设置）
- 小说上传/进度同步等仓库逻辑

## 构建
```bash
cd clients/android
./gradlew :app-phone:assembleDebug   # 手机 APK
./gradlew :app-tablet:assembleDebug  # 平板 APK
```

如未配置 Gradle Wrapper，可在本地安装 Gradle 8.7，然后运行：
```bash
gradle :app-phone:assembleDebug
```

默认 API 地址为 `http://10.0.2.2:8000/`，可在 `core/src/main/java/.../AppGraph.kt` 中修改。

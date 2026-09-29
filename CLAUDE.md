# mpvRx — Claude Code 项目上下文

> 本文档为 Claude Code 提供 mpvRx 项目的全景上下文。所有信息基于源码和现有文档（`docs/` 目录），用于指导代码理解、修改和新功能开发。

---

## 1. 项目定位

mpvRx 是一款基于 **libmpv** 的 Android 多媒体播放器，采用 **AGPL-3.0-or-later** 许可证开源。

- **包名**: `app.gyrolet.mpvrx`
- **当前版本**: `2.5.0` (`releaseVersionCode = 250`)
- **核心特征**: 以 mpv（通过 `mpvlib` AAR）作为底层解码与渲染引擎，UI 层完全基于 Jetpack Compose
- **播放源**: 本地文件、网络协议（SMB/FTP/WebDAV/SFTP）、流媒体（HLS/torrent/yt-dlp）、Jellyfin/Navidrome 媒体服务器
- **高级功能**: AI 字幕翻译、在线字幕搜索、HDR 着色器管线（Anime4K / hdr-toys）、音频可视化器、视频压缩等

---

## 2. 基础元数据

| 属性 | 值 |
|------|-----|
| compileSdk | `37` |
| minSdk | `26`（Android 8.0） |
| targetSdk | `36` |
| NDK 版本 | `27.3.13750724` |
| Kotlin | `2.4.20` |
| AGP | `9.4.0` |
| KSP | `2.3.12` |
| Java/Kotlin 目标 | `17` |
| Room 数据库版本 | `21` |

**构建变体**:
- `standard` — 主发布版本，支持 Vulkan
- `noVulkan` — 移除 Vulkan，打包为 universal APK
- `fongmi` — 启用 `MPV_SUPPORTS_MEDIACODEC_VULKAN`，打包为 universal APK

**构建命令**:
```bash
./gradlew.bat :app:assembleStandardDebug      # Debug
./gradlew.bat :app:assembleStandardRelease    # Release
./gradlew.bat :app:assembleStandardPreview    # Preview
```

---

## 3. 技术栈全景

### 3.1 官方 / AndroidX 生态
- **Jetpack Compose**（BOM `2026.09.00`）+ **Material 3 Expressive**（`1.5.0-alpha28`）
- **Navigation3**（`1.1.7`）— 基于类的类型安全路由，非传统 Navigation Compose
- **Room**（`2.8.5`）— 本地持久化，含 21 版 schema 导出
- **KSP** — Room 编译时代码生成
- **Media3**（`1.11.0`）— 主要用于视频转码/特效（Transformer/Effect）
- **Biometric**（`1.4.0-alpha07`）— 安全文件夹生物识别

### 3.2 第三方库
- **Koin**（`4.2.2`）— 依赖注入（`koin-android`, `koin-compose`, `koin-compose-viewmodel`）
- **OkHttp**（`5.5.0`）— 所有网络请求的统一出口
- **Kotlinx Serialization**（`1.11.0`）— JSON 序列化/反序列化
- **libtorrent4j**（`2.1.0-39`）— torrent 下载与流式播放
- **mpvlib**（`1.0.9`）— 私有仓库发布的 mpv Android 绑定
- **Sora Editor**（`0.24.6`）— 内置 Lua/JavaScript/配置文件的代码编辑器
- **Google Cast**（`22.3.1`）— Chromecast 投屏支持

### 3.3 原生层
- **CMake 3.22.1** — 构建 C/C++ 代码
- `ytdl_wrapper.c` — yt-dlp 的 JNI 桥接器
- `quickjs/` — QuickJS 引擎源码（用于脚本执行）
- 预编译 `libmpv.so` 分 ABI 存放在 `jniLibs/` 下

---

## 4. 模块分层架构

源码根包为 `app.gyrolet.mpvrx`，按职责划分为以下层次：

```
app.gyrolet.mpvrx
├── App.kt                 # Application 类：Koin 初始化、生命周期回调、性能追踪、空闲核心回收
├── MainActivity.kt        # 主 Activity：根导航器、setContent
├── di/                    # Koin 依赖注入模块（5 个模块）
│   ├── PreferencesModule.kt      # 17+ 偏好设置类
│   ├── DatabaseModule.kt         # Room、DAO、Repository、网络客户端
│   ├── FileManagerModule.kt      # FSAF 文件管理器
│   ├── DomainModule.kt           # 领域服务、AI、字幕、网络、着色器、Syncplay、Torrent
│   └── DownloadModule.kt         # 下载引擎
├── data/                  # 数据源层：网络客户端、代理、歌词 API
│   └── network/           # 协议客户端(client/)、代理(proxy/)、凭证加密(credentials/)
├── database/              # 持久化层：Room 实体、DAO、迁移（MIGRATION_1_2 ~ MIGRATION_20_21）
├── domain/                # 领域层：业务模型与逻辑
│   ├── anime4k/           # Anime4K 着色器管理（GLSL 优化、热保护、降级）
│   ├── autocrop/          # 自动黑边裁剪分析
│   ├── browser/           # 文件浏览器模型（Video、VideoFolder）
│   ├── download/          # 下载引擎（yt-dlp、直链）
│   ├── hdr/               # HDR Toys 着色器运行时
│   ├── jellyfin/          # Jellyfin 领域模型与客户端
│   ├── lyrics/            # 歌词领域模型
│   ├── media/             # 视频/媒体领域模型
│   ├── navidrome/         # Navidrome 领域模型与客户端
│   ├── network/           # 网络播放 URI、Xtream 协议
│   ├── playbackstate/     # 播放状态 Repository 接口
│   ├── recentlyplayed/    # 最近播放 Repository 接口
│   ├── seerr/             # Overseerr 模型
│   ├── syncplay/          # Syncplay 客户端协议
│   ├── thumbnail/         # 缩略图提取与缓存（LruCache + 磁盘缓存）
│   ├── torrent/           # torrent 元数据、流引擎、代理服务器
│   └── update/            # 应用更新模型
├── network/               # 网络基础设施：OkHttp 配置（SharedHttpClient）、Cookie、协程扩展
├── preferences/           # 偏好设置：17+ 个偏好类 + PreferenceStore 底层抽象
├── presentation/          # 表示层：共享 UI 组件、Screen 接口、崩溃处理
├── repository/            # 仓库层
│   ├── ai/                # AI 客户端（OpenAI/Anthropic/Groq/OpenRouter/Together/OpenCode）
│   ├── lyrics/            # 歌词仓库
│   ├── subtitle/          # 在线字幕搜索与归档解压
│   ├── subtitlehub/       # SubtitleHub 聚合源
│   └── wyzie/             # Wyzie 字幕搜索
├── ui/                    # UI 层：Jetpack Compose 屏幕与组件
│   ├── browser/           # 文件浏览器（Album/Tree/MediaLibrary 三模式）
│   ├── player/            # 播放器核心（PlaybackSession、MPVView、控制 UI、可视化器）
│   ├── preferences/       # 20+ 个设置屏幕
│   ├── theme/             # 主题系统（32 套内置主题 + 动态 + 自定义）
│   ├── torrent/           # torrent 文件选择
│   ├── update/            # 更新提示 Sheet
│   └── utils/             # 导航工具、响应式网格、配置覆盖状态
└── utils/                 # 通用工具：设备能力、媒体操作、扫描、权限、安全
```

---

## 5. 核心架构设计

### 5.1 播放状态机：PlaybackSession

`PlaybackSession` 是一个 **Kotlin `object` 单例**，实现 `MPVLib.EventObserver`，是应用内对 libmpv 原生核心的唯一进程级持有者。

**设计要点**:
- Android 的 Activity/Service 均不拥有原生播放核心，只观察或控制 `PlaybackSession`
- 屏幕旋转、PiP、后台播放、通知重新进入等场景，通过 **Surface 绑定/解绑** 实现
- 视频解码在 Android 无渲染表面时会被主动挂起，但轨道 ID 保持进程级存储
- 提供 `StateFlow<PlaybackSessionState>` 与 `StateFlow<PlaybackQueueState>` 供 UI 观察
- 通过 `PlaybackProperty<T>` 泛型封装对 mpv 属性的观察

**状态关键字段**:
```kotlin
data class PlaybackSessionState(
  val phase: PlaybackPhase,      // UNINITIALIZED / INITIALIZING / IDLE / LOADING / READY / BACKGROUND / STOPPING / ERROR
  val generation: Long,          // 会话代际，用于区分不同加载周期
  val activeGeneration: Long,
  val surfaceAttached: Boolean,
  val paused: Boolean,
  val currentItem: PlaybackItem?,
  val error: String?,
)
```

**Generation（代际）机制**: 每次加载新文件时原子递增，用于防止竞态、命令过滤、位置恢复标记。

### 5.2 导航架构

- 路由基接口 `Screen`，所有屏幕实现该接口并提供 `@Composable Content()`
- 使用 **Navigation3**（`androidx.navigation3`），基于类的路由（`@Serializable data class`）
- `MainActivity` 通过 `rememberSaveableBackStack(initial = HomeScreen)` 管理返回栈
- 底部导航栏有 6 个标签（HOME, MUSIC, RECENTS, PLAYLISTS, NETWORK, JELLYFIN），可独立配置显示/隐藏
- `PlayerActivity` 是独立的 `singleTask` Activity，通常通过 `Intent` 启动，独立于 Navigation3 系统

### 5.3 依赖注入（Koin）

`App.onCreate()` 中同步初始化 5 个 Koin 模块：
```kotlin
startKoin {
  androidContext(this@App)
  modules(PreferencesModule, DatabaseModule, FileManagerModule, domainModule, DownloadModule)
}
```

- 所有注册均为 `single`（应用级单例），未观察到 `factory` 或 `scoped`
- Repository 层采用接口-实现分离（`singleOf(::Impl).bind(Interface::class)`）
- AI 客户端矩阵使用 `named("provider")` 限定符，6 个提供商共享 `AiClient` 接口

### 5.4 数据库架构

`MpvRxDatabase`（Room）当前版本 **21**，包含 **12 个实体**：

| 实体 | 用途 |
|------|------|
| `PlaybackStateEntity` | 播放位置、速度、音轨/字幕选择、外部字幕路径 |
| `RecentlyPlayedEntity` | 最近播放历史 |
| `VideoMetadataCacheEntity` | 视频元数据缓存 |
| `NetworkConnectionEntity` | SMB/FTP/WebDAV 网络连接配置 |
| `PlaylistEntity` / `PlaylistItemEntity` | 播放列表（支持 M3U、Xtream、音频标记） |
| `DirectoryScanIndexEntity` | 目录扫描索引 |
| `SecureMediaEntity` | 安全文件夹加密媒体 |
| `NetworkStreamEntryEntity` | 网络流条目（torrent/直链/YouTube） |
| `JellyfinServerEntity` | Jellyfin 服务器连接 |
| `DownloadItemEntity` | 下载任务队列 |
| `NavidromeServerEntity` | Navidrome 服务器连接 |

迁移策略：20+ 条 Migration，含修复型迁移（`PRAGMA table_info` 条件检测）和防御性 `try/catch` 包裹。

### 5.5 主题系统

- 内置 **32 套** 预设主题（`AppTheme` 枚举），每套均提供 Light / Dark / AMOLED 纯黑三种配色
- 支持 **动态取色**（Android 12+）与 **用户自定义主题**（`CustomThemeDefinition` 序列化存储）
- 主题切换采用 **Telegram 风格径向揭示动画**（`ThemeTransitionState`）：捕获当前屏幕 Bitmap → 以点击位置为中心径向遮罩揭示新主题
- 使用 `MaterialExpressiveTheme` + `MotionScheme.expressive()` 实现 Material 3 Expressive 动效
- 亮色动态主题经过 `withComfortableLightSurfaces()` 处理，将纯白背景替换为低彩度薰衣草灰基调

---

## 6. 关键设计决策

| 决策 | 说明 |
|------|------|
| **进程级播放核心** | `PlaybackSession` 为 `object` 单例，旋转/PiP/后台不中断播放 |
| **MediaStore 为真相源** | 拒绝从进程启动时递归扫描外部存储根目录，避免大媒体库下的存储唤醒 |
| **网络凭证加密** | `NetworkCredentialCipher` 使用 AES-256-GCM + Android Keystore |
| **本地代理架构** | 网络播放通过 NanoHTTPD 本地回环代理（`127.0.0.1:随机端口`），URI 使用自定义 scheme `mpvrx-network://{connectionId}{path}`，凭证隔离 |
| **热感知性能降级** | `ThermalMonitor` 监控设备温度，高温时自动降低 GPU 负载和 Anime4K 质量 |
| **Deferred 初始化** | 缩略图预热和元数据维护故意延迟到首次 Activity 启动后，避免冷启动竞争 |
| **三级扫描策略** | MediaStore (L1) → 文件系统验证 (L2) → 元数据提取 (L3) |
| **三级大小探测** | WebDAV: PROPFIND → HEAD Content-Length → Range: bytes=0-0 Content-Range |

---

## 7. 代码规范与约定

### 7.1 包结构约定
- 所有源码在 `app/src/main/java/app/gyrolet/mpvrx/` 下
- 按职责分层：`data/` / `database/` / `domain/` / `network/` / `preferences/` / `presentation/` / `repository/` / `ui/` / `utils/`
- UI 层按功能子目录组织：`ui/browser/`、`ui/player/`、`ui/preferences/`、`ui/theme/`

### 7.2 命名约定
- Compose Screen 实现 `Screen` 接口，命名以 `Screen` 结尾（如 `NetworkBrowserScreen`）
- ViewModel 命名以 `ViewModel` 结尾
- Repository 接口与实现分离，实现类以 `Impl` 结尾
- Koin 模块使用 PascalCase（如 `PreferencesModule`）
- 偏好类以 `Preferences` 结尾（如 `AppearancePreferences`）

### 7.3 偏好设置存储
- 底层抽象为 `PreferenceStore` 接口，实现基于 Android `SharedPreferences`
- 偏好键使用 snake_case（如 `"show_music_tab"`）
- 默认值在偏好类中声明，避免魔法值散落

### 7.4 导航
- 使用 `LocalBackStack.current` 获取当前 BackStack
- 导航调用：`backstack.navigateTo(SomeScreen(...))`
- 安全返回：`backstack.popSafely()`
- `PlayerActivity` 通过 `Intent` 启动，携带 `EXTRA_PREPARED_PLAYBACK_TOKEN` 用于恢复播放状态

### 7.5 资源与字符串
- 字符串资源定义在 `res/values/strings.xml` 及多语言变体中
- 主题名称、偏好标题等使用 `@StringRes` 引用

---

## 8. 常见开发任务指南

### 8.1 添加新设置项
1. 在对应的 `preferences/XxxPreferences.kt` 中声明偏好属性
2. 在 `ui/preferences/` 下对应的设置 Screen 中添加 UI 控件
3. 如需要搜索支持，使用 `SearchablePreference` 组件

### 8.2 添加新 Screen
1. 创建 `@Serializable data class` 实现 `Screen` 接口
2. 在 `Navigator()` 的路由分发中添加 `when` 分支
3. 如需底部导航标签，在 `MainTab` 枚举中添加并配置默认可见性

### 8.3 修改数据库 Schema
1. 修改 Entity 类（添加 `@ColumnInfo` 等注解）
2. 增加 `MIGRATION_N_N+1` 到 `DatabaseModule.kt`
3. 导出 schema：`./gradlew :app:compileStandardDebugKotlin`（Room 自动生成 JSON）
4. 更新 `MpvRxDatabase` 的 `version` 字段

### 8.4 添加新 AI 提供商
1. 在 `repository/ai/` 下创建客户端类，实现 `AiClient` 接口
2. 在 `DomainModule.kt` 中以具体类型和 `named("key")` 两种方式注册
3. 在 `AiService` 中添加路由逻辑
4. 在 `AiPreferences` 中添加提供商配置 UI 支持

### 8.5 修改播放器控制按钮
1. `preferences/PlayerButton.kt` — 添加新按钮枚举值
2. `ui/player/controls/PlayerControlsShared.kt` — 在 `RenderPlayerButton()` 中添加渲染逻辑
3. `preferences/AppearancePreferences.kt` — 更新默认值 CSV 字符串（如需）

### 8.6 添加网络协议支持
1. 在 `domain/network/NetworkConnection.kt` 的 `NetworkProtocol` 枚举中添加新协议
2. 创建 `data/network/client/XxxClient.kt` 实现 `NetworkClient` 接口
3. 在 `NetworkClientFactory.createClient()` 中添加分发逻辑
4. 在 `NetworkRepository` 中处理连接管理和凭证解析

---

## 9. 已知信息边界

以下方面从当前源码中**无法完全确认细节**，开发时需注意：

1. **测试覆盖情况**：当前未观察到 `src/test/` 或 `src/androidTest/` 的目录结构
2. **CI/CD 配置**：`.github/workflows/` 存在，但具体工作流定义未详细阅读
3. **C++ 层完整接口**：`ytdl_wrapper.c` 与 QuickJS 的 JNI 方法签名、线程安全模型
4. **完整的手势冲突解决逻辑**：播放器中长按/滑动/捏合/双击的识别器优先级与互斥规则
5. **Syncplay 协议完整消息集**：当前仅看到 `SyncplayMessages.kt` 存在
6. **Google Cast 的本地文件 Token 化服务器**（NanoHTTPD）的完整请求处理流程
7. **具体第三方 API 的调用限制与降级策略**：如 AI 提供商的速率限制、超时重试逻辑

---

## 10. 文档索引

| 如果你想了解... | 请阅读... |
|----------------|----------|
| 项目整体构建配置与变体差异 | `docs/02-构建系统说明.md` |
| Koin 依赖注入全景与模块划分 | `docs/03-依赖注入全景图.md` |
| 播放状态机与 Surface 生命周期 | `docs/04-播放架构文档.md` |
| 数据库 schema 演进史 | `docs/05-数据库架构与迁移史.md` |
| 导航与路由系统 | `docs/06-导航与路由系统.md` |
| 业务功能全景 | `docs/业务功能清单.md` |
| 主题系统实现细节 | `docs/技术实现说明/10-主题与外观定制-技术实现说明.md` |
| 章节与跳过标记 | `docs/技术实现说明/13-章节与跳过标记-技术实现说明.md` |
| 媒体库与本地浏览 | `docs/技术实现说明/媒体库与本地浏览-技术实现说明.md` |
| 播放核心与引擎 | `docs/技术实现说明/播放核心与引擎-技术实现说明.md` |
| 网络播放与流媒体 | `docs/技术实现说明/网络播放与流媒体-技术实现说明.md` |

---

## 11. AGENTS.md 补充说明

本项目已配置 `graft/` 索引系统（详见 `AGENTS.md`）。对于代码理解任务，优先使用 graft 工具获取精确的代码片段和调用关系，比直接 grep 或阅读整文件更高效。

---

*CLAUDE.md 生成时间: 2026-09-16*  
*对应代码版本: master 分支, version 2.5.0*

# mpvRx 构建指南与性能优化

> 本文档汇总了本地构建 mpvRx 时常见的构建类型、性能差异及Flavor选择问题。

---

## 1. 问题：为什么自己编译的 APK 比 GitHub Release 卡？

### 结论
你构建的是 **Debug** 版本，而 GitHub Release 发布的是 **Release** 版本。Debug 构建卡顿是**预期行为**，并非配置错误。

### 核心差异

| 优化项 | Release/Preview | Debug |
|--------|----------------|-------|
| R8 代码优化 | `isMinifyEnabled = true` | 未启用 |
| 资源压缩 | `isShrinkResources = true` | 未启用 |
| Native 代码优化 | `-ffunction-sections`、链接时 GC 等 | 无优化 |
| LeakCanary | 不包含 | `debugImplementation` 引入，后台监控内存泄漏 |
| Baseline Profile | 生效，ART 预编译关键路径 | 不生效 |
| Compose 调试检查 | 关闭 | 启用重组追踪、布局边界检查等 |
| ART AOT 编译 | Profile 引导，全速运行 | 解释/JIT 为主 |

### 特别影响播放器性能的三项

1. **R8 代码优化**：不只是"压缩体积"，还会做方法内联、死代码消除、反射优化。mpvRx 依赖大量库（Compose、Koin、Room、Media3 等），Release 构建后热路径会被大幅优化。
2. **Native 代码零优化**：`ytdl_wrapper` 和 `qjs_runtime` 在 Debug 下是完全未优化的原始 native 代码。播放器是 JNI 密集型应用，native 端每多一次无效跳转或内存访问，Java 层都能感知到卡顿。
3. **Baseline Profile 失效**：Release 安装后，系统会根据 `baseline-prof.txt` 把关键代码预编译成机器码；Debug 构建不仅不触发 profile，ART 还会以解释/JIT 模式运行。

### 解决方案

不要安装 Debug 版本进行性能测试，改用 **Preview** 构建：

```powershell
./gradlew.bat :app:assembleStandardPreview
```

Preview 继承 Release 的全部优化（R8、资源压缩、Native 优化、Baseline Profile），且通过修改配置可使用 Debug 签名直接安装。

---

## 2. Gradle 构建命令详解

### 命令拆解

```
./gradlew.bat :app:assembleStandardDebug
```

| 部分 | 含义 |
|------|------|
| `./gradlew.bat` | Windows 下的 Gradle Wrapper |
| `:app` | 指定构建 app 模块 |
| `assemble` | 打包 APK 任务 |
| `Standard` | Product Flavor（渠道），对应 `standard` |
| `Debug` | Build Type（构建类型），对应 `debug` |

**命名规则**：`assemble` + `[Flavor首字母大写]` + `[BuildType首字母大写]`

### 可用的构建组合

| 命令 | 说明 |
|------|------|
| `:app:assembleStandardDebug` | standard + Debug |
| `:app:assembleStandardRelease` | standard + Release（带混淆优化，需签名） |
| `:app:assembleStandardPreview` | standard + Preview（继承 Release 优化） |
| `:app:assembleNoVulkanDebug` | noVulkan + Debug（不支持 Vulkan） |
| `:app:assembleNoVulkanRelease` | noVulkan + Release |
| `:app:assembleFongmiDebug` | fongmi + Debug（支持 MediaCodec Vulkan） |
| `:app:assembleFongmiRelease` | fongmi + Release |

### 其他常用任务

| 命令 | 作用 |
|------|------|
| `:app:bundleStandardRelease` | 打包 AAB 格式（Google Play 上架用） |
| `:app:clean` | 清空 app/build/ 目录 |
| `:app:installStandardPreview` | 编译并自动 adb install 到手机 |
| `:app:lintStandardDebug` | 运行静态代码检查 |
| `tasks` | 列出所有可用任务 |

### 常用附加参数

```powershell
# 显示详细编译日志
./gradlew.bat :app:assembleStandardPreview --info

# 编译报错时打印完整堆栈
./gradlew.bat :app:assembleStandardPreview --stacktrace

# 离线模式（不联网下载依赖）
./gradlew.bat :app:assembleStandardPreview --offline

# 只编译 arm64 架构（大幅缩短 native 编译时间）
./gradlew.bat :app:assembleStandardPreview -PtargetAbi=arm64-v8a

# 编译并直接安装
./gradlew.bat :app:installStandardPreview -PtargetAbi=arm64-v8a

# 清缓存后重新编译
./gradlew.bat clean :app:assembleStandardPreview --stacktrace
```

---

## 3. Product Flavor 区别（standard / noVulkan / fongmi）

| 特性 | **standard** | **noVulkan** | **fongmi** |
|------|-------------|-------------|-----------|
| Vulkan 视频输出 | 支持 | **不支持** | 支持 |
| MediaCodec Vulkan | 不支持 | 不支持 | **支持** |
| 内置更新 | 有 | 有 | 有 |
| 绑定的 mpvlib | `mpvlib.standard` | `mpvlib.no.vulkan` | `mpvlib.fongmi` |
| APK 分包方式 | 分 ABI + universal | **仅 universal** | **仅 universal** |

### 使用建议

| 场景 | 选择 |
|------|------|
| 一般 Android 手机/平板，想体验最好 | **standard** |
| 设备老、GPU 弱、播视频闪退/花屏 | **noVulkan** |
| 用 fongmi 相关设备，或想试 MediaCodec Vulkan | **fongmi** |

---

## 4. 技术概念解释

### Vulkan 视频输出

mpv 的 GPU 渲染 API 选项，替代传统 OpenGL ES。

| | OpenGL ES | Vulkan |
|--|-----------|--------|
| mpv 配置 | `gpu-api=opengl` | `gpu-api=vulkan` |
| 上下文 | `gpu-context=android` | `gpu-context=androidvk` |
| 特点 | 兼容性好 | 更低开销、更好 HDR 支持、可开 gpu-next |

**设备要求**（mpvRx 设定）：
- Android API 33+（Android 13）
- 设备硬件支持 Vulkan 1.3
- OpenGL ES 3.1+

不满足自动回退到 OpenGL。

### MediaCodec Vulkan

Android 硬件解码器（MediaCodec）和 Vulkan 渲染层之间的**直通机制**（零拷贝）。

- **有 MediaCodec Vulkan**：硬解后的帧直接以 GPU 纹理交给 Vulkan 渲染，零拷贝
- **没有 MediaCodec Vulkan**（standard 构建）：硬解后的帧先从 GPU 拷贝到 CPU 内存，再传给 Vulkan GPU 纹理（`mediacodec-copy` 模式），增加延迟和功耗

### mpvlib 三个版本

| AAR 名称 | Gradle 坐标 | 底层区别 |
|---------|------------|---------|
| **mpvlib.standard** | `app.gyrolet.mpvlib:mpvlib` | 标准编译，带 Vulkan 后端，不带 MediaCodec Vulkan |
| **mpvlib.no.vulkan** | `app.gyrolet.mpvlib:mpvlib-no-vulkan` | 编译时去掉了 Vulkan 后端，只用 OpenGL ES |
| **mpvlib.fongmi** | `app.gyrolet.mpvlib:mpvlib-fongmi` | fongmi 定制版，Vulkan + MediaCodec Vulkan 全开 |

因为 mpv 的 Vulkan 后端和 MediaCodec Vulkan 支持是在**编译期**决定的，不能运行时开关，所以需要三个独立的预编译 AAR。

---

## 5. Preview 构建签名配置

### 问题

Release 和 Preview 默认**没有签名配置**：

```kotlin
buildTypes {
    named("release") {
        // ... 优化配置 ...
        // 没有 signingConfig
    }
    create("preview") {
        initWith(getByName("release"))
        signingConfig = null        // 显式无签名
        // ...
    }
    named("debug") {
        // Debug 自动使用 ~/.android/debug.keystore
    }
}
```

Release/Preview 生成的是**未签名 APK**，无法直接安装到手机上。

### 解决方案：让 Preview 使用 Debug 签名

修改 `app/build.gradle.kts` 中 `preview` 构建类型：

```kotlin
create("preview") {
    initWith(getByName("release"))
    signingConfig = signingConfigs.getByName("debug")  // 原来是 null
    buildConfigField("boolean", "IS_PREVIEW_BUILD", "true")
    versionNameSuffix = "-beta.r${getCommitCount()}"
}
```

这样 Preview 既有 Release 的全部优化，又能像 Debug 一样直接安装。

### 修改后的操作步骤

1. **同步 Gradle**
   - Android Studio：点击右上角大象图标 **"Sync Project with Gradle Files"**
   - 或命令行：`./gradlew.bat --stop` 后重新执行命令

2. **编译并安装**
   ```powershell
   # 一键编译 + 安装（推荐）
   ./gradlew.bat :app:installStandardPreview -PtargetAbi=arm64-v8a
   
   # 或只编译 APK，手动安装
   ./gradlew.bat :app:assembleStandardPreview -PtargetAbi=arm64-v8a
   # APK 路径：app/build/outputs/apk/standard/preview/app-standard-preview.apk
   ```

---

## 6. 推荐工作流

| 场景 | 构建命令 |
|------|---------|
| 日常测试/体验 Release 流畅度 | `./gradlew.bat :app:installStandardPreview -PtargetAbi=arm64-v8a` |
| 需要断点调试代码 | `./gradlew.bat :app:installStandardDebug -PtargetAbi=arm64-v8a` |
| 发布/分发 APK | `./gradlew.bat :app:assembleStandardRelease`（需配置 release 签名） |
| 老设备兼容性测试 | `./gradlew.bat :app:installNoVulkanPreview -PtargetAbi=arm64-v8a` |

---

## 7. 各构建类型对比（修改后）

| 特性 | Debug | Preview（改后） | Release |
|------|-------|----------------|---------|
| 签名 | debug.keystore | debug.keystore | 需配置 release keystore |
| R8 优化 | 无 | **有** | **有** |
| 资源压缩 | 无 | **有** | **有** |
| Native 优化 | 无 | **有** | **有** |
| Baseline Profile | 不生效 | **生效** | **生效** |
| LeakCanary | 有 | **无** | **无** |
| 包名 | `.debug` 后缀 | 正常 | 正常 |
| 流畅度 | 卡 | **流畅** | **流畅** |

# reelPaper UI 重设计 —— 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 mpvRx 播放器应用的 UI 全面重构为 SmartisanOS 拟物风格（纸与墨的质感语言），直接替换默认主题。

**Architecture:** 从底层主题系统开始构建（颜色、字体、阴影、纹理），然后构建核心组件库（按钮、卡片、开关、弹窗等），接着重构导航组件（悬浮纸坞、AppBar），最后逐个重构各页面。所有修改仅限于 UI 层，保持现有 ViewModel 和数据流不变。

**Tech Stack:** Jetpack Compose, Material 3 Expressive, Kotlin, Android Gradle Plugin

---

## 文件结构映射

### 主题系统（新建/修改）
- `app/src/main/java/app/gyrolet/mpvrx/ui/theme/ReelPaperColors.kt` — 全新配色方案（浅色/深色/AMOLED）
- `app/src/main/java/app/gyrolet/mpvrx/ui/theme/ReelPaperTypography.kt` — 衬线体+无衬线体组合排版
- `app/src/main/java/app/gyrolet/mpvrx/ui/theme/ReelPaperElevation.kt` — 海拔阴影系统（浮起/内凹/台阶/软浮）
- `app/src/main/java/app/gyrolet/mpvrx/ui/theme/ReelPaperTexture.kt` — 纸纹噪点背景绘制
- `app/src/main/java/app/gyrolet/mpvrx/ui/theme/Theme.kt` — 修改：应用全新主题到 MaterialExpressiveTheme

### 核心组件库（新建）
- `app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperButton.kt` — 朱砂实体键、卡纸按钮、朱砂圆键 FAB、图标按钮
- `app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperSwitch.kt` — 拨杆开关（标准/迷你）
- `app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperCard.kt` — 通用列表卡片、叠纸卡片
- `app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperSlider.kt` — 滑杆（进度/设置）
- `app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperSearchBar.kt` — 内凹搜索槽
- `app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperSegmentedControl.kt` — 分段控件
- `app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperSheet.kt` — 底部弹窗 Sheet
- `app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperForm.kt` — 表单输入槽、步进器

### 导航组件（新建）
- `app/src/main/java/app/gyrolet/mpvrx/ui/browser/components/ReelPaperTabBar.kt` — 悬浮纸坞（4 标签，激活展开动画）
- `app/src/main/java/app/gyrolet/mpvrx/ui/browser/components/ReelPaperAppBar.kt` — 顶栏（Logo 徽章+标题+图标按钮）

### 页面重构（修改现有文件）
- `app/src/main/java/app/gyrolet/mpvrx/ui/browser/MainScreen.kt` — 主页（文件夹列表）
- `app/src/main/java/app/gyrolet/mpvrx/ui/browser/folderlist/FolderListScreen.kt` — 文件夹列表内容
- `app/src/main/java/app/gyrolet/mpvrx/ui/browser/videolist/VideoListScreen.kt` — 视频列表内容
- `app/src/main/java/app/gyrolet/mpvrx/ui/browser/playlist/PlaylistScreen.kt` — 播放列表页
- `app/src/main/java/app/gyrolet/mpvrx/ui/browser/playlist/PlaylistCard.kt` — 播放列表卡片
- `app/src/main/java/app/gyrolet/mpvrx/ui/framecapture/SnapshotScreen.kt` — 快照主页
- `app/src/main/java/app/gyrolet/mpvrx/ui/framecapture/SnapshotFolderScreen.kt` — 快照文件夹内页
- `app/src/main/java/app/gyrolet/mpvrx/ui/browser/networkstreaming/NetworkStreamingScreen.kt` — 网络页
- `app/src/main/java/app/gyrolet/mpvrx/ui/preferences/PreferencesScreen.kt` — 设置主页
- `app/src/main/java/app/gyrolet/mpvrx/ui/preferences/AppearancePreferencesScreen.kt` — 外观设置
- `app/src/main/java/app/gyrolet/mpvrx/ui/preferences/PlayerPreferencesScreen.kt` — 播放器设置
- `app/src/main/java/app/gyrolet/mpvrx/ui/preferences/DecoderPreferencesScreen.kt` — 解码器设置

### 播放器重构（修改）
- `app/src/main/java/app/gyrolet/mpvrx/ui/player/PlayerActivity.kt` — 播放器 Activity 布局
- `app/src/main/java/app/gyrolet/mpvrx/ui/player/PlayerControls.kt` — 播放控制组件

---

## Phase 1: 主题系统

### Task 1: 创建 reelPaper 配色方案

**Files:**
- Create: `app/src/main/java/app/gyrolet/mpvrx/ui/theme/ReelPaperColors.kt`
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/theme/Color.kt` (append, do not remove existing)

- [ ] **Step 1: 定义 reelPaper 专用颜色常量**

在 `ReelPaperColors.kt` 中定义：
- 浅色主题颜色（Paper, PaperInset, CardPaper, CardLine, Ink, Ink2, Ink3, Line, Cinnabar, CinnabarHi, CinnabarDk, MossGreen）
- 深色主题颜色（DarkPaper, DarkPaperInset, DarkCard, DarkCardLine, DarkInk, DarkInk2, DarkInk3, DarkLine, DarkCinnabar, DarkMossGreen）
- AMOLED 纯黑颜色（AmoledPaper, AmoledPaperInset, AmoledCard, AmoledCardLine, AmoledLine）
- 功能色（Success, Error, Warning 的朱砂风格变体）

- [ ] **Step 2: 构建 Material 3 ColorScheme 映射函数**

创建 `lightReelPaperColorScheme()` 和 `darkReelPaperColorScheme()` 函数，将 reelPaper 颜色映射到 Material 3 的 ColorScheme：
- background → Paper / DarkPaper
- surface → CardPaper / DarkCard
- primary → Cinnabar
- onPrimary → Paper
- secondary → Ink2
- onSecondary → Paper
- error → CinnabarHi
- outline → Line / DarkLine
- surfaceVariant → PaperInset / DarkPaperInset
- 所有 surfaceContainer* 层级映射

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/theme/ReelPaperColors.kt
git commit -m "feat(theme): add reelPaper color palette with light/dark/amoled variants"
```

### Task 2: 构建海拔阴影系统

**Files:**
- Create: `app/src/main/java/app/gyrolet/mpvrx/ui/theme/ReelPaperElevation.kt`

- [ ] **Step 1: 定义海拔 Modifier 扩展函数**

```kotlin
// ① 卡纸浮起 — 卡片/胶囊/迷你键
fun Modifier.reelPaperRaise(shape: Shape = RoundedCornerShape(12.dp)): Modifier

// ② 纸槽内凹 — 输入槽/滑轨/激活 Tab
fun Modifier.reelPaperInset(shape: Shape = RoundedCornerShape(12.dp)): Modifier

// ③ 实体键台阶 — 矩形可按压主操作键
fun Modifier.reelPaperStep(elevation: Dp = 3.dp): Modifier

// ④ 软浮双层投影 — 圆形键(FAB/播放键)
fun Modifier.reelPaperSoftFloat(shape: Shape = CircleShape): Modifier

// ⑤ 珐琅软浮 — Logo 徽章
fun Modifier.reelPaperEnamelBadge(size: Dp = 34.dp): Modifier

// ⑥ 按压态 — 统一按压反馈
fun Modifier.reelPaperPressable(): Modifier
```

实现细节：
- `reelPaperRaise`: `shadow()` + 自定义 ambient/spot 颜色（暖棕色调 rgba(96,76,36,0.10)）
- `reelPaperInset`: 使用 `drawBehind` 绘制内凹阴影（inset 0 1px 3px rgba(96,76,36,0.16), inset 0 -1px 0 rgba(255,255,255,0.55)）
- `reelPaperStep`: 底部 `offset` + `shadow` 模拟台阶，按下时移除 offset
- `reelPaperSoftFloat`: 双层 `shadow`（一层近一层远）
- `reelPaperEnamelBadge`: 渐变背景 + 光泽边框 + 微投影

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/theme/ReelPaperElevation.kt
git commit -m "feat(theme): add reelPaper elevation system (raise, inset, step, soft-float, enamel)"
```

### Task 3: 纸纹噪点纹理

**Files:**
- Create: `app/src/main/java/app/gyrolet/mpvrx/ui/theme/ReelPaperTexture.kt`
- Add: `app/src/main/res/drawable-nodpi/paper_grain.png` — 预生成 140x140 噪点纹理

- [ ] **Step 1: 生成噪点纹理资源**

在 `drawable-nodpi` 下添加 `paper_grain.png`：
- 尺寸 140x140（可平铺）
- fractalNoise 类型，baseFrequency 0.9，numOctaves 2
- 去饱和，55% 透明度
- 可用工具生成或代码生成后保存

- [ ] **Step 2: 创建纹理背景 Modifier**

```kotlin
fun Modifier.paperGrainBackground(
    opacity: Float = 0.07f,
    darkMode: Boolean = false
): Modifier
```

实现：使用 `drawBehind` 绘制 `paper_grain.png` 平铺纹理，深色模式下 opacity 提升至 0.09f。同时绘制顶部径向渐变光泽（模拟纸张反光）。

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/theme/ReelPaperTexture.kt
git add app/src/main/res/drawable-nodpi/paper_grain.png
git commit -m "feat(theme): add paper grain texture and background modifier"
```

### Task 4: 字体排版系统

**Files:**
- Create: `app/src/main/java/app/gyrolet/mpvrx/ui/theme/ReelPaperTypography.kt`
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/theme/Type.kt`

- [ ] **Step 1: 定义 reelPaper 字体家族**

```kotlin
val ReelPaperFontFamily = FontFamily(
    // 衬线体用于 Logo/标题
    Font(R.font.noto_serif_regular, FontWeight.Normal),
    Font(R.font.noto_serif_bold, FontWeight.Bold),
    Font(R.font.noto_serif_italic, FontWeight.Normal, FontStyle.Italic),
    // 无衬线体用于正文
    Font(R.font.noto_sans_regular, FontWeight.Normal),
    Font(R.font.noto_sans_medium, FontWeight.Medium),
    Font(R.font.noto_sans_bold, FontWeight.Bold),
)
```

添加字体资源到 `app/src/main/res/font/`（如使用系统字体则跳过资源添加，直接使用 `FontFamily.Serif` / `FontFamily.SansSerif`）。

- [ ] **Step 2: 构建 reelPaper Typography**

```kotlin
val ReelPaperTypography = Typography(
    displayLarge = TextStyle(fontFamily = ReelPaperFontFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineLarge = TextStyle(fontFamily = ReelPaperFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = ReelPaperFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    bodyLarge = TextStyle(fontFamily = ReelPaperFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = ReelPaperFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    // ... 覆盖所有 Material 3 typography slots
)
```

特殊样式：
- Logo 样式：衬线斜体，24sp，Bold
- 分区标题：letter-spacing 3px，12.5sp，Ink3 色，800 weight
- 时长/数字：fontVariant = FontVariantNumeric.TabularNums

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/theme/ReelPaperTypography.kt
git commit -m "feat(theme): add reelPaper typography with serif/sans-serif combination"
```

### Task 5: 整合主题系统

**Files:**
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/theme/Theme.kt`
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/theme/AppTheme.kt`

- [ ] **Step 1: 修改 Theme.kt 应用 reelPaper 配色**

在 `resolveAppColorScheme()` 中：
- 当 `appTheme == AppTheme.Default`（或新增 `AppTheme.ReelPaper`）时，返回 `lightReelPaperColorScheme()` / `darkReelPaperColorScheme()`
- AMOLED 模式下，将 background/surface 替换为 AMOLED 纯黑变体
- 保留动态颜色、自定义主题、Tidal/Nord/Rosé Pine 等现有主题的支持

- [ ] **Step 2: 修改 MpvrxTheme 应用 reelPaper 排版**

```kotlin
MaterialExpressiveTheme(
    colorScheme = colorScheme,
    typography = if (useReelPaperTheme) ReelPaperTypography else (if (useSystemFont ...) SystemTypography else AppTypography),
    shapes = AppShapes,
    // ...
)
```

- [ ] **Step 3: 全局纸纹背景**

在 `AppWallpaperHost` 或 `MpvrxTheme` 的最外层包裹 `paperGrainBackground()`，确保所有页面都有纸纹纹理。

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/theme/Theme.kt
git commit -m "feat(theme): integrate reelPaper colors and typography into main theme"
```

---

## Phase 2: 核心组件库

### Task 6: 按钮系统

**Files:**
- Create: `app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperButton.kt`

- [ ] **Step 1: 实现朱砂实体键（CinnabarButton）**

```kotlin
@Composable
fun CinnabarButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
)
```

- 高度 50dp，圆角 13dp
- 朱砂渐变背景（上 #D65444，中 #B23129 52%，下 #9C2A22）
- 顶部内高光（inset 0 1px 0 rgba(255,255,255,0.28)）
- 底部台阶阴影（0 3dp 0 CinnabarDk, 0 8dp 16dp rgba(142,38,31,0.28)）
- 文字颜色 #FBEFE1，letter-spacing 3dp，15.5sp
- 按下：translateY(2dp)，阴影收缩

- [ ] **Step 2: 实现卡纸按钮（PaperButton）**

```kotlin
@Composable
fun PaperButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
)
```

- 高度 48dp，圆角 12dp
- 卡纸渐变背景（上 CardPaper，下 PaperInset）
- 浮起阴影
- 按下：translateY(1dp) 变内凹

- [ ] **Step 3: 实现朱砂圆键 FAB（CinnabarFab）**

```kotlin
@Composable
fun CinnabarFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
```

- 直径 56dp，圆形
- 禁用台阶，软浮双层投影
- 径向渐变背景（130% 130% at 32% 24%，#E06A50 → #B23129 55% → #8E261F）
- 按下：translateY(2dp)

- [ ] **Step 4: 实现图标按钮（IconButtonReelPaper）**

```kotlin
@Composable
fun IconButtonReelPaper(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    isActive: Boolean = false,
    darkMode: Boolean = false
)
```

- 40×40dp，圆角 12dp
- 透明背景，默认 Ink2 色图标
- 激活态：朱砂色图标
- 深色模式：文字反色
- 按下：背景变 Press 色（rgba(120,96,48,0.07)）+ 内凹阴影

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperButton.kt
git commit -m "feat(components): add reelPaper button system (cinnabar, paper, fab, icon)"
```

### Task 7: 拨杆开关

**Files:**
- Create: `app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperSwitch.kt`

- [ ] **Step 1: 实现拨杆开关**

```kotlin
@Composable
fun ReelPaperSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    mini: Boolean = false
)
```

- 标准：48×29dp，圆角 15dp
- 迷你：40×24dp
- 轨道：纸槽内凹样式
- 滑块：圆形，纸色渐变（上 #FFFDF6，下 #EDE4CE），带微投影
- 开启态：轨道朱砂渐变（上 #D65444，下 #B23129），滑块右移（21.5dp / 18.5dp）
- 动画：滑块移动 0.22s，cubic-bezier(0.5, 1.6, 0.4, 1)

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperSwitch.kt
git commit -m "feat(components): add reelPaper toggle switch with cinnabar active state"
```

### Task 8: 卡片系统

**Files:**
- Create: `app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperCard.kt`

- [ ] **Step 1: 实现通用列表卡片**

```kotlin
@Composable
fun ReelPaperListCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
)
```

- 圆角 14dp，卡纸背景，浮起阴影
- 内部行：图标槽（38dp，圆角 11dp，内凹）+ 标题 + 副标题 + 可选操作
- 行之间细线分隔（Line 色）

```kotlin
@Composable
fun ReelPaperListRow(
    icon: @Composable (() -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
)
```

- [ ] **Step 2: 实现叠纸卡片（StackedPaperCard）**

```kotlin
@Composable
fun StackedPaperCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable RowScope.() -> Unit
)
```

- 主卡片：圆角 14dp，卡纸背景，浮起阴影，z-index 0
- 伪元素 1：translate(4dp, 4dp) rotate(0.7°)，z-index -1
- 伪元素 2：translate(8dp, 8dp) rotate(-0.6°)，opacity 0.75，z-index -2
- 使用 `Box` + `Modifier.graphicsLayer` 实现错位旋转

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperCard.kt
git commit -m "feat(components): add reelPaper cards (list card and stacked paper card)"
```

### Task 9: 滑杆、搜索槽、分段控件、弹窗、表单

**Files:**
- Create: `app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperSlider.kt`
- Create: `app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperSearchBar.kt`
- Create: `app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperSegmentedControl.kt`
- Create: `app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperSheet.kt`
- Create: `app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/ReelPaperForm.kt`

- [ ] **Step 1: 实现各组件（详细实现见代码）**

**ReelPaperSlider:**
- 轨道：纸槽内凹，6dp 高度，圆角 4dp
- 填充：朱砂渐变（#B23129 → #D65444）
- 滑块：16dp 圆形，纸色渐变（#F7EDD8 → #CBBFA4），带投影
- 支持触摸拖动

**ReelPaperSearchBar:**
- 高度 42dp，圆角 12dp
- 纸槽内凹样式
- 左侧搜索图标（17dp，Ink3）+ 输入区 + 可选清除

**ReelPaperSegmentedControl:**
- 纸槽内凹背景，圆角 12dp，padding 4dp
- 选项：flex 1，圆角 9dp
- 选中态：卡纸背景 + 浮起阴影

**ReelPaperSheet:**
- 圆角 18dp，卡纸背景
- 顶部高光（inset 0 1px 0 rgba(255,255,255,0.7)）
- 弹出动画：scale(0.9) translateY(14dp) → 正常，0.3s cubic-bezier(0.34,1.45,0.5,1)
- 遮罩：rgba(52,42,24,0.45) + blur(3px)
- 底部按钮区：取消（卡纸键）+ 完成（朱砂键）

**ReelPaperForm:**
- 输入槽：高度 40-44dp，圆角 10-11dp，纸槽内凹
- 标签：小字，淡墨，letter-spacing 2px
- 步进器：-/+ 按钮（卡纸键）+ 数值显示（纸槽内凹）

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/theme/components/
git commit -m "feat(components): add reelPaper slider, search bar, segmented control, sheet, and form"
```

---

## Phase 3: 导航组件

### Task 10: 悬浮纸坞 TabBar

**Files:**
- Create: `app/src/main/java/app/gyrolet/mpvrx/ui/browser/components/ReelPaperTabBar.kt`
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/browser/MainScreen.kt` — 替换现有 BottomNavigation

- [ ] **Step 1: 实现悬浮纸坞组件**

```kotlin
@Composable
fun ReelPaperTabBar(
    tabs: List<TabItem>,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    visibleTabs: Set<String> = setOf("home", "playlist", "snap", "network")
)
```

- 尺寸：宽 286dp，高 58dp，圆角 18dp
- 位置：底部中央，距离底部 22dp
- 背景：卡纸 + 边框 + 浮起阴影 + 远景阴影
- 4 个标签页：主页 / 播放列表 / 快照 / 网络
- 非激活态：仅图标（21dp），Ink3 色
- 激活态：
  - 图标变朱砂色
  - 键身内凹（PaperInset + Inset 阴影）
  - 文字展开（max-width 0 → 38dp，opacity 0 → 1，margin-left 0 → 5dp）
  - flex-grow 1 → 1.85
- 动画：0.32s，cubic-bezier(0.45, 1.25, 0.4, 1)
- 隐藏标签页：根据 visibleTabs 过滤，动态调整布局

- [ ] **Step 2: 替换 MainScreen 中的底部导航**

将 `MainScreen` 中的现有 BottomNavigation / NavigationBar 替换为 `ReelPaperTabBar`。

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/browser/components/ReelPaperTabBar.kt
git add app/src/main/java/app/gyrolet/mpvrx/ui/browser/MainScreen.kt
git commit -m "feat(nav): add reelPaper floating tab bar with expand-on-select animation"
```

### Task 11: 顶栏 AppBar

**Files:**
- Create: `app/src/main/java/app/gyrolet/mpvrx/ui/browser/components/ReelPaperAppBar.kt`

- [ ] **Step 1: 实现 Logo 徽章组件**

```kotlin
@Composable
fun ReelPaperLogoBadge(
    size: Dp = 34.dp,
    modifier: Modifier = Modifier
)
```

- 圆角 10dp
- 朱砂渐变背景（上 #D65444，下 #9C2A22）
- 珐琅软浮效果（光泽边框 + 微投影）
- 内部：胶片盘 Logo SVG（22dp）

- [ ] **Step 2: 实现顶栏组件**

```kotlin
@Composable
fun ReelPaperAppBar(
    title: String,
    subtitle: String? = null,
    showLogo: Boolean = true,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
)
```

- 高度：自适应内容 + padding
- 左侧：返回键（如需要）或 Logo 徽章 + 标题/副标题
- 标题：衬线斜体（如 showLogo）或普通无衬线
- 副标题：11sp，Ink2 色
- 右侧：actions 插槽（搜索/排序/设置图标按钮）
- 背景：透明（纸纹背景由全局提供）

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/browser/components/ReelPaperAppBar.kt
git commit -m "feat(nav): add reelPaper app bar with logo badge and serif title"
```

---

## Phase 4: 页面重构

### Task 12: 主页（文件夹列表）

**Files:**
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/browser/MainScreen.kt`
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/browser/folderlist/FolderListScreen.kt`
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/browser/cards/FolderCard.kt`

- [ ] **Step 1: 重构主页布局**

使用 `ReelPaperAppBar` + `ReelPaperSearchBar` + 文件夹列表 + `ReelPaperTabBar`。

文件夹列表项：
- 列表态：文件夹图标（46×38dp）+ 名称 + 视频数量 + 右箭头
- 网格态：文件夹图标放大 + 名称在下方
- 新文件夹角标：红色气泡（fbadge）
- 行分隔线：Line 色，left 79dp，right 20dp

- [ ] **Step 2: 重构排序与视图弹窗**

使用 `ReelPaperSheet` + `ReelPaperSegmentedControl` + `ReelPaperForm`（步进器）。

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/browser/
git commit -m "feat(ui): redesign home screen with reelPaper theme"
```

### Task 13: 文件/播放列表详情页

**Files:**
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/browser/videolist/VideoListScreen.kt`
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/browser/cards/VideoCard.kt`

- [ ] **Step 1: 重构视频列表项**

- 列表态：缩略图（68×48dp，圆角 9dp，16:10）+ 播放三角图标 + 时长角标 + 名称 + 规格 + 来源芯片 + 右箭头
- 网格态：缩略图放大（16:10，圆角 12dp）+ 简化信息
- 行分隔线：Line 色，left 101dp

- [ ] **Step 2: 重构添加视频弹窗**

使用 `ReelPaperSheet` + 选项列表（重新扫描/本地选择/网络添加）。

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/browser/
git commit -m "feat(ui): redesign video list and folder detail screens"
```

### Task 14: 播放列表页

**Files:**
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/browser/playlist/PlaylistScreen.kt`
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/browser/playlist/PlaylistCard.kt`

- [ ] **Step 1: 重构播放列表卡片为叠纸卡片**

使用 `StackedPaperCard`：
- 图标槽（46dp，圆角 12dp）
- 红心图标 = 收藏夹，堆叠图标 = 普通列表
- 芯片显示来源（本地/WebDAV）
- 右侧迷你播放按钮（38dp 圆形）

- [ ] **Step 2: 添加创建播放列表 FAB**

使用 `CinnabarFab`，图标为播放列表+加号。

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/browser/playlist/
git commit -m "feat(ui): redesign playlist screen with stacked paper cards"
```

### Task 15: 快照页

**Files:**
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/framecapture/SnapshotScreen.kt`
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/framecapture/SnapshotFolderScreen.kt`
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/framecapture/SnapshotItems.kt`

- [ ] **Step 1: 重构快照文件夹列表**

- 列表态：照片小叠图标（i-photostack SVG）+ 文件夹名 + 右侧预览缩略图（2 张微圆角小图）
- 网格态：照片小叠放大
- 角标：红色气泡标识新内容

- [ ] **Step 2: 重构快照图片网格**

- 正方形相册规格（1:1，圆角 11dp）
- 时间角标（画面在原影片中的时间点 + 播放三角）
- 列表态：方形缩略图（62dp）+ 名称 + 来源视频 + 时间

- [ ] **Step 3: 重构快照详情页**

- 拍立得展示：白色边框，顶部胶带，微旋 -1.6°，手写标题
- 定位时间轴：朱砂填充 + 纸色滑块
- 信息卡片：来源视频/保存时间/文件夹/规格
- 操作按钮：分享/移动到/删除
- 底部朱砂实体键：跳转回播

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/framecapture/
git commit -m "feat(ui): redesign snapshot screens with polaroid and timeline"
```

### Task 16: 网络页

**Files:**
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/browser/networkstreaming/NetworkStreamingScreen.kt`
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/browser/networkstreaming/NetworkConnectionCard.kt`

- [ ] **Step 1: 重构网络页布局**

- 顶栏：Logo + "网络"标题，右侧下载/设置图标
- 分段控件：本地网络 / 媒体 / Syncplay
- 已保存连接卡片：
  - 服务器图标（38dp，内凹）+ 名称 + 协议·地址
  - 状态指示点（红色脉冲动画=连接失败，绿色=已连接，灰色=未开启）
  - 迷你拨杆开关（自动连接）
- 朱砂 FAB：添加连接

- [ ] **Step 2: 重构添加连接弹窗**

使用 `ReelPaperSheet`：
- 协议选择胶囊组（WebDAV/SFTP/SMB/FTP）
- 表单：名称 + 服务器地址/端口 + 账号/密码
- 自动连接开关
- 底部：测试连接（卡纸键）+ 保存（朱砂键）

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/browser/networkstreaming/
git commit -m "feat(ui): redesign network streaming screen with connection cards"
```

### Task 17: 设置页

**Files:**
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/preferences/PreferencesScreen.kt`
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/preferences/AppearancePreferencesScreen.kt`
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/preferences/PlayerPreferencesScreen.kt`
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/preferences/DecoderPreferencesScreen.kt`

- [ ] **Step 1: 重构设置主页**

- 顶栏：返回 + "设置"标题 + 副标题
- 搜索槽
- 分组卡片（ReelPaperListCard）：
  - 外观、播放器、解码器、音频、字幕、手势、播放器布局、媒体控制、文件夹、安全文件夹、Syncplay、流媒体协议、扫描与过滤、关于
- 每个卡片项：图标槽 + 标题 + 副标题 + 右箭头

- [ ] **Step 2: 重构外观设置**

- 主题模式胶囊组（深色/浅色/跟随系统）
- 应用主题胶囊组（默认/动态/Tidal/Nord/Rosé Pine）
- 壁纸开关、AMOLED 开关、字体选择
- 应用界面缩放滑杆（85%-115%）
- 文件浏览器选项开关
- 缩略图开关
- 导航标签页开关（分别控制）
- 动画开关

- [ ] **Step 3: 重构播放器设置**

- 屏幕方向胶囊组
- 继续播放/退出保存位置开关
- 快退/快进步长步进器（5-30 秒）
- 双击设置开关
- 控制栏自动隐藏滑杆（1-10 秒）
- 保存到快照开关、截图格式胶囊组

- [ ] **Step 4: 重构解码器设置**

- MPV 配置文件、硬件解码、gpu-next、Vulkan、去色带、YUV420P、Anime4K 开关

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/preferences/
git commit -m "feat(ui): redesign preferences screens with reelPaper forms and sliders"
```

---

## Phase 5: 播放器重构

### Task 18: 竖屏播放器

**Files:**
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/player/PlayerActivity.kt`
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/player/PlayerControls.kt`

- [ ] **Step 1: 重构视频区**

- 高度 216dp
- 暖色渐变背景（#E58A3F → #C2582E → #7C3020 → #35221A → #221A15）
- 径向光晕装饰（bk1: #F5B45F 130dp，bk2: #8E4B2F 80dp，blur 20dp）
- 顶部渐变遮罩 + 标题/副标题（深色文字，毛玻璃背景）
- 中央播放按钮：74dp 圆形，半透明 + 轻微模糊，播放时淡出并放大
- 右下角时长角标

- [ ] **Step 2: 重构播放控制区**

- 进度滑杆（ReelPaperSlider）：纸槽轨道 + 朱砂填充 + 纸色滑块
- 主控制行：锁定 / 快退（10s）/ 播放暂停 / 快进（10s）/ 全屏
  - 播放/暂停键：60dp 圆形朱砂键
  - 其他：图标按钮
- 功能芯片：速度 / 音轨 / 字幕 / 循环
- 手势提示文字

- [ ] **Step 3: 重构播放队列**

- 同文件夹视频列表（ReelPaperListCard）
- 当前项：红色左边缘标记（3dp 圆角）+ 均衡器动画（三根朱砂条跳动）
- 其他项：播放按钮

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/player/
git commit -m "feat(player): redesign portrait player with cinnabar controls and eq animation"
```

### Task 19: 横屏播放器与手势系统

**Files:**
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/player/PlayerActivity.kt`
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/player/PlayerControls.kt`

- [ ] **Step 1: 重构横屏布局**

- 视频区全屏
- 控制层悬浮（底部渐变遮罩）
- 底部控制条简化：进度滑杆 + 播放/暂停 + 快退/快进 + 全屏退出
- 隐藏功能芯片和手势提示

- [ ] **Step 2: 实现手势系统**

- 首次横屏：弹出手势说明卡（ReelPaperSheet）
  - 双击左/右半屏 = 快退/快进
  - 左半屏上下滑 = 亮度
  - 右半屏上下滑 = 音量
- 手势指示浮层：
  - 亮度：左侧，暖色渐变条（#E8B45F → #C2582E）
  - 音量：右侧，绿色渐变条（#9DB586 → #5F7A4A）
  - 中央百分比数字
- 双击快退/快进：波纹提示动画

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/player/
git commit -m "feat(player): add landscape player with gesture controls and brightness/volume indicators"
```

---

## Phase 6: 动画与 Polish

### Task 20: 页面切换动画

**Files:**
- Modify: `app/src/main/java/app/gyrolet/mpvrx/ui/utils/ScreenNavDisplay.kt`

- [ ] **Step 1: 实现页面切换动画**

- 新页面进入：translateX(26dp) + alpha(0) → translateX(0) + alpha(1)，0.26s ease
- 旧页面退出：translateX(0) + alpha(1) → translateX(-26dp) + alpha(0)，0.26s ease

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/app/gyrolet/mpvrx/ui/utils/ScreenNavDisplay.kt
git commit -m "feat(anim): add reelPaper page transition animation"
```

### Task 21: 最终审查与测试

- [ ] **Step 1: 编译检查**

```bash
./gradlew :app:compileDebugKotlin
```

- [ ] **Step 2: 视觉审查清单**

- [ ] 浅色主题：纸色背景、朱砂强调、墨字清晰
- [ ] 深色主题：暖灰背景、朱砂强调、反白文字
- [ ] AMOLED 模式：纯黑背景、省电、对比度足够
- [ ] 纸纹纹理：全局可见、不遮挡内容
- [ ] 纸坞动画：切换流畅、文字展开自然
- [ ] 按钮按压：台阶阴影反馈明显
- [ ] 弹窗弹出：弹性动画、遮罩正确
- [ ] 滑杆拖动：滑块跟随、填充实时

- [ ] **Step 3: Commit 最终调整**

```bash
git commit -m "polish: final adjustments to reelPaper UI theme"
```

---

## Self-Review Checklist

### Spec Coverage

| Spec 需求 | 对应 Task |
|-----------|-----------|
| 配色方案（浅色/深色/AMOLED） | Task 1 |
| 海拔阴影系统 | Task 2 |
| 纸纹噪点纹理 | Task 3 |
| 字体排版 | Task 4 |
| 主题整合 | Task 5 |
| 朱砂实体键/卡纸按钮/FAB/图标按钮 | Task 6 |
| 拨杆开关 | Task 7 |
| 通用卡片/叠纸卡片 | Task 8 |
| 滑杆/搜索槽/分段控件/弹窗/表单 | Task 9 |
| 悬浮纸坞 TabBar | Task 10 |
| 顶栏 AppBar | Task 11 |
| 主页文件夹列表 | Task 12 |
| 文件详情视频列表 | Task 13 |
| 播放列表叠纸卡片 | Task 14 |
| 快照照片小叠/拍立得/时间轴 | Task 15 |
| 网络连接卡片/添加连接弹窗 | Task 16 |
| 设置分组/外观/播放器/解码器 | Task 17 |
| 竖屏播放器/控制区/播放队列 | Task 18 |
| 横屏播放器/手势系统 | Task 19 |
| 页面切换动画 | Task 20 |

**无遗漏。**

### Placeholder Scan

- 无 TBD/TODO
- 无 "implement later"
- 无 "add appropriate error handling"（无占位描述）
- 每个 Task 都有明确的文件路径
- 组件名称在整个计划中一致

### Type Consistency

- `ReelPaperColors.kt` 中定义的颜色名称在所有 Task 中一致
- `ReelPaperElevation.kt` 中的 Modifier 扩展函数名称一致
- 组件名称：`CinnabarButton`, `PaperButton`, `CinnabarFab`, `IconButtonReelPaper`, `ReelPaperSwitch`, `ReelPaperListCard`, `StackedPaperCard`, `ReelPaperSlider`, `ReelPaperSearchBar`, `ReelPaperSegmentedControl`, `ReelPaperSheet`, `ReelPaperForm`, `ReelPaperTabBar`, `ReelPaperAppBar`

---

## 执行方式选择

**Plan complete and saved to `docs/superpowers/plans/2026-09-29-reelpaper-ui-redesign.md`.**

**Two execution options:**

**1. Subagent-Driven (recommended)** — 每个 Task 分配独立子代理执行，代理间审查，快速迭代

**2. Inline Execution** — 在本会话中按顺序执行任务，批量执行并在检查点审查

**Which approach?**

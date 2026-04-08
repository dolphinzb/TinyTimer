# add-about-page: 设计文档

## 页面结构

### 关于页面 (AboutPage)

采用与其他页面一致的布局风格，包含：
- 顶部导航栏带返回按钮
- 页面标题"关于"
- 产品信息卡片区
- 功能特性列表

## UI 设计

### 布局结构

```
┌─────────────────────────────┐
│ ← 关于                      │  <- 顶部栏
├─────────────────────────────┤
│                             │
│      [应用图标]             │
│       TinyTimer             │  <- 应用信息区
│        V1.1                │
│                             │
├─────────────────────────────┤
│  产品介绍                   │  <- 产品介绍区
│  TinyTimer 是一款简洁高效...  │
│                             │
├─────────────────────────────┤
│  核心功能                   │  <- 功能特性区
│  • 精准计时                 │
│  • 分组管理                 │
│  • 历史记录                 │
│  • 后台运行                 │
│  • 多分组计时               │
│                             │
└─────────────────────────────┘
```

### 组件设计

**应用信息卡片**
- 应用图标：使用现有 launcher 图标
- 应用名称：TinyTimer
- 版本号：V1.1
- 居中显示

**产品介绍文本**
- 使用 bodyMedium 样式
- 浅色文字色

**功能特性列表**
- 使用 Card 展示每个功能
- 包含图标和文字说明

## 技术实现

### 文件结构

新增文件：
- `app/src/main/java/com/tinytimer/app/ui/pages/AboutPage.kt`

修改文件：
- `app/src/main/java/com/tinytimer/app/ui/pages/SettingsPage.kt`
- `app/src/main/java/com/tinytimer/app/ui/MainActivity.kt`

### 导航实现

在 `MainActivity.kt` 中添加路由：
```kotlin
composable("about") {
    AboutPage(onNavigateBack = { navController.popBackStack() })
}
```

在 `SettingsPage.kt` 中添加回调参数：
```kotlin
@Composable
fun SettingsPage(
    onNavigateToGroups: () -> Unit,
    onNavigateToAbout: () -> Unit  // 新增
)
```

## 产品介绍内容

### 应用简介

TinyTimer 是一款简洁高效的计时器应用，专为需要精确计时的场景设计。支持多分组管理，让您可以同时追踪多个任务的耗时情况。

### 核心功能

1. **精准计时** - 基于系统时间戳的高精度计时
2. **分组管理** - 创建和管理多个计时分组
3. **历史记录** - 查看过往计时记录和统计数据
4. **后台运行** - 应用切换到后台后继续计时
5. **通知控制** - 通过通知栏快捷操作计时器
6. **多分组计时** - 支持同时运行多个分组计时

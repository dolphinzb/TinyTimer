# TinyTimer - AI 开发助手规范

本文档为 AI 开发助手（Trae IDE）提供项目上下文和开发指导原则。

---

## 📱 项目概述

- **项目名称**: TinyTimer
- **项目类型**: Android 原生应用
- **核心功能**: 计时器应用，支持后台计时、分组管理、历史记录查看与统计
- **开发语言**: Kotlin
- **最低 SDK**: Android 11 (API 30)
- **目标 SDK**: Android 15 (API 35)

---

## 🏗️ 技术栈

| 技术 | 版本/说明 |
|------|-----------|
| Kotlin | 1.9.x |
| Jetpack Compose | BOM 2024.02.00 |
| Room | 2.6.1 |
| Lifecycle ViewModel | 2.7.0 |
| Coroutines | 1.8.0 |
| Gradle | 8.14.4 |
| AGP | 8.3.2 |

---

## 📁 项目结构

```
TinyTimer/
├── app/
│   └── src/main/
│       ├── java/com/tinytimer/app/
│       │   ├── TinyTimerApp.kt          # Application 类，初始化数据库和通知渠道
│       │   ├── data/
│       │   │   ├── dao/                 # Room DAO 接口
│       │   │   │   ├── GroupDao.kt
│       │   │   │   ├── RecordDao.kt
│       │   │   │   └── TimerStateDao.kt
│       │   │   ├── database/
│       │   │   │   └── AppDatabase.kt  # Room 数据库单例
│       │   │   ├── entity/              # 数据实体
│       │   │   │   ├── GroupEntity.kt
│       │   │   │   ├── RecordEntity.kt
│       │   │   │   └── TimerStateEntity.kt
│       │   │   └── repository/         # 仓库层（封装数据操作）
│       │   │       ├── GroupRepository.kt
│       │   │       ├── RecordRepository.kt
│       │   │       └── TimerStateRepository.kt
│       │   ├── receiver/
│       │   │   └── BootReceiver.kt      # 开机广播接收器
│       │   ├── service/
│       │   │   └── TimerService.kt     # 前台计时服务（核心）
│       │   └── ui/
│       │       ├── MainActivity.kt     # 应用入口
│       │       ├── pages/              # 页面组件
│       │       │   ├── TimerPage.kt    # 计时主页
│       │       │   ├── GroupPage.kt    # 分组管理页
│       │       │   └── HistoryPage.kt # 历史记录页（含图表视图）
│       │       ├── viewmodel/          # ViewModel
│       │       │   ├── TimerViewModel.kt
│       │       │   ├── GroupViewModel.kt
│       │       │   └── HistoryViewModel.kt
│       │       └── theme/              # Compose 主题
│       │           ├── Color.kt
│       │           └── Theme.kt
│       └── res/
│           ├── drawable/               # 应用图标资源
│           │   ├── ic_launcher_background.xml
│           │   └── ic_launcher_foreground.xml
│           ├── mipmap-anydpi-v26/       # 自适应图标配置
│           ├── values/
│           │   ├── colors.xml
│           │   ├── strings.xml
│           │   └── themes.xml
│           └── AndroidManifest.xml
├── build.gradle.kts                    # 根目录构建配置
├── settings.gradle.kts
├── gradle.properties
└── AGENTS.md                           # 本文件
```

---

## ⚙️ 构建与部署

### 构建命令

```bash
# Debug 构建
./gradlew assembleDebug

# Release 构建（需配置签名）
./gradlew assembleRelease
```

### 安装到设备

```bash
# 查看已连接设备
adb devices

# 安装/更新应用
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 重新启动 ADB（设备连接问题时）
adb kill-server && adb start-server
```

---

## 📐 代码规范

### 代码风格

- **语言**: Kotlin
- **注释**: 公共类、方法添加 Javadoc 风格注释；复杂业务逻辑添加行内注释
- **命名**: 遵循 Kotlin 惯例，类名首字母大写，方法名小写驼峰
- **中文注释**: 使用中文注释提高可读性

### Compose 规范

- 使用 `remember` 保存可组合函数内的状态
- 使用 `collectAsState()` 收集 Flow 数据
- 优先使用 Material 3 组件 (`material3.*`)
- 图标使用 `Icons.AutoMirrored.Filled` 替代已弃用的 `Icons.Default`

### 数据库规范

- Room 实体类添加 `@Entity` 注解
- 使用 `Flow` 返回实时数据更新
- 关联查询使用 `ForeignKey` 和 `Index`

---

## 🔑 核心业务逻辑

### 前台服务 (TimerService)

- 使用 `START_STICKY` 确保服务被系统杀死后能重新启动
- 计时基于 `System.currentTimeMillis()` 时间戳计算，避免累加误差
- 定期保存计时状态到 `TimerStateEntity`
- 通知栏操作通过 `ActionReceiver` 处理暂停/恢复/停止

### 状态恢复机制

1. 应用启动时检查 `TimerStateDao` 是否有未完成的计时
2. 如果有，计算经过的时间并恢复状态
3. 服务断开时保存当前状态

### 通知渠道

- **ID**: `TinyTimerApp.NOTIFICATION_CHANNEL_ID`
- **名称**: 计时通知
- **重要性**: `IMPORTANCE_LOW`（不打扰用户）

---

## 🧪 测试要点

### 功能测试

- [ ] 应用切换后台后计时继续
- [ ] 锁屏后计时继续
- [ ] 通知栏可操作暂停/恢复/停止
- [ ] 应用重启后能恢复计时状态
- [ ] 长时间运行无内存泄漏
- [ ] 计时记录正确保存到数据库
- [ ] 分组筛选和日期筛选正常工作

### 构建测试

- [ ] Debug 构建成功
- [ ] Release 构建成功（如配置签名）
- [ ] 应用图标正确显示

---

## 🚀 未来功能规划

### 高优先级
- 倒计时模式
- 深色模式（当前为浅色主题）
- 数据导出 CSV/Excel
- 柱状图统计视图

### 中优先级
- 桌面小组件
- 数据备份与恢复
- 日历视图
- 多次分段计时

### 低优先级
- 嵌套分组/标签系统
- 循环计时提醒
- 多语言支持

---

## ⚠️ 开发注意事项

1. **不要修改 Room 数据库版本号** 除非进行数据库迁移
2. **Foreground Service 必须设置正确的 `foregroundServiceType`**（当前为 `specialUse`）
3. **通知渠道只创建一次**，后续直接使用已有 ID
4. **使用 ADB 安装时**，如果设备显示"无法安装"可能是签名问题，需卸载旧版本
5. **构建前先同步 Gradle** (`./gradlew sync`)

---

## 📝 提交规范

使用 Conventional Commits 格式：

```
<type>(<scope>): <subject>

feat(history): 添加折线图视图
fix(timer): 修复后台计时丢失问题
chore(build): 更新 Gradle 版本
```

**Type 类型**:
- `feat` - 新功能
- `fix` - Bug 修复
- `docs` - 文档变更
- `style` - 代码格式
- `refactor` - 重构
- `test` - 测试相关
- `chore` - 构建/工具变更

---

*最后更新: 2026-03-27*

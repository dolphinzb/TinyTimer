# TinyTimer

一款简洁高效的安卓计时器应用，支持后台计时、分组管理和历史记录查看。

## 功能特性

### 1. 计时功能

- **开始/暂停/恢复/停止** - 完整的计时控制
- **后台持续计时** - 应用切换到后台或锁屏时计时不中断
- **通知栏控制** - 支持在通知栏直接操作暂停、恢复、停止
- **状态恢复** - 应用重启后可自动恢复之前的计时状态
- **分组选择** - 计时前可选择所属分组
- **标记功能** - 计时过程中可标记时间点，生成分段记录

### 2. 分组管理

- 创建、编辑、删除分组
- 为分组设置颜色
- 分组数据与计时记录关联

### 3. 历史记录

- 查看所有计时记录
- 按分组筛选
- 按日期筛选
- 显示计时时长、起止时间、备注等信息
- **批量删除** - 支持多选记录进行批量删除
- **图表视图** - 提供折线图统计分析
- **记录分组编辑** - 可直接修改记录所属分组

## 技术栈

| 技术               | 说明                         |
| ------------------ | ---------------------------- |
| Kotlin             | 开发语言                     |
| Jetpack Compose    | UI 框架                      |
| MVVM               | 架构模式                     |
| Room               | 本地数据库                   |
| Coroutines + Flow  | 异步处理与响应式编程         |
| Foreground Service | 后台计时服务                 |
| Android 11+        | 最低支持 Android 11 (API 30) |

## 项目结构

```
app/src/main/java/com/tinytimer/app/
├── TinyTimerApp.kt          # Application 类
├── data/
│   ├── dao/                 # Room DAO 接口
│   ├── database/            # Room Database
│   ├── entity/              # 数据实体
│   └── repository/          # 仓库层
├── receiver/
│   └── BootReceiver.kt       # 开机广播接收器
├── service/
│   └── TimerService.kt       # 前台计时服务
└── ui/
    ├── MainActivity.kt       # 主界面
    ├── pages/                # 页面组件
    ├── theme/               # 主题配置
    └── viewmodel/           # ViewModel
```

## 界面预览

- **计时页面** - 显示当前计时时间、控制按钮、分组选择、标记功能
- **分组页面** - 管理所有分组（增删改）
- **历史页面** - 查看和筛选历史记录、批量删除、图表视图

## 通知栏功能

应用在计时时会显示前台通知，支持：

- 显示当前计时时间
- 暂停/恢复按钮
- 停止按钮

## 构建与安装

```bash
# 构建 Debug APK
./gradlew assembleDebug  # Linux/Mac
gradlew.bat assembleDebug  # Windows

# 安装到设备
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 应用信息

- **包名**: com.tinytimer.app
- **版本**: 1.0
- **目标SDK**: Android 15 (API 35)
- **最低SDK**: Android 11 (API 30)

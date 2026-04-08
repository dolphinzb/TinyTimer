# manual-timer-record 设计方案

## Context

TinyTimer 是一款 Android 计时器应用，支持实时计时、分组管理和历史记录查看。用户当前只能通过计时器实时记录，无法补录历史计时。

**背景问题**：
- 用户可能忘记启动计时器，需要事后补录
- 需要从其他设备迁移历史数据
- 系统故障导致记录丢失需要补录

**当前实现**：
- `RecordEntity` 存储计时记录，包含 `groupId`、`startTime`、`endTime`、`duration` 字段
- `RecordRepository` 提供 CRUD 操作
- 历史页面支持导入 CSV 功能

## Goals / Non-Goals

**Goals:**
- 提供手动添加计时记录的对话框
- 支持输入开始时间和计时时长
- 支持选择关联分组（可选）
- 自动计算结束时间并保存

**Non-Goals:**
- 不支持编辑已有记录（已有导入功能）
- 不支持批量手动添加
- 不修改现有数据库结构

## Decisions

### 1. 使用 AlertDialog 实现而非独立页面

**决定**：采用 AlertDialog 对话框形式

**理由**：
- 操作便捷，无需页面导航
- 与现有批量删除确认对话框风格一致
- 实现简单，代码复用性高

**替代方案考虑**：
- 独立页面：适合复杂表单，但当前场景简单，不需要

### 2. 时间输入使用 DateTimePicker

**决定**：
- 开始时间：使用 `DatePicker` + `TimePicker`
- 计时时长：使用小时/分钟/秒的数值输入框

**理由**：
- `DatePicker` 和 `TimePicker` 是 Material3 原生组件，用户熟悉
- 时长输入框避免 Picker 切换繁琐，直接输入数字更高效

### 3. 分组选择使用下拉菜单

**决定**：使用 `DropdownMenu` 或 `ExposedDropdownMenu`

**理由**：
- 分组数量有限（通常 < 20），下拉菜单足够
- 支持"无分组"选项

### 4. 参数校验

**决定**：
- 开始时间：不能晚于当前时间
- 时长：必须 > 0
- 结束时间 = 开始时间 + 时长，自动计算

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| 用户输入错误时间导致记录混乱 | 限制开始时间不能晚于当前时间 |
| 跨天记录处理复杂 | 数据库使用时间戳存储，自动处理跨天情况 |
| 验证逻辑分散 | 在 ViewModel 统一验证，提供友好错误提示 |

## Migration Plan

1. 新增 `ManualRecordDialog.kt` 组件
2. 修改 `HistoryPage.kt` 添加手动添加按钮和对话框
3. 修改 `HistoryViewModel.kt` 添加 `addManualRecord()` 方法
4. 测试验证记录正确保存

## Open Questions

- 是否需要添加备注字段？（当前 MVP 暂不包含）
- 是否需要在主页也提供手动添加入口？（当前仅在历史页面提供）
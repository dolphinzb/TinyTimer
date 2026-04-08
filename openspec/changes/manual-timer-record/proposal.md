# manual-timer-record: 手动添加计时记录

## Why

用户有时需要补录已经完成的计时记录，例如：
- 忘记启动计时器而事后补录
- 从其他设备或方式迁移历史数据
- 修正因系统故障导致丢失的计时记录

当前应用仅支持通过计时器实时记录，无法手动添加历史记录。

## What Changes

- 新增手动添加计时记录的对话框/页面
- 支持输入开始时间和计时时长
- 支持选择关联的分组
- 自动保存到历史记录数据库

## Capabilities

### New Capabilities
- `manual-record-entry`: 手动记录入口功能，用户可补录计时历史

### Modified Capabilities
- `history-records`: 扩展历史记录功能，支持显示手动添加的记录（无需修改规格，仅实现层面支持）

## Impact

- 新增 UI：`ManualRecordDialog.kt`（对话框形式）
- 修改 `RecordRepository.kt`：添加插入记录方法
- 修改 `HistoryPage.kt`：添加手动添加按钮和对话框调用
- 修改 `HistoryViewModel.kt`：添加保存手动记录的方法